package org.example.data.repository

import org.example.data.source.remote.contract.RemoteTaskStateDataSource
import org.example.logic.models.State
import org.example.logic.repositries.TaskStateRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class TaskStateRepositoryImpl(
    private val remoteTaskStateDataSource: RemoteTaskStateDataSource,
) : TaskStateRepository {
    override suspend fun createTaskState(taskState: State): State = remoteTaskStateDataSource.createTaskState(taskState)

    override suspend fun updateTaskState(updatedTaskState: State): State = remoteTaskStateDataSource.updateTaskState(updatedTaskState)

    override suspend fun deleteTaskState(taskStateId: Uuid) {
        remoteTaskStateDataSource.deleteTaskState(taskStateId.toHexString())
    }

    override suspend fun getProjectTaskStates(taskStateIds: List<Uuid>): List<State> =
        remoteTaskStateDataSource.getProjectTaskStates(
            taskStateIds.map {
                it.toHexString()
            },
        )

    override suspend fun getTaskStateById(taskStateId: Uuid): State? = remoteTaskStateDataSource.getTaskStateById(taskStateId.toHexString())
}
