package org.example.data.source.remote.mongo

import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.MongoCollection
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import org.example.data.repository.sources.remote.RemoteAuditLogDataSource
import org.example.data.source.remote.models.AuditLogDTO
import org.example.data.source.remote.mongo.utils.mapper.toAuditLog
import org.example.data.source.remote.mongo.utils.mapper.toAuditLogDTO
import org.example.data.utils.Constants.ENTITY_ID
import org.example.data.utils.Constants.ID
import org.example.logic.models.AuditLog
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MongoAuditLogDataSource(
    private val auditLogCollection: MongoCollection<AuditLogDTO>,
) : RemoteAuditLogDataSource {
    override fun saveAuditLog(auditLog: AuditLog): Single<AuditLog> =
        Single
            .fromPublisher(auditLogCollection.insertOne(auditLog.toAuditLogDTO()))
            .map {
                auditLog
            }

    override fun deleteAuditLog(auditLogId: Uuid): Completable =
        Single
            .fromPublisher(
                auditLogCollection.deleteOne(Filters.eq(ID, auditLogId.toHexString())),
            ).flatMapCompletable {
                Completable.complete()
            }

    override fun getEntityLogs(
        entityId: Uuid,
        entityType: AuditLog.EntityType,
    ): Single<List<AuditLog>> {
        val entityPublisher = auditLogCollection.find(Filters.eq(ENTITY_ID, entityId.toHexString()))
        return Flowable
            .fromPublisher(entityPublisher)
            .map { auditLogDTO ->
                auditLogDTO.toAuditLog()
            }.toList()
    }

    override fun getEntityLogByLogId(auditLogId: Uuid): Single<AuditLog> {
        val publisher = auditLogCollection.find(Filters.eq(ID, auditLogId.toHexString())).first()

        return Single
            .fromPublisher(publisher)
            .map { auditLogDTO ->
                auditLogDTO.toAuditLog()
            }
    }
}
