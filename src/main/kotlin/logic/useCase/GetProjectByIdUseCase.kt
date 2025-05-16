package org.example.logic.useCase

import io.reactivex.rxjava3.core.Single
import org.example.logic.models.Project
import org.example.logic.repositries.ProjectRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetProjectByIdUseCase(
    private val projectRepository: ProjectRepository,
) {
    operator fun invoke(projectId: Uuid): Single<Project> = projectRepository.getProjectById(projectId)
}
