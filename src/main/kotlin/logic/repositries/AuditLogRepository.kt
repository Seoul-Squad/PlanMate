package org.example.logic.repositries

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.AuditLog
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface AuditLogRepository {
    fun createAuditLog(log: AuditLog): Single<AuditLog>

    fun deleteAuditLog(logId: Uuid): Completable

    fun getEntityLogs(
        entityId: Uuid,
        entityType: AuditLog.EntityType,
    ): Single<List<AuditLog>>

    fun getEntityLogByLogId(auditLogId: Uuid): Single<AuditLog>
}
