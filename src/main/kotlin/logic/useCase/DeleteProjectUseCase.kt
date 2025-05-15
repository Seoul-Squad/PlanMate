package org.example.logic.useCase

import org.example.logic.repositries.ProjectRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DeleteProjectUseCase(
    private val projectRepository: ProjectRepository,
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val createAuditLogUseCase: CreateAuditLogUseCase,
) {
    operator fun invoke(projectId: Uuid) {
        getProjectByIdUseCase(projectId).let { project ->
            projectRepository.deleteProject(projectId)
//                .also {
//                createAuditLogUseCase.logDeletion(
//                    entityType = AuditLog.EntityType.PROJECT,
//                    entityId = project.blockingGet().id,
//                    entityName = project.blockingGet().name,
//                )
//            }
        }
    }
}
