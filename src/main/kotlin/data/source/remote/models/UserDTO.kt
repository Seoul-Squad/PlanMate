@file:OptIn(ExperimentalSerializationApi::class)

package org.example.data.source.remote.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class UserDTO(
    @SerialName("_id") val id: String,
    val username: String,
    val role: String,
    val authMethod: AuthenticationMethodDto,
) {
    @Serializable
    @JsonClassDiscriminator("type")
    sealed class AuthenticationMethodDto {
        @Serializable
        @SerialName("password")
        data class Password(
            val password: String,
        ) : AuthenticationMethodDto()
    }
}
