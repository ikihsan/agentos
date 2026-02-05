package dev.agentos.memory.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.agentos.memory.data.db.*
import dev.agentos.memory.data.repository.*
import dev.agentos.memory.domain.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MemoryDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AgentDatabase {
        return Room.databaseBuilder(
            context,
            AgentDatabase::class.java,
            AgentDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTaskDao(database: AgentDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    fun provideMessageDao(database: AgentDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    fun provideUserPreferenceDao(database: AgentDatabase): UserPreferenceDao {
        return database.userPreferenceDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MemoryRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPersistentTaskRepository(
        impl: RoomTaskRepository
    ): PersistentTaskRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        impl: RoomMessageRepository
    ): MessageRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferenceRepository(
        impl: RoomUserPreferenceRepository
    ): UserPreferenceRepository
}
