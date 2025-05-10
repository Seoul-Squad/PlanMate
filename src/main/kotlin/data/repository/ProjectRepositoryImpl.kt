package org.example.data.repository

import org.example.data.repository.mapper.mapExceptionsToDomainException
import org.example.data.repository.sources.remote.RemoteProjectDataSource
import org.example.data.source.remote.RoleValidationInterceptor
import org.example.logic.models.Project
import org.example.logic.repositries.ProjectRepository
import org.example.logic.utils.NoProjectsFoundException
import org.example.logic.utils.ProjectCreationFailedException
import org.example.logic.utils.ProjectDeletionFailedException
import org.example.logic.utils.ProjectNotChangedException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProjectRepositoryImpl(
    private val remoteProjectDataSource: RemoteProjectDataSource,
    private val roleValidationInterceptor: RoleValidationInterceptor,
) : ProjectRepository {
    override suspend fun createProject(project: Project): Project =
        mapExceptionsToDomainException(ProjectCreationFailedException()) {
            roleValidationInterceptor.validateRole { remoteProjectDataSource.createProject(project) }
        }

    override suspend fun updateProject(updatedProject: Project): Project =
        mapExceptionsToDomainException(ProjectNotChangedException()) {
            roleValidationInterceptor.validateRole { remoteProjectDataSource.updateProject(updatedProject) }
        }

    override suspend fun deleteProject(projectId: Uuid) =
        mapExceptionsToDomainException(ProjectDeletionFailedException()) {
            roleValidationInterceptor.validateRole { remoteProjectDataSource.deleteProject(projectId.toHexString()) }
        }

    override suspend fun getAllProjects(): List<Project> =
        mapExceptionsToDomainException(NoProjectsFoundException()) {
            remoteProjectDataSource.getAllProjects()
        }

    override suspend fun getProjectById(projectId: Uuid): Project? =
        mapExceptionsToDomainException(NoProjectsFoundException()) {
            remoteProjectDataSource.getProjectById(projectId.toHexString())
        }
}
