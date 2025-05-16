package org.example.data.repository.sources.remote

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.Task
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface RemoteTaskDataSource {
    fun createTask(task: Task): Single<Task>

    fun updateTask(updatedTask: Task): Single<Task>

    fun deleteTask(taskId: Uuid): Completable

    fun getAllTasks(): Single<List<Task>>

    fun getTaskById(taskId: Uuid): Single<Task>

    fun getTasksByProjectState(stateId: Uuid): Single<List<Task>>
}
