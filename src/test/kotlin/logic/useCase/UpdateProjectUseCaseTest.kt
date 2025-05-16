package org.example.logic.useCase.updateProject

import io.mockk.*
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.AuditLog
import org.example.logic.models.Project
import org.example.logic.repositries.ProjectRepository
import org.example.logic.useCase.CreateAuditLogUseCase
import org.example.logic.useCase.Validation
import org.example.logic.utils.BlankInputException
import org.example.logic.utils.ProjectNotChangedException
import org.example.logic.utils.ProjectNotFoundException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class UpdateProjectUseCaseTest {
    private lateinit var useCase: UpdateProjectUseCase
    private lateinit var repo: ProjectRepository
    private lateinit var auditLogUC: CreateAuditLogUseCase
    private lateinit var validation: Validation

    @BeforeTest
    fun setup() {
        repo = mockk(relaxed = true)
        auditLogUC = mockk(relaxed = true)
        validation = mockk(relaxed = true)

        useCase =
            UpdateProjectUseCase(
                projectRepository = repo,
                createAuditLogUseCase = auditLogUC,
                validation = validation,
            )
    }

    @Test
    fun `when name blank then throws BlankInputException`() {
        val id = Uuid.random()
        val updated = Project(id = id, name = "")
        every { validation.validateInputNotBlankOrThrow("") } throws BlankInputException()

        assertFailsWith<BlankInputException> {
            useCase(updated).blockingGet()
        }
    }

    @Test
    fun `when project not found then throws ProjectNotFoundException`() {
        val id = Uuid.random()
        val updated = Project(id = id, name = "New name")
        every { validation.validateInputNotBlankOrThrow("New name") } just Runs
        every { repo.getProjectById(id) } returns Single.error(ProjectNotFoundException())

        val testObserver = useCase(updated).test()

        testObserver.assertError(ProjectNotFoundException::class.java)
    }

    @Test
    fun `when no change in name then throws ProjectNotChangedException`() {
        val id = Uuid.random()
        val original = Project(id = id, name = "Same")
        val updated = Project(id = id, name = "Same")

        every { validation.validateInputNotBlankOrThrow("Same") } just Runs
        every { repo.getProjectById(id) } returns Single.just(original)

        assertFailsWith<ProjectNotChangedException> {
            useCase(updated).blockingGet()
        }
    }

    @Test
    fun `when name changed then calls update and logs each field change`() {
        // arrange
        val id = Uuid.random()
        val original = Project(id = id, name = "Old")
        val updated = Project(id = id, name = "New")

        val changes =
            listOf(
                AuditLog.FieldChange(fieldName = "name", oldValue = "Old", newValue = "New"),
            )

        every { validation.validateInputNotBlankOrThrow("New") } just Runs
        every { repo.getProjectById(id) } returns Single.just(original)
        every { repo.updateProject(updated) } returns Single.just(updated)

        every {
            auditLogUC.logUpdate(
                entityId = id,
                entityName = "New",
                entityType = AuditLog.EntityType.PROJECT,
                fieldChange = changes[0],
            )
        } returns
            Single.just(
                AuditLog(
                    userId = Uuid.random(),
                    userName = "tester",
                    entityId = id,
                    entityType = AuditLog.EntityType.PROJECT,
                    entityName = "New",
                    actionType = AuditLog.ActionType.UPDATE,
                    fieldChange = changes[0],
                ),
            )

        // act
        val testObserver = useCase(updated).test()

        // assert
        testObserver.assertValue { it.name == "New" }
        testObserver.assertComplete()
        testObserver.assertNoErrors()

        verify(exactly = 1) { repo.updateProject(updated) }
        verify(exactly = 1) {
            auditLogUC.logUpdate(
                entityId = id,
                entityName = "New",
                entityType = AuditLog.EntityType.PROJECT,
                fieldChange = changes[0],
            )
        }
    }
}
