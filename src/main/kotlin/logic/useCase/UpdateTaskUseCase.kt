package org.example.logic.useCase

import org.example.logic.models.AuditLog
import org.example.logic.models.AuditLog.FieldChange.Companion.detectChanges
import org.example.logic.models.Task
import org.example.logic.repositries.TaskRepository
import org.example.logic.utils.TaskNotChangedException
import org.example.logic.utils.TaskNotFoundException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class UpdateTaskUseCase(
    private val taskRepository: TaskRepository,
    private val createAuditLogUseCase: CreateAuditLogUseCase,
) {
    suspend operator fun invoke(
        taskId: Uuid,
        updatedTask: Task,
    ): Task {
        val existingTask = getExistingTaskOrThrow(taskId)
        ensureTaskIsChanged(existingTask, updatedTask)
        return updateAndLogTask(existingTask, updatedTask)
    }

    private suspend fun updateAndLogTask(
        oldTask: Task,
        updatedTask: Task,
    ): Task {
        val logsIds =
            updatedTask.detectChanges(oldTask).map { change ->
                createAuditLogUseCase
                    .logUpdate(
                        entityType = AuditLog.EntityType.TASK,
                        entityId = oldTask.id,
                        entityName = updatedTask.name,
                        fieldChange = change,
                    ).id
            }
        return taskRepository.updateTask(updatedTask.copy(auditLogsIds = oldTask.auditLogsIds.plus(logsIds)))
    }

    private suspend fun getExistingTaskOrThrow(taskId: Uuid): Task = taskRepository.getTaskById(taskId) ?: throw TaskNotFoundException()

    private fun ensureTaskIsChanged(
        oldTask: Task,
        newTask: Task,
    ) {
        if (oldTask == newTask) {
            throw TaskNotChangedException()
        }
    }
}
