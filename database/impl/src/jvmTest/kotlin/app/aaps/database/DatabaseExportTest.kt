package app.aaps.database

import androidx.room.Transactor.SQLiteTransactionType
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.aaps.database.di.JvmAppDatabaseBuilder
import app.aaps.database.entities.APSResult
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

/**
 * [AppRepository.exportDatabaseCopy]: a copy of the database file with `VACUUM INTO`, made on its own
 * connection while Room has the same file open - as on the phone.
 */
class DatabaseExportTest {

    private val directory = createTempDirectory("aaps-export-test")
    private val source = "$directory/aaps.db"
    private val database = JvmAppDatabaseBuilder().provideAppDatabase(source)
    private val repository = AppRepository { database }
    private val now = Clock.System.now().toEpochMilliseconds()
    private val target = "$directory/export.db"

    @AfterTest
    fun tearDown() {
        repository.close()
        database.close()
        directory.toFile().deleteRecursively()
    }

    @Test
    fun `the copy holds all rows and needs no wal file`() = runTest {
        repeat(3) { database.apsResultDao.insert(apsResult(now - it * 300_000L)) }

        repository.exportDatabaseCopy(source, target)

        assertThat(countRows(target, "apsResults")).isEqualTo(3L)
        assertThat(File("$target-wal").exists()).isFalse()
    }

    @Test
    fun `an existing file is not overwritten`() = runTest {
        File(target).writeText("keep me")

        assertFails { repository.exportDatabaseCopy(source, target) }
        assertThat(File(target).readText()).isEqualTo("keep me")
    }

    /** The loop must be able to write while an export runs, so the export must not need the writer. */
    @Test
    fun `the export does not wait for an open write transaction`() = runBlocking {
        database.apsResultDao.insert(apsResult(now))
        val writing = CompletableDeferred<Unit>()
        val finishWriting = CompletableDeferred<Unit>()
        val writer = launch(Dispatchers.IO) {
            database.useWriterConnection { connection ->
                connection.withTransaction(SQLiteTransactionType.IMMEDIATE) {
                    writing.complete(Unit)
                    finishWriting.await()
                }
            }
        }
        writing.await()

        withTimeout(10.seconds) { repository.exportDatabaseCopy(source, target) }
        finishWriting.complete(Unit)
        writer.join()

        assertThat(countRows(target, "apsResults")).isEqualTo(1L)
    }

    private fun countRows(path: String, table: String): Long =
        BundledSQLiteDriver().open(path).use { connection ->
            connection.prepare("SELECT COUNT(*) FROM $table").use { statement ->
                statement.step()
                statement.getLong(0)
            }
        }

    private fun apsResult(timestamp: Long) = APSResult(
        timestamp = timestamp,
        algorithm = APSResult.Algorithm.SMB,
        glucoseStatusJson = null,
        currentTempJson = null,
        iobDataJson = null,
        profileJson = null,
        autosensDataJson = null,
        mealDataJson = null,
        resultJson = "{}"
    )
}
