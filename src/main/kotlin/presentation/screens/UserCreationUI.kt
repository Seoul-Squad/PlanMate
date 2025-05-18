package org.example.presentation.screens

import org.example.logic.useCase.CreateUserUseCase
import org.example.logic.utils.*
import presentation.utils.blockingCollect
import presentation.utils.cyan
import presentation.utils.green
import presentation.utils.io.Reader
import presentation.utils.io.Viewer

class UserCreationUI(
    private val createUserUseCase: CreateUserUseCase,
    private val reader: Reader,
    private val viewer: Viewer,
    private val onBack: () -> Unit,
) {
    init {
        run()
    }

    private fun run() {
        viewer.display("\n========== Create New Mate User ==========".cyan())
        viewer.display("Enter username: ")
        val username = reader.readString()

        viewer.display("Enter password: ")
        val password = reader.readString()

        try {
            val user = createUserUseCase(username, password).blockingCollect()
            viewer.display("✅ User '${user.username}' created successfully".green())
        } catch (e: BlankInputException) {
            viewer.display("❌ Error: Username or Password cannot be blank")
        } catch (e: InvalidUsernameException) {
            viewer.display("❌ Error: Username cannot contain spaces")
        } catch (e: UserAlreadyExistsException) {
            viewer.display("❌ Error: User already exists")
        } catch (e: Exception) {
            viewer.display("❌ Unexpected error: ${e.message}")
        }

        viewer.display("\nReturning...")
        onBack()
    }
}
