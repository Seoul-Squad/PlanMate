@file:OptIn(ExperimentalUuidApi::class)

package org.example.data.source.remote.mongo.utils.mapper

import org.example.data.source.remote.models.AuditLogDTO
import org.example.logic.models.AuditLog
import org.example.logic.utils.toInstant
import org.example.logic.utils.toUuid
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun AuditLogDTO.toAuditLog(): AuditLog =
    AuditLog(
        id = Uuid.parse(id),
        userId = userId.toUuid(),
        userName = userName,
        createdAt = createdAt.toInstant(),
        entityType = AuditLog.EntityType.valueOf(entityType),
        entityName = entityName,
        entityId = entityId.toUuid(),
        actionType = AuditLog.ActionType.valueOf(actionType),
        fieldChange = fieldChange?.toFieldChange(),
    )

fun AuditLog.toAuditLogDTO(): AuditLogDTO =
    AuditLogDTO(
        id = id.toHexString(),
        userId = userId.toHexString(),
        userName = userName,
        createdAt = createdAt.toEpochMilliseconds(),
        entityType = entityType.name,
        entityName = entityName,
        entityId = entityId.toHexString(),
        actionType = actionType.name,
        fieldChange = fieldChange?.toFieldChangeDto(),
    )

fun AuditLog.FieldChange.toFieldChangeDto(): AuditLogDTO.FieldChangeDto =
    AuditLogDTO.FieldChangeDto(
        fieldName = fieldName,
        oldValue = oldValue,
        newValue = newValue,
    )

fun AuditLogDTO.FieldChangeDto.toFieldChange(): AuditLog.FieldChange =
    AuditLog.FieldChange(
        fieldName = fieldName,
        oldValue = oldValue,
        newValue = newValue,
    )
