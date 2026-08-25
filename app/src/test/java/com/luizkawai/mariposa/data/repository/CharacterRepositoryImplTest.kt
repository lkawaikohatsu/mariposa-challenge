package com.luizkawai.mariposa.data.repository

import com.luizkawai.mariposa.data.local.dao.FavoriteDao
import com.luizkawai.mariposa.data.local.entity.FavoriteEntity
import com.luizkawai.mariposa.data.remote.api.CharacterApi
import com.luizkawai.mariposa.data.remote.dto.CharacterDto
import com.luizkawai.mariposa.data.remote.dto.CharacterPageDto
import com.luizkawai.mariposa.data.remote.dto.PageInfoDto
import com.luizkawai.mariposa.data.remote.dto.PlaceDto
import com.luizkawai.mariposa.domain.model.Character
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterRepositoryImplTest {
    private val characterApi = mockk<CharacterApi>()
    private val favoriteDao = mockk<FavoriteDao>()
    private val repository = CharacterRepositoryImpl(characterApi, favoriteDao)

    @Test
    fun `toggle favorite maps the domain model before persisting it locally`() = runTest {
        coJustRun { favoriteDao.toggle(any()) }

        repository.toggleFavorite(testCharacter)

        coVerify(exactly = 1) {
            favoriteDao.toggle(
                match<FavoriteEntity> { favorite ->
                    favorite.id == testCharacter.id &&
                        favorite.name == testCharacter.name &&
                        favorite.episodeUrls == testCharacter.episodeUrls
                },
            )
        }
    }

    @Test
    fun `returns a character already loaded by paging without another network request`() = runTest {
        coEvery { characterApi.getCharacters(page = 1, name = null) } returns characterPageDto

        repository.getCharacters(query = "", page = 1)
        val character = repository.getCharacter(testCharacter.id)

        assertEquals(testCharacter, character)
        coVerify(exactly = 0) { characterApi.getCharacter(any()) }
    }

    @Test
    fun `retries a transient detail request before returning an error`() = runTest {
        coEvery { characterApi.getCharacter(testCharacter.id) } throws IOException() andThen characterDto

        val character = repository.getCharacter(testCharacter.id)

        assertEquals(testCharacter, character)
        coVerify(exactly = 2) { characterApi.getCharacter(testCharacter.id) }
    }

    private companion object {
        val testCharacter = Character(
            id = 1,
            name = "Rick Sanchez",
            status = "Alive",
            species = "Human",
            type = "",
            gender = "Male",
            originName = "Earth (C-137)",
            locationName = "Citadel of Ricks",
            imageUrl = "",
            episodeUrls = listOf("https://rickandmortyapi.com/api/episode/1"),
            createdAt = "",
        )

        val characterDto = CharacterDto(
            id = testCharacter.id,
            name = testCharacter.name,
            status = testCharacter.status,
            species = testCharacter.species,
            type = testCharacter.type,
            gender = testCharacter.gender,
            origin = PlaceDto(testCharacter.originName, ""),
            location = PlaceDto(testCharacter.locationName, ""),
            imageUrl = testCharacter.imageUrl,
            episodeUrls = testCharacter.episodeUrls,
            createdAt = testCharacter.createdAt,
        )

        val characterPageDto = CharacterPageDto(
            info = PageInfoDto(
                count = 1,
                pages = 1,
                nextPageUrl = null,
                previousPageUrl = null,
            ),
            results = listOf(characterDto),
        )
    }
}
