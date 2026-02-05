package dev.agentos.llm.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit interface for OpenAI API.
 */
interface OpenAiApi {

    @POST("v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}

@Serializable
data class ChatCompletionRequest(
    val model: String = "gpt-4o",
    val messages: List<OpenAiMessage>,
    val temperature: Float = 0.7f,
    @SerialName("max_tokens")
    val maxTokens: Int = 2000,
    @SerialName("response_format")
    val responseFormat: ResponseFormat? = null
)

@Serializable
data class OpenAiMessage(
    val role: String,
    val content: String
)

@Serializable
data class ResponseFormat(
    val type: String = "json_object"
)

@Serializable
data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val index: Int,
    val message: OpenAiMessage,
    @SerialName("finish_reason")
    val finishReason: String?
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int
)
