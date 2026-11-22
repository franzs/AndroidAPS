package app.aaps.database

import java.io.File

/**
 * Where an imported database waits for the next start of the app: next to the database file, with this
 * suffix. The import checks the file first and gives it this name only when it is complete and valid.
 */
const val STAGED_IMPORT_SUFFIX = ".import"

/**
 * Puts an imported database in place of [database], if one waits next to it (see [STAGED_IMPORT_SUFFIX]).
 *
 * Call it before Room opens the database. The open database cannot be swapped while the app runs, so the
 * import only stages the file and restarts the app, and the swap happens here, on the way back.
 *
 * The `-wal` and `-shm` files of the old database are deleted first: SQLite would apply an old WAL file
 * to the new database and break it. If the app stops in between, the staged file is still there and the
 * next start finishes the swap.
 *
 * @return true when the database was replaced
 */
fun applyStagedDatabaseImport(database: File): Boolean {
    val staged = File(database.path + STAGED_IMPORT_SUFFIX)
    if (!staged.exists()) return false
    for (suffix in listOf("-wal", "-shm", "-journal")) {
        val file = File(database.path + suffix)
        if (file.exists() && !file.delete()) return false
    }
    return staged.renameTo(database)
}
