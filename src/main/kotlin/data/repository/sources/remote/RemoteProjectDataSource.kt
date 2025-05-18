package org.example.data.repository.sources.remote

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.example.logic.models.Project
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface RemoteProjectDataSource {
    fun createProject(project: Project): Single<Project>

    fun updateProject(updatedProject: Project): Single<Project>

    fun deleteProject(projectId: Uuid): Completable

    fun getAllProjects(): Single<List<Project>>

    fun getProjectById(projectId: Uuid): Single<Project>
}
