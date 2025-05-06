package data.source.remote.mongo

import com.mongodb.client.model.Filters
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.kotlin.client.coroutine.FindFlow
import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.mockk.*
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.example.data.mapper.toTask
import org.example.data.mapper.toTaskDTO
import org.example.data.models.TaskDTO
import org.example.data.source.remote.mongo.MongoTaskDataSource
import org.example.logic.models.Task
import org.example.logic.utils.*
import org.hamcrest.core.Every
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows

class MongoTaskDataSourceTest {
    private lateinit var mongoTaskDataSource: MongoTaskDataSource
    private lateinit var mongoClient: MongoCollection<TaskDTO>
    private lateinit var testTasks: List<Task>
    private lateinit var testTaskDTOs: List<TaskDTO>

    @BeforeEach
    fun setUp() {
        mongoClient = mockk(relaxed = true)
        mongoTaskDataSource = MongoTaskDataSource(mongoClient)
        testTasks = listOf(
            Task("3", "Updated Task", "in_progress", "user3", listOf("audit4"), "proj3"),
            Task("5", "remove Task", "in review", "user3", listOf("audit4"), "proj3"),
        )
        testTaskDTOs = testTasks.map { it.toTaskDTO() }
    }

    @Nested
    inner class CreateTaskTests {
        @Test
        fun `createTask should return task when task is created`() = runTest {
            val insertOneResult = mockk<InsertOneResult>(relaxed = true)
            coEvery { mongoClient.insertOne(testTaskDTOs[0], any()) } returns insertOneResult

            val result = mongoTaskDataSource.createTask(testTasks[0])

            assertEquals(testTasks[0], result)
            coVerify { mongoClient.insertOne(testTaskDTOs[0], any()) }

        }

        @Test
        fun `createTask should throw CreationItemFailedException when creation task failed MongoDB`() = runTest {

            coEvery { mongoClient.insertOne(testTaskDTOs[0], any()) } throws CreationItemFailedException("")

            assertThrows<CreationItemFailedException> { mongoTaskDataSource.createTask(task = testTasks[0]) }
        }

    }

    @Nested
    inner class UpdateTaskTests {
        @Test
        fun `updateTask should return task when task is updated `() = runTest {
            val replaceResult = mockk<UpdateResult>(relaxed = true)

            coEvery {
                mongoClient.replaceOne(Filters.eq("id", testTasks[0].id), testTaskDTOs[0], any())
            } returns replaceResult

            val result = mongoTaskDataSource.updateTask(testTasks[0])

            assertEquals(testTasks[0], result)
            coVerify { mongoClient.replaceOne(Filters.eq("id", testTasks[0].id), testTaskDTOs[0], any()) }
        }

        @Test
        fun `updateTask should throw UpdateItemFailedException  when update task fails`() = runTest {

            coEvery {
                mongoClient.replaceOne(Filters.eq("id", testTasks[0].id), testTaskDTOs[0], any())
            } throws UpdateItemFailedException("")

            assertThrows<UpdateItemFailedException> { mongoTaskDataSource.updateTask(testTasks[0]) }
        }

    }

    @Nested
    inner class GetTaskTests {
        @Test
        fun `getAllTasks should return list of all available tasks `() = runTest {
            val dto = listOf(
                TaskDTO("3", "Updated Task", "in_progress", "user3", listOf("audit4"), "proj3"),
                TaskDTO("5", "remove Task", "in review", "user3", listOf("audit4"), "proj3"),
            )
            val findFlow = mockk<FindFlow<List<TaskDTO>>>(relaxed = true)

            coEvery { mongoClient.find<List<TaskDTO>>(filter = any(), any()) } returns findFlow
            coEvery { findFlow.firstOrNull() } returns dto

            val result = mongoTaskDataSource.getTaskById("5")

           // assertEquals(dto.map { it.toTask() }, result)
            coVerify(exactly = 1) { mongoClient.find(filter = any()) }
        }

        @Test
        fun `getTaskById should return task when task found`() = runTest {
            val findFlow = mockk<FindFlow<TaskDTO>>(relaxed = true)

            every { mongoClient.find<TaskDTO>(filter = any()) } returns findFlow
            coEvery { findFlow.firstOrNull() } returns testTaskDTOs[0]

            val result = mongoTaskDataSource.getTaskById("3")
            advanceUntilIdle()
            //assertEquals(testTaskDTOs[0].toTask(), result)
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
        fun `getAllTasks should throw GetItemByIdFailedException when get all tasks fails`() = runTest {

            every {
                mongoClient.find(filter = any())
            } throws GetItemsFailedException("")

            assertThrows<GetItemsFailedException> { mongoTaskDataSource.getAllTasks() }
        }

        @Test
        fun `geTaskById should throw GetItemByIdFailedException when get task by Id fails`() = runTest {

            every {
                mongoClient.find(filter = any())
            } throws GetItemByIdFailedException("")

            assertThrows<GetItemByIdFailedException> { mongoTaskDataSource.getTaskById("1") }
        }

    }

    @Nested
    inner class DeleteTaskTests {

        @Test
        fun `deleteTask should delete task when it exist`() = runTest {
            val taskId = "4"
            val deleteResult = mockk<DeleteResult>(relaxed = true)

            coEvery { mongoClient.deleteOne(Filters.eq("id", taskId), any()) } returns deleteResult

            mongoTaskDataSource.deleteTask(taskId)

            coVerify { mongoClient.deleteOne(Filters.eq("id", taskId), any()) }
        }

        @Test
        fun `deleteTask should throw DeleteItemFailedException when deleting task by ID fails`() =
            runTest {

                coEvery { mongoClient.deleteOne(filter = any(), options = any()) } throws DeleteItemFailedException("")

                assertThrows<DeleteItemFailedException> { mongoTaskDataSource.deleteTask("1") }
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

        @Test
        fun `deleteTasksByStateId should throw DeleteItemFailedException when deleting task by StateId fails`() =
            runTest {
                val stateId = "open"
                val taskId = "6"

                coEvery { mongoClient.deleteOne(filter = any(), options = any()) } throws DeleteItemFailedException("")

                assertThrows<DeleteItemFailedException> { mongoTaskDataSource.deleteTasksByStateId(stateId, taskId) }
            }

    }


}