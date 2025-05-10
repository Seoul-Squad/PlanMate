package org.example.data.source.remote.mongo.utils.mapper

import org.example.data.source.remote.models.StateDTO
import org.example.logic.models.State
import org.example.logic.utils.toUuid
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
fun StateDTO.toState(): State = State(id = id.toUuid(), title = title)

@OptIn(ExperimentalUuidApi::class)
fun State.toStateDTO(): StateDTO = StateDTO(id = id.toHexString(), title = title)
