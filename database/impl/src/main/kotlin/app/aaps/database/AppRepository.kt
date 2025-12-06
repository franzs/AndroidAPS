package app.aaps.database

import androidx.room.withTransaction
import app.aaps.database.entities.APSResult
import app.aaps.database.entities.Bolus
import app.aaps.database.entities.BolusCalculatorResult
import app.aaps.database.entities.Carbs
import app.aaps.database.entities.DeviceStatus
import app.aaps.database.entities.EffectiveProfileSwitch
import app.aaps.database.entities.ExtendedBolus
import app.aaps.database.entities.Food
import app.aaps.database.entities.GlucoseValue
import app.aaps.database.entities.HeartRate
import app.aaps.database.entities.ProfileSwitch
import app.aaps.database.entities.RunningMode
import app.aaps.database.entities.StepsCount
import app.aaps.database.entities.TemporaryBasal
import app.aaps.database.entities.TemporaryTarget
import app.aaps.database.entities.TherapyEvent
import app.aaps.database.entities.TotalDailyDose
import app.aaps.database.entities.UserEntry
import app.aaps.database.entities.data.NewEntries
import app.aaps.database.entities.embedments.InterfaceIDs
import app.aaps.database.entities.interfaces.DBEntry
import app.aaps.database.transactions.Transaction
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.rxMaybe
import kotlinx.coroutines.rx3.rxSingle
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class AppRepository @Inject internal constructor(
    internal val database: AppDatabase
) {

    private val changeSubject = PublishSubject.create<List<DBEntry>>()

    fun changeObservable(): Observable<List<DBEntry>> = changeSubject.subscribeOn(Schedulers.io())

    /**
     * Executes a transaction ignoring its result (coroutine version)
     * Uses Room's suspend withTransaction API for proper coroutine support
     */
    suspend fun <T> runTransactionSuspend(transaction: Transaction<T>) {
        val changes = mutableListOf<DBEntry>()
        database.withTransaction {
            transaction.database = DelegatedAppDatabase(changes, database)
            transaction.run()
        }
        changeSubject.onNext(changes)
    }

    /**
     * Executes a transaction and returns its result (coroutine version)
     * Uses Room's suspend withTransaction API for proper coroutine support
     */
    suspend fun <T : Any> runTransactionForResultSuspend(transaction: Transaction<T>): T {
        val changes = mutableListOf<DBEntry>()
        val result = database.withTransaction {
            transaction.database = DelegatedAppDatabase(changes, database)
            transaction.run()
        }
        changeSubject.onNext(changes)
        return result
    }

    /**
     * Executes a transaction ignoring its result (RxJava version)
     * Runs on IO scheduler
     */
    fun <T> runTransaction(transaction: Transaction<T>): Completable {
        val changes = mutableListOf<DBEntry>()
        return Completable.fromCallable {
            database.runInTransaction {
                transaction.database = DelegatedAppDatabase(changes, database)
                runBlocking { transaction.run() }
            }
        }.subscribeOn(Schedulers.io()).doOnComplete {
            changeSubject.onNext(changes)
        }
    }

    /**
     * Executes a transaction and returns its result (RxJava version)
     * Runs on IO scheduler
     */
    fun <T : Any> runTransactionForResult(transaction: Transaction<T>): Single<T> {
        val changes = mutableListOf<DBEntry>()
        return Single.fromCallable {
            database.runInTransaction(Callable {
                transaction.database = DelegatedAppDatabase(changes, database)
                runBlocking { transaction.run() }
            })
        }.subscribeOn(Schedulers.io()).doOnSuccess {
            changeSubject.onNext(changes)
        }
    }

    fun clearDatabases() = database.clearAllTables()

    fun clearApsResults() = database.apsResultDao.deleteAllEntries()

    fun cleanupDatabase(keepDays: Long, deleteTrackedChanges: Boolean): String = runBlocking {
        val than = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(keepDays)
        val removed = mutableListOf<Pair<String, Int>>()
        removed.add(Pair("APSResult", database.apsResultDao.deleteOlderThan(than)))
        removed.add(Pair("GlucoseValue", database.glucoseValueDao.deleteOlderThan(than)))
        removed.add(Pair("TherapyEvent", database.therapyEventDao.deleteOlderThan(than)))
        removed.add(Pair("TemporaryBasal", database.temporaryBasalDao.deleteOlderThan(than)))
        removed.add(Pair("ExtendedBolus", database.extendedBolusDao.deleteOlderThan(than)))
        removed.add(Pair("Bolus", database.bolusDao.deleteOlderThan(than)))
        removed.add(Pair("TotalDailyDose", database.totalDailyDoseDao.deleteOlderThan(than)))
        removed.add(Pair("Carbs", database.carbsDao.deleteOlderThan(than)))
        removed.add(Pair("TemporaryTarget", database.temporaryTargetDao.deleteOlderThan(than)))
        removed.add(Pair("BolusCalculatorResult", database.bolusCalculatorResultDao.deleteOlderThan(than)))
        // keep at least one EPS
        if (database.effectiveProfileSwitchDao.getEffectiveProfileSwitchDataFromTime(than + 1).isNotEmpty())
            removed.add(Pair("EffectiveProfileSwitch", database.effectiveProfileSwitchDao.deleteOlderThan(than)))
        removed.add(Pair("ProfileSwitch", database.profileSwitchDao.deleteOlderThan(than)))
        removed.add(Pair("ApsResult", database.apsResultDao.deleteOlderThan(than)))
        // keep version history database.versionChangeDao.deleteOlderThan(than)
        removed.add(Pair("UserEntry", database.userEntryDao.deleteOlderThan(than)))
        removed.add(Pair("PreferenceChange", database.preferenceChangeDao.deleteOlderThan(than)))
        // keep foods database.foodDao.deleteOlderThan(than)
        removed.add(Pair("DeviceStatus", database.deviceStatusDao.deleteOlderThan(than)))
        removed.add(Pair("RunningMode", database.runningModeDao.deleteOlderThan(than)))
        removed.add(Pair("HeartRate", database.heartRateDao.deleteOlderThan(than)))
        removed.add(Pair("StepsCount", database.stepsCountDao.deleteOlderThan(than)))

        if (deleteTrackedChanges) {
            removed.add(Pair("CHANGES APSResult", database.apsResultDao.deleteTrackedChanges()))
            removed.add(Pair("CHANGES GlucoseValue", database.glucoseValueDao.deleteTrackedChanges()))
            removed.add(Pair("CHANGES TherapyEvent", database.therapyEventDao.deleteTrackedChanges()))
            removed.add(Pair("CHANGES TemporaryBasal", database.temporaryBasalDao.deleteTrackedChanges()))
            removed.add(Pair("CHANGES Bolus", database.bolusDao.deleteTrackedChanges()))
            removed.add(Pair("CHANGES ExtendedBolus", database.extendedBolusDao.deleteTrackedChanges()))
            removed.add(Pair("CHANGES TotalDailyDose", database.totalDailyDoseDao.deleteTrackedChanges()))
            removed.add(Pair("CHANGES Carbs", database.carbsDao.deleteTrackedChanges()))
            removed.add(Pair("CHANGES TemporaryTarget", database.temporaryTargetDao.deleteTrackedChanges()))
            removed.add(Pair("CHANGES BolusCalculatorResult", database.bolusCalculatorResultDao.deleteTrackedChanges()))
            removed.add(Pair("CHANGES EffectiveProfileSwitch", database.effectiveProfileSwitchDao.deleteTrackedChanges()))
            removed.add(Pair("CHANGES ProfileSwitch", database.profileSwitchDao.deleteTrackedChanges()))
            removed.add(Pair("CHANGES ApsResult", database.apsResultDao.deleteTrackedChanges()))
            // keep food database.foodDao.deleteHistory()
            removed.add(Pair("CHANGES RunningMode", database.runningModeDao.deleteTrackedChanges()))
            removed.add(Pair("CHANGES HeartRate", database.heartRateDao.deleteTrackedChanges()))
            removed.add(Pair("CHANGES StepsCount", database.stepsCountDao.deleteTrackedChanges()))
        }
        val ret = StringBuilder()
        removed
            .filter { it.second > 0 }
            .map { ret.append(it.first + " " + it.second + "<br>") }
        ret.toString()
    }

    fun clearCachedTddData(from: Long) = database.totalDailyDoseDao.deleteNewerThan(from, InterfaceIDs.PumpType.CACHE)

    //BG READINGS -- only valid records
    fun compatGetBgReadingsDataFromTime(timestamp: Long, ascending: Boolean): Single<List<GlucoseValue>> = rxSingle {
        val data = database.glucoseValueDao.compatGetBgReadingsDataFromTime(timestamp)
        if (!ascending) data.reversed() else data
    }

    fun compatGetBgReadingsDataFromTime(start: Long, end: Long, ascending: Boolean): Single<List<GlucoseValue>> = rxSingle {
        val data = database.glucoseValueDao.compatGetBgReadingsDataFromTime(start, end)
        if (!ascending) data.reversed() else data
    }

    //BG READINGS -- including invalid/history records
    fun findBgReadingByNSId(nsId: String): GlucoseValue? = runBlocking {
        database.glucoseValueDao.findByNSId(nsId)
    }

    fun getLastGlucoseValueId(): Long? = runBlocking {
        database.glucoseValueDao.getLastId()
    }

    fun getLastGlucoseValue(): GlucoseValue? = runBlocking {
        database.glucoseValueDao.getLast()
    }

    /*
       * returns a Pair of the next entity to sync and the ID of the "update".
       * The update id might either be the entry id itself if it is a new entry - or the id
       * of the update ("historic") entry. The sync counter should be incremented to that id if it was synced successfully.
       *
       * It is a Maybe as there might be no next element.
       * */
    fun getNextSyncElementGlucoseValue(id: Long): Maybe<Pair<GlucoseValue, GlucoseValue>> = rxMaybe {
        val nextIdElement = database.glucoseValueDao.getNextModifiedOrNewAfter(id) ?: return@rxMaybe null
        val nextIdElemReferenceId = nextIdElement.referenceId
        if (nextIdElemReferenceId == null) {
            nextIdElement to nextIdElement
        } else {
            val historic = database.glucoseValueDao.getCurrentFromHistoric(nextIdElemReferenceId) ?: return@rxMaybe null
            historic to nextIdElement
        }
    }

    // TEMP TARGETS
    fun findTemporaryTargetByNSId(nsId: String): TemporaryTarget? = runBlocking {
        database.temporaryTargetDao.findByNSId(nsId)
    }

    /*
       * returns a Pair of the next entity to sync and the ID of the "update".
       * The update id might either be the entry id itself if it is a new entry - or the id
       * of the update ("historic") entry. The sync counter should be incremented to that id if it was synced successfully.
       *
       * It is a Maybe as there might be no next element.
       * */
    fun getNextSyncElementTemporaryTarget(id: Long): Maybe<Pair<TemporaryTarget, TemporaryTarget>> = rxMaybe {
        val nextIdElement = database.temporaryTargetDao.getNextModifiedOrNewAfter(id) ?: return@rxMaybe null
        val nextIdElemReferenceId = nextIdElement.referenceId
        if (nextIdElemReferenceId == null) {
            nextIdElement to nextIdElement
        } else {
            val historic = database.temporaryTargetDao.getCurrentFromHistoric(nextIdElemReferenceId)
            historic?.let { it to nextIdElement }
        }
    }

    fun getTemporaryTargetDataFromTime(timestamp: Long, ascending: Boolean): Single<List<TemporaryTarget>> = rxSingle {
        val result = database.temporaryTargetDao.getTemporaryTargetDataFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getTemporaryTargetDataIncludingInvalidFromTime(timestamp: Long, ascending: Boolean): Single<List<TemporaryTarget>> = rxSingle {
        val result = database.temporaryTargetDao.getTemporaryTargetDataIncludingInvalidFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getTemporaryTargetActiveAt(timestamp: Long): Maybe<TemporaryTarget> = rxMaybe {
        database.temporaryTargetDao.getTemporaryTargetActiveAt(timestamp)
    }

    fun getLastTempTargetId(): Long? = runBlocking {
        database.temporaryTargetDao.getLastId()
    }

    // USER ENTRY
    fun getUserEntryDataFromTime(timestamp: Long): Single<List<UserEntry>> = rxSingle {
        database.userEntryDao.getUserEntryDataFromTime(timestamp)
    }

    fun getUserEntryFilteredDataFromTime(timestamp: Long): Single<List<UserEntry>> = rxSingle {
        database.userEntryDao.getUserEntryFilteredDataFromTime(UserEntry.Sources.Loop, timestamp)
    }

    suspend fun insert(word: UserEntry) {
        database.userEntryDao.insert(word)
        changeSubject.onNext(mutableListOf(word)) // Not TraceableDao
    }

    // PROFILE SWITCH

    fun findProfileSwitchByNSId(nsId: String): ProfileSwitch? = runBlocking {
        database.profileSwitchDao.findByNSId(nsId)
    }

    fun getNextSyncElementProfileSwitch(id: Long): Maybe<Pair<ProfileSwitch, ProfileSwitch>> = rxMaybe {
        val nextIdElement = database.profileSwitchDao.getNextModifiedOrNewAfter(id) ?: return@rxMaybe null
        val nextIdElemReferenceId = nextIdElement.referenceId
        if (nextIdElemReferenceId == null) {
            nextIdElement to nextIdElement
        } else {
            val historic = database.profileSwitchDao.getCurrentFromHistoric(nextIdElemReferenceId)
            historic?.let { it to nextIdElement }
        }
    }

    fun getProfileSwitchActiveAt(timestamp: Long): ProfileSwitch? = runBlocking {
        val tps = database.profileSwitchDao.getTemporaryProfileSwitchActiveAt(timestamp)
        val ps = database.profileSwitchDao.getPermanentProfileSwitchActiveAt(timestamp)
        if (tps != null && ps != null)
            return@runBlocking if (ps.timestamp > tps.timestamp) ps else tps
        if (ps == null) return@runBlocking tps
        ps // if (tps == null)
    }

    fun getPermanentProfileSwitchActiveAt(timestamp: Long): Maybe<ProfileSwitch> = rxMaybe {
        database.profileSwitchDao.getPermanentProfileSwitchActiveAt(timestamp)
    }

    fun getAllProfileSwitches(): Single<List<ProfileSwitch>> = rxSingle {
        database.profileSwitchDao.getAllProfileSwitches()
    }

    fun getProfileSwitchesFromTime(timestamp: Long, ascending: Boolean): Single<List<ProfileSwitch>> = rxSingle {
        val result = database.profileSwitchDao.getProfileSwitchDataFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getProfileSwitchesIncludingInvalidFromTime(timestamp: Long, ascending: Boolean): Single<List<ProfileSwitch>> = rxSingle {
        val result = database.profileSwitchDao.getProfileSwitchDataIncludingInvalidFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getLastProfileSwitchId(): Long? = runBlocking {
        database.profileSwitchDao.getLastId()
    }

    // RUNNING MODE

    fun findRunningModeByNSId(nsId: String): RunningMode? = runBlocking {
        database.runningModeDao.findByNSId(nsId)
    }

    fun getNextSyncElementRunningMode(id: Long): Maybe<Pair<RunningMode, RunningMode>> = rxMaybe {
        val nextIdElement = database.runningModeDao.getNextModifiedOrNewAfter(id) ?: return@rxMaybe null
        val nextIdElemReferenceId = nextIdElement.referenceId
        if (nextIdElemReferenceId == null) {
            nextIdElement to nextIdElement
        } else {
            val historic = database.runningModeDao.getCurrentFromHistoric(nextIdElemReferenceId)
            historic?.let { it to nextIdElement }
        }
    }

    fun getRunningModeActiveAt(timestamp: Long): RunningMode? = runBlocking {
        val trm = database.runningModeDao.getTemporaryRunningModeActiveAt(timestamp)
        val prm = database.runningModeDao.getPermanentRunningModeActiveAt(timestamp)
        if (trm != null && prm != null)
            return@runBlocking if (prm.timestamp > trm.timestamp) prm else trm
        if (prm == null) return@runBlocking trm
        prm // if (trm == null)
    }

    fun getPermanentRunningModeActiveAt(timestamp: Long): Maybe<RunningMode> = rxMaybe {
        database.runningModeDao.getPermanentRunningModeActiveAt(timestamp)
    }

    fun getAllRunningModes(): Single<List<RunningMode>> = rxSingle {
        database.runningModeDao.getAllRunningModes()
    }

    fun getRunningModesFromTime(timestamp: Long, ascending: Boolean): Single<List<RunningMode>> = rxSingle {
        val result = database.runningModeDao.getRunningModeDataFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getRunningModesFromTimeToTime(startTime: Long, endTime: Long, ascending: Boolean): Single<List<RunningMode>> = rxSingle {
        val result = database.runningModeDao.getRunningModeDataFromTimeToTime(startTime, endTime)
        if (!ascending) result.reversed() else result
    }

    fun getRunningModesIncludingInvalidFromTime(timestamp: Long, ascending: Boolean): Single<List<RunningMode>> = rxSingle {
        val result = database.runningModeDao.getRunningModeDataIncludingInvalidFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getLastRunningModeId(): Long? = runBlocking {
        database.runningModeDao.getLastId()
    }

    // EFFECTIVE PROFILE SWITCH
    fun findEffectiveProfileSwitchByNSId(nsId: String): EffectiveProfileSwitch? = runBlocking {
        database.effectiveProfileSwitchDao.findByNSId(nsId)
    }

    /*
       * returns a Pair of the next entity to sync and the ID of the "update".
       * The update id might either be the entry id itself if it is a new entry - or the id
       * of the update ("historic") entry. The sync counter should be incremented to that id if it was synced successfully.
       *
       * It is a Maybe as there might be no next element.
       * */
    fun getNextSyncElementEffectiveProfileSwitch(id: Long): Maybe<Pair<EffectiveProfileSwitch, EffectiveProfileSwitch>> = rxMaybe {
        val nextIdElement = database.effectiveProfileSwitchDao.getNextModifiedOrNewAfter(id) ?: return@rxMaybe null
        val nextIdElemReferenceId = nextIdElement.referenceId
        if (nextIdElemReferenceId == null) {
            nextIdElement to nextIdElement
        } else {
            val historic = database.effectiveProfileSwitchDao.getCurrentFromHistoric(nextIdElemReferenceId)
            historic?.let { it to nextIdElement }
        }
    }

    fun getOldestEffectiveProfileSwitchRecord(): Maybe<EffectiveProfileSwitch> = rxMaybe {
        database.effectiveProfileSwitchDao.getOldestEffectiveProfileSwitchRecord()
    }

    fun getEffectiveProfileSwitchActiveAt(timestamp: Long): Maybe<EffectiveProfileSwitch> = rxMaybe {
        database.effectiveProfileSwitchDao.getEffectiveProfileSwitchActiveAt(timestamp)
    }

    fun getEffectiveProfileSwitchesFromTime(timestamp: Long, ascending: Boolean): Single<List<EffectiveProfileSwitch>> = rxSingle {
        val result = database.effectiveProfileSwitchDao.getEffectiveProfileSwitchDataFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getEffectiveProfileSwitchesIncludingInvalidFromTime(timestamp: Long, ascending: Boolean): Single<List<EffectiveProfileSwitch>> = rxSingle {
        val result = database.effectiveProfileSwitchDao.getEffectiveProfileSwitchDataIncludingInvalidFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getEffectiveProfileSwitchesFromTimeToTime(start: Long, end: Long, ascending: Boolean): Single<List<EffectiveProfileSwitch>> = rxSingle {
        val result = database.effectiveProfileSwitchDao.getEffectiveProfileSwitchDataFromTimeToTime(start, end)
        if (!ascending) result.reversed() else result
    }

    fun getLastEffectiveProfileSwitchId(): Long? = runBlocking {
        database.effectiveProfileSwitchDao.getLastId()
    }

    // THERAPY EVENT
    /*
       * returns a Pair of the next entity to sync and the ID of the "update".
       * The update id might either be the entry id itself if it is a new entry - or the id
       * of the update ("historic") entry. The sync counter should be incremented to that id if it was synced successfully.
       *
       * It is a Maybe as there might be no next element.
       * */
    fun findTherapyEventByNSId(nsId: String): TherapyEvent? = runBlocking {
        database.therapyEventDao.findByNSId(nsId)
    }

    fun getNextSyncElementTherapyEvent(id: Long): Maybe<Pair<TherapyEvent, TherapyEvent>> = rxMaybe {
        val nextIdElement = database.therapyEventDao.getNextModifiedOrNewAfter(id) ?: return@rxMaybe null
        val nextIdElemReferenceId = nextIdElement.referenceId
        if (nextIdElemReferenceId == null) {
            nextIdElement to nextIdElement
        } else {
            val historic = database.therapyEventDao.getCurrentFromHistoric(nextIdElemReferenceId)
            historic?.let { it to nextIdElement }
        }
    }

    fun getTherapyEventDataFromTime(timestamp: Long, ascending: Boolean): Single<List<TherapyEvent>> = rxSingle {
        val result = database.therapyEventDao.getTherapyEventDataFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getTherapyEventDataFromTime(timestamp: Long, type: TherapyEvent.Type, ascending: Boolean): Single<List<TherapyEvent>> = rxSingle {
        val result = database.therapyEventDao.getTherapyEventDataFromTime(timestamp, type)
        if (!ascending) result.reversed() else result
    }

    fun getTherapyEventDataIncludingInvalidFromTime(timestamp: Long, ascending: Boolean): Single<List<TherapyEvent>> = rxSingle {
        val result = database.therapyEventDao.getTherapyEventDataIncludingInvalidFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getLastTherapyRecordUpToNow(type: TherapyEvent.Type): Maybe<TherapyEvent> = rxMaybe {
        database.therapyEventDao.getLastTherapyRecord(type, System.currentTimeMillis())
    }

    fun compatGetTherapyEventDataFromToTime(from: Long, to: Long): Single<List<TherapyEvent>> = rxSingle {
        database.therapyEventDao.compatGetTherapyEventDataFromToTime(from, to)
    }

    fun getLastTherapyEventId(): Long? = runBlocking {
        database.therapyEventDao.getLastId()
    }

    // FOOD
    /*
       * returns a Pair of the next entity to sync and the ID of the "update".
       * The update id might either be the entry id itself if it is a new entry - or the id
       * of the update ("historic") entry. The sync counter should be incremented to that id if it was synced successfully.
       *
       * It is a Maybe as there might be no next element.
       * */
    fun getNextSyncElementFood(id: Long): Maybe<Pair<Food, Food>> = rxMaybe {
        val nextIdElement = database.foodDao.getNextModifiedOrNewAfter(id) ?: return@rxMaybe null
        val nextIdElemReferenceId = nextIdElement.referenceId
        if (nextIdElemReferenceId == null) {
            nextIdElement to nextIdElement
        } else {
            val historic = database.foodDao.getCurrentFromHistoric(nextIdElemReferenceId) ?: return@rxMaybe null
            historic to nextIdElement
        }
    }

    fun getFoodData(): Single<List<Food>> = rxSingle {
        database.foodDao.getFoodData()
    }

    fun getLastFoodId(): Long? = runBlocking {
        database.foodDao.getLastId()
    }

    // BOLUS
    fun getBolusByNSId(nsId: String): Bolus? = runBlocking {
        database.bolusDao.getByNSId(nsId)
    }

    /*
      * returns a Pair of the next entity to sync and the ID of the "update".
      * The update id might either be the entry id itself if it is a new entry - or the id
      * of the update ("historic") entry. The sync counter should be incremented to that id if it was synced successfully.
      *
      * It is a Maybe as there might be no next element.
      * */
    fun getNextSyncElementBolus(id: Long): Maybe<Pair<Bolus, Bolus>> = rxMaybe {
        val nextIdElement = database.bolusDao.getNextModifiedOrNewAfterExclude(id, Bolus.Type.PRIMING) ?: return@rxMaybe null
        val nextIdElemReferenceId = nextIdElement.referenceId
        if (nextIdElemReferenceId == null) {
            nextIdElement to nextIdElement
        } else {
            val historic = database.bolusDao.getCurrentFromHistoric(nextIdElemReferenceId)
            historic?.let { it to nextIdElement }
        }
    }

    fun getNewestBolus(): Maybe<Bolus> = rxMaybe {
        database.bolusDao.getLastBolusRecord()
    }

    fun getLastBolusRecordOfType(type: Bolus.Type): Maybe<Bolus> = rxMaybe {
        database.bolusDao.getLastBolusRecordOfType(type)
    }

    fun getOldestBolus(): Maybe<Bolus> = rxMaybe {
        database.bolusDao.getOldestBolusRecord()
    }

    fun getBolusesDataFromTime(timestamp: Long, ascending: Boolean): Single<List<Bolus>> = rxSingle {
        val result = database.bolusDao.getBolusesFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getBolusesDataFromTimeToTime(from: Long, to: Long, ascending: Boolean): Single<List<Bolus>> = rxSingle {
        val result = database.bolusDao.getBolusesFromTime(from, to)
        if (!ascending) result.reversed() else result
    }

    fun getBolusesIncludingInvalidFromTime(timestamp: Long, ascending: Boolean): Single<List<Bolus>> = rxSingle {
        val result = database.bolusDao.getBolusesIncludingInvalidFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getLastBolusId(): Long? = runBlocking {
        database.bolusDao.getLastId()
    }
    // CARBS

    fun getCarbsByNSId(nsId: String): Carbs? = runBlocking {
        database.carbsDao.getByNSId(nsId)
    }

    private fun expandCarbs(carbs: Carbs): List<Carbs> =
        if (carbs.duration == 0L) {
            listOf(carbs)
        } else {
            var remainingCarbs = carbs.amount
            val ticks = (carbs.duration / 1000 / 60 / 15).coerceAtLeast(1L)
            (0 until ticks).map {
                val carbTime = carbs.timestamp + it * 15 * 60 * 1000
                val smallCarbAmount = (1.0 * remainingCarbs / (ticks - it)).roundToInt() //on last iteration (ticks-i) is 1 -> smallCarbAmount == remainingCarbs
                remainingCarbs -= smallCarbAmount.toLong()
                Carbs(timestamp = carbTime, amount = smallCarbAmount.toDouble(), duration = 0)
            }.filter { it.amount != 0.0 }
        }

    /*
      * returns a Pair of the next entity to sync and the ID of the "update".
      * The update id might either be the entry id itself if it is a new entry - or the id
      * of the update ("historic") entry. The sync counter should be incremented to that id if it was synced successfully.
      *
      * It is a Maybe as there might be no next element.
      * */
    fun getNextSyncElementCarbs(id: Long): Maybe<Pair<Carbs, Carbs>> = rxMaybe {
        val nextIdElement = database.carbsDao.getNextModifiedOrNewAfter(id) ?: return@rxMaybe null
        val nextIdElemReferenceId = nextIdElement.referenceId
        if (nextIdElemReferenceId == null) {
            nextIdElement to nextIdElement
        } else {
            val historic = database.carbsDao.getCurrentFromHistoric(nextIdElemReferenceId)
            historic?.let { it to nextIdElement }
        }
    }

    fun getLastCarbs(): Maybe<Carbs> = rxMaybe {
        database.carbsDao.getLastCarbsRecord()
    }

    fun getOldestCarbs(): Maybe<Carbs> = rxMaybe {
        database.carbsDao.getOldestCarbsRecord()
    }

    fun getCarbsDataFromTime(timestamp: Long, ascending: Boolean): Single<List<Carbs>> = rxSingle {
        val result = database.carbsDao.getCarbsFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getCarbsDataFromTimeExpanded(timestamp: Long, ascending: Boolean): Single<List<Carbs>> = rxSingle {
        val data = database.carbsDao.getCarbsFromTimeExpandable(timestamp)
        val expanded = data.map(::expandCarbs).flatten()
        val filtered = expanded.filter { it.timestamp >= timestamp }
        if (!ascending) filtered.reversed() else filtered
    }

    fun getCarbsDataFromTimeNotExpanded(timestamp: Long, ascending: Boolean): Single<List<Carbs>> = rxSingle {
        val result = database.carbsDao.getCarbsFromTimeExpandable(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getCarbsDataFromTimeToTimeExpanded(from: Long, to: Long, ascending: Boolean): Single<List<Carbs>> = rxSingle {
        val data = database.carbsDao.getCarbsFromTimeToTimeExpandable(from, to)
        val expanded = data.map(::expandCarbs).flatten()
        val filtered = expanded.filter { it.timestamp in from..to }
        val sorted = filtered.sortedBy { it.timestamp }
        if (!ascending) sorted.reversed() else sorted
    }

    fun getCarbsIncludingInvalidFromTime(timestamp: Long, ascending: Boolean): Single<List<Carbs>> = rxSingle {
        val result = database.carbsDao.getCarbsIncludingInvalidFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getLastCarbsId(): Long? = runBlocking {
        database.carbsDao.getLastId()
    }

    // BOLUS CALCULATOR RESULT
    fun findBolusCalculatorResultByNSId(nsId: String): BolusCalculatorResult? = runBlocking {
        database.bolusCalculatorResultDao.findByNSId(nsId)
    }

    /*
      * returns a Pair of the next entity to sync and the ID of the "update".
      * The update id might either be the entry id itself if it is a new entry - or the id
      * of the update ("historic") entry. The sync counter should be incremented to that id if it was synced successfully.
      *
      * It is a Maybe as there might be no next element.
      * */
    fun getNextSyncElementBolusCalculatorResult(id: Long): Maybe<Pair<BolusCalculatorResult, BolusCalculatorResult>> = rxMaybe {
        val nextIdElement = database.bolusCalculatorResultDao.getNextModifiedOrNewAfter(id) ?: return@rxMaybe null
        val nextIdElemReferenceId = nextIdElement.referenceId
        if (nextIdElemReferenceId == null) {
            nextIdElement to nextIdElement
        } else {
            val historic = database.bolusCalculatorResultDao.getCurrentFromHistoric(nextIdElemReferenceId) ?: return@rxMaybe null
            historic to nextIdElement
        }
    }

    fun getBolusCalculatorResultsDataFromTime(timestamp: Long, ascending: Boolean): Single<List<BolusCalculatorResult>> = rxSingle {
        val data = database.bolusCalculatorResultDao.getBolusCalculatorResultsFromTime(timestamp)
        if (!ascending) data.reversed() else data
    }

    fun getBolusCalculatorResultsIncludingInvalidFromTime(timestamp: Long, ascending: Boolean): Single<List<BolusCalculatorResult>> = rxSingle {
        val data = database.bolusCalculatorResultDao.getBolusCalculatorResultsIncludingInvalidFromTime(timestamp)
        if (!ascending) data.reversed() else data
    }

    fun getLastBolusCalculatorResultId(): Long? = runBlocking {
        database.bolusCalculatorResultDao.getLastId()
    }

    // DEVICE STATUS
    fun insert(deviceStatus: DeviceStatus) {
        database.deviceStatusDao.insert(deviceStatus)
        changeSubject.onNext(mutableListOf(deviceStatus)) // Not TraceableDao
    }

    /*
       * returns a Pair of the next entity to sync and the ID of the "update".
       * The update id might either be the entry id itself if it is a new entry - or the id
       * of the update ("historic") entry. The sync counter should be incremented to that id if it was synced successfully.
       *
       * It is a Maybe as there might be no next element.
       * */

    fun getNextSyncElementDeviceStatus(id: Long): Maybe<DeviceStatus> = rxMaybe {
        database.deviceStatusDao.getNextModifiedOrNewAfter(id)
    }

    fun getLastDeviceStatusId(): Long? = runBlocking {
        database.deviceStatusDao.getLastId()
    }

    // TEMPORARY BASAL
    fun findTemporaryBasalByNSId(nsId: String): TemporaryBasal? = runBlocking {
        database.temporaryBasalDao.findByNSId(nsId)
    }

    /*
        * returns a Pair of the next entity to sync and the ID of the "update".
        * The update id might either be the entry id itself if it is a new entry - or the id
        * of the update ("historic") entry. The sync counter should be incremented to that id if it was synced successfully.
        *
        * It is a Maybe as there might be no next element.
        * */

    fun getNextSyncElementTemporaryBasal(id: Long): Maybe<Pair<TemporaryBasal, TemporaryBasal>> = rxMaybe {
        val nextIdElement = database.temporaryBasalDao.getNextModifiedOrNewAfter(id) ?: return@rxMaybe null
        val nextIdElemReferenceId = nextIdElement.referenceId
        if (nextIdElemReferenceId == null) {
            nextIdElement to nextIdElement
        } else {
            val historic = database.temporaryBasalDao.getCurrentFromHistoric(nextIdElemReferenceId)
            historic?.let { it to nextIdElement }
        }
    }

    fun getTemporaryBasalActiveAt(timestamp: Long): Maybe<TemporaryBasal> = rxMaybe {
        database.temporaryBasalDao.getTemporaryBasalActiveAt(timestamp)
    }

    fun getTemporaryBasalsActiveBetweenTimeAndTime(from: Long, to: Long): Single<List<TemporaryBasal>> = rxSingle {
        database.temporaryBasalDao.getTemporaryBasalActiveBetweenTimeAndTime(from, to)
    }

    fun getTemporaryBasalsStartingFromTime(timestamp: Long, ascending: Boolean): Single<List<TemporaryBasal>> = rxSingle {
        val result = database.temporaryBasalDao.getTemporaryBasalDataFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getTemporaryBasalsStartingFromTimeToTime(from: Long, to: Long, ascending: Boolean): Single<List<TemporaryBasal>> = rxSingle {
        val result = database.temporaryBasalDao.getTemporaryBasalStartingFromTimeToTime(from, to)
        if (!ascending) result.reversed() else result
    }

    fun getTemporaryBasalsStartingFromTimeIncludingInvalid(timestamp: Long, ascending: Boolean): Single<List<TemporaryBasal>> = rxSingle {
        val result = database.temporaryBasalDao.getTemporaryBasalDataIncludingInvalidFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getOldestTemporaryBasalRecord(): Maybe<TemporaryBasal> = rxMaybe {
        database.temporaryBasalDao.getOldestRecord()
    }

    fun getLastTemporaryBasalId(): Long? = runBlocking {
        database.temporaryBasalDao.getLastId()
    }

    // EXTENDED BOLUS
    fun findExtendedBolusByNSId(nsId: String): ExtendedBolus? = runBlocking {
        database.extendedBolusDao.findByNSId(nsId)
    }

    /*
       * returns a Pair of the next entity to sync and the ID of the "update".
       * The update id might either be the entry id itself if it is a new entry - or the id
       * of the update ("historic") entry. The sync counter should be incremented to that id if it was synced successfully.
       *
       * It is a Maybe as there might be no next element.
       * */

    fun getNextSyncElementExtendedBolus(id: Long): Maybe<Pair<ExtendedBolus, ExtendedBolus>> = rxMaybe {
        val nextIdElement = database.extendedBolusDao.getNextModifiedOrNewAfter(id) ?: return@rxMaybe null
        val nextIdElemReferenceId = nextIdElement.referenceId
        if (nextIdElemReferenceId == null) {
            nextIdElement to nextIdElement
        } else {
            val historic = database.extendedBolusDao.getCurrentFromHistoric(nextIdElemReferenceId)
            historic?.let { it to nextIdElement }
        }
    }

    fun getExtendedBolusActiveAt(timestamp: Long): Maybe<ExtendedBolus> = rxMaybe {
        database.extendedBolusDao.getExtendedBolusActiveAt(timestamp)
    }

    fun getExtendedBolusesStartingFromTime(timestamp: Long, ascending: Boolean): Single<List<ExtendedBolus>> = rxSingle {
        val result = database.extendedBolusDao.getExtendedBolusesStartingFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getExtendedBolusesStartingFromTimeToTime(start: Long, end: Long, ascending: Boolean): Single<List<ExtendedBolus>> = rxSingle {
        val result = database.extendedBolusDao.getExtendedBolusDataFromTimeToTime(start, end)
        if (!ascending) result.reversed() else result
    }

    fun getExtendedBolusStartingFromTimeIncludingInvalid(timestamp: Long, ascending: Boolean): Single<List<ExtendedBolus>> = rxSingle {
        val result = database.extendedBolusDao.getExtendedBolusDataIncludingInvalidFromTime(timestamp)
        if (!ascending) result.reversed() else result
    }

    fun getOldestExtendedBolusRecord(): Maybe<ExtendedBolus> = rxMaybe {
        database.extendedBolusDao.getOldestRecord()
    }

    fun getLastExtendedBolusId(): Long? = runBlocking {
        database.extendedBolusDao.getLastId()
    }

    // TotalDailyDose
    fun getLastTotalDailyDoses(count: Int, ascending: Boolean): Single<List<TotalDailyDose>> = rxSingle {
        val result = database.totalDailyDoseDao.getLastTotalDailyDoses(count)
        if (!ascending) result.reversed() else result
    }

    fun getCalculatedTotalDailyDose(timestamp: Long): Maybe<TotalDailyDose> = rxMaybe {
        database.totalDailyDoseDao.findByTimestamp(timestamp, InterfaceIDs.PumpType.CACHE)
    }

    // HEART RATES

    fun getHeartRatesFromTime(timeMillis: Long): Single<List<HeartRate>> = rxSingle {
        database.heartRateDao.getFromTime(timeMillis)
    }

    fun getHeartRatesFromTimeToTime(startMillis: Long, endMillis: Long): Single<List<HeartRate>> = rxSingle {
        database.heartRateDao.getFromTimeToTime(startMillis, endMillis)
    }

    fun getStepsCountFromTime(timeMillis: Long): Single<List<StepsCount>> = rxSingle {
        database.stepsCountDao.getFromTime(timeMillis)
    }

    fun getStepsCountFromTimeToTime(startMillis: Long, endMillis: Long): List<StepsCount> = runBlocking {
        database.stepsCountDao.getFromTimeToTime(startMillis, endMillis)
    }

    fun getLastStepsCountFromTimeToTime(startMillis: Long, endMillis: Long): StepsCount? = runBlocking {
        database.stepsCountDao.getLastStepsCountFromTimeToTime(startMillis, endMillis)
    }

    fun collectNewEntriesSince(since: Long, until: Long, limit: Int, offset: Int) = runBlocking {
        NewEntries(
        apsResults = database.apsResultDao.getNewEntriesSince(since, until, limit, offset),
        bolusCalculatorResults = database.bolusCalculatorResultDao.getNewEntriesSince(since, until, limit, offset),
        boluses = database.bolusDao.getNewEntriesSince(since, until, limit, offset),
        carbs = database.carbsDao.getNewEntriesSince(since, until, limit, offset),
        effectiveProfileSwitches = database.effectiveProfileSwitchDao.getNewEntriesSince(since, until, limit, offset),
        extendedBoluses = database.extendedBolusDao.getNewEntriesSince(since, until, limit, offset),
        glucoseValues = database.glucoseValueDao.getNewEntriesSince(since, until, limit, offset),
        runningModes = database.runningModeDao.getNewEntriesSince(since, until, limit, offset),
        preferencesChanges = database.preferenceChangeDao.getNewEntriesSince(since, until, limit, offset),
        profileSwitches = database.profileSwitchDao.getNewEntriesSince(since, until, limit, offset),
        temporaryBasals = database.temporaryBasalDao.getNewEntriesSince(since, until, limit, offset),
        temporaryTarget = database.temporaryTargetDao.getNewEntriesSince(since, until, limit, offset),
        therapyEvents = database.therapyEventDao.getNewEntriesSince(since, until, limit, offset),
        totalDailyDoses = database.totalDailyDoseDao.getNewEntriesSince(since, until, limit, offset),
        versionChanges = database.versionChangeDao.getNewEntriesSince(since, until, limit, offset),
        heartRates = database.heartRateDao.getNewEntriesSince(since, until, limit, offset),
        stepsCount = database.stepsCountDao.getNewEntriesSince(since, until, limit, offset),
        )
    }

    fun getApsResultCloseTo(timestamp: Long): Maybe<APSResult> = rxMaybe {
        database.apsResultDao.getApsResult(timestamp - 5 * 60 * 1000, timestamp)
    }

    fun getApsResults(start: Long, end: Long): Single<List<APSResult>> = rxSingle {
        database.apsResultDao.getApsResults(start, end)
    }

}

@Suppress("USELESS_CAST", "unused")
inline fun <reified T : Any> Maybe<T>.toWrappedSingle(): Single<ValueWrapper<T>> =
    this.map { ValueWrapper.Existing(it) as ValueWrapper<T> }
        .switchIfEmpty(Maybe.just(ValueWrapper.Absent()))
        .toSingle()
