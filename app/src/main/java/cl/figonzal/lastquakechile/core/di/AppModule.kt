package cl.figonzal.lastquakechile.core.di

import cl.figonzal.lastquakechile.core.utils.provideDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {

    //IoDispatcher
    single(named("ioDispatcher")) { Dispatchers.IO }

    //Database
    single(named("database")) { provideDatabase(get()) }
}