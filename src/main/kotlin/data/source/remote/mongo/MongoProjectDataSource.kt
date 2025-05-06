package org.example.data.source.remote.mongo

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.example.data.mapper.toProject
import org.example.data.mapper.toProjectDTO
import org.example.data.models.ProjectDTO
import org.example.data.source.remote.contract.RemoteProjectDataSource
import org.example.data.source.remote.mongo.MongoExceptionHandler.Companion.handleMongoExceptions
import org.example.data.utils.Constants.ID
import org.example.logic.models.Project
import org.example.logic.utils.*

class MongoProjectDataSource(
    private val projectCollection : MongoCollection<ProjectDTO>,
) : RemoteProjectDataSource {
    override suspend fun createProject(project: Project): Project {
        return handleMongoExceptions(
            operation = { projectCollection.insertOne(project.toProjectDTO()); project },
            errorMessage = "Failed to create project",
            exceptionFactory = { message -> CreationItemFailedException(message) }
        )
    }

    override suspend fun updateProject(updatedProject: Project): Project {
        return  handleMongoExceptions(
            operation = {  projectCollection .replaceOne(Filters.eq(ID, updatedProject.id), updatedProject.toProjectDTO());updatedProject},
            errorMessage = "Failed to update project",
            exceptionFactory = { message -> UpdateItemFailedException(message) }
        )
    }

    override suspend fun deleteProject(projectId: String) {
         return handleMongoExceptions(
             operation = { projectCollection .deleteOne(Filters.eq(ID, projectId)) },
             errorMessage = "Failed to delete project",
             exceptionFactory = { message -> DeleteItemFailedException(message) }
         )
    }

    override suspend fun getAllProjects(): List<Project> {
        return handleMongoExceptions(
            operation = { projectCollection .find().toList().map { it.toProject() } },
            errorMessage = "Failed to get projects",
            exceptionFactory = { message -> GetItemsFailedException(message) }
        )
    }

    override suspend fun getProjectById(projectId: String): Project? {
        return handleMongoExceptions(
            operation = { projectCollection .find(Filters.eq(ID, projectId)).firstOrNull()?.toProject() },
            errorMessage = "Failed to get project by id",
            exceptionFactory = { message -> GetItemByIdFailedException(message) }
        )
    }
}