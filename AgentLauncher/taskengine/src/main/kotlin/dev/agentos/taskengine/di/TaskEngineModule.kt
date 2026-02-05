package dev.agentos.taskengine.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.agentos.taskengine.data.InMemoryTaskRepository
import dev.agentos.taskengine.domain.TaskRepository
import javax.inject.Singleton

/**
 * Hilt module for task engine dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TaskEngineModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        impl: InMemoryTaskRepository
    ): TaskRepository
}
