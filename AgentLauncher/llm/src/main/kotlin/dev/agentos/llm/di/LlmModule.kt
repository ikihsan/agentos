package dev.agentos.llm.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.agentos.llm.BuildConfig
import dev.agentos.llm.data.ApiKeyProvider
import dev.agentos.llm.data.OpenAiLlmClient
import dev.agentos.llm.data.api.OpenAiApi
import dev.agentos.llm.domain.LlmClient
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LlmModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenAiApi(
        okHttpClient: OkHttpClient,
        json: Json
    ): OpenAiApi {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OpenAiApi::class.java)
    }

    @Provides
    @Singleton
    fun provideApiKeyProvider(): ApiKeyProvider = object : ApiKeyProvider {
        override val openAiKey: String
            get() = BuildConfig.OPENAI_API_KEY
        override val anthropicKey: String
            get() = BuildConfig.ANTHROPIC_API_KEY
    }

    @Provides
    @Singleton
    fun provideLlmClient(impl: OpenAiLlmClient): LlmClient = impl
}
