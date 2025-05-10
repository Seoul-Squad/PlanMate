package org.example.logic.useCase

import org.example.logic.models.Project
import org.example.logic.repositries.ProjectRepository
import org.example.logic.repositries.TaskRepository
import org.example.logic.repositries.TaskStateRepository
import org.example.logic.utils.ProjectNotFoundException
import org.example.logic.utils.TaskDeletionFailedException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DeleteStateUseCase(
    private val taskStateRepository: TaskStateRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(
        stateId: Uuid,
        projectId: Uuid,
    ) {
        val project = getProject(projectId)
        val updatedStates = removeState(project, stateId)
        if (project.tasksStatesIds.size == 1) throw TaskDeletionFailedException()
        val projectStateTasks =
            taskRepository.getAllTasks().filter { it.projectId == projectId && it.stateId == stateId }
        for (task in projectStateTasks) {
            taskRepository.deleteTask(task.id)
        }
        taskStateRepository.deleteTaskState(stateId)
        projectRepository.updateProject(project.copy(tasksStatesIds = updatedStates))
    }

    private fun removeState(
        project: Project,
        stateId: Uuid,
    ): List<Uuid> = project.tasksStatesIds.filter { it != stateId }

    private suspend fun getProject(projectId: Uuid): Project =
        projectRepository.getProjectById(projectId)
            ?: throw ProjectNotFoundException()
}
