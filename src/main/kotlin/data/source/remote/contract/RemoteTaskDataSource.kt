package org.example.data.source.remote.contract

import org.example.logic.models.Task

interface RemoteTaskDataSource {
    suspend fun createTask(task: Task): Task

    suspend fun updateTask(updatedTask: Task): Task

    suspend fun deleteTask(taskId: String)

    suspend fun getAllTasks(): List<Task>

    suspend fun getTaskById(taskId: String): Task?
    
    suspend fun deleteTasksByStateId(stateId: String, taskId: String)
}
