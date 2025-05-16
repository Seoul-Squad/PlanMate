package logic.useCase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.test.runTest
import org.example.logic.models.ProjectState
import org.example.logic.models.Task
import org.example.logic.repositries.ProjectStateRepository
import org.example.logic.useCase.*
import org.example.logic.utils.BlankInputException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class UpdateProjectStateUseCaseTest {

    private lateinit var projectStateRepository: ProjectStateRepository
    private lateinit var getProjectStatesUseCase: GetProjectStatesUseCase
    private lateinit var createAuditLogUseCase: CreateAuditLogUseCase
    private lateinit var getTasksByProjectStateUseCase: GetTasksByProjectStateUseCase
    private lateinit var updateTaskUseCase: UpdateTaskUseCase
    private lateinit var validation: Validation

    private lateinit var useCase: UpdateProjectStateUseCase

    private val stateId = Uuid.random()
    private val projectId = Uuid.random()

    @BeforeEach
    fun setUp() {
        projectStateRepository = mockk(relaxed = true)
        getProjectStatesUseCase = mockk()
        createAuditLogUseCase = mockk(relaxed = true)
        getTasksByProjectStateUseCase = mockk()
        updateTaskUseCase = mockk(relaxed = true)
        validation = mockk(relaxed = true)

        useCase = UpdateProjectStateUseCase(
            projectStateRepository,
            getProjectStatesUseCase,
            createAuditLogUseCase,
            getTasksByProjectStateUseCase,
            updateTaskUseCase,
            validation
        )
    }

    @Test
    fun `should throw BlankInputException when state name is blank`() = runTest {
        coEvery { validation.validateInputNotBlankOrThrow("") } throws BlankInputException()

        assertThrows<BlankInputException> {
            useCase("", stateId, projectId)
        }

        coVerify(exactly = 0) { projectStateRepository.updateProjectState(any()) }
    }

    @Test
    fun `should update all tasks state name if state is updated`() = runTest {
        val oldStates = listOf(ProjectState(stateId, "Old State", projectId))
        val tasks = listOf(
            Task(Uuid.random(), "Task 1", projectId, "u1", stateId, "Old State", Uuid.random()),
            Task(Uuid.random(), "Task 2", projectId, "u2", stateId, "Old State", Uuid.random())
        )

        // Return Single.just(...) to match the expected return type
        coEvery { getProjectStatesUseCase(projectId) } returns Single.just(oldStates)
        coEvery { getTasksByProjectStateUseCase(stateId) } returns Single.just(tasks)
        coEvery { createAuditLogUseCase.logUpdate(any(), any(), any(), any()) } returns mockk()

        useCase("Updated State", stateId, projectId)

        coVerify(exactly = 1) {
            projectStateRepository.updateProjectState(
                ProjectState(
                    stateId,
                    "Updated State",
                    projectId
                )
            )
        }
        coVerify(exactly = tasks.size) { updateTaskUseCase(match { it.stateName == "Updated State" }) }
    }

    @Test
    fun `should not update tasks if no tasks exist for state`() = runTest {
        val oldStates = listOf(ProjectState(stateId, "Old State", projectId))

        coEvery { getProjectStatesUseCase(projectId) } returns Single.just(oldStates)
        coEvery { getTasksByProjectStateUseCase(stateId) } returns Single.just(emptyList())
        coEvery { createAuditLogUseCase.logUpdate(any(), any(), any(), any()) } returns mockk()

        useCase("New State", stateId, projectId)

        coVerify(exactly = 1) {
            projectStateRepository.updateProjectState(
                ProjectState(
                    stateId,
                    "New State",
                    projectId
                )
            )
        }
        coVerify(exactly = 0) { updateTaskUseCase(any()) }
    }
}
