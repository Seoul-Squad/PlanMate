package org.example.data.source.local.csv

import org.example.data.repository.sources.local.LocalTaskDataSource
import org.example.data.source.local.csv.utils.CSVReader
import org.example.data.source.local.csv.utils.CSVWriter
import org.example.data.source.local.csv.utils.mapper.toCsvLines
import org.example.data.source.local.csv.utils.mapper.toTasks
import org.example.logic.models.Task
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class CsvTaskDataSource(
    private val csvReader: CSVReader,
    private val csvWriter: CSVWriter,
) : LocalTaskDataSource {
    private var tasks = mutableListOf<Task>()

    init {
        readCsvTasks()
    }

    override fun createTask(task: Task): Task {
        tasks.add(task)
        writeCsvTasks()
        return task
    }

    override fun updateTask(updatedTask: Task): Task {
        tasks =
            tasks
                .map { task ->
                    if (task.id == updatedTask.id) {
                        updatedTask
                    } else {
                        task
                    }
                }.toMutableList()
        writeCsvTasks()
        return updatedTask
    }

    override fun deleteTask(taskId: String) {
        tasks.removeIf { it.id.toHexString() == taskId }
        writeCsvTasks()
    }

    override fun getAllTasks(): List<Task> = tasks

    override fun getTaskById(taskId: String): Task? = tasks.firstOrNull { it.id.toHexString() == taskId }

    override fun deleteTasksByStateId(
        stateId: String,
        projectId: String,
    ) {
        tasks.removeIf { it.stateId.toHexString() == stateId && it.projectId.toHexString() == projectId }
        writeCsvTasks()
    }

    private fun readCsvTasks() {
        csvReader.readLines().toTasks().let { updatedTasks ->
            tasks = updatedTasks.toMutableList()
        }
    }

    private fun writeCsvTasks() {
        csvWriter.writeLines(
            tasks.toCsvLines(),
        )
        readCsvTasks()
    }
}
