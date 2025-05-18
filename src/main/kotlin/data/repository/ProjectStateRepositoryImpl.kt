package org.example.data.repository

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.example.data.source.remote.contract.RemoteProjectStateDataSource
import org.example.logic.models.ProjectState
import org.example.logic.repositries.ProjectStateRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProjectStateRepositoryImpl(
    private val remoteProjectStateDataSource: RemoteProjectStateDataSource,
) : ProjectStateRepository {
    override fun createProjectState(projectState: ProjectState): Single<ProjectState> =
        remoteProjectStateDataSource.createProjectState(projectState)

    override fun updateProjectState(updatedProjectState: ProjectState): Single<ProjectState> =
        remoteProjectStateDataSource.updateProjectState(updatedProjectState)

    override fun deleteProjectState(projectStateId: Uuid): Completable = remoteProjectStateDataSource.deleteProjectState(projectStateId)

    override fun getProjectStates(projectId: Uuid): Single<List<ProjectState>> = remoteProjectStateDataSource.getProjectStates(projectId)

    override fun getProjectStateById(projectStateId: Uuid): Single<ProjectState> =
        remoteProjectStateDataSource.getProjectStateById(projectStateId)
}
