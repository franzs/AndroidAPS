package app.aaps.database

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.aaps.database.di.JvmAppDatabaseBuilder
import app.aaps.database.entities.APSResult
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.time.Clock

/**
 * The two steps of a database import in this module: [AppRepository.checkDatabaseImport] decides
 * whether a file may replace the database, and [applyStagedDatabaseImport] replaces it on the next start.
 */
class DatabaseImportTest {

    private val directory = createTempDirectory("aaps-import-test")
    private val source = "$directory/aaps.db"
    private val database = JvmAppDatabaseBuilder().provideAppDatabase(source)
    private val repository = AppRepository { database }
    private val candidate = "$directory/candidate.db"

    @AfterTest
    fun tearDown() {
        repository.close()
        database.close()
        directory.toFile().deleteRecursively()
    }

    @Test
    fun `an export of the same database is valid`() = runTest {
        database.apsResultDao.insert(apsResult())
        repository.exportDatabaseCopy(source, candidate)

        assertThat(repository.checkDatabaseImport(candidate)).isEqualTo(ImportCheckOutcome.VALID)
    }

    @Test
    fun `a database of another version is refused`() = runTest {
        repository.exportDatabaseCopy(source, candidate)
        BundledSQLiteDriver().open(candidate).use { connection ->
            connection.prepare("PRAGMA user_version = ${DATABASE_VERSION - 1}").use { it.step() }
        }

        assertThat(repository.checkDatabaseImport(candidate)).isEqualTo(ImportCheckOutcome.OTHER_VERSION)
    }

    @Test
    fun `a database with another schema is refused`() = runTest {
        repository.exportDatabaseCopy(source, candidate)
        BundledSQLiteDriver().open(candidate).use { connection ->
            connection.prepare("UPDATE room_master_table SET identity_hash = 'other'").use { it.step() }
        }

        assertThat(repository.checkDatabaseImport(candidate)).isEqualTo(ImportCheckOutcome.OTHER_VERSION)
    }

    @Test
    fun `a file that is not a database is damaged`() = runTest {
        File(candidate).writeText("not a database")

        assertThat(repository.checkDatabaseImport(candidate)).isEqualTo(ImportCheckOutcome.DAMAGED)
    }

    @Test
    fun `a staged import replaces the database and its wal files`() {
        val target = File(directory.toFile(), "target.db").apply { writeText("old") }
        File(target.path + "-wal").writeText("old wal")
        File(target.path + "-shm").writeText("old shm")
        File(target.path + STAGED_IMPORT_SUFFIX).writeText("new")

        assertThat(applyStagedDatabaseImport(target)).isTrue()

        assertThat(target.readText()).isEqualTo("new")
        assertThat(File(target.path + "-wal").exists()).isFalse()
        assertThat(File(target.path + "-shm").exists()).isFalse()
        assertThat(File(target.path + STAGED_IMPORT_SUFFIX).exists()).isFalse()
    }

    @Test
    fun `without a staged import nothing changes`() {
        val target = File(directory.toFile(), "target.db").apply { writeText("old") }
        File(target.path + "-wal").writeText("old wal")

        assertThat(applyStagedDatabaseImport(target)).isFalse()

        assertThat(target.readText()).isEqualTo("old")
        assertThat(File(target.path + "-wal").exists()).isTrue()
    }

    private fun apsResult() = APSResult(
        timestamp = Clock.System.now().toEpochMilliseconds(),
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
