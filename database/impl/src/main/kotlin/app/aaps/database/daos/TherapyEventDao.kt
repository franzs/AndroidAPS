package app.aaps.database.daos

import androidx.room.Dao
import androidx.room.Query
import app.aaps.database.entities.TABLE_THERAPY_EVENTS
import app.aaps.database.entities.TherapyEvent

@Dao
internal interface TherapyEventDao : TraceableDao<TherapyEvent> {

    @Query("SELECT * FROM $TABLE_THERAPY_EVENTS WHERE id = :id")
    override fun findById(id: Long): TherapyEvent?

    @Query("DELETE FROM $TABLE_THERAPY_EVENTS")
    override fun deleteAllEntries()

    @Query("DELETE FROM $TABLE_THERAPY_EVENTS WHERE timestamp < :than")
    override fun deleteOlderThan(than: Long): Int

    @Query("DELETE FROM $TABLE_THERAPY_EVENTS WHERE referenceId IS NOT NULL")
    override fun deleteTrackedChanges(): Int

    @Query("SELECT id FROM $TABLE_THERAPY_EVENTS ORDER BY id DESC limit 1")
    suspend fun getLastId(): Long?

    @Query("SELECT * FROM $TABLE_THERAPY_EVENTS WHERE likely(type = :type) AND unlikely(timestamp = :timestamp) AND likely(referenceId IS NULL)")
    suspend fun findByTimestamp(type: TherapyEvent.Type, timestamp: Long): TherapyEvent?

    @Query("SELECT * FROM $TABLE_THERAPY_EVENTS WHERE unlikely(type = :type) AND likely(referenceId IS NULL)")
    suspend fun getValidByType(type: TherapyEvent.Type): List<TherapyEvent>

    @Query("SELECT * FROM $TABLE_THERAPY_EVENTS WHERE unlikely(nightscoutId = :nsId) AND likely(referenceId IS NULL)")
    suspend fun findByNSId(nsId: String): TherapyEvent?

    @Query("SELECT * FROM $TABLE_THERAPY_EVENTS WHERE unlikely(timestamp >= :timestamp) AND likely(isValid = 1) AND likely(referenceId IS NULL) ORDER BY timestamp ASC")
    suspend fun getTherapyEventDataFromTime(timestamp: Long): List<TherapyEvent>

    @Query("SELECT * FROM $TABLE_THERAPY_EVENTS WHERE type = :type AND unlikely(timestamp >= :timestamp) AND likely(isValid = 1) AND likely(referenceId IS NULL) ORDER BY timestamp ASC")
    suspend fun getTherapyEventDataFromTime(timestamp: Long, type: TherapyEvent.Type): List<TherapyEvent>

    @Query("SELECT * FROM $TABLE_THERAPY_EVENTS WHERE unlikely(timestamp >= :timestamp) AND likely(referenceId IS NULL) ORDER BY timestamp ASC")
    suspend fun getTherapyEventDataIncludingInvalidFromTime(timestamp: Long): List<TherapyEvent>

    @Query("SELECT * FROM $TABLE_THERAPY_EVENTS WHERE type = :type AND likely(isValid = 1) AND unlikely(timestamp <= :now) AND likely(referenceId IS NULL) ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastTherapyRecord(type: TherapyEvent.Type, now: Long): TherapyEvent?

    @Query("SELECT * FROM $TABLE_THERAPY_EVENTS WHERE unlikely(timestamp BETWEEN :from AND :to) AND likely(isValid = 1) AND likely(referenceId IS NULL) ORDER BY timestamp ASC")
    suspend fun compatGetTherapyEventDataFromToTime(from: Long, to: Long): List<TherapyEvent>

    // for WS we need 1 record only
    @Query("SELECT * FROM $TABLE_THERAPY_EVENTS WHERE id > :id ORDER BY id ASC limit 1")
    suspend fun getNextModifiedOrNewAfter(id: Long): TherapyEvent?

    @Query("SELECT * FROM $TABLE_THERAPY_EVENTS WHERE id = :referenceId")
    suspend fun getCurrentFromHistoric(referenceId: Long): TherapyEvent?

    @Query("SELECT * FROM $TABLE_THERAPY_EVENTS WHERE dateCreated > :since AND dateCreated <= :until LIMIT :limit OFFSET :offset")
    suspend fun getNewEntriesSince(since: Long, until: Long, limit: Int, offset: Int): List<TherapyEvent>
}
