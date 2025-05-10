package org.example.presentation.navigation

import org.example.logic.models.UserRole
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
sealed interface Route {
    data object LoginRoute : Route

    data object AdminHomeRoute : Route

    data class ProjectsOverviewUI(
        val userRole: UserRole,
    ) : Route

    data object CreateProjectRoute : Route

    data class ShowProjectTasksRoute(
        val projectId: Uuid,
    ) : Route

    data class ProjectStatusRoute(
        val projectId: Uuid,
    ) : Route

    data object CreateUserRoute : Route

    data class TaskDetailsRoute(
        val taskId: Uuid,
    ) : Route
}
