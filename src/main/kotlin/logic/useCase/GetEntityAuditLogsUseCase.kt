package org.example.logic.useCase

import io.reactivex.rxjava3.core.Single
import org.example.logic.models.AuditLog
import org.example.logic.repositries.AuditLogRepository
import org.example.logic.utils.ProjectNotFoundException
import org.example.logic.utils.TaskNotFoundException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetEntityAuditLogsUseCase(
    private val auditLogRepository: AuditLogRepository,
) {
    operator fun invoke(
        entityId: Uuid,
        entityType: AuditLog.EntityType,
    ): Single<List<AuditLog>> = auditLogRepository.getEntityLogs(entityId, entityType)

    private fun getEntityNotFoundException(entityType: AuditLog.EntityType): Throwable =
        when (entityType) {
            AuditLog.EntityType.TASK -> TaskNotFoundException()
            AuditLog.EntityType.PROJECT -> ProjectNotFoundException()
        }
}
