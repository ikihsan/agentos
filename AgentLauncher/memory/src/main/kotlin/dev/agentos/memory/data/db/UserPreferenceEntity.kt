package dev.agentos.memory.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room entity for user preferences learned from interactions.
 */
@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey
    val key: String,
    
    val value: String,
    
    val domain: String, // e.g., "contacts", "apps", "settings"
    
    @ColumnInfo(name = "confidence")
    val confidence: Float, // 0.0 to 1.0
    
    @ColumnInfo(name = "usage_count")
    val usageCount: Int,
    
    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)

/**
 * DAO for UserPreference operations.
 */
@Dao
interface UserPreferenceDao {
    
    @Query("SELECT * FROM user_preferences ORDER BY usage_count DESC")
    fun getAllPreferences(): Flow<List<UserPreferenceEntity>>
    
    @Query("SELECT * FROM user_preferences WHERE domain = :domain ORDER BY usage_count DESC")
    fun getPreferencesByDomain(domain: String): Flow<List<UserPreferenceEntity>>
    
    @Query("SELECT * FROM user_preferences WHERE `key` = :key")
    suspend fun getPreference(key: String): UserPreferenceEntity?
    
    @Query("SELECT * FROM user_preferences WHERE `key` LIKE :pattern ORDER BY usage_count DESC LIMIT :limit")
    suspend fun searchPreferences(pattern: String, limit: Int = 10): List<UserPreferenceEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preference: UserPreferenceEntity)
    
    @Update
    suspend fun update(preference: UserPreferenceEntity)
    
    @Query("UPDATE user_preferences SET usage_count = usage_count + 1, last_used_at = :timestamp WHERE `key` = :key")
    suspend fun incrementUsage(key: String, timestamp: Long)
    
    @Delete
    suspend fun delete(preference: UserPreferenceEntity)
    
    @Query("DELETE FROM user_preferences WHERE last_used_at < :beforeTimestamp AND usage_count < :minUsageCount")
    suspend fun deleteUnusedPreferences(beforeTimestamp: Long, minUsageCount: Int): Int
}
