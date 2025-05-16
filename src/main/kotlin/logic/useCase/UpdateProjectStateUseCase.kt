package org.example.logic.useCase

import org.example.logic.models.AuditLog
import org.example.logic.models.ProjectState
import org.example.logic.repositries.ProjectStateRepository
import org.example.logic.utils.Constants
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class UpdateProjectStateUseCase(
    private val projectStateRepository: ProjectStateRepository,
    private val getProjectStatesUseCase: GetProjectStatesUseCase,
    private val createAuditLogUseCase: CreateAuditLogUseCase,
    private val getTasksByProjectStateUseCase: GetTasksByProjectStateUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val validation: Validation,
) {
    operator fun invoke(
        newStateName: String,
        stateId: Uuid,
        projectId: Uuid,
    ) {
        validation.validateInputNotBlankOrThrow(newStateName)
        val oldStates = getProjectStatesUseCase(projectId).blockingGet()
        projectStateRepository
            .updateProjectState(
                ProjectState(stateId, newStateName, projectId),
            ).blockingSubscribe {
                createAuditLogUseCase
                    .logUpdate(
                        entityType = AuditLog.EntityType.PROJECT,
                        entityId = projectId,
                        entityName = "",
                        fieldChange =
                            AuditLog.FieldChange(
                                fieldName = Constants.FIELD_STATES,
                                oldValue = oldStates.joinToString(separator = ", ") { it.title },
                                newValue =
                                    oldStates
                                        .map { it.updateStateName(stateId, newStateName) }
                                        .joinToString(separator = ", ") { it.title },
                            ),
                    ).blockingGet()
                getTasksByProjectStateUseCase(stateId).blockingGet().forEach { task ->
                    updateTaskUseCase(task.copy(stateName = newStateName)).blockingGet()
                }
            }
    }

    private fun ProjectState.updateStateName(
        updatedStateId: Uuid,
        updatedStateName: String,
    ) = if (this.id == updatedStateId) this.copy(title = updatedStateName) else this
}
