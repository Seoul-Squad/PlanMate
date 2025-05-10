package org.example.data.source.local.csv

import org.example.data.repository.sources.local.LocalAuditLogDataSource
import org.example.data.source.local.csv.utils.CSVReader
import org.example.data.source.local.csv.utils.CSVWriter
import org.example.data.source.local.csv.utils.mapper.toAuditLogs
import org.example.data.source.local.csv.utils.mapper.toCsvRows
import org.example.logic.models.AuditLog
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CsvAuditLogDataSource(
    private val csvReader: CSVReader,
    private val csvWriter: CSVWriter,
) : LocalAuditLogDataSource {
    private var audits = mutableListOf<AuditLog>()

    init {
        readCsvAuditLogs()
    }

    override fun saveAuditLog(auditLog: AuditLog): AuditLog {
        audits.add(auditLog)
        writeCsvAuditLogs()
        return auditLog
    }

    override fun deleteAuditLog(auditLogId: Uuid) {
        audits.removeIf { it.id == auditLogId }
        writeCsvAuditLogs()
    }

    override fun getEntityLogs(
        entityId: Uuid,
        entityType: AuditLog.EntityType,
    ): List<AuditLog> =
        audits.filter {
            it.entityId == entityId && it.entityType == entityType
        }

    override fun getEntityLogByLogId(auditLogId: Uuid): AuditLog? = audits.firstOrNull { it.id == auditLogId }

    private fun readCsvAuditLogs() {
        csvReader.readLines().toAuditLogs().let { updatedTasks ->
            audits = updatedTasks.toMutableList()
        }
    }

    private fun writeCsvAuditLogs() {
        csvWriter.writeLines(
            audits.toCsvRows(),
        )
        readCsvAuditLogs()
    }
}
