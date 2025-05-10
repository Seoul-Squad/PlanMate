package org.example.data.source.remote.mongo

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.example.data.source.remote.contract.RemoteTaskStateDataSource
import org.example.data.source.remote.models.StateDTO
import org.example.data.source.remote.mongo.utils.mapper.toState
import org.example.data.source.remote.mongo.utils.mapper.toStateDTO
import org.example.data.utils.Constants.ID
import org.example.logic.models.State
import org.example.logic.utils.TaskCreationFailedException
import org.example.logic.utils.TaskDeletionFailedException
import org.example.logic.utils.TaskNotChangedException
import org.example.logic.utils.TaskNotFoundException
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class MongoTaskStateDataSource(
    private val mongoClient: MongoCollection<StateDTO>,
) : RemoteTaskStateDataSource {
    override suspend fun createTaskState(taskState: State): State {
        try {
            mongoClient.insertOne(taskState.toStateDTO())
            return taskState
        } catch (e: Exception) {
            throw TaskCreationFailedException()
        }
    }

    override suspend fun updateTaskState(updatedTaskState: State): State {
        try {
            mongoClient.replaceOne(Filters.eq(ID, updatedTaskState.id), updatedTaskState.toStateDTO())
            return updatedTaskState
        } catch (e: Exception) {
            throw TaskNotChangedException()
        }
    }

    override suspend fun deleteTaskState(taskStateId: String) {
        try {
            mongoClient.deleteOne(Filters.eq(ID, taskStateId))
        } catch (e: Exception) {
            throw TaskDeletionFailedException()
        }
    }

    override suspend fun getTaskStateById(taskStateId: String): State? {
        try {
            return mongoClient.find(Filters.eq(ID, taskStateId)).firstOrNull()?.toState()
        } catch (e: Exception) {
            throw TaskNotFoundException()
        }
    }

    override suspend fun getProjectTaskStates(taskStateIds: List<String>): List<State> =
        try {
            mongoClient
                .find(Filters.`in`(ID, taskStateIds))
                .toList()
                .map { it.toState() }
        } catch (e: Exception) {
            throw TaskNotFoundException()
        }
}
