package data.source.remote.mongo

import com.mongodb.MongoClientException
import com.mongodb.MongoTimeoutException
import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.data.source.remote.models.UserDTO
import org.example.data.source.remote.mongo.MongoAuthenticationDataSource
import org.example.data.source.remote.mongo.utils.mapper.toUserDTO
import org.example.logic.models.User
import org.example.logic.models.UserRole
import org.example.logic.utils.AuthenticationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.emptyFlow


@OptIn(ExperimentalUuidApi::class)
class MongoAuthenticationDataSourceTest {
    private lateinit var mongoCollection: MongoCollection<UserDTO>
    private lateinit var remoteAuthenticationDataSource: MongoAuthenticationDataSource
    private val ids = List(6) { Uuid.random() }
    @OptIn(ExperimentalUuidApi::class)
    private val user = User(ids[0], "test", UserRole.USER, authMethod = User.AuthenticationMethod.Password(""))
    private val userDTO = user.toUserDTO()

    @BeforeEach
    fun setup() {
        mongoCollection = mockk(relaxed = true)
        remoteAuthenticationDataSource = MongoAuthenticationDataSource(mongoCollection)
    }

    @Test
    fun `saveUser should insert the user into the user collection when called`() =
        runTest {
            remoteAuthenticationDataSource.saveUser(user)

            coVerify(exactly = 1) { mongoCollection.insertOne(userDTO, any()) }
        }

    @Test
    fun `saveUser should throw CreationItemFailedException when happen incorrect configuration`() =
        runTest {
            coEvery { mongoCollection.insertOne(userDTO, any()) } throws MongoClientException("Error")

            assertThrows<MongoClientException> {
                remoteAuthenticationDataSource.saveUser(user)
            }
        }

    @Test
    fun `getAllUsers should return all users when get all from the user collection`() =
        runTest {
            remoteAuthenticationDataSource.getAllUsers()

            coVerify(exactly = 1) { mongoCollection.find(filter = any()) }
        }

    @Test
    fun `getAllUsers should throw MongoTimeoutException when a connection or operation exceeds its time limit`() =
        runTest {
            coEvery { mongoCollection.find(filter = any()) } throws MongoTimeoutException("Timeout")

            assertThrows<MongoTimeoutException> {
                remoteAuthenticationDataSource.getAllUsers()
            }
        }

    @Test
    fun `logout should set the current user to null`() =
        runTest {
            remoteAuthenticationDataSource.logout()

            assert(remoteAuthenticationDataSource.getCurrentUser() == null)
        }




}
