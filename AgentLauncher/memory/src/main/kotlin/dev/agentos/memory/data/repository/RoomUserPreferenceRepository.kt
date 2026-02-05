package dev.agentos.memory.data.repository

import dev.agentos.core.common.DispatcherProvider
import dev.agentos.memory.data.db.UserPreferenceDao
import dev.agentos.memory.data.db.UserPreferenceEntity
import dev.agentos.memory.domain.UserPreference
import dev.agentos.memory.domain.UserPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed implementation of UserPreferenceRepository.
 */
@Singleton
class RoomUserPreferenceRepository @Inject constructor(
    private val preferenceDao: UserPreferenceDao,
    private val dispatchers: DispatcherProvider
) : UserPreferenceRepository {

    override fun observeAllPreferences(): Flow<List<UserPreference>> {
        return preferenceDao.getAllPreferences().map { entities ->
            entities.map { it.toPreference() }
        }
    }

    override fun observePreferencesByDomain(domain: String): Flow<List<UserPreference>> {
        return preferenceDao.getPreferencesByDomain(domain).map { entities ->
            entities.map { it.toPreference() }
        }
    }

    override suspend fun getPreference(key: String): UserPreference? {
        return withContext(dispatchers.io) {
            preferenceDao.getPreference(key)?.toPreference()
        }
    }

    override suspend fun searchPreferences(pattern: String, limit: Int): List<UserPreference> {
        return withContext(dispatchers.io) {
            preferenceDao.searchPreferences("%$pattern%", limit).map { it.toPreference() }
        }
    }

    override suspend fun savePreference(preference: UserPreference) {
        withContext(dispatchers.io) {
            preferenceDao.insert(preference.toEntity())
        }
    }

    override suspend fun recordUsage(key: String) {
        withContext(dispatchers.io) {
            preferenceDao.incrementUsage(key, System.currentTimeMillis())
        }
    }

    override suspend fun deletePreference(key: String) {
        withContext(dispatchers.io) {
            preferenceDao.getPreference(key)?.let {
                preferenceDao.delete(it)
            }
        }
    }

    override suspend fun cleanupUnusedPreferences(olderThanMs: Long, minUsageCount: Int): Int {
        return withContext(dispatchers.io) {
            val beforeTimestamp = System.currentTimeMillis() - olderThanMs
            preferenceDao.deleteUnusedPreferences(beforeTimestamp, minUsageCount)
        }
    }

    private fun UserPreferenceEntity.toPreference(): UserPreference {
        return UserPreference(
            key = key,
            value = value,
            domain = domain,
            confidence = confidence,
            usageCount = usageCount,
            lastUsedAt = lastUsedAt,
            createdAt = createdAt
        )
    }

    private fun UserPreference.toEntity(): UserPreferenceEntity {
        return UserPreferenceEntity(
            key = key,
            value = value,
            domain = domain,
            confidence = confidence,
            usageCount = usageCount,
            lastUsedAt = lastUsedAt,
            createdAt = createdAt
        )
    }
}
