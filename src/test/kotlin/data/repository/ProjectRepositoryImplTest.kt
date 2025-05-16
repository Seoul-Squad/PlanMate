package data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.example.data.repository.ProjectRepositoryImpl
import org.example.data.repository.sources.remote.RemoteProjectDataSource
import org.example.data.source.remote.RoleValidationInterceptor
import org.example.logic.models.Project
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ProjectRepositoryImplTest {
    private lateinit var mockRemoteDataSource: RemoteProjectDataSource
    private lateinit var repository: ProjectRepositoryImpl
    private lateinit var testProjects: List<Project>
    private lateinit var roleValidationInterceptor: RoleValidationInterceptor
    private val id1 = Uuid.random()
    private val id2 = Uuid.random()
    private val id3 = Uuid.random()

    @BeforeEach
    fun setUp() {
        mockRemoteDataSource = mockk(relaxed = true)
        roleValidationInterceptor = mockk(relaxed = true)

        testProjects =
            listOf(
                Project(id = id1, name = "Project 1"),
                Project(id = id2, name = "Project 2"),
            )

        every { mockRemoteDataSource.getAllProjects() } returns Single.just(testProjects)
        repository = ProjectRepositoryImpl(mockRemoteDataSource, roleValidationInterceptor)
    }

    @Test
    fun `createProject should delegate to RoleValidationInterceptor and return result`() {
        val newProject = Project(id = id3, name = "Project 3")
        every { roleValidationInterceptor.validateRole<Project>(any(), any()) } returns newProject

        val result = repository.createProject(newProject).blockingGet()

        verify(exactly = 1) { roleValidationInterceptor.validateRole<Project>(any(), any()) }
        assertThat(result).isEqualTo(newProject)
    }

    @Test
    fun `updateProject should delegate to RoleValidationInterceptor and return result`() {
        val updatedProject = Project(id = id1, name = "Updated Project 1")
        every { roleValidationInterceptor.validateRole<Project>(any(), any()) } returns updatedProject

        val result = repository.updateProject(updatedProject).blockingGet()

        verify(exactly = 1) { roleValidationInterceptor.validateRole<Project>(any(), any()) }
        assertThat(result).isEqualTo(updatedProject)
    }

    @Test
    fun `getAllProjects should return result from remote data source`() {
        val result = repository.getAllProjects().blockingGet()

        verify(exactly = 1) { mockRemoteDataSource.getAllProjects() }
        assertThat(result).isEqualTo(testProjects)
        assertThat(result).hasSize(2)
    }

    @Test
    fun `deleteProject should delegate to RoleValidationInterceptor`() {
        val projectIdToDelete = id1
        every { mockRemoteDataSource.deleteProject(any()) } returns Completable.complete()
        every { roleValidationInterceptor.validateRole<Project>(any(), any()) } returns
            Project(id = id1, name = "Project 1")

        repository.deleteProject(projectIdToDelete).blockingAwait()

        verify(exactly = 1) { roleValidationInterceptor.validateRole<Project>(any(), any()) }
        verify(exactly = 1) { mockRemoteDataSource.deleteProject(projectIdToDelete) }
    }

    @Test
    fun `getProjectById should return project when found`() {
        val projectId = id1
        val expectedProject = Project(id = id1, name = "Project 1")
        every { mockRemoteDataSource.getProjectById(projectId) } returns Single.just(expectedProject)

        val result = repository.getProjectById(projectId).blockingGet()

        verify(exactly = 1) { mockRemoteDataSource.getProjectById(projectId) }
        assertThat(result).isNotNull()
        assertThat(result.id).isEqualTo(id1)
        assertThat(result.name).isEqualTo("Project 1")
    }
}
