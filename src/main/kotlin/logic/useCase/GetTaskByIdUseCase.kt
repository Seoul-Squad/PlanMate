package org.example.logic.useCase

import org.example.logic.models.Task
import org.example.logic.repositries.TaskRepository
import org.example.logic.utils.TaskNotFoundException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetTaskByIdUseCase(
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(taskId: Uuid): Task =
        taskRepository.getTaskById(taskId).takeIf { it?.id == taskId }
            ?: throw TaskNotFoundException()
}
