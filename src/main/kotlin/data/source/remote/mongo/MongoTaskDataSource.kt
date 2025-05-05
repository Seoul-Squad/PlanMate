package org.example.data.source.remote.mongo

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.example.data.mapper.toTask
import org.example.data.mapper.toTaskDTO
import org.example.data.models.TaskDTO
import org.example.data.source.remote.contract.RemoteTaskDataSource
import org.example.logic.models.Task

class MongoTaskDataSource(
    private val mongoClient: MongoCollection<TaskDTO>
) : RemoteTaskDataSource {
    override suspend fun createTask(task: Task): Task {
        mongoClient.insertOne(task.toTaskDTO())
        return task
    }

    override suspend fun updateTask(updatedTask: Task): Task {
        mongoClient.replaceOne(Filters.eq("id", updatedTask.id), updatedTask.toTaskDTO())
        return updatedTask
    }

    override suspend fun deleteTask(taskId: String) {
        mongoClient.deleteOne(Filters.eq("id", taskId))
    }

    override suspend fun getAllTasks(): List<Task> {
        return mongoClient.find().toList().map { it.toTask() }

    }

    override suspend fun getTaskById(taskId: String): Task? {
        return mongoClient.find(Filters.eq("id", taskId)).firstOrNull()?.toTask()

    }

    override suspend fun deleteTasksByStateId(stateId: String, taskId: String) {
        mongoClient.deleteOne(Filters.and(Filters.eq("stateId", stateId), (Filters.eq("id", taskId))))
    }
}