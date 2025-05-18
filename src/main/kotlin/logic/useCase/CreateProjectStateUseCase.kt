package org.example.logic.useCase

import io.reactivex.rxjava3.core.Single
import org.example.logic.models.AuditLog
import org.example.logic.models.ProjectState
import org.example.logic.repositries.ProjectStateRepository
import org.example.logic.utils.Constants
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CreateProjectStateUseCase(
    private val projectStateRepository: ProjectStateRepository,
    private val getProjectStatesUseCase: GetProjectStatesUseCase,
    private val createAuditLogUseCase: CreateAuditLogUseCase,
    private val validation: Validation,
) {
    @OptIn(ExperimentalUuidApi::class)
    operator fun invoke(
        projectId: Uuid,
        stateName: String,
    ): Single<ProjectState> {
        validation.validateInputNotBlankOrThrow(stateName)
        val oldStates = getProjectStatesUseCase(projectId).blockingGet()
        return projectStateRepository
            .createProjectState(
                ProjectState(
                    title = stateName,
                    projectId = projectId,
                ),
            ).doOnSuccess {
                createAuditLogUseCase.logUpdate(
                    entityType = AuditLog.EntityType.PROJECT,
                    entityId = projectId,
                    entityName = "",
                    fieldChange =
                        AuditLog.FieldChange(
                            fieldName = Constants.FIELD_STATES,
                            oldValue = oldStates.joinToString(separator = ", ") { it.title },
                            newValue =
                                oldStates
                                    .plusElement(it)
                                    .joinToString(separator = ", ") { it.title },
                        ),
                )
            }
    }
}
