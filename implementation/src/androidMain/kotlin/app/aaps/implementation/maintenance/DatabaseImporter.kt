package app.aaps.implementation.maintenance

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import app.aaps.core.interfaces.configuration.AppExit
import app.aaps.core.interfaces.db.DatabaseImportCheck
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.LTag
import app.aaps.core.interfaces.maintenance.FileListProvider
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventAppExit
import app.aaps.core.interfaces.rx.events.EventShowSnackbar
import app.aaps.implementation.R
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipInputStream

/** The name the app opens its database with (`DatabaseConfig.PRODUCTION`), kept here to avoid a module dependency. */
internal const val DATABASE_FILE_NAME = "androidaps.db"

/** The end of the name of every database export (see `FileListProvider.newExportDatabaseFile`). */
internal const val DATABASE_EXPORT_SUFFIX = "_androidaps.db.zip"

/**
 * Replaces the whole database with a database export from the exports folder, for moving AAPS to a new phone.
 *
 * Room cannot swap the open database file, so the import works in two steps:
 * 1. Here: unzip the export next to the database, check it, and give it the name of a staged import.
 *    Then the app restarts. Until then the app keeps running on the old database.
 * 2. On the next start, before Room opens the database: `applyStagedDatabaseImport` in the database
 *    module puts the staged file in place of the old one.
 *
 * Only an export with the same database version and schema is accepted (see
 * [PersistenceLayer.checkDatabaseImport]), so the next start never has to migrate or drop it.
 *
 * Not a WorkManager job on purpose: the import ends with a restart, and WorkManager would run an
 * unfinished job again after it, and with it the import and the restart.
 */
@SingleIn(AppScope::class)
@Inject
class DatabaseImporter(
    private val context: Context,
    private val aapsLogger: AAPSLogger,
    private val rh: ResourceHelper,
    private val prefFileList: FileListProvider,
    private val persistenceLayer: PersistenceLayer,
    private val rxBus: RxBus,
    private val appExit: AppExit
) {

    private val running = AtomicBoolean(false)

    /** The database exports in the exports folder, newest first. The names start with the export time. */
    suspend fun databaseExports(): List<DocumentFile> = withContext(Dispatchers.IO) {
        prefFileList.ensureExportDirExists()?.listFiles().orEmpty()
            .filter { it.isFile && it.name?.endsWith(DATABASE_EXPORT_SUFFIX) == true }
            .sortedByDescending { it.name }
    }

    /**
     * Imports the database export [fileName] from the exports folder and restarts the app.
     * When it fails, it says why in a snackbar, and the app keeps its database. A second call while
     * an import runs does nothing.
     */
    suspend fun import(fileName: String) {
        if (!running.compareAndSet(false, true)) return
        try {
            if (stage(fileName)) {
                aapsLogger.info(LTag.CORE, "Database import of $fileName staged, restarting")
                rxBus.send(EventAppExit())
                appExit.exit(launchAgain = true)
            }
        } finally {
            running.set(false)
        }
    }

    /** Step 1 of the import. True when the staged file is in place. */
    private suspend fun stage(fileName: String): Boolean = withContext(Dispatchers.IO) {
        val database = context.getDatabasePath(DATABASE_FILE_NAME)
        val staged = File(database.path + STAGED_IMPORT_SUFFIX)
        val candidate = File(staged.path + ".tmp")
        // Left-overs of an import that did not finish. A staged file of an import whose restart did not
        // happen is replaced as well: the user has chosen another export now.
        candidate.delete()
        staged.delete()
        if (prefFileList.ensureExportDirExists() == null) {
            return@withContext fail(rh.gs(R.string.error_accessing_filesystem_select_aaps_directory_properly))
        }
        val source = databaseExports().firstOrNull { it.name == fileName }
            ?: return@withContext fail(rh.gs(R.string.database_import_not_found, fileName))
        rxBus.send(EventShowSnackbar(rh.gs(R.string.database_import_started), EventShowSnackbar.Type.Info))
        try {
            if (!unzip(source, candidate)) return@withContext fail(rh.gs(R.string.database_import_not_an_export))
            when (persistenceLayer.checkDatabaseImport(candidate.path)) {
                DatabaseImportCheck.VALID         -> Unit
                DatabaseImportCheck.DAMAGED       -> return@withContext fail(rh.gs(R.string.database_import_damaged))
                DatabaseImportCheck.OTHER_VERSION -> return@withContext fail(rh.gs(R.string.database_import_other_version))
            }
            if (!candidate.renameTo(staged)) throw IOException("Cannot rename ${candidate.name}")
            true
        } catch (e: CancellationException) {
            throw e
        } catch (_: NotEnoughSpaceException) {
            fail(rh.gs(R.string.database_import_no_space))
        } catch (e: Exception) {
            aapsLogger.error(LTag.CORE, "Database import failed", e)
            fail(rh.gs(R.string.database_import_failed))
        } finally {
            candidate.delete()
        }
    }

    /** Unzips the database from [source] into [target]. False when [source] is not a database export. */
    private fun unzip(source: DocumentFile, target: File): Boolean {
        val input = context.contentResolver.openInputStream(source.uri) ?: throw IOException("Cannot open ${source.name}")
        ZipInputStream(BufferedInputStream(input, BUFFER_SIZE)).use { zip ->
            val entry = zip.nextEntry ?: return false
            if (entry.name != DATABASE_FILE_NAME) return false
            target.outputStream().use { output -> copyKeepingFreeSpace(zip, output, target.parentFile ?: target) }
        }
        return true
    }

    /**
     * Copies like [InputStream.copyTo], but stops before the phone runs full. The size of the database is
     * not known before it is unzipped, and a full phone would also stop the running app from writing.
     */
    private fun copyKeepingFreeSpace(input: InputStream, output: OutputStream, directory: File) {
        val buffer = ByteArray(BUFFER_SIZE)
        var sinceCheck = SPACE_CHECK_INTERVAL
        while (true) {
            val read = input.read(buffer)
            if (read < 0) return
            if (sinceCheck >= SPACE_CHECK_INTERVAL) {
                if (directory.usableSpace < FREE_SPACE_MARGIN) throw NotEnoughSpaceException()
                sinceCheck = 0
            }
            output.write(buffer, 0, read)
            sinceCheck += read
        }
    }

    private fun fail(message: String): Boolean {
        rxBus.send(EventShowSnackbar(message, EventShowSnackbar.Type.Error))
        return false
    }

    private class NotEnoughSpaceException : IOException("Not enough free space")

    companion object {

        /** `STAGED_IMPORT_SUFFIX` of the database module, kept here to avoid a module dependency. */
        internal const val STAGED_IMPORT_SUFFIX = ".import"
        private const val FREE_SPACE_MARGIN = 100L * 1024 * 1024
        private const val SPACE_CHECK_INTERVAL = 64L * 1024 * 1024
        private const val BUFFER_SIZE = 1024 * 1024
    }
}
