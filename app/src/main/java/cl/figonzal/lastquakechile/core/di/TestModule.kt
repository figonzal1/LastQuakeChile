package cl.figonzal.lastquakechile.core.di

import cl.figonzal.lastquakechile.core.utils.provideTestDatabase
import org.koin.dsl.module

/**
 * Dependencies for instrumented Test
 */
val instrumentationTestModule = module {

    //Test database
    single { provideTestDatabase(get()) }

}


