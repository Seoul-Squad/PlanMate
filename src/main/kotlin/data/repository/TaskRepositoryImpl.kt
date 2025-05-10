package org.example.data.repository

import org.example.data.repository.mapper.mapExceptionsToDomainException
import org.example.data.repository.sources.remote.RemoteTaskDataSource
import org.example.logic.models.Task
import org.example.logic.repositries.TaskRepository
import org.example.logic.utils.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class TaskRepositoryImpl(
    private val remoteTaskDataSource: RemoteTaskDataSource,
) : TaskRepository {
    override suspend fun createTask(task: Task): Task =
        mapExceptionsToDomainException(TaskCreationFailedException()) {
            remoteTaskDataSource.createTask(task)
        }

    override suspend fun updateTask(updatedTask: Task): Task =
        mapExceptionsToDomainException(TaskNotChangedException()) {
            remoteTaskDataSource.updateTask(updatedTask)
        }

    override suspend fun deleteTask(taskId: Uuid) =
        mapExceptionsToDomainException(TaskDeletionFailedException()) {
            remoteTaskDataSource.deleteTask(taskId.toHexString())
        }

    override suspend fun getAllTasks(): List<Task> =
        mapExceptionsToDomainException(NoTasksFoundException()) {
            remoteTaskDataSource.getAllTasks()
        }

    override suspend fun getTaskById(taskId: Uuid): Task? =
        mapExceptionsToDomainException(NoTaskFoundException()) {
            remoteTaskDataSource.getTaskById(taskId.toHexString())
        }

    override suspend fun deleteTasksByStateId(
        stateId: Uuid,
        projectId: Uuid,
    ) = mapExceptionsToDomainException(TaskStateNotFoundException()) {
        remoteTaskDataSource.deleteTasksByStateId(stateId.toHexString(), projectId.toHexString())
    }
}
