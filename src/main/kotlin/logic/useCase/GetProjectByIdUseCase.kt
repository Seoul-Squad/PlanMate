package org.example.logic.useCase

import org.example.logic.models.Project
import org.example.logic.repositries.ProjectRepository
import org.example.logic.utils.ProjectNotFoundException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GetProjectByIdUseCase(
    private val projectRepository: ProjectRepository,
) {
    suspend operator fun invoke(projectId: Uuid): Project =
        projectId
            .let { projectRepository.getProjectById(projectId) ?: throw ProjectNotFoundException() }
}
