package org.example.data.source.remote.mongo

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.example.data.repository.sources.remote.RemoteTaskDataSource
import org.example.data.source.remote.models.TaskDTO
import org.example.data.source.remote.mongo.utils.executeMongoOperation
import org.example.data.source.remote.mongo.utils.mapper.toTask
import org.example.data.source.remote.mongo.utils.mapper.toTaskDTO
import org.example.data.utils.Constants.ID
import org.example.data.utils.Constants.STATE_ID_FIELD
import org.example.logic.models.Task
import org.example.logic.utils.*
import kotlin.uuid.ExperimentalUuidApi

class MongoTaskDataSource(
    private val mongoClient: MongoCollection<TaskDTO>,
) : RemoteTaskDataSource {
    override suspend fun createTask(task: Task): Task =
        executeMongoOperation {
            mongoClient.insertOne(task.toTaskDTO())
            task
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun updateTask(updatedTask: Task): Task =
        executeMongoOperation {
            mongoClient.replaceOne(Filters.eq(ID, updatedTask.id.toHexString()), updatedTask.toTaskDTO())
            updatedTask
        }

    override suspend fun deleteTask(taskId: String) {
        executeMongoOperation {
            mongoClient.deleteOne(Filters.eq(ID, taskId))
        }
    }

    override suspend fun getAllTasks(): List<Task> =
        executeMongoOperation {
            mongoClient.find().toList().map { it.toTask() }
        }

    override suspend fun getTaskById(taskId: String): Task? =
        executeMongoOperation {
            mongoClient.find(Filters.eq(ID, taskId)).firstOrNull()?.toTask()
        }

    override suspend fun deleteTasksByStateId(
        stateId: String,
        taskId: String,
    ) {
        executeMongoOperation {
            mongoClient.deleteMany(Filters.and(Filters.eq(STATE_ID_FIELD, stateId), (Filters.eq(ID, taskId))))
        }
    }
}
