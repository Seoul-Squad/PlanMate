package org.example.data.source.remote.mongo

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.example.data.repository.sources.remote.RemoteProjectDataSource
import org.example.data.source.remote.models.ProjectDTO
import org.example.data.source.remote.mongo.utils.executeMongoOperation
import org.example.data.source.remote.mongo.utils.mapper.toProject
import org.example.data.source.remote.mongo.utils.mapper.toProjectDTO
import org.example.data.utils.Constants.ID
import org.example.logic.models.Project
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class MongoProjectDataSource(
    private val projectCollection: MongoCollection<ProjectDTO>,
) : RemoteProjectDataSource {
    override suspend fun createProject(project: Project): Project =
        executeMongoOperation {
            projectCollection.insertOne(project.toProjectDTO())
            project
        }

    override suspend fun updateProject(updatedProject: Project): Project =
        executeMongoOperation {
            projectCollection.replaceOne(Filters.eq(ID, updatedProject.id.toHexString()), updatedProject.toProjectDTO())
            updatedProject
        }

    override suspend fun deleteProject(projectId: String) {
        executeMongoOperation {
            projectCollection.deleteOne(Filters.eq(ID, projectId))
        }
    }

    override suspend fun getAllProjects(): List<Project> =
        executeMongoOperation {
            projectCollection.find().toList().map { it.toProject() }
        }

    override suspend fun getProjectById(projectId: String): Project? =
        executeMongoOperation {
            projectCollection.find(Filters.eq(ID, projectId)).firstOrNull()?.toProject()
        }
}
