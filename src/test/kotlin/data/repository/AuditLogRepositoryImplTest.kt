package org.example.data.repository

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.datetime.Instant
import org.example.data.repository.sources.remote.RemoteAuditLogDataSource
import org.example.logic.models.AuditLog
import org.example.logic.utils.AuditLogCreationFailedException
import org.example.logic.utils.AuditLogDeletionFailedException
import org.example.logic.utils.AuditLogNotFoundException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class AuditLogRepositoryImplTest {
    private val remoteAuditLogDataSource = mockk<RemoteAuditLogDataSource>()
    private val repository = AuditLogRepositoryImpl(remoteAuditLogDataSource)

    private val fixedInstant = Instant.parse("2025-05-12T00:00:00Z")
    private val dummyLog =
        AuditLog(
            id = Uuid.random(),
            createdAt = fixedInstant,
            userId = Uuid.random(),
            userName = "Sarah",
            entityId = Uuid.random(),
            entityType = AuditLog.EntityType.TASK,
            entityName = "Task #123",
            actionType = AuditLog.ActionType.CREATE,
            fieldChange =
                AuditLog.FieldChange(
                    fieldName = "status",
                    oldValue = "pending",
                    newValue = "completed",
                ),
        )

    @Test
    fun `should return audit log when createAuditLog succeeds`() {
        every { remoteAuditLogDataSource.saveAuditLog(dummyLog) } returns Single.just(dummyLog)

        val testObserver = repository.createAuditLog(dummyLog).test()

        testObserver.assertComplete()
        testObserver.assertValue(dummyLog)

        verify { remoteAuditLogDataSource.saveAuditLog(dummyLog) }
    }

    @Test
    fun `should throw AuditLogCreationFailedException when createAuditLog fails`() {
        every { remoteAuditLogDataSource.saveAuditLog(dummyLog) } returns Single.error(RuntimeException())

        assertThrows(AuditLogCreationFailedException::class.java) {
            repository.createAuditLog(dummyLog).blockingGet()
        }
    }

    @Test
    fun `should complete successfully when deleteAuditLog succeeds`() {
        every { remoteAuditLogDataSource.deleteAuditLog(dummyLog.id) } returns Completable.complete()

        val testObserver = repository.deleteAuditLog(dummyLog.id).test()

        testObserver.assertComplete()
        verify { remoteAuditLogDataSource.deleteAuditLog(dummyLog.id) }
    }

    @Test
    fun `should throw AuditLogDeletionFailedException when deleteAuditLog fails`() {
        every { remoteAuditLogDataSource.deleteAuditLog(dummyLog.id) } returns Completable.error(RuntimeException())

        assertThrows(AuditLogDeletionFailedException::class.java) {
            repository.deleteAuditLog(dummyLog.id).blockingAwait()
        }
    }

    @Test
    fun `should return entity logs when getEntityLogs succeeds`() {
        val expectedLogs = listOf(dummyLog)
        every {
            remoteAuditLogDataSource.getEntityLogs(dummyLog.entityId, dummyLog.entityType)
        } returns Single.just(expectedLogs)

        val testObserver = repository.getEntityLogs(dummyLog.entityId, dummyLog.entityType).test()

        testObserver.assertComplete()
        testObserver.assertValue(expectedLogs)

        verify { remoteAuditLogDataSource.getEntityLogs(dummyLog.entityId, dummyLog.entityType) }
    }

    @Test
    fun `should throw AuditLogNotFoundException when getEntityLogs fails`() {
        every {
            remoteAuditLogDataSource.getEntityLogs(dummyLog.entityId, dummyLog.entityType)
        } returns Single.error(RuntimeException())

        assertThrows(AuditLogNotFoundException::class.java) {
            repository.getEntityLogs(dummyLog.entityId, dummyLog.entityType).blockingGet()
        }
    }

    @Test
    fun `should return audit log when getEntityLogByLogId succeeds`() {
        every { remoteAuditLogDataSource.getEntityLogByLogId(dummyLog.id) } returns Single.just(dummyLog)

        val testObserver = repository.getEntityLogByLogId(dummyLog.id).test()

        testObserver.assertComplete()
        testObserver.assertValue(dummyLog)

        verify { remoteAuditLogDataSource.getEntityLogByLogId(dummyLog.id) }
    }

    @Test
    fun `should throw AuditLogNotFoundException when getEntityLogByLogId fails`() {
        every { remoteAuditLogDataSource.getEntityLogByLogId(dummyLog.id) } returns Single.error(RuntimeException())

        assertThrows(AuditLogNotFoundException::class.java) {
            repository.getEntityLogByLogId(dummyLog.id).blockingGet()
        }
    }
}
