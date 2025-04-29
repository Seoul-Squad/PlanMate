package logic.useCase

import org.example.logic.models.AuditLog
import org.example.logic.models.AuditLogEntityType
import org.example.logic.repositries.AuditLogRepository

class GetEntityAuditLogsUseCase(
    private val auditLogRepository: AuditLogRepository
) {
    operator fun invoke(entityId: String, entityType: AuditLogEntityType): List<AuditLog>{
        TODO("Not yet implemented")
    }
}