package org.example.data.source.remote.mongo

import com.mongodb.*


suspend fun <T> executeMongoOperation(
    operation: suspend () -> T,
    errorMessage: String,
    exceptionFactory: (String) -> Exception ): T {
    return try {
        operation()
    }catch (e:MongoCommandException){
        throw Exception("Database command exception")
    } catch (e: MongoSecurityException) {
        throw Exception("Database security exception")
    }catch (e: MongoSocketOpenException){
        throw Exception("Database connection exception")
    }catch (e: MongoTimeoutException) {
        throw Exception("Database timeout")
    }catch (e: MongoWriteException) {
        throw Exception("Database write exception")
    } catch (e: MongoClientException) {
        throw Exception("Database client exception")
    } catch (e:Exception){
        throw exceptionFactory(errorMessage)
    }

}