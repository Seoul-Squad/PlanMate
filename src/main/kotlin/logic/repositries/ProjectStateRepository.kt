package org.example.logic.repositries

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.ProjectState
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface ProjectStateRepository {
    fun createProjectState(projectState: ProjectState): Single<ProjectState>

    fun updateProjectState(updatedProjectState: ProjectState): Single<ProjectState>

    fun deleteProjectState(projectStateId: Uuid): Completable

    fun getProjectStates(projectId: Uuid): Single<List<ProjectState>>

    fun getProjectStateById(projectStateId: Uuid): Single<ProjectState>
}
