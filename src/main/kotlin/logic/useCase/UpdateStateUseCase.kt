package org.example.logic.useCase

import org.example.logic.models.Project
import org.example.logic.models.State
import org.example.logic.repositries.ProjectRepository
import org.example.logic.repositries.TaskStateRepository
import org.example.logic.utils.BlankInputException
import org.example.logic.utils.ProjectNotFoundException
import org.example.logic.utils.TaskStateNotFoundException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class UpdateStateUseCase(
    private val taskStateRepository: TaskStateRepository,
    private val projectRepository: ProjectRepository,
) {
    suspend operator fun invoke(
        newStateName: String,
        stateId: Uuid,
        projectId: Uuid,
    ) {
        checkInputValidation(newStateName)
        val project = getProject(projectId)
        checkStateExists(project.tasksStatesIds, stateId)
        getProject(projectId)

        taskStateRepository.updateTaskState(State(stateId, newStateName))
    }

    private fun checkStateExists(
        states: List<Uuid>,
        stateId: Uuid,
    ) {
        if (states.none { state -> state == stateId }) throw TaskStateNotFoundException()
    }

    private suspend fun getProject(projectId: Uuid): Project =
        projectRepository.getProjectById(projectId)
            ?: throw ProjectNotFoundException()

    private fun checkInputValidation(newStateName: String) {
        when {
            newStateName.isBlank() -> throw BlankInputException()
        }
    }
}
