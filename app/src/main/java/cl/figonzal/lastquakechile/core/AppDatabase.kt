package cl.figonzal.lastquakechile.core

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cl.figonzal.lastquakechile.quake_feature.data.local.QuakeDAO
import cl.figonzal.lastquakechile.quake_feature.data.local.entity.QuakeEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.ReportDAO
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.QuakeCityEntity
import cl.figonzal.lastquakechile.reports_feature.data.local.entity.ReportEntity

@Database(
    entities = [QuakeEntity::class, ReportEntity::class, QuakeCityEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun quakeDao(): QuakeDAO
    abstract fun reportDao(): ReportDAO

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lqch_database"
                ).fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }

    }
}