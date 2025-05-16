package org.example.data.source.remote.mongo

import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.MongoCollection
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import org.example.data.source.remote.contract.RemoteProjectStateDataSource
import org.example.data.source.remote.models.ProjectStateDTO
import org.example.data.source.remote.mongo.utils.executeMongoOperationRx
import org.example.data.source.remote.mongo.utils.mapper.toState
import org.example.data.source.remote.mongo.utils.mapper.toStateDTO
import org.example.data.utils.Constants.ID
import org.example.data.utils.Constants.PROJECT_ID
import org.example.logic.models.ProjectState
import org.example.logic.utils.ProjectStateNotFoundException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MongoProjectStateDataSource(
    private val mongoClient: MongoCollection<ProjectStateDTO>,
) : RemoteProjectStateDataSource {
    override fun createProjectState(projectState: ProjectState): Single<ProjectState> =
        executeMongoOperationRx {
            Single
                .fromPublisher(mongoClient.insertOne(projectState.toStateDTO()))
                .map { projectState }
        }

    override fun updateProjectState(updatedProjectProjectState: ProjectState): Single<ProjectState> =
        executeMongoOperationRx {
            Single
                .fromPublisher(
                    mongoClient.replaceOne(
                        Filters.eq(ID, updatedProjectProjectState.id.toHexString()),
                        updatedProjectProjectState.toStateDTO(),
                    ),
                ).map { updatedProjectProjectState }
        }

    override fun deleteProjectState(projectStateId: Uuid): Completable =
        executeMongoOperationRx {
            Single
                .fromPublisher(mongoClient.deleteOne(Filters.eq(ID, projectStateId.toHexString())))
                .flatMapCompletable { Completable.complete() }
        }

    override fun getProjectStateById(projectStateId: Uuid): Single<ProjectState> =
        executeMongoOperationRx {
            Single
                .fromPublisher(mongoClient.find(Filters.eq(ID, projectStateId.toHexString())).first())
                .map { it.toState() }
                .onErrorResumeNext { error ->
                    if (error is NoSuchElementException) {
                        Single.error(ProjectStateNotFoundException())
                    } else {
                        Single.error(error)
                    }
                }
        }

    override fun getProjectStates(projectId: Uuid): Single<List<ProjectState>> =
        executeMongoOperationRx {
            Flowable
                .fromPublisher(mongoClient.find(Filters.eq(PROJECT_ID, projectId.toHexString())))
                .map { it.toState() }
                .toList()
        }
}
