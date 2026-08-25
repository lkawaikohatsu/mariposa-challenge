package com.luizkawai.mariposa.feature.characters.list

import androidx.paging.PagingSource
import com.luizkawai.mariposa.domain.model.Character
import com.luizkawai.mariposa.domain.model.CharacterPage
import com.luizkawai.mariposa.domain.usecase.GetCharactersUseCase
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterPagingSourceTest {
    private val getCharacters = mockk<GetCharactersUseCase>()
    private val pagingSource = CharacterPagingSource(
        query = "",
        getCharacters = getCharacters,
    )

    @Test
    fun `load returns the domain page when the use case succeeds`() = runTest {
        coEvery { getCharacters("", 1) } returns characterPage

        val result = pagingSource.load(refreshParams())

        assertEquals(
            PagingSource.LoadResult.Page(
                data = characterPage.characters,
                prevKey = null,
                nextKey = 2,
            ),
            result,
        )
    }

    @Test
    fun `load returns an error when loading a page fails with an IO exception`() = runTest {
        coEvery { getCharacters("", 1) } throws IOException()

        assertTrue(pagingSource.load(refreshParams()) is PagingSource.LoadResult.Error)
    }

    @Test
    fun `load returns an error when decoding the API response fails`() = runTest {
        coEvery { getCharacters("", 1) } throws SerializationException("Invalid response")

        assertTrue(pagingSource.load(refreshParams()) is PagingSource.LoadResult.Error)
    }

    private fun refreshParams(): PagingSource.LoadParams<Int> = PagingSource.LoadParams.Refresh(
        key = null,
        loadSize = 20,
        placeholdersEnabled = false,
    )

    private companion object {
        val characterPage = CharacterPage(
            characters = listOf(
                Character(
                    id = 1,
                    name = "Rick Sanchez",
                    status = "Alive",
                    species = "Human",
                    type = "",
                    gender = "Male",
                    originName = "Earth (C-137)",
                    locationName = "Citadel of Ricks",
                    imageUrl = "https://example.com/rick.jpeg",
                    episodeUrls = listOf("https://rickandmortyapi.com/api/episode/1"),
                    createdAt = "2017-11-04T18:48:46.250Z",
                ),
            ),
            previousPage = null,
            nextPage = 2,
        )
    }
}
