package logic.useCase

import io.mockk.every
import io.mockk.mockk
import mockdata.createAuditLog
import org.example.logic.models.AuditLog
import org.example.logic.repositries.AuditLogRepository
import org.example.logic.useCase.GetEntityAuditLogsUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetEntityAuditLogsUseCaseTest {
    private lateinit var auditLogRepository: AuditLogRepository
    private lateinit var getEntityAuditLogsUseCase: GetEntityAuditLogsUseCase

    @BeforeEach
    fun setUp() {
        auditLogRepository = mockk(relaxed = true)
        getEntityAuditLogsUseCase = GetEntityAuditLogsUseCase(auditLogRepository)
    }

    @ParameterizedTest
    @MethodSource("provideExistingEntitiesScenarios")
    fun `should return list of audit logs when entity exists`(
        entityId: String,
        entityType: String,
    ) {
        val entityTypeEnum = AuditLog.EntityType.valueOf(entityType)
        val entityUuid = Uuid.parse(entityId)

        every { auditLogRepository.getEntityLogs(entityUuid, entityTypeEnum) } returns
            io.reactivex.rxjava3.core.Single.just(
                listOf(createAuditLog(entityId = entityUuid, entityType = entityTypeEnum)),
            )

        val testObserver = getEntityAuditLogsUseCase(entityUuid, entityTypeEnum).test()

        testObserver.assertComplete()
        testObserver.assertValue { auditLogs ->
            auditLogs.isNotEmpty() && auditLogs.all { it.entityId == entityUuid && it.entityType == entityTypeEnum }
        }
    }

    companion object {
        @JvmStatic
        fun provideExistingEntitiesScenarios(): Stream<Arguments> =
            Stream.of(
                Arguments.of(Uuid.random().toHexString(), AuditLog.EntityType.TASK.name),
                Arguments.of(Uuid.random().toHexString(), AuditLog.EntityType.PROJECT.name),
            )
    }
}
