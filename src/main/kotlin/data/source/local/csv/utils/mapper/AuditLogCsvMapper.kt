@file:OptIn(ExperimentalUuidApi::class)

package org.example.data.source.local.csv.utils.mapper

import org.example.logic.models.AuditLog
import org.example.logic.utils.toInstant
import org.example.logic.utils.toUuid
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun AuditLog.toCsvRow(): String =
    listOf(
        id.toHexString(),
        userId,
        userName,
        createdAt.toEpochMilliseconds(),
        entityType.name,
        entityId,
        entityName,
        actionType.name,
        fieldChange?.fieldName ?: "",
        fieldChange?.oldValue ?: "",
        fieldChange?.newValue ?: "",
    ).joinToString(",")

fun List<AuditLog>.toCsvRows(): List<String> = this.map { it.toCsvRow() }

fun List<String>.toAuditLogs(): List<AuditLog> {
    if (this.isEmpty()) return emptyList()

    return this.filter { it.isNotBlank() }.map { line ->
        line.split(",").map { it.trim() }.takeIf { it.size == 7 }?.let { parts ->
            AuditLog(
                id = Uuid.parse(parts[0]),
                userId = parts[1].toUuid(),
                userName = parts[2],
                createdAt =
                    parts[3].toLongOrNull()?.toInstant()
                        ?: throw IllegalArgumentException("Invalid timestamp: ${parts[3]}"),
                entityType = AuditLog.EntityType.valueOf(parts[4]),
                entityId = parts[5].toUuid(),
                entityName = parts[6],
                actionType = AuditLog.ActionType.valueOf(parts[7]),
                fieldChange =
                    if (parts[8].isBlank()) {
                        null
                    } else {
                        AuditLog.FieldChange(
                            fieldName = parts[8],
                            oldValue = parts[9],
                            newValue = parts[10],
                        )
                    },
            )
        } ?: throw IllegalArgumentException("CSV line doesn't have enough fields: $line")
    }
}
