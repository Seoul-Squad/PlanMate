package data.source.remote.mongo

import com.mongodb.client.model.Filters
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.FindPublisher
import com.mongodb.reactivestreams.client.MongoCollection
import io.mockk.every
import io.mockk.mockk
import mockdata.createProject
import org.example.data.repository.sources.remote.RemoteProjectDataSource
import org.example.data.source.remote.models.ProjectDTO
import org.example.data.source.remote.mongo.MongoProjectDataSource
import org.example.data.source.remote.mongo.utils.mapper.toProjectDTO
import org.example.data.utils.Constants.ID
import org.example.logic.models.Project
import org.example.logic.utils.ProjectCreationFailedException
import org.example.logic.utils.ProjectDeletionFailedException
import org.example.logic.utils.ProjectNotChangedException
import org.example.logic.utils.ProjectNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MongoProjectDataSourceTest {
    private lateinit var mongoClientCollection: MongoCollection<ProjectDTO>
    private lateinit var remoteProjectDataSource: RemoteProjectDataSource
    private lateinit var testProjects: List<Project>
    private lateinit var testProjectDTOs: List<ProjectDTO>
    private val ids = List(6) { Uuid.random() }

    @BeforeEach
    fun setUp() {
        mongoClientCollection = mockk(relaxed = true)
        testProjects =
            listOf(
                createProject(id = ids[1], name = "Project 1"),
                Project(id = ids[2], name = "Project 2"),
            )
        testProjectDTOs = testProjects.map { it.toProjectDTO() }
        remoteProjectDataSource = MongoProjectDataSource(mongoClientCollection)
    }

    private fun mockFindPublisher(items: List<ProjectDTO>): FindPublisher<ProjectDTO> {
        val publisher = mockk<FindPublisher<ProjectDTO>>(relaxed = true)
        every { publisher.subscribe(any()) } answers {
            val subscriber = arg<Subscriber<in ProjectDTO>>(0)
            subscriber.onSubscribe(
                object : Subscription {
                    override fun request(n: Long) {
                        items.forEach { subscriber.onNext(it) }
                        subscriber.onComplete()
                    }

                    override fun cancel() {}
                },
            )
        }
        return publisher
    }

    private fun mockFindPublisherForOne(item: ProjectDTO): FindPublisher<ProjectDTO> {
        val publisher = mockk<FindPublisher<ProjectDTO>>(relaxed = true)
        every { publisher.subscribe(any()) } answers {
            val subscriber = arg<Subscriber<in ProjectDTO>>(0)
            subscriber.onSubscribe(
                object : Subscription {
                    override fun request(n: Long) {
                        subscriber.onNext(item)
                        subscriber.onComplete()
                    }

                    override fun cancel() {}
                },
            )
        }
        return publisher
    }

    @Test
    fun `should return list of projects when getting from MongoDB`() {
        every { mongoClientCollection.find() } returns mockFindPublisher(testProjectDTOs)

        val testObserver = remoteProjectDataSource.getAllProjects().test()

        testObserver.assertComplete()
        testObserver.assertValue { it.size == testProjectDTOs.size }
    }

    @Test
    fun `should throw ProjectNotFoundException when find by ID fails`() {
        val error = ProjectNotFoundException()
        val brokenPublisher = mockk<FindPublisher<ProjectDTO>>()
        every { brokenPublisher.subscribe(any()) } answers {
            val subscriber = arg<Subscriber<in ProjectDTO>>(0)
            subscriber.onSubscribe(mockk())
            subscriber.onError(error)
        }

        every { mongoClientCollection.find(Filters.eq(ID, ids[1].toHexString())) } returns brokenPublisher

        val testObserver = remoteProjectDataSource.getProjectById(ids[1]).test()
        testObserver.assertError(ProjectNotFoundException::class.java)
    }

    @Test
    fun `should return project when creating in MongoDB`() {
        val project = createProject(id = ids[3])
        val dto = project.toProjectDTO()

        val insertResult = mockk<InsertOneResult>()
        every { mongoClientCollection.insertOne(dto) } returns flowOf(insertResult)

        val testObserver = remoteProjectDataSource.createProject(project).test()
        testObserver.assertComplete()
        testObserver.assertValue(project)
    }

    @Test
    fun `should throw exception when creating fails in MongoDB`() {
        val project = createProject(id = ids[3])
        val dto = project.toProjectDTO()

        every { mongoClientCollection.insertOne(dto) } returns flowError(ProjectCreationFailedException())

        val testObserver = remoteProjectDataSource.createProject(project).test()
        testObserver.assertError(ProjectCreationFailedException::class.java)
    }

    @Test
    fun `should update project in MongoDB`() {
        val project = createProject(id = ids[3])
        val dto = project.toProjectDTO()

        val result = mockk<UpdateResult>()
        every {
            mongoClientCollection.replaceOne(Filters.eq(ID, dto.id), dto)
        } returns flowOf(result)

        val testObserver = remoteProjectDataSource.updateProject(project).test()
        testObserver.assertComplete()
        testObserver.assertValue(project)
    }

    @Test
    fun `should throw exception when update fails in MongoDB`() {
        val project = createProject(id = ids[3])
        val dto = project.toProjectDTO()

        every {
            mongoClientCollection.replaceOne(Filters.eq(ID, dto.id), dto)
        } returns flowError(ProjectNotChangedException())

        val testObserver = remoteProjectDataSource.updateProject(project).test()
        testObserver.assertError(ProjectNotChangedException::class.java)
    }

    @Test
    fun `should delete project by ID`() {
        val deleteResult = mockk<DeleteResult>()
        every { mongoClientCollection.deleteOne(Filters.eq(ID, ids[1].toHexString())) } returns flowOf(deleteResult)

        val testObserver = remoteProjectDataSource.deleteProject(ids[1]).test()
        testObserver.assertComplete()
    }

    @Test
    fun `should throw exception when delete fails`() {
        every {
            mongoClientCollection.deleteOne(Filters.eq(ID, ids[1].toHexString()))
        } returns flowError(ProjectDeletionFailedException())

        val testObserver = remoteProjectDataSource.deleteProject(ids[1]).test()
        testObserver.assertError(ProjectDeletionFailedException::class.java)
    }

    // Helpers
    private fun <T> flowOf(item: T): Publisher<T> =
        Publisher<T> { s ->
            s.onSubscribe(
                object : Subscription {
                    override fun request(n: Long) {
                        s.onNext(item)
                        s.onComplete()
                    }

                    override fun cancel() {}
                },
            )
        }

    private fun <T> flowError(error: Throwable): Publisher<T> =
        Publisher<T> { s ->
            s.onSubscribe(mockk())
            s.onError(error)
        }
}
