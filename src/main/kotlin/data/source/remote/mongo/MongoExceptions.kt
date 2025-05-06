package org.example.data.source.remote.mongo

import com.mongodb.*

class MongoExceptionHandler {
    companion object {
        private const val COMMAND_EXCEPTION_MESSAGE = "Database command exception"
        private const val SECURITY_EXCEPTION_MESSAGE = "Database security exception"
        private  const val SOCKET_OPEN_EXCEPTION_MESSAGE = "Database connection exception"
        private  const val TIMEOUT_EXCEPTION_MESSAGE = "Database timeout"
        private  const val WRITE_EXCEPTION_MESSAGE = "Database write exception"
        private  const val CLIENT_EXCEPTION_MESSAGE = "Database client exception"


        suspend fun <T> handleMongoExceptions(
            operation: suspend () -> T,
            errorMessage: String,
            exceptionFactory: (String) -> Exception
        ): T {
            return try {
                operation()
            } catch (e: MongoCommandException) {
                throw Exception(COMMAND_EXCEPTION_MESSAGE)
            } catch (e: MongoSecurityException) {
                throw Exception(SECURITY_EXCEPTION_MESSAGE)
            } catch (e: MongoSocketOpenException) {
                throw Exception(SOCKET_OPEN_EXCEPTION_MESSAGE)
            } catch (e: MongoTimeoutException) {
                throw Exception(TIMEOUT_EXCEPTION_MESSAGE)
            } catch (e: MongoWriteException) {
                throw Exception(WRITE_EXCEPTION_MESSAGE)
            } catch (e: MongoClientException) {
                throw Exception(CLIENT_EXCEPTION_MESSAGE)
            } catch (e: Exception) {
                throw exceptionFactory(errorMessage)
            }
        }
    }
}