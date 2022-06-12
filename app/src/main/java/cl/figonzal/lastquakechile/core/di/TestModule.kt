package cl.figonzal.lastquakechile.core.di

import cl.figonzal.lastquakechile.core.utils.provideTestDatabase
import org.koin.core.qualifier.named
import org.koin.dsl.module

//Dependencies for instrumented Test
val intrumentationTestModule = module {

    //Database
    single(named("test_database")) { provideTestDatabase(get()) }

}


