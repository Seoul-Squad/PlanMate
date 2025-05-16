package org.example.logic.useCase

import io.reactivex.rxjava3.core.Single
import org.example.logic.models.AuditLog
import org.example.logic.repositries.AuditLogRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CreateAuditLogUseCase(
    private val auditLogRepository: AuditLogRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) {
    fun logCreation(
        entityType: AuditLog.EntityType,
        entityId: Uuid,
        entityName: String,
    ): Single<AuditLog> {
        val currentUser = getCurrentUserUseCase().blockingGet()
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

    fun logUpdate(
        entityType: AuditLog.EntityType,
        entityId: Uuid,
        entityName: String,
        fieldChange: AuditLog.FieldChange,
    ): Single<AuditLog> {
        val currentUser = getCurrentUserUseCase().blockingGet()
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

    fun logDeletion(
        entityType: AuditLog.EntityType,
        entityId: Uuid,
        entityName: String,
    ): Single<AuditLog> {
        val currentUser =
            getCurrentUserUseCase().blockingGet()
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
