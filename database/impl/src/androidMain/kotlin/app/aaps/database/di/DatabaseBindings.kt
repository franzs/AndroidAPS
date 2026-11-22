package app.aaps.database.di

import android.content.Context
import app.aaps.database.AppRepository
import app.aaps.database.applyStagedDatabaseImport
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * The database, seen from the graph.
 *
 * Only [AppRepository] leaves this module: `AppDatabase` is internal, so a binding for it could not
 * be referenced from the graph in `:app`. Building both here keeps the Room type inside the module
 * that owns it.
 */
@ContributesTo(AppScope::class)
@BindingContainer
object DatabaseBindings {

    @Provides
    @SingleIn(AppScope::class)
    fun appRepository(context: Context, config: DatabaseConfig): AppRepository {
        val builder = AppDatabaseBuilder()
        return AppRepository {
            if (config.inMemory) builder.provideInMemoryAppDatabase(context)
            else {
                // A database import waits for this moment, when the file is not open yet.
                applyStagedDatabaseImport(context.getDatabasePath(config.fileName))
                builder.provideAppDatabase(context, config.fileName)
            }
        }
    }
}
