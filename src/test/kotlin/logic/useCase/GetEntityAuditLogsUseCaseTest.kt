package logic.useCase

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import mockdata.createAuditLog
import org.example.logic.models.AuditLogEntityType
import org.example.logic.repositries.AuditLogRepository
import org.example.logic.utils.BlankInputException
import org.example.logic.utils.ProjectNotFoundException
import org.example.logic.utils.TaskNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetEntityAuditLogsUseCaseTest {
    private lateinit var auditLogRepository: AuditLogRepository
    private lateinit var getEntityAuditLogsUseCase: GetEntityAuditLogsUseCase

    @BeforeEach
    fun setUp() {
        auditLogRepository = mockk(relaxed = true)
        getEntityAuditLogsUseCase = GetEntityAuditLogsUseCase(auditLogRepository)
    }

    @Test
    fun `should return list of entity audit logs when they are available`(){
        val entityId = Uuid.random().toHexString()
        val entityType = AuditLogEntityType.TASK
        val expectedLog = createAuditLog(id = entityId, entityType = entityType)
        every { auditLogRepository.getEntityLogs(any(), any()) } returns listOf(expectedLog)

        val result = getEntityAuditLogsUseCase(entityId, entityType)

        assertThat(result).containsExactly(expectedLog)
    }

    @Test
    fun `should throw BlankInputException when entity id is blank`(){
        val entityId = ""

        assertThrows<BlankInputException> {
            getEntityAuditLogsUseCase(entityId, AuditLogEntityType.TASK)
        }
    }

    @Test
    fun `should throw TaskNotFoundException when no logs are found for the task id`(){
        val entityId = Uuid.random().toHexString()
        val entityType = AuditLogEntityType.TASK
        every { auditLogRepository.getEntityLogs(any(), any()) } returns emptyList()

        assertThrows<TaskNotFoundException> {
            getEntityAuditLogsUseCase(entityId, entityType)
        }
    }

    @Test
    fun `should throw ProjectNotFoundException when no logs are found for the project id`(){
        val entityId = Uuid.random().toHexString()
        val entityType = AuditLogEntityType.PROJECT
        every { auditLogRepository.getEntityLogs(any(), any()) } returns emptyList()

        assertThrows<ProjectNotFoundException> {
            getEntityAuditLogsUseCase(entityId, entityType)
        }
    }
}