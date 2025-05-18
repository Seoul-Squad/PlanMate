package org.example

import di.appModule
import org.example.presentation.MainUiController
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin

fun main() {
    startKoin {
        modules(appModule)
    }

    getKoin().get<MainUiController>()
}
