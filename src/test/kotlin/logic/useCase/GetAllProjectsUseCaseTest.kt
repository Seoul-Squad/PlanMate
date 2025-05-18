package logic.useCase

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.Project
import org.example.logic.repositries.ProjectRepository
import org.example.logic.useCase.GetAllProjectsUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetAllProjectsUseCaseTest {
    private lateinit var projectRepository: ProjectRepository
    private lateinit var getAllProjectsUseCase: GetAllProjectsUseCase

    private val projects =
        listOf(
            Project(id = Uuid.random(), name = "Spacecraft Work"),
            Project(id = Uuid.random(), name = "Mars Rover Development"),
            Project(id = Uuid.random(), name = "Satellite Deployment"),
            Project(id = Uuid.random(), name = "Empty Project"),
            Project(id = Uuid.random(), name = "Lunar Base Planning"),
        )

    @BeforeEach
    fun setUp() {
        projectRepository = mockk()
        getAllProjectsUseCase = GetAllProjectsUseCase(projectRepository)
    }

    @Test
    fun `should return all projects when found projects at file`() {
        every { projectRepository.getAllProjects() } returns Single.just(projects)

        val testObserver = getAllProjectsUseCase().test()

        testObserver.assertComplete()
        testObserver.assertValue { returnedProjects ->
            returnedProjects == projects
        }
    }
}
