package org.example.data.source.remote.mongo.utils

import com.mongodb.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

suspend fun <T> executeMongoOperation(operation: suspend () -> T): T =
    try {
        operation()
    } catch (mongoCommandException: MongoCommandException) {
        throw mongoCommandException
    } catch (mongoSecurityException: MongoSecurityException) {
        throw mongoSecurityException
    } catch (mongoSocketOpenException: MongoSocketOpenException) {
        throw mongoSocketOpenException
    } catch (mongoTimeoutException: MongoTimeoutException) {
        throw mongoTimeoutException
    } catch (mongoWriteException: MongoWriteException) {
        throw mongoWriteException
    } catch (mongoClientException: MongoClientException) {
        throw mongoClientException
    } catch (mongoBulkWriteException: MongoBulkWriteException) {
        throw mongoBulkWriteException
    } catch (mongoInterruptedException: MongoInterruptedException) {
        throw mongoInterruptedException
    } catch (mongoExecutionTimeoutException: MongoExecutionTimeoutException) {
        throw mongoExecutionTimeoutException
    } catch (mongoNodeIsRecoveringException: MongoNodeIsRecoveringException) {
        throw mongoNodeIsRecoveringException
    } catch (mongoNotPrimaryException: MongoNotPrimaryException) {
        throw mongoNotPrimaryException
    } catch (unexpectedException: Exception) {
        throw unexpectedException
    }

inline fun <reified T : Any> executeMongoOperationRx(crossinline operation: () -> T): T {
    val source = operation()

    return when (source) {
        is Single<*> ->
            source.onErrorResumeNext { error ->
                Single.error(mapMongoError(error))
            } as T

        is Observable<*> ->
            source.onErrorResumeNext { error ->
                Observable.error(mapMongoError(error))
            } as T

        is Maybe<*> ->
            source.onErrorResumeNext { error ->
                Maybe.error(mapMongoError(error))
            } as T

        is Completable ->
            source.onErrorResumeNext { error ->
                Completable.error(mapMongoError(error))
            } as T

        else -> throw IllegalArgumentException("Unsupported RxJava type: ${T::class}")
    }
}

fun mapMongoError(exception: Throwable): Throwable =
    when (exception) {
        is MongoCommandException,
        is MongoSecurityException,
        is MongoSocketOpenException,
        is MongoTimeoutException,
        is MongoWriteException,
        is MongoClientException,
        is MongoBulkWriteException,
        is MongoInterruptedException,
        is MongoExecutionTimeoutException,
        is MongoNodeIsRecoveringException,
        is MongoNotPrimaryException,
        -> exception

        else -> Exception("Unknown Mongo exception", exception)
    }
