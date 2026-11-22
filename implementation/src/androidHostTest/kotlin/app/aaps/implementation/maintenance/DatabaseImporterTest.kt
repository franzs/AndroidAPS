package app.aaps.implementation.maintenance

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import app.aaps.core.interfaces.configuration.AppExit
import app.aaps.core.interfaces.db.DatabaseImportCheck
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.maintenance.FileListProvider
import app.aaps.shared.tests.TestBaseWithProfile
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.createTempDirectory

class DatabaseImporterTest : TestBaseWithProfile() {

    @Mock lateinit var prefFileList: FileListProvider
    @Mock lateinit var persistenceLayer: PersistenceLayer
    @Mock lateinit var appExit: AppExit

    private val directory = createTempDirectory("aaps-db-import").toFile()
    private val databaseFile = File(directory, DATABASE_FILE_NAME).apply { writeText("old database") }
    private val staged = File(databaseFile.path + DatabaseImporter.STAGED_IMPORT_SUFFIX)
    private val exportDir = mock<DocumentFile>()
    private val export = mock<DocumentFile>()
    private val contentResolver = mock<ContentResolver>()
    private val fileName = "2026-11-09_222557$DATABASE_EXPORT_SUFFIX"
    private val newDatabase = "new database".toByteArray()

    private fun importer() = DatabaseImporter(context, aapsLogger, rh, prefFileList, persistenceLayer, rxBus, appExit)

    @BeforeEach
    fun setup() {
        whenever(rh.gs(any<Int>())).thenReturn("msg")
        whenever(rh.gs(any<Int>(), anyVararg<Any>())).thenReturn("msg")
        whenever(context.getDatabasePath(any())).thenReturn(databaseFile)
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(prefFileList.ensureExportDirExists()).thenReturn(exportDir)
        whenever(exportDir.listFiles()).thenReturn(arrayOf(export))
        whenever(export.isFile).thenReturn(true)
        whenever(export.name).thenReturn(fileName)
    }

    @AfterEach
    fun tearDown() {
        directory.deleteRecursively()
    }

    private fun exportHolds(entryName: String) {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            zip.putNextEntry(ZipEntry(entryName))
            zip.write(newDatabase)
            zip.closeEntry()
        }
        whenever(contentResolver.openInputStream(anyOrNull())).thenReturn(ByteArrayInputStream(bytes.toByteArray()))
    }

    @Test
    fun `a valid export is staged and the app restarts`() = runTest {
        exportHolds(DATABASE_FILE_NAME)
        whenever(persistenceLayer.checkDatabaseImport(any())).thenReturn(DatabaseImportCheck.VALID)

        importer().import(fileName)

        Assertions.assertArrayEquals(newDatabase, staged.readBytes())
        Assertions.assertEquals("old database", databaseFile.readText())
        verify(appExit).exit(true)
        Assertions.assertEquals(listOf(DATABASE_FILE_NAME, staged.name), directory.list()?.sorted())
    }

    @Test
    fun `a damaged database is not staged`() = runTest {
        exportHolds(DATABASE_FILE_NAME)
        whenever(persistenceLayer.checkDatabaseImport(any())).thenReturn(DatabaseImportCheck.DAMAGED)

        importer().import(fileName)

        verify(appExit, never()).exit(any())
        Assertions.assertEquals(listOf(DATABASE_FILE_NAME), directory.list()?.toList())
    }

    @Test
    fun `a zip file with another content is not checked`() = runTest {
        exportHolds("something.txt")

        importer().import(fileName)

        verify(persistenceLayer, never()).checkDatabaseImport(any())
        verify(appExit, never()).exit(any())
        Assertions.assertEquals(listOf(DATABASE_FILE_NAME), directory.list()?.toList())
    }

    @Test
    fun `an export that is not in the exports folder is not imported`() = runTest {
        importer().import("other$DATABASE_EXPORT_SUFFIX")

        verify(contentResolver, never()).openInputStream(anyOrNull())
        verify(appExit, never()).exit(any())
    }

    @Test
    fun `the newest export comes first`() = runTest {
        val older = mock<DocumentFile>()
        whenever(older.isFile).thenReturn(true)
        whenever(older.name).thenReturn("2026-10-01_080000$DATABASE_EXPORT_SUFFIX")
        val other = mock<DocumentFile>()
        whenever(other.isFile).thenReturn(true)
        whenever(other.name).thenReturn("2026-12-01_080000_UserEntry.csv")
        whenever(exportDir.listFiles()).thenReturn(arrayOf(older, other, export))

        Assertions.assertEquals(listOf(fileName, older.name), importer().databaseExports().map { it.name })
    }
}
