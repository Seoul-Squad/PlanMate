package org.example.data.source.remote.mongo

import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.MongoCollection
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import org.example.data.repository.sources.remote.RemoteProjectDataSource
import org.example.data.source.remote.models.ProjectDTO
import org.example.data.source.remote.mongo.utils.executeMongoOperationRx
import org.example.data.source.remote.mongo.utils.mapper.toProject
import org.example.data.source.remote.mongo.utils.mapper.toProjectDTO
import org.example.data.utils.Constants.ID
import org.example.logic.models.Project
import org.example.logic.utils.ProjectNotFoundException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MongoProjectDataSource(
    private val projectCollection: MongoCollection<ProjectDTO>,
) : RemoteProjectDataSource {
    override fun createProject(project: Project): Single<Project> =
        executeMongoOperationRx {
            Single
                .fromPublisher(projectCollection.insertOne(project.toProjectDTO()))
                .map {
                    project
                }
        }

    override fun updateProject(updatedProject: Project): Single<Project> =
        executeMongoOperationRx {
            Single
                .fromPublisher(
                    projectCollection.replaceOne(
                        Filters.eq(ID, updatedProject.id.toHexString()),
                        updatedProject.toProjectDTO(),
                    ),
                ).map {
                    updatedProject
                }
        }

    override fun deleteProject(projectId: Uuid): Completable =
        executeMongoOperationRx {
            Single
                .fromPublisher(projectCollection.deleteOne(Filters.eq(ID, projectId.toHexString())))
                .flatMapCompletable {
                    Completable.complete()
                }
        }

    override fun getAllProjects(): Single<List<Project>> {
        val publisher = projectCollection.find()

        return executeMongoOperationRx {
            Flowable
                .fromPublisher(publisher)
                .map { projectDTO ->
                    projectDTO.toProject()
                }.toList()
        }
    }

    override fun getProjectById(projectId: Uuid): Single<Project> =
        executeMongoOperationRx {
            Single
                .fromPublisher(projectCollection.find(Filters.eq(ID, projectId.toHexString())))
                .map { projectDTO ->
                    projectDTO.toProject()
                }.onErrorResumeNext { error ->
                    if (error is NoSuchElementException) {
                        Single.error(ProjectNotFoundException())
                    } else {
                        Single.error(error)
                    }
                }
        }
}
