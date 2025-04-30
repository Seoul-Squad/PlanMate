package org.example.presentation.navigation

class NavigationController(
    private val startDestination: Route
) {
    private val navigationBackStack: MutableList<Route> = mutableListOf(startDestination)
    private lateinit var navigationCallBack: NavigationCallBack

    fun registerNavigationCallBack(navigationCallBack: NavigationCallBack) {
        this.navigationCallBack = navigationCallBack
        navigateTo(startDestination)
    }

    fun popBackStack() {
        if (navigationBackStack.size > 1) {
            navigationBackStack.removeLast()
            navigationCallBack.onNavigate(navigationBackStack.last())
        } else {
            navigationCallBack.onFinish()
        }
    }

    fun navigateTo(destination: Route) {
        navigationBackStack.add(destination)
        navigationCallBack.onNavigate(destination)
    }
}