package logic.useCase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.observers.TestObserver
import org.example.logic.models.ProjectState
import org.example.logic.repositries.ProjectStateRepository
import org.example.logic.useCase.GetProjectStatesUseCase
import org.example.logic.utils.ProjectNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetProjectStatesUseCaseTest {
    private lateinit var projectStateRepository: ProjectStateRepository
    private lateinit var getProjectStatesUseCase: GetProjectStatesUseCase

    private val projectId = Uuid.random()
    private val states =
        listOf(
            ProjectState(
                id = Uuid.random(),
                title = "To Do",
                projectId = projectId,
            ),
            ProjectState(
                id = Uuid.random(),
                title = "In Progress",
                projectId = projectId,
            ),
            ProjectState(
                id = Uuid.random(),
                title = "Done",
                projectId = projectId,
            ),
        )

    @BeforeEach
    fun setUp() {
        projectStateRepository = mockk(relaxed = true)
        getProjectStatesUseCase = GetProjectStatesUseCase(projectStateRepository)
    }

    @Test
    fun `should return list of project states when project exists`() {
        every { projectStateRepository.getProjectStates(projectId) } returns Single.just(states)

        val testObserver: TestObserver<List<ProjectState>> = getProjectStatesUseCase(projectId).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue(states)

        verify(exactly = 1) { projectStateRepository.getProjectStates(projectId) }
    }

    @Test
    fun `should return empty list when project has no states`() {
        every { projectStateRepository.getProjectStates(projectId) } returns Single.just(emptyList())

        val testObserver: TestObserver<List<ProjectState>> = getProjectStatesUseCase(projectId).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue(emptyList<ProjectState>())

        verify(exactly = 1) { projectStateRepository.getProjectStates(projectId) }
    }

    @Test
    fun `should propagate repository exceptions`() {
        val exception = ProjectNotFoundException()
        every { projectStateRepository.getProjectStates(projectId) } returns Single.error(exception)

        val testObserver: TestObserver<List<ProjectState>> = getProjectStatesUseCase(projectId).test()

        testObserver.assertError(ProjectNotFoundException::class.java)
        testObserver.assertNotComplete()

        verify(exactly = 1) { projectStateRepository.getProjectStates(projectId) }
    }

    @Test
    fun `should handle repository runtime exceptions`() {
        val errorMessage = "Database connection failed"
        val runtimeException = RuntimeException(errorMessage)
        every { projectStateRepository.getProjectStates(projectId) } returns Single.error(runtimeException)

        val testObserver: TestObserver<List<ProjectState>> = getProjectStatesUseCase(projectId).test()

        testObserver.assertError(RuntimeException::class.java)
        testObserver.assertNotComplete()

        verify(exactly = 1) { projectStateRepository.getProjectStates(projectId) }
    }
}
