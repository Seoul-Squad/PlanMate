package org.example.data.source.remote.mongo

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import data.source.remote.mongo.utils.AuthenticationMethodDtoCodec
import io.github.cdimascio.dotenv.dotenv
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.example.data.source.remote.models.*
import org.example.data.utils.Constants
import org.example.data.utils.Constants.CollectionNames.AUDIT_LOGS_DOCUMENTATION
import org.example.data.utils.Constants.CollectionNames.PROJECTS_DOCUMENTATION
import org.example.data.utils.Constants.CollectionNames.STATE_DOCUMENTATION
import org.example.data.utils.Constants.CollectionNames.TASKS_DOCUMENTATION
import org.example.data.utils.Constants.CollectionNames.USERS_DOCUMENTATION
import org.example.data.utils.Constants.MONGODB_URI
import org.example.logic.utils.DataBaseException

object PlanMateDataBase {
    private val uri: String = dotenv()[MONGODB_URI] ?: throw DataBaseException()

    private val codecRegistry: CodecRegistry =
        CodecRegistries.fromRegistries(
            CodecRegistries.fromCodecs(AuthenticationMethodDtoCodec()),
            MongoClientSettings.getDefaultCodecRegistry(),
        )

    private val settings =
        MongoClientSettings
            .builder()
            .applyConnectionString(ConnectionString(uri))
            .codecRegistry(codecRegistry)
            .build()

    private val clientRx = MongoClients.create(settings)
    private val databaseRx = clientRx.getDatabase(Constants.DATABASE_NAME)

    val projectDoc: MongoCollection<ProjectDTO> =
        databaseRx.getCollection(PROJECTS_DOCUMENTATION, ProjectDTO::class.java)
    val taskDoc: MongoCollection<TaskDTO> = databaseRx.getCollection(TASKS_DOCUMENTATION, TaskDTO::class.java)
    val userDoc: MongoCollection<UserDTO> = databaseRx.getCollection(USERS_DOCUMENTATION, UserDTO::class.java)
    val auditLogDoc: MongoCollection<AuditLogDTO> = databaseRx.getCollection(AUDIT_LOGS_DOCUMENTATION, AuditLogDTO::class.java)
    val stateDoc: MongoCollection<ProjectStateDTO> = databaseRx.getCollection(STATE_DOCUMENTATION, ProjectStateDTO::class.java)
}
