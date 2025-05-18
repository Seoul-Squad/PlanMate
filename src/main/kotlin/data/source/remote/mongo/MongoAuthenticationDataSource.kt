package org.example.data.source.remote.mongo

import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.MongoCollection
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.example.data.repository.sources.remote.RemoteAuthenticationDataSource
import org.example.data.source.remote.models.UserDTO
import org.example.data.source.remote.mongo.utils.mapper.toUser
import org.example.data.source.remote.mongo.utils.mapper.toUserDTO
import org.example.data.utils.Constants.AUTH_TYPE_FIELD
import org.example.data.utils.Constants.PASSWORD_FIELD
import org.example.data.utils.Constants.USERNAME_FIELD
import org.example.logic.models.User
import org.example.logic.utils.UserAlreadyExistsException
import org.example.logic.utils.UserNotFoundException

class MongoAuthenticationDataSource(
    private val mongoClient: MongoCollection<UserDTO>,
) : RemoteAuthenticationDataSource {
    private var currentUser: User? = null

    override fun saveUser(user: User): Single<User> =
        Observable
            .fromPublisher(
                mongoClient.find(Filters.eq(USERNAME_FIELD, user.username)).limit(1),
            ).isEmpty
            .flatMap { isEmpty ->
                if (!isEmpty) {
                    Single.error(UserAlreadyExistsException())
                } else {
                    Single.fromPublisher(mongoClient.insertOne(user.toUserDTO())).map { user }
                }
            }

    override fun getAllUsers(): Single<List<User>> =
        Flowable
            .fromPublisher(mongoClient.find())
            .map { userDTO ->
                userDTO.toUser()
            }.toList()

    override fun loginWithPassword(
        username: String,
        hashedPassword: String,
    ): Single<User> {
        val publisher =
            mongoClient
                .find(
                    Filters.and(
                        Filters.eq(USERNAME_FIELD, username),
                        Filters.eq(AUTH_TYPE_FIELD, "password"),
                        Filters.eq(PASSWORD_FIELD, hashedPassword),
                    ),
                ).first()

        return Single
            .fromPublisher(publisher)
            .map { userDTO ->
                currentUser = userDTO.toUser()
                currentUser!!
            }
    }

    override fun logout(): Completable = Completable.fromAction { currentUser = null }

    override fun getCurrentUser(): Single<User> =
        Single.create { emitter ->
            if (currentUser != null) {
                emitter.onSuccess(currentUser!!)
            } else {
                emitter.onError(UserNotFoundException())
            }
        }
}
