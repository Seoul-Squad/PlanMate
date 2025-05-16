package org.example.data.repository

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.example.data.repository.mapper.mapExceptionsToDomainExceptionRx
import org.example.data.repository.sources.remote.RemoteProjectDataSource
import org.example.data.source.remote.RoleValidationInterceptor
import org.example.logic.models.Project
import org.example.logic.repositries.ProjectRepository
import org.example.logic.utils.NoProjectsFoundException
import org.example.logic.utils.ProjectCreationFailedException
import org.example.logic.utils.ProjectNotChangedException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProjectRepositoryImpl(
    private val remoteProjectDataSource: RemoteProjectDataSource,
    private val roleValidationInterceptor: RoleValidationInterceptor,
) : ProjectRepository {
    override fun createProject(project: Project): Single<Project> =
        mapExceptionsToDomainExceptionRx(ProjectCreationFailedException()) {
            roleValidationInterceptor.validateRole { remoteProjectDataSource.createProject(project) }
        }

    override fun updateProject(updatedProject: Project): Single<Project> =
        mapExceptionsToDomainExceptionRx(ProjectNotChangedException()) {
            roleValidationInterceptor.validateRole { remoteProjectDataSource.updateProject(updatedProject) }
        }

    override fun deleteProject(projectId: Uuid): Completable =
        roleValidationInterceptor.validateRole { remoteProjectDataSource.deleteProject(projectId) }

    override fun getAllProjects(): Single<List<Project>> =
        mapExceptionsToDomainExceptionRx(NoProjectsFoundException()) {
            remoteProjectDataSource.getAllProjects()
        }

    override fun getProjectById(projectId: Uuid): Single<Project> =
        mapExceptionsToDomainExceptionRx(NoProjectsFoundException()) {
            remoteProjectDataSource.getProjectById(projectId)
        }
}
