package io.realworld.android.utils

import android.util.Log
import kotlinx.coroutines.delay
import retrofit2.Response

private const val TAG = "RetryUtils"
private const val MAX_ATTEMPTS = 3
private val BACKOFF_DELAYS_MS = listOf(500L, 1000L, 2000L)

/**
 * Retries a Retrofit call up to [MAX_ATTEMPTS] times when the server returns a 5xx status.
 * Uses exponential backoff between attempts. Does not retry on 4xx errors.
 */
suspend fun <T> withRetry(block: suspend () -> Response<T>): Response<T> {
    var lastResponse: Response<T>? = null
    for (attempt in 0 until MAX_ATTEMPTS) {
        if (attempt > 0) {
            val delayMs = BACKOFF_DELAYS_MS[attempt - 1]
            Log.w(TAG, "Retrying after ${delayMs}ms (attempt ${attempt + 1}/$MAX_ATTEMPTS)")
            delay(delayMs)
        }
        val response = block()
        if (response.code() < 500) return response
        lastResponse = response
        Log.w(TAG, "Server returned ${response.code()}, will retry")
    }
    return lastResponse!!
}
