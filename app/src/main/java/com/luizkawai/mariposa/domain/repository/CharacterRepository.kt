package com.luizkawai.mariposa.domain.repository

import com.luizkawai.mariposa.domain.model.Character
import com.luizkawai.mariposa.domain.model.CharacterPage
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    suspend fun getCharacter(id: Int): Character

    suspend fun getCharacters(
        query: String,
        page: Int,
    ): CharacterPage

    fun observeFavoriteIds(): Flow<Set<Int>>

    fun observeFavorites(): Flow<List<Character>>

    suspend fun toggleFavorite(character: Character)
}
