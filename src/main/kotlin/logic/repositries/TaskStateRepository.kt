package org.example.logic.repositries

import org.example.logic.models.State

interface TaskStateRepository {
    suspend fun createTaskState(taskState: State): State
    suspend fun updateTaskState(updatedTaskState: State): State
    suspend fun deleteTaskState(taskStateId: String)
    suspend fun getProjectTaskStates(taskStateIds: List<String>): List<State>
    suspend fun getTaskStateById(taskStateId: String): State?
}