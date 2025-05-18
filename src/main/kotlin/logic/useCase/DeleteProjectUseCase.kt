package org.example.logic.useCase

import io.reactivex.rxjava3.core.Completable
import org.example.logic.models.AuditLog
import org.example.logic.repositries.ProjectRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DeleteProjectUseCase(
    private val projectRepository: ProjectRepository,
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val createAuditLogUseCase: CreateAuditLogUseCase,
) {
    operator fun invoke(projectId: Uuid): Completable {
        val project = getProjectByIdUseCase(projectId).blockingGet()
        return projectRepository.deleteProject(projectId).doOnComplete {
            createAuditLogUseCase
                .logDeletion(
                    entityType = AuditLog.EntityType.PROJECT,
                    entityId = project.id,
                    entityName = project.name,
                ).blockingGet()
        }
    }
}
