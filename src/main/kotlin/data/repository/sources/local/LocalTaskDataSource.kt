package org.example.data.repository.sources.local

import org.example.logic.models.Task
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface LocalTaskDataSource {
    fun createTask(task: Task): Task

    fun updateTask(updatedTask: Task): Task

    fun deleteTask(taskId: Uuid)

    fun getAllTasks(): List<Task>

    fun getTaskById(taskId: Uuid): Task?

    fun getTasksByProjectState(stateId: Uuid): List<Task>
}
