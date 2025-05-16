package data.source.remote.mongo

import com.google.common.truth.Truth.assertThat
import com.mongodb.MongoClientException
import com.mongodb.MongoException
import com.mongodb.MongoTimeoutException
import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.MongoCollection
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.data.repository.sources.remote.RemoteAuditLogDataSource
import org.example.data.source.remote.models.AuditLogDTO
import org.example.data.source.remote.mongo.MongoAuditLogDataSource
import org.example.data.source.remote.mongo.utils.mapper.toAuditLogDTO
import org.example.data.utils.Constants.ENTITY_ID
import org.example.data.utils.Constants.ENTITY_TYPE
import org.example.data.utils.Constants.ID
import org.example.logic.models.AuditLog
import org.example.logic.models.AuditLog.ActionType
import org.example.logic.models.AuditLog.EntityType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
class MongoAuditLogDataSourceTest {
    private lateinit var mongoClientCollection: MongoCollection<AuditLogDTO>
    private lateinit var remoteAuditLogDataSource: RemoteAuditLogDataSource

    private val currentTime =
        kotlinx.datetime.Clock.System
            .now()
    private val testAuditLogs =
        listOf(
            AuditLog(
                id = Uuid.random(),
                userId = Uuid.random(),
                userName = "User1",
                createdAt = currentTime,
                entityType = EntityType.PROJECT,
                entityId = Uuid.random(),
                entityName = "Entity1",
                actionType = ActionType.CREATE,
            ),
            AuditLog(
                id = Uuid.random(),
                userId = Uuid.random(),
                userName = "User2",
                createdAt = currentTime,
                entityType = EntityType.TASK,
                entityId = Uuid.random(),
                entityName = "Entity2",
                actionType = ActionType.UPDATE,
            ),
        )

    private val testAuditLogDTOs = testAuditLogs.map { it.toAuditLogDTO() }

    private val newAuditLog =
        AuditLog(
            id = Uuid.random(),
            userId = Uuid.random(),
            userName = "User3",
            createdAt = currentTime,
            entityType = EntityType.PROJECT,
            entityId = Uuid.random(),
            entityName = "Entity3",
            actionType = ActionType.DELETE,
        )
    private val newAuditLogDTO = newAuditLog.toAuditLogDTO()

    @BeforeEach
    fun setUp() {
        mongoClientCollection = mockk(relaxed = true)
        remoteAuditLogDataSource = MongoAuditLogDataSource(mongoClientCollection)
    }

    @Test
    fun `getEntityLogs should return list of AuditLog when try to get audit logs from MongoDB`() {
        every { mongoClientCollection.find() } returns mockk()

        val testObserver = remoteAuditLogDataSource.getEntityLogs(Uuid.random(), EntityType.PROJECT).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()

        verify(exactly = 1) { mongoClientCollection.find() }
    }

    @Test
    fun `saveAuditLog should return audit log that created when create audit log at MongoDB`() {
        every { mongoClientCollection.insertOne(newAuditLogDTO, any()) } returns mockk()

        val testObserver = remoteAuditLogDataSource.saveAuditLog(newAuditLog).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue(newAuditLog)

        verify(exactly = 1) { mongoClientCollection.insertOne(newAuditLogDTO, any()) }
    }

    @Test
    fun `saveAuditLog should throw MongoClientException when happen incorrect configuration`() {
        every { mongoClientCollection.insertOne(newAuditLogDTO, any()) } throws MongoClientException("Error")

        assertThrows<MongoClientException> {
            remoteAuditLogDataSource.saveAuditLog(newAuditLog).blockingGet()
        }
    }

    @Test
    fun `getEntityLogByLogId should return audit log when get audit log by Id from MongoDB`() {
        every { mongoClientCollection.find() } returns mockk()

        val testObserver = remoteAuditLogDataSource.getEntityLogByLogId(Uuid.random()).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()

        verify(exactly = 1) { mongoClientCollection.find() }
    }

    @Test
    fun `getEntityLogByLogId should throw MongoClientException when happen incorrect configuration`() {
        every { mongoClientCollection.find() } throws MongoClientException("Error")

        val error =
            assertThrows<MongoClientException> {
                remoteAuditLogDataSource.getEntityLogByLogId(Uuid.random()).blockingGet()
            }
        assertThat(error).hasMessageThat().contains("Error")
    }

    @Test
    fun `deleteAuditLog should delete audit log when delete audit log from MongoDB`() {
        every { mongoClientCollection.deleteOne(Filters.eq("", "")) } returns mockk()

        val testObserver = remoteAuditLogDataSource.deleteAuditLog(Uuid.random()).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()

        verify(exactly = 1) { mongoClientCollection.deleteOne(Filters.eq("", "")) }
    }

    @Test
    fun `deleteAuditLog should throw MongoTimeoutException when a connection or operation exceeds its time limit`() {
        every { mongoClientCollection.deleteOne(Filters.eq("", "")) } throws MongoTimeoutException("Timeout")

        val error =
            assertThrows<MongoTimeoutException> {
                remoteAuditLogDataSource.deleteAuditLog(Uuid.random()).blockingSubscribe()
            }
        assertThat(error).hasMessageThat().contains("Timeout")
    }

    @Test
    fun `getEntityLogByLogId should return null when log doesn't exist`() {
        val auditLogId = Uuid.random()

        every {
            mongoClientCollection.find(
                Filters.eq(ID, auditLogId.toHexString()),
            )
        } returns mockk()

        val testObserver = remoteAuditLogDataSource.getEntityLogByLogId(auditLogId).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue { it == null }

        verify(exactly = 1) {
            mongoClientCollection.find(
                Filters.eq(ID, auditLogId.toHexString()),
            )
        }
    }

    @Test
    fun `getEntityLogs should throws MongoClientException when`() {
        val entityId = Uuid.random()
        val entityType = EntityType.PROJECT

        every { mongoClientCollection.find() } throws MongoClientException("Error")

        assertThrows<MongoClientException> {
            remoteAuditLogDataSource.getEntityLogs(entityId, entityType).blockingGet()
        }
    }

    @Test
    fun `getEntityLogByLogId should throw MongoDB exceptions`() {
        val auditLogId = Uuid.random()

        every { mongoClientCollection.find() } throws MongoClientException("Error")

        assertThrows<MongoException> {
            remoteAuditLogDataSource.getEntityLogByLogId(auditLogId).blockingGet()
        }
    }

    @Test
    fun `getEntityLogByLogId should return audit log when exists`() {
        val auditLogId = Uuid.random()

        every {
            mongoClientCollection.find(
                Filters.eq(ID, auditLogId.toHexString()),
            )
        } returns mockk()

        val testObserver = remoteAuditLogDataSource.getEntityLogByLogId(auditLogId).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.id.toHexString() == newAuditLogDTO.id
        }

        verify(exactly = 1) {
            mongoClientCollection.find(
                Filters.eq(ID, auditLogId.toHexString()),
            )
        }
    }

    @Test
    fun `getEntityLogs should return empty list when no logs exist`() {
        val entityId = Uuid.random()
        val entityType = EntityType.PROJECT

        every {
            mongoClientCollection.find(
                Filters.and(
                    Filters.eq(ENTITY_ID, entityId.toHexString()),
                    Filters.eq(ENTITY_TYPE, entityType.name),
                ),
            )
        } returns mockk()

        val testObserver = remoteAuditLogDataSource.getEntityLogs(entityId, entityType).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue { it.isEmpty() }

        verify(exactly = 1) {
            mongoClientCollection.find(
                Filters.and(
                    Filters.eq(ENTITY_ID, entityId.toHexString()),
                    Filters.eq(ENTITY_TYPE, entityType.name),
                ),
            )
        }
    }

    @Test
    fun `getEntityLogs should return list of audit logs`() {
        val entityId = Uuid.random()
        val entityType = EntityType.PROJECT

        every {
            mongoClientCollection.find(
                Filters.and(
                    Filters.eq(ENTITY_ID, entityId.toHexString()),
                    Filters.eq(ENTITY_TYPE, entityType.name),
                ),
            )
        } returns mockk()

        val testObserver = remoteAuditLogDataSource.getEntityLogs(entityId, entityType).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue { list ->
            list.size == 1 && list[0].id.toHexString() == newAuditLogDTO.id
        }

        verify(exactly = 1) {
            mongoClientCollection.find(
                Filters.and(
                    Filters.eq(ENTITY_ID, entityId.toHexString()),
                    Filters.eq(ENTITY_TYPE, entityType.name),
                ),
            )
        }
    }
}
