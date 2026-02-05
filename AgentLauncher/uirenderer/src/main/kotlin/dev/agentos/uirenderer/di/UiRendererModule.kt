package dev.agentos.uirenderer.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UiRendererModule {
    // No dependencies to provide - all UI rendering is done via Composables
    // This module exists for future extensibility
}
