package logic.useCase

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.AuditLog
import org.example.logic.models.AuditLog.FieldChange
import org.example.logic.models.User
import org.example.logic.models.UserRole
import org.example.logic.repositries.AuditLogRepository
import org.example.logic.useCase.CreateAuditLogUseCase
import org.example.logic.useCase.GetCurrentUserUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CreateAuditLogUseCaseTest {

    private lateinit var auditLogRepository: AuditLogRepository
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var createAuditLogUseCase: CreateAuditLogUseCase

    private val testUser = User(
        id = Uuid.random(),
        username = "tester",
        role = UserRole.ADMIN,
        authMethod = User.AuthenticationMethod.Password("test123")
    )

    private val entityId = Uuid.random()
    private val entityName = "TestEntity"

    @BeforeEach
    fun setUp() {
        auditLogRepository = mockk(relaxed = true)
        getCurrentUserUseCase = mockk()
        createAuditLogUseCase = CreateAuditLogUseCase(auditLogRepository, getCurrentUserUseCase)

        every { getCurrentUserUseCase() } returns Single.just(testUser)
    }

    @Test
    fun `should call createAuditLog with correct CREATE action when logCreation is called`() {
        val logSlot = slot<AuditLog>()
        every { auditLogRepository.createAuditLog(capture(logSlot)) } returns Single.just(mockk())

        createAuditLogUseCase.logCreation(
            entityType = AuditLog.EntityType.PROJECT,
            entityId = entityId,
            entityName = entityName,
        ).blockingGet()

        verify { auditLogRepository.createAuditLog(any()) }

        val capturedLog = logSlot.captured
        assertThat(capturedLog.actionType).isEqualTo(AuditLog.ActionType.CREATE)
        assertThat(capturedLog.userId).isEqualTo(testUser.id)
        assertThat(capturedLog.entityType).isEqualTo(AuditLog.EntityType.PROJECT)
    }

    @Test
    fun `should call createAuditLog with correct UPDATE action and field change when logUpdate is called`() {
        val fieldChange = FieldChange("field", "old", "new")
        val logSlot = slot<AuditLog>()
        every { auditLogRepository.createAuditLog(capture(logSlot)) } returns Single.just(mockk())

        createAuditLogUseCase.logUpdate(
            entityType = AuditLog.EntityType.TASK,
            entityId = entityId,
            entityName = entityName,
            fieldChange = fieldChange,
        ).blockingGet()

        verify { auditLogRepository.createAuditLog(any()) }

        val capturedLog = logSlot.captured
        assertThat(capturedLog.actionType).isEqualTo(AuditLog.ActionType.UPDATE)
        assertThat(capturedLog.fieldChange).isEqualTo(fieldChange)
    }

    @Test
    fun `should call createAuditLog with correct DELETE action when logDeletion is called`() {
        val logSlot = slot<AuditLog>()
        every { auditLogRepository.createAuditLog(capture(logSlot)) } returns Single.just(mockk())

        createAuditLogUseCase.logDeletion(
            entityType = AuditLog.EntityType.TASK,
            entityId = entityId,
            entityName = entityName,
        ).blockingGet()

        verify { auditLogRepository.createAuditLog(any()) }

        val capturedLog = logSlot.captured
        assertThat(capturedLog.actionType).isEqualTo(AuditLog.ActionType.DELETE)
        assertThat(capturedLog.fieldChange).isNull()
    }
}
