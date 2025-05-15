package org.example.data.repository

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.example.data.repository.mapper.mapExceptionsToDomainExceptionRx
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
    override fun createAuditLog(log: AuditLog): Single<AuditLog> =
        mapExceptionsToDomainExceptionRx(AuditLogCreationFailedException()) {
            remoteAuditLogDataSource.saveAuditLog(log)
        }

    override fun deleteAuditLog(logId: Uuid): Completable =
        mapExceptionsToDomainExceptionRx(AuditLogDeletionFailedException()) {
            remoteAuditLogDataSource.deleteAuditLog(logId)
        }

    override fun getEntityLogs(
        entityId: Uuid,
        entityType: AuditLog.EntityType,
    ): Single<List<AuditLog>> =
        mapExceptionsToDomainExceptionRx(AuditLogNotFoundException()) {
            remoteAuditLogDataSource.getEntityLogs(entityId, entityType)
        }

    override fun getEntityLogByLogId(auditLogId: Uuid): Single<AuditLog> =
        mapExceptionsToDomainExceptionRx(AuditLogNotFoundException()) {
            remoteAuditLogDataSource.getEntityLogByLogId(auditLogId)
        }
}
