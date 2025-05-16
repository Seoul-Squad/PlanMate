package org.example.logic.useCase

import io.reactivex.rxjava3.core.Single
import org.example.logic.models.Task
import org.example.logic.repositries.TaskRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetProjectTasksUseCase(
    private val taskRepository: TaskRepository,
) {
    operator fun invoke(projectId: Uuid): Single<List<Task>> =
        taskRepository
            .getAllTasks()
            .map { tasks -> tasks.filter { task -> isTaskForProject(task, projectId) } }

    private fun isTaskForProject(
        task: Task,
        projectId: Uuid,
    ) = task.projectId == projectId
}
