package org.example.logic.useCase

import org.example.logic.models.AuditLog
import org.example.logic.repositries.TaskRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DeleteTaskUseCase(
    private val taskRepository: TaskRepository,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val createAuditLogUseCase: CreateAuditLogUseCase,
) {
    suspend operator fun invoke(taskId: Uuid) {
        getTaskByIdUseCase(taskId).let { task ->
            createAuditLogUseCase.logDeletion(
                entityType = AuditLog.EntityType.PROJECT,
                entityId = task.id,
                entityName = task.name,
            )
            taskRepository.deleteTask(taskId)
        }
    }
}
