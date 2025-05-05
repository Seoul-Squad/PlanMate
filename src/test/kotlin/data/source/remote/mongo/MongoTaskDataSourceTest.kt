package data.source.remote.mongo

import com.mongodb.client.model.Filters
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.example.data.mapper.toTask
import org.example.data.mapper.toTaskDTO
import org.example.data.models.TaskDTO
import org.example.data.source.remote.mongo.MongoTaskDataSource
import org.example.logic.models.Task
import org.example.logic.utils.TaskNotChangedException
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows

class MongoTaskDataSourceTest {
    private lateinit var mongoTaskDataSource: MongoTaskDataSource
    private lateinit var mongoClient: MongoCollection<TaskDTO>

    @BeforeEach
    fun setUp() {
        mongoClient = mockk(relaxed = true)
        mongoTaskDataSource = MongoTaskDataSource(mongoClient)
    }

    @Test
    fun `createTask should return task when task is created`() = runTest {
        val newTask = Task("2", "New Task", "todo", "user2", listOf("audit3"), "proj2")
        val newTaskDto = newTask.toTaskDTO()
        val insertOneResult = mockk<InsertOneResult>(relaxed = true)
        coEvery { mongoClient.insertOne(newTaskDto, any()) } returns insertOneResult

        val result = mongoTaskDataSource.createTask(newTask)

        assertEquals(newTask, result)
        coVerify() { mongoClient.insertOne(newTaskDto, any()) }

    }

    @Test
    fun `updateTask should return task when task is updated `() = runTest {
        val task = Task("3", "Updated Task", "in_progress", "user3", listOf("audit4"), "proj3")
        val taskDto = task.toTaskDTO()
        val replaceResult = mockk<UpdateResult>(relaxed = true)

        coEvery {
            mongoClient.replaceOne(Filters.eq("id", task.id), taskDto, any())
        } returns replaceResult

        val result = mongoTaskDataSource.updateTask(task)

        assertEquals(task, result)
        coVerify { mongoClient.replaceOne(Filters.eq("id", task.id), taskDto, any()) }
    }

    @Test
    fun `deleteTask should delete task when it exist`() = runTest {
        val taskId = "4"
        val deleteResult = mockk<DeleteResult>(relaxed = true)

        coEvery { mongoClient.deleteOne(Filters.eq("id", taskId), any()) } returns deleteResult

        mongoTaskDataSource.deleteTask(taskId)

        coVerify { mongoClient.deleteOne(Filters.eq("id", taskId), any()) }
    }

    @Test
    fun `getAllTasks should return list of all available tasks `() = runTest {
        mongoTaskDataSource.getAllTasks()
        advanceUntilIdle()
        coVerify(exactly = 1) { mongoClient.find(filter = any()) }
    }

    @Test
    fun `getTaskById should return task when task found`() = runTest {

        mongoTaskDataSource.getTaskById("5")
        advanceUntilIdle()
        coVerify(exactly = 1) { mongoClient.find(filter = any()) }
    }

    @Test
    fun `getTaskById should return null when  task not found`() = runTest {
        coEvery { mongoClient.find(Filters.eq("id", "10")).firstOrNull() } returns null

        val result = mongoTaskDataSource.getTaskById("10")

        assertNull(result)
        coVerify(exactly = 1) { mongoClient.find(filter = any()) }
    }

    @Test
    fun `deleteTasksByStateId should delete when task exist`() = runTest {
        val stateId = "open"
        val taskId = "6"

        val deleteResult = mockk<DeleteResult>(relaxed = true)

        coEvery {
            mongoClient.deleteOne(Filters.and(Filters.eq("stateId", stateId), Filters.eq("id", taskId)), any())
        } returns deleteResult

        mongoTaskDataSource.deleteTasksByStateId(stateId, taskId)

        coVerify {
            mongoClient.deleteOne(Filters.and(Filters.eq("stateId", stateId), Filters.eq("id", taskId)), any())
        }
    }

}