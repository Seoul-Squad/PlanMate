package org.example.logic.useCase

import io.reactivex.rxjava3.core.Completable
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
    operator fun invoke(taskId: Uuid): Completable {
        val task = getTaskByIdUseCase(taskId).blockingGet()
        return taskRepository.deleteTask(taskId).doOnComplete {
            createAuditLogUseCase
                .logDeletion(
                    entityType = AuditLog.EntityType.TASK,
                    entityId = task.id,
                    entityName = task.name,
                ).blockingGet()
        }
    }
}
