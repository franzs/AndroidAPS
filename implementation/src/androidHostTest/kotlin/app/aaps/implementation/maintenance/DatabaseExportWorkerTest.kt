package app.aaps.implementation.maintenance

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
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
import java.io.IOException
import java.util.zip.ZipInputStream
import kotlin.io.path.createTempDirectory
import kotlin.test.assertIs

class DatabaseExportWorkerTest : TestBaseWithProfile() {

    @Mock lateinit var prefFileList: FileListProvider
    @Mock lateinit var persistenceLayer: PersistenceLayer
    @Mock lateinit var workerParameters: WorkerParameters

    private val directory = createTempDirectory("aaps-db-export").toFile()
    private val cacheDir = File(directory, "cache").apply { mkdirs() }
    private val databaseFile = File(directory, "androidaps.db").apply { writeText("database") }
    private val exportDir = mock<DocumentFile>()
    private val target = mock<DocumentFile>()
    private val output = ByteArrayOutputStream()
    private val databaseCopy = "copy of the database".toByteArray()

    private fun worker() =
        ImportExportPrefsImpl.DatabaseExportWorker(
            context, workerParameters, aapsLogger, fabricPrivacy, rh, prefFileList, persistenceLayer, rxBus
        )

    @BeforeEach
    fun setup() {
        whenever(rh.gs(any<Int>())).thenReturn("msg")
        whenever(rh.gs(any<Int>(), anyVararg<Any>())).thenReturn("msg")
        whenever(context.cacheDir).thenReturn(cacheDir)
        whenever(context.getDatabasePath(any())).thenReturn(databaseFile)
        val contentResolver = mock<ContentResolver>()
        whenever(context.contentResolver).thenReturn(contentResolver)
        whenever(contentResolver.openOutputStream(anyOrNull())).thenReturn(output)
        whenever(prefFileList.ensureExportDirExists()).thenReturn(exportDir)
        whenever(prefFileList.newExportDatabaseFile()).thenReturn(target)
    }

    @AfterEach
    fun tearDown() {
        directory.deleteRecursively()
    }

    @Test
    fun `the copy is zipped into the exports folder and the cache file is deleted`() = runTest {
        whenever(persistenceLayer.exportDatabaseCopy(any())).thenAnswer {
            File(it.getArgument<String>(0)).writeBytes(databaseCopy)
        }

        val result = worker().doWorkAndLog()

        Assertions.assertEquals(ListenableWorker.Result.success(), result)
        ZipInputStream(ByteArrayInputStream(output.toByteArray())).use { zip ->
            Assertions.assertEquals("androidaps.db", zip.nextEntry?.name)
            Assertions.assertArrayEquals(databaseCopy, zip.readBytes())
        }
        Assertions.assertEquals(0, cacheDir.listFiles()?.size)
    }

    @Test
    fun `a failed copy removes the zip file and the cache file`() = runTest {
        whenever(persistenceLayer.exportDatabaseCopy(any())).thenAnswer {
            File(it.getArgument<String>(0)).writeBytes(databaseCopy)
            throw IOException("disk full")
        }

        val result = worker().doWorkAndLog()

        assertIs<ListenableWorker.Result.Failure>(result)
        Assertions.assertEquals(0, cacheDir.listFiles()?.size)
    }

    @Test
    fun `a zip file that cannot be written is deleted`() = runTest {
        whenever(persistenceLayer.exportDatabaseCopy(any())).thenAnswer {
            File(it.getArgument<String>(0)).writeBytes(databaseCopy)
        }
        whenever(context.contentResolver.openOutputStream(anyOrNull())).thenReturn(null)

        val result = worker().doWorkAndLog()

        assertIs<ListenableWorker.Result.Failure>(result)
        verify(target).delete()
        Assertions.assertEquals(0, cacheDir.listFiles()?.size)
    }

    @Test
    fun `without an AAPS directory nothing is copied`() = runTest {
        whenever(prefFileList.ensureExportDirExists()).thenReturn(null)

        val result = worker().doWorkAndLog()

        assertIs<ListenableWorker.Result.Failure>(result)
        verify(persistenceLayer, never()).exportDatabaseCopy(any())
    }
}
