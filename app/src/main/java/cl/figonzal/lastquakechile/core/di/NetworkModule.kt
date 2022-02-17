package cl.figonzal.lastquakechile.core.di

import cl.figonzal.lastquakechile.core.utils.provideApiService
import org.koin.core.qualifier.named
import org.koin.dsl.module


val networkModule = module {
    single(named("apiService")) { provideApiService() }
}
