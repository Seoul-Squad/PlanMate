package org.example.data.repository.mapper

import com.mongodb.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.example.logic.utils.DataBaseException
import org.example.logic.utils.UnauthorizedAccessException

suspend fun <T> mapExceptionsToDomainException(
    customException: Exception,
    operation: suspend () -> T,
): T {
    try {
        return operation()
    } catch (exception: Exception) {
        throw when (exception) {
            is MongoSocketOpenException,
            is MongoTimeoutException,
            is MongoExecutionTimeoutException,
            is MongoInterruptedException,
            is MongoNodeIsRecoveringException,
            -> DataBaseException()

            is MongoSecurityException -> UnauthorizedAccessException()

            else -> customException
        }
    }
}

inline fun <reified T : Any> mapExceptionsToDomainExceptionRx(
    customException: Exception,
    crossinline operation: () -> T,
): T {
    val source = operation()

    return when (source) {
        is Observable<*> ->
            source.onErrorResumeNext { error ->
                Observable.error(mapError(error, customException))
            } as T

        is Single<*> ->
            source.onErrorResumeNext { error ->
                Single.error(mapError(error, customException))
            } as T

        is Maybe<*> ->
            source.onErrorResumeNext { error ->
                Maybe.error(mapError(error, customException))
            } as T

        is Completable ->
            source.onErrorResumeNext { error ->
                Completable.error(mapError(error, customException))
            } as T

        else -> throw IllegalArgumentException("Unsupported RxJava type: ${T::class}")
    }
}

fun mapError(
    throwable: Throwable,
    fallback: Exception,
): Exception =
    when (throwable) {
        is MongoSocketOpenException,
        is MongoTimeoutException,
        is MongoExecutionTimeoutException,
        is MongoInterruptedException,
        is MongoNodeIsRecoveringException,
        -> DataBaseException()

        is MongoSecurityException -> UnauthorizedAccessException()

        else -> fallback
    }
