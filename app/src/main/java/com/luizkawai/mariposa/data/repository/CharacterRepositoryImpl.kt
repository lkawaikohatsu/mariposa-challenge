package com.luizkawai.mariposa.data.repository

import com.luizkawai.mariposa.data.mapper.toDomain
import com.luizkawai.mariposa.data.mapper.toFavoriteEntity
import com.luizkawai.mariposa.data.local.dao.FavoriteDao
import com.luizkawai.mariposa.data.remote.api.CharacterApi
import com.luizkawai.mariposa.domain.model.Character
import com.luizkawai.mariposa.domain.model.CharacterPage
import com.luizkawai.mariposa.domain.repository.CharacterRepository
import java.io.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import javax.inject.Inject

class CharacterRepositoryImpl @Inject constructor(
    private val characterApi: CharacterApi,
    private val favoriteDao: FavoriteDao,
) : CharacterRepository {
    private val characterCache = mutableMapOf<Int, Character>()
    private val characterCacheMutex = Mutex()

    override suspend fun getCharacter(id: Int): Character {
        cachedCharacter(id)?.let { return it }

        val character = executeWithTransientRetry {
            characterApi.getCharacter(id).toDomain()
        }
        cacheCharacter(character)
        return character
    }

    override suspend fun getCharacters(query: String, page: Int): CharacterPage {
        val characterPage = try {
            characterApi.getCharacters(
                page = page,
                name = query.trim().takeIf(String::isNotBlank),
            ).toDomain()
        } catch (exception: HttpException) {
            if (exception.code() == HTTP_NOT_FOUND) {
                CharacterPage(
                    characters = emptyList(),
                    previousPage = null,
                    nextPage = null,
                )
            } else {
                throw exception
            }
        }

        cacheCharacters(characterPage.characters)
        return characterPage
    }

    override fun observeFavoriteIds(): Flow<Set<Int>> =
        favoriteDao.observeFavoriteIds().map { ids -> ids.toSet() }

    override fun observeFavorites(): Flow<List<Character>> =
        favoriteDao.observeFavorites().map { favorites ->
            favorites.map { favorite -> favorite.toDomain() }
        }

    override suspend fun toggleFavorite(character: Character) {
        favoriteDao.toggle(character.toFavoriteEntity())
    }

    private suspend fun cachedCharacter(id: Int): Character? =
        characterCacheMutex.withLock { characterCache[id] }

    private suspend fun cacheCharacter(character: Character) {
        characterCacheMutex.withLock {
            characterCache[character.id] = character
        }
    }

    private suspend fun cacheCharacters(characters: List<Character>) {
        characterCacheMutex.withLock {
            characters.forEach { character ->
                characterCache[character.id] = character
            }
        }
    }

    private suspend fun <T> executeWithTransientRetry(
        request: suspend () -> T,
    ): T {
        var retryDelayMillis = INITIAL_RETRY_DELAY_MILLIS

        repeat(MAX_NETWORK_ATTEMPTS) { attempt ->
            try {
                return request()
            } catch (exception: IOException) {
                if (attempt == MAX_NETWORK_ATTEMPTS - 1) throw exception
                delay(retryDelayMillis)
                retryDelayMillis *= RETRY_BACKOFF_MULTIPLIER
            } catch (exception: HttpException) {
                if (!exception.isTransient() || attempt == MAX_NETWORK_ATTEMPTS - 1) {
                    throw exception
                }
                delay(exception.retryAfterMillis() ?: retryDelayMillis)
                retryDelayMillis *= RETRY_BACKOFF_MULTIPLIER
            }
        }

        error("Retry loop completed without a result")
    }

    private fun HttpException.isTransient(): Boolean =
        code() == HTTP_TOO_MANY_REQUESTS || code() in HTTP_SERVER_ERROR_RANGE

    private fun HttpException.retryAfterMillis(): Long? =
        response()
            ?.headers()
            ?.get(RETRY_AFTER_HEADER)
            ?.toLongOrNull()
            ?.times(MILLIS_PER_SECOND)

    private companion object {
        const val HTTP_NOT_FOUND = 404
        const val HTTP_TOO_MANY_REQUESTS = 429
        const val MAX_NETWORK_ATTEMPTS = 3
        const val INITIAL_RETRY_DELAY_MILLIS = 500L
        const val RETRY_BACKOFF_MULTIPLIER = 2
        const val RETRY_AFTER_HEADER = "Retry-After"
        const val MILLIS_PER_SECOND = 1_000L
        val HTTP_SERVER_ERROR_RANGE = 500..599
    }
}
