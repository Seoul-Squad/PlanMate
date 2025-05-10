package org.example.logic.models

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Task(
    val id: Uuid = Uuid.random(),
    val name: String,
    val stateId: Uuid,
    val stateName: String,
    val addedBy: String,
    val auditLogsIds: List<Uuid>,
    val projectId: Uuid,
)
