package logic.useCase

import io.reactivex.rxjava3.core.Single
import org.example.logic.models.AuditLog
import org.example.logic.models.ProjectState
import org.example.logic.models.Task
import org.example.logic.repositries.ProjectStateRepository
import org.example.logic.repositries.TaskRepository
import org.example.logic.useCase.CreateAuditLogUseCase
import org.example.logic.useCase.GetCurrentUserUseCase
import org.example.logic.useCase.Validation
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CreateTaskUseCase(
    private val taskRepository: TaskRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val createAuditLogUseCase: CreateAuditLogUseCase,
    private val projectStateRepository: ProjectStateRepository,
    private val validation: Validation,
) {
    operator fun invoke(
        name: String,
        projectId: Uuid,
        stateId: Uuid,
    ): Task {
        validation.validateInputNotBlankOrThrow(name)
        val state = getState(stateId)
        val currentUser = getCurrentUserUseCase().blockingGet()
        val task =
            Task(
                name = name,
                stateId = stateId,
                stateName = state.title,
                projectId = projectId,
                addedById = currentUser.id,
                addedByName = currentUser.username,
            )

        return taskRepository.createTask(task).doOnSuccess {
            createAuditLogUseCase
                .logCreation(
                    entityId = task.id,
                    entityName = task.name,
                    entityType = AuditLog.EntityType.TASK,
                ).blockingGet()
        }
    }

    private fun getState(stateId: Uuid): ProjectState = projectStateRepository.getProjectStateById(stateId).blockingGet()
}
