package org.example

import com.sun.tools.javac.Main
import di.appModule
import org.example.presentation.MainUiController
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin

fun main() {
    startKoin {
        modules(appModule)
    }

    println("Loading, Please wait...")
    val consoleUi: MainUiController = getKoin().get()
}
