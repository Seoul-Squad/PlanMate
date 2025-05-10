@file:OptIn(ExperimentalUuidApi::class)

package org.example.data.source.local.csv.utils.mapper

import org.example.logic.models.Task
import org.example.logic.utils.toUuid
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

typealias CsvLine = String

fun List<CsvLine>.toTasks(): List<Task> {
    if (this.size <= 1) return emptyList()

    return this
        .drop(1)
        .filter { it.isNotBlank() }
        .map { line ->
            line
                .split(",")
                .map { it.trim() }
                .takeIf { it.size == 7 }
                ?.let { parts ->
                    Task(
                        id = parts[0].toUuid(),
                        name = parts[1],
                        stateId = parts[2].toUuid(),
                        stateName = parts[3],
                        addedBy = parts[4],
                        auditLogsIds = if (parts[5].isBlank()) emptyList() else parts[5].split("|").map { Uuid.parse(it) },
                        projectId = parts[6].toUuid(),
                    )
                } ?: throw IllegalArgumentException("CSV line doesn't have enough fields: $line")
        }
}

fun List<Task>.toCsvLines(): List<CsvLine> {
    val header = "id,name,stateId, stateName, addedBy,auditLogsIds,projectId"
    val dataLines =
        this.map { task ->
            if (task.name.contains(",")) throw IllegalArgumentException("CSV fields cannot contain comma")
            listOf(
                task.id,
                task.name,
                task.stateId,
                task.stateName,
                task.addedBy,
                task.auditLogsIds.joinToString("|"),
                task.projectId,
            ).joinToString(",")
        }

    return listOf(header) + dataLines
}
