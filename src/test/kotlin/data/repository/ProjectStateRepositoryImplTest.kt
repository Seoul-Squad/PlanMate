package data.repository

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.example.data.repository.ProjectStateRepositoryImpl
import org.example.data.source.remote.contract.RemoteProjectStateDataSource
import org.example.logic.models.ProjectState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProjectStateRepositoryImplTest {
    private lateinit var repository: ProjectStateRepositoryImpl
    private lateinit var remoteDataSource: RemoteProjectStateDataSource
    private val projectId1 = Uuid.random()
    private val projectState =
        ProjectState(
            id = Uuid.random(),
            title = "ToDO",
            projectId = projectId1,
        )

    @BeforeEach
    fun setup() {
        remoteDataSource = mockk(relaxed = true)
        repository = ProjectStateRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `createProjectState should return created project state when a new project state is created`() {
        every { remoteDataSource.createProjectState(projectState) } returns Single.just(projectState)

        val result = repository.createProjectState(projectState).blockingGet()

        assertEquals(projectState, result)
        verify(exactly = 1) { remoteDataSource.createProjectState(projectState) }
    }

    @Test
    fun `createProjectState should throw exception when remote source fails`() {
        val exception = RuntimeException("Network error")
        every { remoteDataSource.createProjectState(projectState) } throws exception

        assertThrows<RuntimeException> {
            repository.createProjectState(projectState).blockingGet()
        }
    }

    @Test
    fun `updateProjectState should return updated project state`() {
        every { remoteDataSource.updateProjectState(projectState) } returns Single.just(projectState)

        val result = repository.updateProjectState(projectState).blockingGet()

        assertEquals(projectState, result)
        verify(exactly = 1) { remoteDataSource.updateProjectState(projectState) }
    }

    @Test
    fun `updateProjectState should throw exception when remote source fails`() {
        val exception = RuntimeException("Network error")
        every { remoteDataSource.updateProjectState(projectState) } throws exception

        assertThrows<RuntimeException> {
            repository.updateProjectState(projectState).blockingGet()
        }
    }

    @Test
    fun `deleteProjectState should call remote source`() {
        val projectStateId = Uuid.random()
        every { remoteDataSource.deleteProjectState(projectStateId) } returns Completable.complete()

        repository.deleteProjectState(projectStateId).blockingAwait()

        verify(exactly = 1) { remoteDataSource.deleteProjectState(projectStateId) }
    }

    @Test
    fun `deleteProjectState should throw exception when remote source fails`() {
        val projectStateId = Uuid.random()
        val exception = RuntimeException("Network error")
        every { remoteDataSource.deleteProjectState(projectStateId) } throws exception

        assertThrows<RuntimeException> {
            repository.deleteProjectState(projectStateId).blockingAwait()
        }
    }

    @Test
    fun `getProjectStates should return list of project states`() {
        val projectId = Uuid.random()
        val projectStates = listOf(projectState, projectState.copy(title = "RUN"))
        every { remoteDataSource.getProjectStates(projectId) } returns Single.just(projectStates)

        val result = repository.getProjectStates(projectId).blockingGet()

        assertEquals(projectStates, result)
        verify(exactly = 1) { remoteDataSource.getProjectStates(projectId) }
    }

    @Test
    fun `getProjectStates should return empty list when no states exist`() {
        val projectId = Uuid.random()
        every { remoteDataSource.getProjectStates(projectId) } returns Single.just(emptyList())

        val result = repository.getProjectStates(projectId).blockingGet()

        assertEquals(emptyList(), result)
    }

    @Test
    fun `getProjectStateById should return project state when exists`() {
        val projectStateId = Uuid.random()
        every { remoteDataSource.getProjectStateById(projectStateId) } returns Single.just(projectState)

        val result = repository.getProjectStateById(projectStateId).blockingGet()

        assertEquals(projectState, result)
        verify(exactly = 1) { remoteDataSource.getProjectStateById(projectStateId) }
    }
}
