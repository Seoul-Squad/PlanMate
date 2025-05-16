package org.example.logic.useCase

import io.reactivex.rxjava3.core.Single
import org.example.logic.models.ProjectState
import org.example.logic.repositries.ProjectStateRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetProjectStatesUseCase(
    private val projectStateRepository: ProjectStateRepository,
) {
    operator fun invoke(projectId: Uuid): Single<List<ProjectState>> = projectStateRepository.getProjectStates(projectId)
}
