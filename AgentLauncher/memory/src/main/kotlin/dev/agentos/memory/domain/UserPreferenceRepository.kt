package dev.agentos.memory.domain

import kotlinx.coroutines.flow.Flow

/**
 * User preference learned from interactions.
 */
data class UserPreference(
    val key: String,
    val value: String,
    val domain: String,
    val confidence: Float,
    val usageCount: Int,
    val lastUsedAt: Long,
    val createdAt: Long
)

/**
 * Repository for user preferences.
 */
interface UserPreferenceRepository {
    
    /**
     * Observes all preferences.
     */
    fun observeAllPreferences(): Flow<List<UserPreference>>
    
    /**
     * Observes preferences for a domain.
     */
    fun observePreferencesByDomain(domain: String): Flow<List<UserPreference>>
    
    /**
     * Gets a specific preference.
     */
    suspend fun getPreference(key: String): UserPreference?
    
    /**
     * Searches preferences by pattern.
     */
    suspend fun searchPreferences(pattern: String, limit: Int = 10): List<UserPreference>
    
    /**
     * Saves or updates a preference.
     */
    suspend fun savePreference(preference: UserPreference)
    
    /**
     * Records usage of a preference.
     */
    suspend fun recordUsage(key: String)
    
    /**
     * Deletes a preference.
     */
    suspend fun deletePreference(key: String)
    
    /**
     * Cleans up unused preferences.
     */
    suspend fun cleanupUnusedPreferences(olderThanMs: Long, minUsageCount: Int): Int
}
