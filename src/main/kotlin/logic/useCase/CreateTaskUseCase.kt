package logic.useCase

import org.example.logic.models.*
import org.example.logic.repositries.ProjectRepository
import org.example.logic.repositries.TaskRepository
import org.example.logic.repositries.TaskStateRepository
import org.example.logic.useCase.CreateAuditLogUseCase
import org.example.logic.useCase.GetCurrentUserUseCase
import org.example.logic.utils.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CreateTaskUseCase(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val createAuditLogUseCase: CreateAuditLogUseCase,
    private val taskStateRepository: TaskStateRepository,
) {
    suspend operator fun invoke(
        name: String,
        projectId: Uuid,
        stateId: Uuid,
    ): Task {
        verifyNoBlankInputs(name)
        val state = verifyProjectAndStateExist(projectId, stateId)
        return createAndLogTask(name, projectId, stateId, state.title, getCurrentUserUseCase())
    }

    private suspend fun createAndLogTask(
        taskName: String,
        projectId: Uuid,
        stateId: Uuid,
        stateName: String,
        loggedInUser: User,
    ): Task {
        val taskId = Uuid.random()
        val taskAuditLog =
            createAuditLogUseCase.logCreation(
                entityId = taskId,
                entityName = taskName,
                entityType = AuditLog.EntityType.TASK,
            )
        val newTask =
            Task(
                id = taskId,
                name = taskName,
                stateId = stateId,
                stateName = stateName,
                projectId = projectId,
                addedBy = loggedInUser.username,
                auditLogsIds = listOf(taskAuditLog.id),
            )
        taskRepository.createTask(newTask)
        return newTask
    }

    private suspend fun verifyProjectAndStateExist(
        projectId: Uuid,
        stateId: Uuid,
    ): State =
        projectRepository.getProjectById(projectId)?.let { project ->
            if (project.tasksStatesIds.none { it == stateId }) throw TaskStateNotFoundException()
            taskStateRepository.getTaskStateById(stateId)
        } ?: throw ProjectNotFoundException()

    private fun verifyNoBlankInputs(name: String) {
        when {
            name.isBlank() -> throw BlankInputException()
        }
    }
}
