package org.example.logic.useCase

import io.reactivex.rxjava3.core.Single
import org.example.logic.models.Project
import org.example.logic.repositries.ProjectRepository

class GetAllProjectsUseCase(
    private val projectRepository: ProjectRepository,
) {
    operator fun invoke(): Single<List<Project>> = projectRepository.getAllProjects()
}
