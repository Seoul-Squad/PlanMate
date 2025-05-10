package org.example.presentation.screens

import kotlinx.coroutines.runBlocking
import org.example.logic.useCase.CreateProjectUseCase
import org.example.logic.utils.*
import presentation.utils.cyan
import presentation.utils.green
import presentation.utils.io.Reader
import presentation.utils.io.Viewer
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class CreateNewProjectUi(
    private val createProjectUseCase: CreateProjectUseCase,
    private val onBack: () -> Unit,
    private val reader: Reader,
    private val viewer: Viewer,
) {
    init {
        run()
    }

    private fun run() =
        runBlocking {
            viewer.display("\n========== Create New Project ==========".cyan())
            viewer.display("Enter project name: ")

            val projectName = reader.readString()

            try {
                val project = createProjectUseCase(projectName)
                viewer.display("✅ Project '${project.name}' created successfully with ID: ${project.id}".green())
            } catch (e: BlankInputException) {
                viewer.display("❌ Error: $BLANK_INPUT_EXCEPTION_MESSAGE")
            } catch (e: ProjectCreationFailedException) {
                viewer.display("❌ Error: $PROJECT_NAME_LENGTH_EXCEPTION_MESSAGE")
            } catch (e: NoLoggedInUserException) {
                viewer.display("❌ Error: ${e.message}")
            } catch (e: UnauthorizedAccessException) {
                viewer.display("❌ Error: ${e.message}")
            } catch (e: InvalidAuditInputException) {
                viewer.display("❌ Error: ${e.message}")
            } catch (e: Exception) {
                viewer.display("❌ Unexpected error: ${e.message}")
            }

            viewer.display("\nReturning to Admin Home...")
            onBack()
        }

    companion object {
        const val BLANK_INPUT_EXCEPTION_MESSAGE = "Project name cannot be blank"
        const val PROJECT_CREATION_FAILED_EXCEPTION_MESSAGE = "Failed to create project"
        const val PROJECT_NAME_LENGTH_EXCEPTION_MESSAGE = "Project name should not exceed 16 characters"
    }
}
