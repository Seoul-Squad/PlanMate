package org.example.logic.repositries

import org.example.logic.models.ProjectState
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface ProjectStateRepository {
    suspend fun createProjectState(projectState: ProjectState): ProjectState
    suspend fun updateProjectState(updatedProjectState: ProjectState): ProjectState
    suspend fun deleteProjectState(projectStateId: Uuid)
    suspend fun getProjectStates(projectId: Uuid): List<ProjectState>
    suspend fun getProjectStateById(projectStateId: Uuid): ProjectState?
}