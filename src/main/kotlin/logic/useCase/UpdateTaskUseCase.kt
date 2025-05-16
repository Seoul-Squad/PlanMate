package org.example.logic.useCase

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.example.logic.models.AuditLog
import org.example.logic.models.AuditLog.FieldChange.Companion.detectChanges
import org.example.logic.models.Task
import org.example.logic.repositries.TaskRepository
import org.example.logic.utils.TaskNotChangedException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class UpdateTaskUseCase(
    private val taskRepository: TaskRepository,
    private val createAuditLogUseCase: CreateAuditLogUseCase,
) {
    operator fun invoke(updatedTask: Task): Single<Task> {
        val existingTask = getExistingTaskOrThrow(updatedTask.id)
        ensureTaskIsChanged(existingTask, updatedTask)
        return taskRepository.updateTask(updatedTask).doOnSuccess {
            createLogs(
                oldTask = existingTask,
                updatedTask = updatedTask,
            )
        }
    }

    private fun createLogs(
        oldTask: Task,
        updatedTask: Task,
    ) {
        updatedTask.detectChanges(oldTask).map { change ->
            createAuditLogUseCase
                .logUpdate(
                    entityType = AuditLog.EntityType.TASK,
                    entityId = oldTask.id,
                    entityName = updatedTask.name,
                    fieldChange = change,
                ).blockingGet()
                .id
        }
    }

    private fun getExistingTaskOrThrow(taskId: Uuid): Task = taskRepository.getTaskById(taskId).subscribeOn(Schedulers.io()).blockingGet()

    private fun ensureTaskIsChanged(
        oldTask: Task,
        newTask: Task,
    ) {
        if (oldTask == newTask) {
            throw TaskNotChangedException()
        }
    }
}
