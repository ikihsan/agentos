package dev.agentos.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Extension functions for Flow operations.
 */

/**
 * Maps a Flow to emit Result types with error handling.
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> = this
    .map<T, Result<T>> { Result.Success(it) }
    .onStart { emit(Result.Loading) }
    .catch { emit(Result.Error(it)) }

/**
 * Catches exceptions and emits an error result.
 */
fun <T> Flow<Result<T>>.catchAsError(): Flow<Result<T>> = catch { 
    emit(Result.Error(it)) 
}
