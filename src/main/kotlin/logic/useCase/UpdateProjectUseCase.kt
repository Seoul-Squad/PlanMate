package org.example.logic.useCase.updateProject

import io.reactivex.rxjava3.core.Single
import org.example.logic.models.AuditLog
import org.example.logic.models.AuditLog.FieldChange.Companion.detectChanges
import org.example.logic.models.Project
import org.example.logic.repositries.ProjectRepository
import org.example.logic.useCase.CreateAuditLogUseCase
import org.example.logic.useCase.Validation
import org.example.logic.utils.ProjectNotChangedException
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class UpdateProjectUseCase(
    private val projectRepository: ProjectRepository,
    private val createAuditLogUseCase: CreateAuditLogUseCase,
    private val validation: Validation,
) {
    operator fun invoke(updatedProject: Project): Single<Project> {
        validation.validateInputNotBlankOrThrow(updatedProject.name)
        val originalProject = currentOriginalProject(updatedProject)
        detectChanges(updatedProject, originalProject)
        return projectRepository.updateProject(updatedProject)
//            .also {
//                createLogs(
//                    originalProject = originalProject,
//                    newProject = updatedProject,
//                )
//            }
    }

    private suspend fun createLogs(
        originalProject: Project,
        newProject: Project,
    ) {
        newProject.detectChanges(originalProject).map { change ->
            createAuditLogUseCase
                .logUpdate(
                    entityId = newProject.id,
                    entityName = newProject.name,
                    entityType = AuditLog.EntityType.PROJECT,
                    fieldChange = change,
                ).id
        }
    }

    private fun detectChanges(
        originalProject: Project,
        newProject: Project,
    ) {
        if (originalProject.name == newProject.name) throw ProjectNotChangedException()
    }

    private fun currentOriginalProject(updatedProject: Project): Project {
        return projectRepository.getProjectById(updatedProject.id).blockingGet() //  ?: throw ProjectNotFoundException()
    }
}
