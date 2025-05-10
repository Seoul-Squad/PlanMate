@file:OptIn(ExperimentalUuidApi::class)

package org.example.data.source.remote.mongo.utils.mapper

import org.example.data.source.remote.models.TaskDTO
import org.example.logic.models.Task
import org.example.logic.utils.toUuid
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun TaskDTO.toTask(): Task =
    Task(
        id = id.toUuid(),
        name = name,
        stateId = stateId.toUuid(),
        stateName = stateName,
        addedBy = addedBy,
        auditLogsIds = auditLogsIds.map { Uuid.parse(it) },
        projectId = projectId.toUuid(),
    )

fun Task.toTaskDTO(): TaskDTO =
    TaskDTO(
        id = id.toHexString(),
        name = name,
        stateId = stateId.toHexString(),
        stateName = stateName,
        addedBy = addedBy,
        auditLogsIds = auditLogsIds.map { it.toHexString() },
        projectId = projectId.toHexString(),
    )
