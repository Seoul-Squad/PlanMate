package org.example.data.repository

import io.reactivex.rxjava3.core.Single
import org.example.data.repository.mapper.mapExceptionsToDomainExceptionRx
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
    override fun createTask(task: Task): Single<Task> =
        mapExceptionsToDomainExceptionRx(TaskCreationFailedException()) {
            remoteTaskDataSource.createTask(task)
        }

    override fun updateTask(updatedTask: Task): Single<Task> =
        mapExceptionsToDomainExceptionRx(TaskNotChangedException()) {
            remoteTaskDataSource.updateTask(updatedTask)
        }

    override fun deleteTask(taskId: Uuid) =
        mapExceptionsToDomainExceptionRx(TaskDeletionFailedException()) {
            remoteTaskDataSource.deleteTask(taskId)
        }

    override fun getAllTasks(): Single<List<Task>> =
        mapExceptionsToDomainExceptionRx(NoTasksFoundException()) {
            remoteTaskDataSource.getAllTasks()
        }

    override fun getTaskById(taskId: Uuid): Single<Task> =
        mapExceptionsToDomainExceptionRx(NoTaskFoundException()) {
            remoteTaskDataSource.getTaskById(taskId)
        }

    override fun getTasksByProjectState(stateId: Uuid): Single<List<Task>> =
        mapExceptionsToDomainExceptionRx(NoTaskFoundException()) {
            remoteTaskDataSource.getTasksByProjectState(stateId)
        }
}
