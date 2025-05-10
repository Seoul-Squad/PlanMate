package org.example.logic.useCase

import org.example.logic.models.AuditLog
import org.example.logic.repositries.AuditLogRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CreateAuditLogUseCase(
    private val auditLogRepository: AuditLogRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) {
    suspend fun logCreation(
        entityType: AuditLog.EntityType,
        entityId: Uuid,
        entityName: String,
    ): AuditLog {
        val currentUser = getCurrentUserUseCase()
        return auditLogRepository.createAuditLog(
            AuditLog(
                userId = currentUser.id,
                userName = currentUser.username,
                entityId = entityId,
                entityType = entityType,
                entityName = entityName,
                actionType = AuditLog.ActionType.CREATE,
            ),
        )
    }

    suspend fun logUpdate(
        entityType: AuditLog.EntityType,
        entityId: Uuid,
        entityName: String,
        fieldChange: AuditLog.FieldChange,
    ): AuditLog {
        val currentUser = getCurrentUserUseCase()
        return auditLogRepository.createAuditLog(
            AuditLog(
                userId = currentUser.id,
                userName = currentUser.username,
                entityId = entityId,
                entityName = entityName,
                entityType = entityType,
                actionType = AuditLog.ActionType.UPDATE,
                fieldChange = fieldChange,
            ),
        )
    }

    suspend fun logDeletion(
        entityType: AuditLog.EntityType,
        entityId: Uuid,
        entityName: String,
    ): AuditLog {
        val currentUser = getCurrentUserUseCase()
        return auditLogRepository.createAuditLog(
            AuditLog(
                userId = currentUser.id,
                userName = currentUser.username,
                entityId = entityId,
                entityType = entityType,
                entityName = entityName,
                actionType = AuditLog.ActionType.DELETE,
            ),
        )
    }
}
