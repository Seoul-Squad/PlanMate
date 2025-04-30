package org.example.presentation.navigation

sealed interface Route {
    data object LoginRoute : Route
    data class ShowAllProjectsRoute(val userId: String) : Route
}