package org.example.presentation.screens

import org.example.logic.models.UserRole
import org.example.logic.useCase.LoginUserUseCase
import org.example.logic.utils.BlankInputException
import org.example.logic.utils.UserNotFoundException
import presentation.utils.blockingCollect
import presentation.utils.cyan
import presentation.utils.io.Reader
import presentation.utils.io.Viewer
import presentation.utils.red

class LoginUI(
    private val onNavigateToAdminHome: () -> Unit,
    private val onNavigateToShowAllProjects: (userRole: UserRole) -> Unit,
    private val loginUserUseCase: LoginUserUseCase,
    private val reader: Reader,
    private val viewer: Viewer,
) {
    init {
        run()
    }

    private fun run() {
        viewer.display("====================================".cyan())
        viewer.display(" Welcome to the Task Management System ")
        viewer.display("====================================".cyan())

        while (true) {
            viewer.display("Enter username: ")
            val username = reader.readString()

            viewer.display("Enter password: ")
            val password = reader.readString()

            try {
                val user = loginUserUseCase(username, password).blockingCollect()
                when (user.role) {
                    UserRole.ADMIN -> onNavigateToAdminHome()
                    UserRole.USER -> onNavigateToShowAllProjects(user.role)
                }
                break
            } catch (e: BlankInputException) {
                viewer.display("Error: ${e.message}")
            } catch (e: UserNotFoundException) {
                viewer.display("Error: ${e.message}")
            } catch (e: Exception) {
                viewer.display("\nInvalid Login Credentials. Please try again.".red())
            }
        }
    }
}
