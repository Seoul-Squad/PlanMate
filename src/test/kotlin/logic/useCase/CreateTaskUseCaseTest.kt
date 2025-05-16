package logic.useCase

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.test.runTest
import mockdata.createTask
import mockdata.createUser
import org.example.logic.repositries.ProjectStateRepository
import org.example.logic.repositries.TaskRepository
import org.example.logic.useCase.CreateAuditLogUseCase
import org.example.logic.useCase.GetCurrentUserUseCase
import org.example.logic.useCase.Validation
import org.example.logic.utils.BlankInputException
import org.example.logic.utils.ProjectStateNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CreateTaskUseCaseTest {
    private lateinit var taskRepository: TaskRepository
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var createAuditLogUseCase: CreateAuditLogUseCase
    private lateinit var projectStateRepository: ProjectStateRepository
    private lateinit var validation: Validation
    private lateinit var createTaskUseCase: CreateTaskUseCase

    @BeforeEach
    fun setUp() {
        taskRepository = mockk(relaxed = true)
        getCurrentUserUseCase = mockk(relaxed = true)
        createAuditLogUseCase = mockk(relaxed = true)
        projectStateRepository = mockk(relaxed = true)
        validation = mockk(relaxed = true)
        createTaskUseCase =
            CreateTaskUseCase(
                taskRepository,
                getCurrentUserUseCase,
                createAuditLogUseCase,
                projectStateRepository,
                validation,
            )
    }

    @Test
    fun `should return created task when there is no blank input parameters and project and state exist`() {
        val taskName = "Write CreateTaskUseCase test cases"
        val projectId = Uuid.random()
        val stateId = Uuid.random()
        every { getCurrentUserUseCase() } returns Single.just(createUser())
        every { taskRepository.createTask(any()) } returns
            Single.just(
                createTask(
                    name = taskName,
                    projectId = projectId,
                    stateId = stateId,
                ),
            )

        val result = createTaskUseCase(name = taskName, projectId = projectId, stateId = stateId).blockingGet()

        verify { getCurrentUserUseCase() }
        verify { taskRepository.createTask(any()) }
        assertThat(result.name).isEqualTo(taskName)
        assertThat(result.projectId).isEqualTo(projectId)
        assertThat(result.stateId).isEqualTo(stateId)
    }

    @ParameterizedTest
    @MethodSource("provideBlankInputScenarios")
    fun `should throw BlankInputException when any of the inputs is blank`(
        taskName: String,
        projectId: Uuid,
        stateId: Uuid,
    ) = runTest {
        every { validation.validateInputNotBlankOrThrow(any()) } throws BlankInputException()

        assertThrows<BlankInputException> {
            createTaskUseCase(name = taskName, projectId = projectId, stateId = stateId).blockingGet()
        }
    }

    @Test
    fun `should throw TaskStateNotFoundException when state doesn't exist`() {
        val taskName = "Test"
        val projectId = Uuid.random()
        val stateId = Uuid.random()
        every { projectStateRepository.getProjectStateById(any()) } returns
            Single.error(
                ProjectStateNotFoundException(),
            )

        assertThrows<ProjectStateNotFoundException> {
            createTaskUseCase(name = taskName, projectId = projectId, stateId = stateId).blockingGet()
        }
    }

    companion object {
        @JvmStatic
        fun provideBlankInputScenarios(): Stream<Arguments> =
            Stream.of(
                Arguments.argumentSet(
                    "blank task name",
                    "",
                    Uuid.random(),
                    Uuid.random(),
                ),
            )
    }
}
