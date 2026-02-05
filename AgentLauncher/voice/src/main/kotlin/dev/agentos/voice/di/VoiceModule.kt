package dev.agentos.voice.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.agentos.voice.data.AndroidTextToSpeechManager
import dev.agentos.voice.data.AndroidVoiceInputManager
import dev.agentos.voice.domain.TextToSpeechManager
import dev.agentos.voice.domain.VoiceInputManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceModule {

    @Binds
    @Singleton
    abstract fun bindVoiceInputManager(
        impl: AndroidVoiceInputManager
    ): VoiceInputManager

    @Binds
    @Singleton
    abstract fun bindTextToSpeechManager(
        impl: AndroidTextToSpeechManager
    ): TextToSpeechManager
}
