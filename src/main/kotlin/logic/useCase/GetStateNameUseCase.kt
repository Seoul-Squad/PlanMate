package org.example.logic.useCase

import org.example.logic.models.Task
import org.example.logic.repositries.TaskStateRepository
import org.example.logic.utils.TaskStateNotFoundException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetStateNameUseCase(
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val taskStateRepository: TaskStateRepository,
) {
    suspend operator fun invoke(taskId: Uuid): String {
        val task: Task = getTaskByIdUseCase(taskId)
        taskStateRepository.getTaskStateById(task.stateId)
        return taskStateRepository.getTaskStateById(task.stateId)?.title
            ?: throw TaskStateNotFoundException()
    }
}
