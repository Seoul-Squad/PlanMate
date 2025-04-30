package org.example.presentation

import org.example.presentation.navigation.NavigationCallBack
import org.example.presentation.navigation.NavigationController
import org.example.presentation.navigation.Route
import org.example.presentation.screens.LoginUI
import org.example.presentation.screens.ShowAllProjectsUI
import org.koin.java.KoinJavaComponent.getKoin
import kotlin.system.exitProcess


class MainUiController(
    private val navigationController: NavigationController
) : NavigationCallBack {

    init {
        navigationController.registerNavigationCallBack(this)
    }
    override fun onNavigate(route: Route) {
        when (route) {
            is Route.LoginRoute -> {
                LoginUI(
                    onNavigateToShowAllProjects = { navigationController.navigateTo(Route.ShowAllProjectsRoute(userId = it)) },
                   loginUserUseCase =  getKoin().get()
                )
            }

            is Route.ShowAllProjectsRoute -> ShowAllProjectsUI(
                route.userId,
                { navigationController.popBackStack() }
            )
        }
    }

    override fun onFinish() {
        println("Exiting...")
        exitProcess(0)
    }

}