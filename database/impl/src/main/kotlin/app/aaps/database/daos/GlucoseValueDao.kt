package app.aaps.database.daos

import androidx.room.Dao
import androidx.room.Query
import app.aaps.database.entities.GlucoseValue
import app.aaps.database.entities.TABLE_GLUCOSE_VALUES

@Dao
internal interface GlucoseValueDao : TraceableDao<GlucoseValue> {

    @Query("SELECT * FROM $TABLE_GLUCOSE_VALUES WHERE id = :id")
    override fun findById(id: Long): GlucoseValue?

    @Query("DELETE FROM $TABLE_GLUCOSE_VALUES")
    override fun deleteAllEntries()

    @Query("DELETE FROM $TABLE_GLUCOSE_VALUES WHERE timestamp < :than")
    override fun deleteOlderThan(than: Long): Int

    @Query("DELETE FROM $TABLE_GLUCOSE_VALUES WHERE referenceId IS NOT NULL")
    override fun deleteTrackedChanges(): Int

    @Query("SELECT * FROM $TABLE_GLUCOSE_VALUES WHERE isValid = 1 AND referenceId IS NULL ORDER BY timestamp DESC limit 1")
    suspend fun getLast(): GlucoseValue?

    @Query("SELECT id FROM $TABLE_GLUCOSE_VALUES ORDER BY id DESC limit 1")
    suspend fun getLastId(): Long?

    @Query("SELECT * FROM $TABLE_GLUCOSE_VALUES WHERE unlikely(nightscoutId = :nsId) AND likely(referenceId IS NULL)")
    suspend fun findByNSId(nsId: String): GlucoseValue?

    @Query("SELECT * FROM $TABLE_GLUCOSE_VALUES WHERE unlikely(timestamp = :timestamp) AND likely(sourceSensor = :sourceSensor) AND likely(referenceId IS NULL)")
    suspend fun findByTimestampAndSensor(timestamp: Long, sourceSensor: GlucoseValue.SourceSensor): GlucoseValue?

    @Query("SELECT * FROM $TABLE_GLUCOSE_VALUES WHERE unlikely(timestamp >= :timestamp) AND likely(isValid = 1) AND likely(referenceId IS NULL) AND likely(value >= 39) ORDER BY timestamp ASC")
    suspend fun compatGetBgReadingsDataFromTime(timestamp: Long): List<GlucoseValue>

    @Query("SELECT * FROM $TABLE_GLUCOSE_VALUES WHERE unlikely(timestamp BETWEEN :start AND :end) AND likely(isValid = 1) AND likely(referenceId IS NULL) AND likely(value >= 39) ORDER BY timestamp ASC")
    suspend fun compatGetBgReadingsDataFromTime(start: Long, end: Long): List<GlucoseValue>

    // for WS we need 1 record only
    @Query("SELECT * FROM $TABLE_GLUCOSE_VALUES WHERE id > :id ORDER BY id ASC limit 1")
    suspend fun getNextModifiedOrNewAfter(id: Long): GlucoseValue?

    @Query("SELECT * FROM $TABLE_GLUCOSE_VALUES WHERE id = :referenceId")
    suspend fun getCurrentFromHistoric(referenceId: Long): GlucoseValue?

    @Query("SELECT * FROM $TABLE_GLUCOSE_VALUES WHERE dateCreated > :since AND dateCreated <= :until LIMIT :limit OFFSET :offset")
    suspend fun getNewEntriesSince(since: Long, until: Long, limit: Int, offset: Int): List<GlucoseValue>
}