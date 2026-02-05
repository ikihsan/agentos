package dev.agentos.core.common

/**
 * Sealed class representing the result of an operation.
 * Used throughout the application for consistent error handling.
 */
sealed class Result<out T> {
    /**
     * Represents a successful result with data.
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents a failed result with error information.
     */
    data class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "Unknown error"
    ) : Result<Nothing>()

    /**
     * Represents a loading state.
     */
    data object Loading : Result<Nothing>()

    /**
     * Returns true if this is a Success result.
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Returns true if this is an Error result.
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Returns the success data or null if this is not a Success.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Returns the success data or throws the exception if this is an Error.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is still loading")
    }

    /**
     * Returns the success data or a default value.
     */
    fun getOrDefault(default: T): T = when (this) {
        is Success -> data
        else -> default
    }

    /**
     * Returns the success data or computes a default from the error.
     */
    inline fun getOrElse(onError: (Throwable) -> T): T = when (this) {
        is Success -> data
        is Error -> onError(exception)
        is Loading -> throw IllegalStateException("Result is still loading")
    }

    /**
     * Transforms the success value using the given function.
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }

    /**
     * Transforms the success value using the given function that returns a Result.
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> this
    }

    /**
     * Performs the given action on the success value.
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Performs the given action on the error.
     */
    inline fun onError(action: (Throwable) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }

    /**
     * Performs the given action regardless of the result.
     */
    inline fun onComplete(action: () -> Unit): Result<T> {
        if (this !is Loading) action()
        return this
    }

    companion object {
        /**
         * Creates a Success result.
         */
        fun <T> success(data: T): Result<T> = Success(data)

        /**
         * Creates an Error result from an exception.
         */
        fun error(exception: Throwable): Result<Nothing> = Error(exception)

        /**
         * Creates an Error result from a message.
         */
        fun error(message: String): Result<Nothing> = Error(Exception(message), message)

        /**
         * Returns Loading state.
         */
        fun loading(): Result<Nothing> = Loading

        /**
         * Runs the given block and wraps the result.
         */
        inline fun <T> runCatching(block: () -> T): Result<T> = try {
            Success(block())
        } catch (e: Throwable) {
            Error(e)
        }

        /**
         * Runs the given suspending block and wraps the result.
         */
        suspend inline fun <T> runCatchingSuspend(crossinline block: suspend () -> T): Result<T> = try {
            Success(block())
        } catch (e: Throwable) {
            Error(e)
        }
    }
}

/**
 * Extension to convert Kotlin's Result to our Result type.
 */
fun <T> kotlin.Result<T>.toResult(): Result<T> = fold(
    onSuccess = { Result.Success(it) },
    onFailure = { Result.Error(it) }
)
