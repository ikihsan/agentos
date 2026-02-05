package dev.agentos.memory.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Main Room database for Agent OS.
 */
@Database(
    entities = [
        TaskEntity::class,
        MessageEntity::class,
        UserPreferenceEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AgentDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    
    abstract fun messageDao(): MessageDao
    
    abstract fun userPreferenceDao(): UserPreferenceDao
    
    companion object {
        const val DATABASE_NAME = "agent_os_db"
    }
}
