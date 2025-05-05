package org.example.data.models

data class TaskDTO(
    val id: String,
    val name: String,
    val stateId: String,
    val addedBy: String,
    val auditLogsIds: List<String>,
    val projectId: String
)

