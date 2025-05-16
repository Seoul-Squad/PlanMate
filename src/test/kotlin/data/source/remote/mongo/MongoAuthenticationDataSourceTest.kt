package data.source.remote.mongo

import com.google.common.truth.Truth.assertThat
import com.mongodb.MongoClientException
import com.mongodb.MongoTimeoutException
import com.mongodb.client.result.InsertOneResult
import com.mongodb.reactivestreams.client.FindPublisher
import com.mongodb.reactivestreams.client.MongoCollection
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Flowable
import org.example.data.source.remote.models.UserDTO
import org.example.data.source.remote.mongo.MongoAuthenticationDataSource
import org.example.data.source.remote.mongo.utils.mapper.toUserDTO
import org.example.logic.models.User
import org.example.logic.models.UserRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.reactivestreams.Publisher
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class MongoAuthenticationDataSourceTest {
    private lateinit var mongoCollection: MongoCollection<UserDTO>
    private lateinit var remoteAuthenticationDataSource: MongoAuthenticationDataSource
    private val ids = List(6) { Uuid.random() }
    private val user = User(ids[0], "test", UserRole.USER, authMethod = User.AuthenticationMethod.Password(""))
    private val userDTO = user.toUserDTO()

    @BeforeEach
    fun setup() {
        mongoCollection = mockk(relaxed = true)
        remoteAuthenticationDataSource = MongoAuthenticationDataSource(mongoCollection)
    }

    @Test
    fun `saveUser should insert the user into the user collection when called`() {
        val findPublisher = mockk<Publisher<InsertOneResult>>()
        every { mongoCollection.insertOne(userDTO) } returns findPublisher

        remoteAuthenticationDataSource.saveUser(user).blockingSubscribe()

        verify(exactly = 1) { mongoCollection.insertOne(userDTO) }
    }

    @Test
    fun `saveUser should throw CreationItemFailedException when happen incorrect configuration`() {
        val findPublisher = mockk<FindPublisher<UserDTO>>()
        every { mongoCollection.find() } returns findPublisher
        every { findPublisher.first() } returns Flowable.error(MongoClientException(""))

        assertThrows<MongoClientException> {
            remoteAuthenticationDataSource.saveUser(user).blockingSubscribe()
        }
    }

    @Test
    fun `getAllUsers should return all users when get all from the user collection`() {
        val findPublisher = mockk<FindPublisher<UserDTO>>()
        every { mongoCollection.find() } returns findPublisher
        every { findPublisher.first() } returns Flowable.just(userDTO)

        val users = remoteAuthenticationDataSource.getAllUsers().blockingGet()

        assertThat(users).containsExactly(user)
        verify(exactly = 1) { mongoCollection.find() }
    }

    @Test
    fun `getAllUsers should throw MongoTimeoutException when a connection or operation exceeds its time limit`() {
        val findPublisher = mockk<FindPublisher<UserDTO>>()
        every { mongoCollection.find() } returns findPublisher
        every { findPublisher.first() } returns Flowable.error(MongoTimeoutException(""))

        assertThrows<MongoTimeoutException> {
            remoteAuthenticationDataSource.getAllUsers().blockingGet()
        }
    }

    @Test
    fun `logout should set the current user to null`() {
        remoteAuthenticationDataSource.logout().blockingSubscribe()
        val currentUser = remoteAuthenticationDataSource.getCurrentUser()
        assertThat(currentUser).isNull()
    }
}
