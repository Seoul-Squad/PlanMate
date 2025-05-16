package org.example.logic.useCase

import io.reactivex.rxjava3.core.Single
import org.example.logic.models.Task
import org.example.logic.repositries.TaskRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetTaskByIdUseCase(
    private val taskRepository: TaskRepository,
) {
    operator fun invoke(taskId: Uuid): Single<Task> = taskRepository.getTaskById(taskId)
}
