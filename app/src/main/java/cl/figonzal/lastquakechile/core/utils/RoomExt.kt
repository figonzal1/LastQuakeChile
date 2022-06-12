package cl.figonzal.lastquakechile.core.utils

import android.app.Application
import androidx.room.Room
import cl.figonzal.lastquakechile.core.AppDatabase
import cl.figonzal.lastquakechile.quake_feature.data.local.QuakeDAO
import cl.figonzal.lastquakechile.reports_feature.data.local.ReportDAO

/**
 * Provide production database with injection
 */
fun provideDatabase(application: Application): AppDatabase = AppDatabase.getDatabase(application)

/**
 * Provide quakeDAO for injection
 */
fun provideQuakeDao(database: AppDatabase): QuakeDAO = database.quakeDao()

/**
 * Provide reportDAO for injection
 */
fun provideReportDao(database: AppDatabase): ReportDAO = database.reportDao()

/**
 * Provide in memory database for injection test
 */
fun provideTestDatabase(application: Application): AppDatabase =
    Room.inMemoryDatabaseBuilder(
        application,
        AppDatabase::class.java
    ).allowMainThreadQueries().build()