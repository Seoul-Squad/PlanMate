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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MongoTaskDataSource(
    private val mongoClient: MongoCollection<TaskDTO>,
) : RemoteTaskDataSource {
    override fun createTask(task: Task): Single<Task> =
        executeMongoOperationRx {
            Single
                .fromPublisher(mongoClient.insertOne(task.toTaskDTO()))
                .map {
                    task
                }
        }

    @OptIn(ExperimentalUuidApi::class)
    override fun updateTask(updatedTask: Task): Single<Task> =
        executeMongoOperationRx {
            Single
                .fromPublisher(
                    mongoClient.replaceOne(
                        Filters.eq(ID, updatedTask.id.toHexString()),
                        updatedTask.toTaskDTO(),
                    ),
                ).map {
                    updatedTask
                }
        }

    override fun deleteTask(taskId: Uuid): Completable =
        executeMongoOperationRx {
            Single
                .fromPublisher(mongoClient.deleteOne(Filters.eq(ID, taskId.toHexString())))
                .flatMapCompletable {
                    Completable.complete()
                }
        }

    override fun getAllTasks(): Single<List<Task>> {
        val publisher = mongoClient.find()
        return executeMongoOperationRx {
            Flowable
                .fromPublisher(publisher)
                .map { taskDto ->
                    taskDto.toTask()
                }.toList()
        }
    }

    override fun getTaskById(taskId: Uuid): Single<Task> =
        executeMongoOperationRx {
            Single
                .fromPublisher(mongoClient.find(Filters.eq(ID, taskId.toHexString())))
                .map {
                    it.toTask()
                }.onErrorResumeNext { error ->
                    if (error is NoSuchElementException) {
                        Single.error(TaskNotFoundException())
                    } else {
                        Single.error(error)
                    }
                }
        }

    override fun getTasksByProjectState(stateId: Uuid): Single<List<Task>> {
        val publisher = mongoClient.find(Filters.eq(STATE_ID_FIELD, stateId.toHexString()))
        return executeMongoOperationRx {
            Flowable
                .fromPublisher(publisher)
                .map { taskDto ->
                    taskDto.toTask()
                }.toList()
        }
    }
}
