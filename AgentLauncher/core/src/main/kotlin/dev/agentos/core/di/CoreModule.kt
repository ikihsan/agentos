package dev.agentos.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.agentos.core.common.DefaultDispatcherProvider
import dev.agentos.core.common.DispatcherProvider
import javax.inject.Singleton

/**
 * Hilt module for core dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CoreModule {

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(
        impl: DefaultDispatcherProvider
    ): DispatcherProvider
}
