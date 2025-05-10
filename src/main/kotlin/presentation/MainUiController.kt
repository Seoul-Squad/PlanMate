package org.example.presentation

import org.example.logic.models.UserRole
import org.example.presentation.navigation.NavigationCallBack
import org.example.presentation.navigation.NavigationController
import org.example.presentation.navigation.Route
import org.example.presentation.role.AdminOptions
import org.example.presentation.role.MateOptions
import org.example.presentation.role.SharedOptions
import org.example.presentation.screens.*
import org.koin.java.KoinJavaComponent.getKoin
import presentation.utils.io.Reader
import presentation.utils.io.Viewer
import kotlin.system.exitProcess
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class MainUiController(
    private val navigationController: NavigationController,
    private val viewer: Viewer,
    private val reader: Reader,
) : NavigationCallBack {
    init {
        navigationController.registerNavigationCallBack(this)
    }

    override fun onNavigate(route: Route) {
        when (route) {
            is Route.LoginRoute -> {
                LoginUI(
                    onNavigateToAdminHome = { navigationController.navigateTo(Route.AdminHomeRoute) },
                    onNavigateToShowAllProjects = { navigationController.navigateTo(Route.ProjectsOverviewUI(it)) },
                    loginUserUseCase = getKoin().get(),
                    reader = reader,
                    viewer = viewer,
                )
            }

            is Route.AdminHomeRoute -> {
                AdminHomeUI(
                    logoutUseCase = getKoin().get(),
                    onNavigateToShowAllProjectsUI = { navigationController.navigateTo(Route.ProjectsOverviewUI(it)) },
                    onNavigateToCreateProject = { navigationController.navigateTo(Route.CreateProjectRoute) },
                    onNavigateToCreateUser = { navigationController.navigateTo(Route.CreateUserRoute) },
                    onLogout = { navigationController.popBackStack() },
                    onExit = { onFinish() },
                    viewer = viewer,
                    reader = reader,
                    userRole = UserRole.ADMIN,
                )
            }

            is Route.ProjectsOverviewUI -> {
                ProjectsOverviewUI.create(
                    onNavigateToShowProjectTasksUI = { projectId ->
                        navigationController.navigateTo(Route.ShowProjectTasksRoute(projectId = projectId))
                    },
                    onNavigateToProjectStatusUI = { projectId ->
                        navigationController.navigateTo(Route.ProjectStatusRoute(projectId = projectId))
                    },
                    onLogout = {
                        navigationController.popBackStack()
                    },
                    onExit = {
                        onFinish()
                    },
                    projectScreensOptions = userFactory(route.userRole),
                )
            }

            is Route.CreateProjectRoute -> {
                CreateNewProjectUi(
                    createProjectUseCase = getKoin().get(),
                    onBack = { navigationController.navigateTo(Route.AdminHomeRoute) },
                    reader = reader,
                    viewer = viewer,
                )
            }

            is Route.ShowProjectTasksRoute -> {
                ShowProjectTasksUI.create(
                    projectId = route.projectId,
                    onNavigateBack = navigationController::popBackStack,
                    onNavigateToTaskDetails = {
                        navigationController.navigateTo(Route.TaskDetailsRoute(taskId = it))
                    },
                )
            }

            is Route.CreateUserRoute -> {
                CreateUserUi(
                    createMateUseCase = getKoin().get(),
                    reader = reader,
                    viewer = viewer,
                    onBack = { navigationController.popBackStack() },
                )
            }

            is Route.ProjectStatusRoute -> {
                ProjectStatusUI.create(
                    projectId = route.projectId,
                    onNavigateBack = navigationController::popBackStack,
                )
            }

            is Route.TaskDetailsRoute -> {
                ShowTaskInformation
                    .create(
                        onNavigateBack = navigationController::popBackStack,
                    ).showTaskInformation(taskId = route.taskId)
            }
        }
    }

    override fun onFinish() {
        viewer.display("Exiting...")
        exitProcess(0)
    }

    private fun userFactory(type: UserRole): SharedOptions =
        when (type) {
            UserRole.ADMIN -> AdminOptions()
            UserRole.USER -> MateOptions()
        }
}
