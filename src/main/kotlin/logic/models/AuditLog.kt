package org.example.logic.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class AuditLog(
    val id: Uuid = Uuid.random(),
    val createdAt: Instant = Clock.System.now(),
    val userId: Uuid,
    val userName: String,
    val entityId: Uuid,
    val entityType: EntityType,
    val entityName: String,
    val actionType: ActionType,
    val fieldChange: FieldChange? = null,
) {
    data class FieldChange(
        val fieldName: String,
        val oldValue: String,
        val newValue: String,
    ) {
        companion object {
            fun Project.detectChanges(oldProject: Project): List<FieldChange> {
                val changes = mutableListOf<FieldChange>()
                if (this.name != oldProject.name) {
                    changes.add(FieldChange("name", oldProject.name, this.name))
                }
                return changes
            }

            fun Task.detectChanges(oldTask: Task): List<FieldChange> {
                val changes = mutableListOf<FieldChange>()
                if (this.name != oldTask.name) {
                    changes.add(FieldChange("name", oldTask.name, this.name))
                }

                if (this.stateId != oldTask.stateId) {
                    changes.add(FieldChange("state", oldTask.stateId.toHexString(), this.stateId.toHexString()))
                }
                return changes
            }
        }
    }

    enum class ActionType {
        CREATE,
        UPDATE,
        DELETE,
    }

    enum class EntityType {
        TASK,
        PROJECT,
    }
}
