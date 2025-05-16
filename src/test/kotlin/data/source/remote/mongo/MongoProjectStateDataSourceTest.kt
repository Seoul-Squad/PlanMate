package data.source.remote.mongo

import com.google.common.truth.Truth.assertThat
import com.mongodb.client.model.Filters
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.FindPublisher
import com.mongodb.reactivestreams.client.MongoCollection
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Flowable
import org.example.data.source.remote.models.ProjectStateDTO
import org.example.data.source.remote.mongo.MongoProjectStateDataSource
import org.example.data.source.remote.mongo.utils.mapper.toStateDTO
import org.example.logic.models.ProjectState
import org.example.logic.utils.ProjectStateNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MongoProjectStateDataSourceTest {
    private lateinit var mongoCollection: MongoCollection<ProjectStateDTO>
    private lateinit var dataSource: MongoProjectStateDataSource
    private lateinit var testState: ProjectState
    private lateinit var testDto: ProjectStateDTO
    private val testId = Uuid.random()
    private val testProjectId = Uuid.random()

    @BeforeEach
    fun setUp() {
        mongoCollection = mockk(relaxed = true)
        dataSource = MongoProjectStateDataSource(mongoCollection)

        testState =
            ProjectState(
                id = testId,
                title = "To Do",
                projectId = testProjectId,
            )
        testDto = testState.toStateDTO()
    }

    @Test
    fun `should return project state when creating in MongoDB`() {
        val insertResult = mockk<InsertOneResult>()
        every { mongoCollection.insertOne(testDto) } returns Flowable.just(insertResult)

        val result = dataSource.createProjectState(testState).blockingGet()

        assertThat(result).isEqualTo(testState)
    }

    @Test
    fun `should return project state when updating in MongoDB`() {
        val updateResult = mockk<UpdateResult>()
        every {
            mongoCollection.replaceOne(Filters.eq("id", testId.toHexString()), testDto)
        } returns Flowable.just(updateResult)

        val result = dataSource.updateProjectState(testState).blockingGet()

        assertThat(result).isEqualTo(testState)
    }

    @Test
    fun `should complete when deleting project state`() {
        val deleteResult = mockk<DeleteResult>()
        every { mongoCollection.deleteOne(Filters.eq("id", testId.toHexString())) } returns Flowable.just(deleteResult)

        val result = dataSource.deleteProjectState(testId).test()

        result.assertComplete()
    }

    @Test
    fun `should return project state when found by id`() {
        val publisher = mockk<FindPublisher<ProjectStateDTO>>()
        every { mongoCollection.find(Filters.eq("id", testId.toHexString())).first() } returns publisher
        every { publisher.subscribe(any()) } answers {
            val subscriber = firstArg<org.reactivestreams.Subscriber<in ProjectStateDTO>>()
            subscriber.onNext(testDto)
            subscriber.onComplete()
        }

        val result = dataSource.getProjectStateById(testId).blockingGet()

        assertThat(result).isEqualTo(testState)
    }

    @Test
    fun `should throw ProjectStateNotFoundException when get by id fails`() {
        every {
            mongoCollection.find(Filters.eq("id", testId.toHexString())).first()
        } returns Flowable.error(RuntimeException("not found"))

        assertThrows<ProjectStateNotFoundException> {
            dataSource.getProjectStateById(testId).blockingGet()
        }
    }

    @Test
    fun `should return list of project states by project id`() {
        val publisher = mockk<FindPublisher<ProjectStateDTO>>()
        every {
            mongoCollection.find(Filters.eq("projectId", testProjectId.toHexString()))
        } returns publisher
        every { publisher.subscribe(any()) } answers {
            val subscriber = firstArg<org.reactivestreams.Subscriber<in ProjectStateDTO>>()
            subscriber.onNext(testDto)
            subscriber.onComplete()
        }

        val result = dataSource.getProjectStates(testProjectId).blockingGet()

        assertThat(result).containsExactly(testState)
    }

    @Test
    fun `should throw ProjectStateNotFoundException when get by project id fails`() {
        val publisher = mockk<FindPublisher<ProjectStateDTO>>()

        every {
            mongoCollection.find(Filters.eq("projectId", testProjectId.toHexString()))
        } returns publisher

        every { publisher.subscribe(any()) } answers {
            val subscriber = firstArg<org.reactivestreams.Subscriber<in ProjectStateDTO>>()
            subscriber.onError(RuntimeException("db error"))
        }

        assertThrows<ProjectStateNotFoundException> {
            dataSource.getProjectStates(testProjectId).blockingGet()
        }
    }
}
