package org.example.data.source.remote.contract

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.ProjectState
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface RemoteProjectStateDataSource {
    fun createProjectState(projectState: ProjectState): Single<ProjectState>

    fun updateProjectState(updatedProjectProjectState: ProjectState): Single<ProjectState>

    fun deleteProjectState(projectStateId: Uuid): Completable

    fun getProjectStates(projectId: Uuid): Single<List<ProjectState>>

    fun getProjectStateById(projectStateId: Uuid): Single<ProjectState>
}
