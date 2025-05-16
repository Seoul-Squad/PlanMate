package org.example.data.source.remote.mongo

import com.mongodb.client.model.Filters
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.FindPublisher
import com.mongodb.reactivestreams.client.MongoCollection
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Flowable
import org.example.data.source.remote.models.TaskDTO
import org.example.data.source.remote.mongo.utils.mapper.toTaskDTO
import org.example.logic.models.Task
import org.example.logic.utils.TaskCreationFailedException
import org.example.logic.utils.TaskDeletionFailedException
import org.example.logic.utils.TaskNotChangedException
import org.example.logic.utils.TaskNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MongoTaskDataSourceTest {
    private lateinit var mongoCollection: MongoCollection<TaskDTO>
    private lateinit var dataSource: MongoTaskDataSource
    private lateinit var testTasks: List<Task>
    private lateinit var testTaskDTOs: List<TaskDTO>
    private val ids = List(3) { Uuid.random() }

    @BeforeEach
    fun setup() {
        mongoCollection = mockk()
        dataSource = MongoTaskDataSource(mongoCollection)

        testTasks =
            listOf(
                Task(ids[0], "Task 1", ids[1], "To Do", ids[2], "User A", ids[2]),
                Task(ids[1], "Task 2", ids[1], "In Progress", ids[2], "User B", ids[2]),
            )
        testTaskDTOs = testTasks.map { it.toTaskDTO() }
    }

    @Test
    fun `createTask should return task when insert succeeds`() {
        every { mongoCollection.insertOne(testTaskDTOs[0]) } returns Flowable.just(mockk<InsertOneResult>())

        val result = dataSource.createTask(testTasks[0]).blockingGet()

        assertEquals(testTasks[0], result)
    }

    @Test
    fun `createTask should throw error when insert fails`() {
        every { mongoCollection.insertOne(any()) } returns Flowable.error(TaskCreationFailedException())

        assertThrows<TaskCreationFailedException> {
            dataSource.createTask(testTasks[0]).blockingGet()
        }
    }

    @Test
    fun `updateTask should return task when replace succeeds`() {
        every {
            mongoCollection.replaceOne(Filters.eq("id", testTasks[0].id.toHexString()), testTaskDTOs[0])
        } returns Flowable.just(mockk<UpdateResult>())

        val result = dataSource.updateTask(testTasks[0]).blockingGet()

        assertEquals(testTasks[0], result)
    }

    @Test
    fun `updateTask should throw error when replace fails`() {
        every { mongoCollection.replaceOne(any(), any()) } returns Flowable.error(TaskNotChangedException())

        assertThrows<TaskNotChangedException> {
            dataSource.updateTask(testTasks[0]).blockingGet()
        }
    }

    @Test
    fun `deleteTask should complete when delete succeeds`() {
        every { mongoCollection.deleteOne(any()) } returns Flowable.just(mockk<DeleteResult>())

        dataSource.deleteTask(ids[0]).test().assertComplete()
    }

    @Test
    fun `deleteTask should error when delete fails`() {
        every { mongoCollection.deleteOne(any()) } returns Flowable.error(TaskDeletionFailedException())

        dataSource.deleteTask(ids[0]).test().assertError(TaskDeletionFailedException::class.java)
    }

    @Test
    fun `getAllTasks should return list of tasks`() {
        val publisher = mockk<FindPublisher<TaskDTO>>()
        every { mongoCollection.find() } returns publisher
        every { publisher.subscribe(any()) } answers {
            val subscriber = it.invocation.args[0] as Subscriber<in TaskDTO>
            subscriber.onSubscribe(NoopSubscription)
            subscriber.onNext(testTaskDTOs[0])
            subscriber.onNext(testTaskDTOs[1])
            subscriber.onComplete()
        }

        val result = dataSource.getAllTasks().blockingGet()
        assertEquals(2, result.size)
    }

    @Test
    fun `getAllTasks should throw error when find fails`() {
        val publisher = mockk<FindPublisher<TaskDTO>>()
        every { mongoCollection.find() } returns publisher
        every { publisher.subscribe(any()) } answers {
            val subscriber = it.invocation.args[0] as Subscriber<in TaskDTO>
            subscriber.onSubscribe(NoopSubscription)
            subscriber.onError(TaskNotFoundException())
        }

        assertThrows<TaskNotFoundException> {
            dataSource.getAllTasks().blockingGet()
        }
    }

    @Test
    fun `getTaskById should return task when found`() {
        val publisher = mockk<FindPublisher<TaskDTO>>()
        every { mongoCollection.find(Filters.eq("id", ids[0].toHexString())) } returns publisher
        every { publisher.subscribe(any()) } answers {
            val subscriber = it.invocation.args[0] as Subscriber<in TaskDTO>
            subscriber.onSubscribe(NoopSubscription)
            subscriber.onNext(testTaskDTOs[0])
            subscriber.onComplete()
        }

        val result = dataSource.getTaskById(ids[0]).blockingGet()
        assertEquals(testTasks[0], result)
    }

    @Test
    fun `getTaskById should throw TaskNotFoundException when no element found`() {
        val publisher = mockk<FindPublisher<TaskDTO>>()
        every { mongoCollection.find(Filters.eq("id", ids[0].toHexString())) } returns publisher
        every { publisher.subscribe(any()) } answers {
            val subscriber = it.invocation.args[0] as Subscriber<in TaskDTO>
            subscriber.onSubscribe(NoopSubscription)
            subscriber.onComplete()
        }

        assertThrows<TaskNotFoundException> {
            dataSource.getTaskById(ids[0]).blockingGet()
        }
    }

    @Test
    fun `getTasksByProjectState should return list of tasks`() {
        val publisher = mockk<FindPublisher<TaskDTO>>()
        every { mongoCollection.find(Filters.eq("stateId", ids[2].toHexString())) } returns publisher
        every { publisher.subscribe(any()) } answers {
            val subscriber = it.invocation.args[0] as Subscriber<in TaskDTO>
            subscriber.onSubscribe(NoopSubscription)
            subscriber.onNext(testTaskDTOs[0])
            subscriber.onNext(testTaskDTOs[1])
            subscriber.onComplete()
        }

        val result = dataSource.getTasksByProjectState(ids[2]).blockingGet()
        assertEquals(2, result.size)
    }

    private object NoopSubscription : Subscription {
        override fun request(n: Long) {}

        override fun cancel() {}
    }
}
