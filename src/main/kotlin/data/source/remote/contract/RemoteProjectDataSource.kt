package org.example.data.source.remote.contract

import org.example.logic.models.Project

interface RemoteProjectDataSource {
    suspend fun createProject(project: Project): Project

    suspend fun updateProject(updatedProject: Project): Project

    suspend fun deleteProject(projectId: String)

    suspend fun getAllProjects(): List<Project>

    suspend fun getProjectById(projectId: String): Project?
}
