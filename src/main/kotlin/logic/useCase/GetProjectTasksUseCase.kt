package org.example.logic.useCase

import org.example.logic.models.Task
import org.example.logic.repositries.TaskRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetProjectTasksUseCase(
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(projectId: Uuid): List<Task> =
        taskRepository
            .getAllTasks()
            .filter { isTaskForProject(it, projectId) }

    private fun isTaskForProject(
        task: Task,
        projectId: Uuid,
    ) = task.projectId == projectId
}
