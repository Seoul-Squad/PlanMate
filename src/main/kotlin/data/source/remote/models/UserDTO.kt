package org.example.data.source.remote.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

@Serializable
data class UserDTO(
    @BsonId val id: String,
    val username: String,
    val password: String,
    val role: String,
)
