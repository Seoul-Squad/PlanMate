package di

import org.koin.dsl.module

val appModule =
    module {
        includes(uiModule, repositoryModule, useCaseModule)
    }
