package logic.useCase

import com.google.common.truth.Truth.assertThat
import io.mockk.*
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.AuditLog
import org.example.logic.models.ProjectState
import org.example.logic.repositries.ProjectStateRepository
import org.example.logic.useCase.CreateAuditLogUseCase
import org.example.logic.useCase.CreateProjectStateUseCase
import org.example.logic.useCase.GetProjectStatesUseCase
import org.example.logic.useCase.Validation
import org.example.logic.utils.BlankInputException
import org.example.logic.utils.Constants
import org.example.logic.utils.ProjectNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CreateProjectStateUseCaseTest {
    private lateinit var projectStateRepository: ProjectStateRepository
    private lateinit var getProjectStatesUseCase: GetProjectStatesUseCase
    private lateinit var createAuditLogUseCase: CreateAuditLogUseCase
    private lateinit var validation: Validation
    private lateinit var createProjectStateUseCase: CreateProjectStateUseCase

    private val id1 = Uuid.random()

    private val dummyProjectStates =
        listOf(
            ProjectState(
                id = id1,
                title = "StateTest1",
                projectId = id1,
            ),
        )

    private val stateName = "StateTest4"

    @BeforeEach
    fun setUp() {
        projectStateRepository = mockk(relaxed = true)
        getProjectStatesUseCase = mockk()
        createAuditLogUseCase = mockk(relaxed = true)
        validation = mockk(relaxed = true)

        createProjectStateUseCase =
            CreateProjectStateUseCase(
                projectStateRepository,
                getProjectStatesUseCase,
                createAuditLogUseCase,
                validation,
            )
    }

    @Test
    fun `should return the updated project with the added state when given valid project id, state name is not blank and project exists`() {
        // Arrange
        val projectId = Uuid.random()
        val newProjectState = ProjectState(title = stateName, projectId = projectId)

        every { validation.validateInputNotBlankOrThrow(any()) } just Runs
        every { getProjectStatesUseCase(projectId) } returns Single.just(dummyProjectStates)
        every { projectStateRepository.createProjectState(any()) } returns Single.just(newProjectState)
        every { createAuditLogUseCase.logUpdate(any(), any(), any(), any()) } returns Single.just(mockk())

        val result = createProjectStateUseCase(projectId, stateName).blockingGet()

        assertThat(result).isEqualTo(newProjectState)

        verify { validation.validateInputNotBlankOrThrow(stateName) }
        verify { getProjectStatesUseCase(projectId) }
        verify {
            projectStateRepository.createProjectState(
                withArg {
                    assertThat(it.title).isEqualTo(stateName)
                    assertThat(it.projectId).isEqualTo(projectId)
                },
            )
        }
        verify {
            createAuditLogUseCase.logUpdate(
                entityType = AuditLog.EntityType.PROJECT,
                entityId = projectId,
                entityName = "",
                fieldChange =
                    match {
                        it.fieldName == Constants.FIELD_STATES &&
                            it.oldValue == dummyProjectStates.joinToString(", ") { s -> s.title } &&
                            it.newValue ==
                            dummyProjectStates
                                .plus(newProjectState)
                                .joinToString(", ") { s -> s.title }
                    },
            )
        }
    }

    @Test
    fun `should throw BlankInputException when state name is blank`() {
        // Arrange
        val blankStateName = ""
        every { validation.validateInputNotBlankOrThrow(blankStateName) } throws BlankInputException()

        assertThrows<BlankInputException> {
            createProjectStateUseCase(Uuid.random(), blankStateName).blockingGet()
        }

        verify { validation.validateInputNotBlankOrThrow(blankStateName) }
        confirmVerified(projectStateRepository, getProjectStatesUseCase, createAuditLogUseCase)
    }

    @Test
    fun `should throw ProjectNotFoundException when no project found with the given id`() {
        // Arrange
        val projectId = Uuid.random()
        every { validation.validateInputNotBlankOrThrow(stateName) } just Runs
        every { getProjectStatesUseCase(projectId) } returns Single.error(ProjectNotFoundException())

        assertThrows<ProjectNotFoundException> {
            createProjectStateUseCase(projectId, stateName).blockingGet()
        }

        verify { validation.validateInputNotBlankOrThrow(stateName) }
        verify { getProjectStatesUseCase(projectId) }
        confirmVerified(projectStateRepository, createAuditLogUseCase)
    }
}
