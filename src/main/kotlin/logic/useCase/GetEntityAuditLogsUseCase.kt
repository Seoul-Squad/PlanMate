package org.example.logic.useCase

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
    suspend operator fun invoke(
        entityId: Uuid,
        entityType: AuditLog.EntityType,
    ): List<AuditLog> =
        auditLogRepository
            .getEntityLogs(entityId, entityType)
            .takeIf {
                it.isNotEmpty()
            } ?: throw getEntityNotFoundException(entityType)

    private fun getEntityNotFoundException(entityType: AuditLog.EntityType) =
        when (entityType) {
            AuditLog.EntityType.TASK -> TaskNotFoundException()
            AuditLog.EntityType.PROJECT -> ProjectNotFoundException()
        }
}
