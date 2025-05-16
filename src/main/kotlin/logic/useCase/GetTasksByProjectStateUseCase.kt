package org.example.logic.useCase

import io.reactivex.rxjava3.core.Single
import org.example.logic.models.Task
import org.example.logic.repositries.TaskRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetTasksByProjectStateUseCase(
    private val taskRepository: TaskRepository,
) {
    operator fun invoke(stateId: Uuid): Single<List<Task>> = taskRepository.getTasksByProjectState(stateId)
}
