package di

import org.example.presentation.MainUiController
import org.example.presentation.navigation.NavigationController
import org.example.presentation.navigation.Route
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val uiModule =
    module {
        single { NavigationController(Route.LoginRoute) }
        singleOf(::MainUiController)
    }
