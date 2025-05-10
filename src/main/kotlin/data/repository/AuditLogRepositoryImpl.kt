package org.example.data.repository

import org.example.data.repository.mapper.mapExceptionsToDomainException
import org.example.data.repository.sources.remote.RemoteAuditLogDataSource
import org.example.logic.models.AuditLog
import org.example.logic.repositries.AuditLogRepository
import org.example.logic.utils.AuditLogCreationFailedException
import org.example.logic.utils.AuditLogDeletionFailedException
import org.example.logic.utils.AuditLogNotFoundException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class AuditLogRepositoryImpl(
    private val remoteAuditLogDataSource: RemoteAuditLogDataSource,
) : AuditLogRepository {
    override suspend fun createAuditLog(log: AuditLog): AuditLog =
        mapExceptionsToDomainException(AuditLogCreationFailedException()) {
            remoteAuditLogDataSource.saveAuditLog(log)
        }

    override suspend fun deleteAuditLog(logId: Uuid) =
        mapExceptionsToDomainException(AuditLogDeletionFailedException()) {
            remoteAuditLogDataSource.deleteAuditLog(logId)
        }

    override suspend fun getEntityLogs(
        entityId: Uuid,
        entityType: AuditLog.EntityType,
    ): List<AuditLog> =
        mapExceptionsToDomainException(AuditLogNotFoundException()) {
            remoteAuditLogDataSource.getEntityLogs(entityId.toHexString(), entityType)
        }

    override suspend fun getEntityLogByLogId(auditLogId: Uuid): AuditLog? =
        mapExceptionsToDomainException(AuditLogNotFoundException()) {
            remoteAuditLogDataSource.getEntityLogByLogId(auditLogId)
        }
}
