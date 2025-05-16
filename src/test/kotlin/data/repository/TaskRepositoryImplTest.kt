package data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import mockdata.createTask
import org.example.data.repository.TaskRepositoryImpl
import org.example.data.repository.sources.remote.RemoteTaskDataSource
import org.example.data.source.remote.RoleValidationInterceptor
import org.example.logic.utils.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class TaskRepositoryImplTest {
    private lateinit var mockRemoteDataSource: RemoteTaskDataSource
    private lateinit var taskRepositoryImpl: TaskRepositoryImpl
    private val id1 = Uuid.random()
    private val id2 = Uuid.random()
    private val id3 = Uuid.random()

    private val testTask =
        createTask(
            id = id1,
            name = "Test Task",
        )

    private val updatedTask =
        testTask.copy(
            name = "Updated Task",
            stateId = id2,
        )

    private val taskList =
        listOf(
            testTask,
            createTask(id = id2, name = "Task 2"),
        )
    private lateinit var roleValidationInterceptor: RoleValidationInterceptor

    @BeforeEach
    fun setUp() {
        mockRemoteDataSource = mockk(relaxed = true)
        taskRepositoryImpl = TaskRepositoryImpl(mockRemoteDataSource)
    }

    @Test
    fun `should return created task when createTask succeeds`() {
        every { mockRemoteDataSource.createTask(testTask) } returns Single.just(testTask)

        val result = taskRepositoryImpl.createTask(testTask).blockingGet()

        verify(exactly = 1) { mockRemoteDataSource.createTask(testTask) }
        assertThat(result).isEqualTo(testTask)
    }

    @Test
    fun `should throw TaskCreationFailedException when createTask fails`() {
        every { mockRemoteDataSource.createTask(testTask) } returns Single.error(RuntimeException("error"))

        taskRepositoryImpl.createTask(testTask).test().assertError(TaskCreationFailedException::class.java)
    }

    @Test
    fun `should return updated task when updateTask succeeds`() {
        every { mockRemoteDataSource.updateTask(updatedTask) } returns Single.just(updatedTask)

        val result = taskRepositoryImpl.updateTask(updatedTask).blockingGet()

        verify(exactly = 1) { mockRemoteDataSource.updateTask(updatedTask) }
        assertThat(result).isEqualTo(updatedTask)
    }

    @Test
    fun `should throw TaskNotChangedException when updateTask fails`() {
        every { mockRemoteDataSource.updateTask(updatedTask) } returns Single.error(RuntimeException("error"))

        taskRepositoryImpl.updateTask(updatedTask).test().assertError(TaskNotChangedException::class.java)
    }

    @Test
    fun `should call remote data source once when deleteTask is called`() {
        every { mockRemoteDataSource.deleteTask(testTask.id) } returns Completable.complete()

        taskRepositoryImpl.deleteTask(testTask.id).blockingAwait()

        verify(exactly = 1) { mockRemoteDataSource.deleteTask(testTask.id) }
    }

    @Test
    fun `should throw TaskDeletionFailedException when deleteTask fails`() {
        every { mockRemoteDataSource.deleteTask(testTask.id) } returns Completable.error(RuntimeException("error"))

        taskRepositoryImpl.deleteTask(testTask.id).test().assertError(TaskDeletionFailedException::class.java)
    }

    @Test
    fun `should return all tasks when getAllTasks succeeds`() {
        every { mockRemoteDataSource.getAllTasks() } returns Single.just(taskList)

        val result = taskRepositoryImpl.getAllTasks().blockingGet()

        verify(exactly = 1) { mockRemoteDataSource.getAllTasks() }
        assertThat(result).isEqualTo(taskList)
        assertThat(result).hasSize(2)
    }

    @Test
    fun `should throw NoTasksFoundException when getAllTasks fails`() {
        every { mockRemoteDataSource.getAllTasks() } returns Single.error(RuntimeException("error"))

        taskRepositoryImpl.getAllTasks().test().assertError(NoTasksFoundException::class.java)
    }

    @Test
    fun `should return task when getTaskById finds task`() {
        every { mockRemoteDataSource.getTaskById(testTask.id) } returns Single.just(testTask)

        val result = taskRepositoryImpl.getTaskById(testTask.id).blockingGet()

        verify(exactly = 1) { mockRemoteDataSource.getTaskById(testTask.id) }
        assertThat(result).isEqualTo(testTask)
    }

    @Test
    fun `should throw NoTaskFoundException when getTaskById fails`() {
        every { mockRemoteDataSource.getTaskById(testTask.id) } returns Single.error(RuntimeException("error"))

        taskRepositoryImpl.getTaskById(testTask.id).test().assertError(NoTaskFoundException::class.java)
    }

    @Test
    fun `should return tasks by project state when getTasksByProjectState succeeds`() {
        every { mockRemoteDataSource.getTasksByProjectState(id1) } returns Single.just(taskList)

        val result = taskRepositoryImpl.getTasksByProjectState(id1).blockingGet()

        verify(exactly = 1) { mockRemoteDataSource.getTasksByProjectState(id1) }
        assertThat(result).isEqualTo(taskList)
    }

    @Test
    fun `should throw NoTaskFoundException when getTasksByProjectState fails`() {
        every { mockRemoteDataSource.getTasksByProjectState(id1) } returns Single.error(RuntimeException("error"))

        taskRepositoryImpl.getTasksByProjectState(id1).test().assertError(NoTaskFoundException::class.java)
    }
}
