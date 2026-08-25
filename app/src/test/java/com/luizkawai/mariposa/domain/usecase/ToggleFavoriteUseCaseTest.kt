package com.luizkawai.mariposa.domain.usecase

import com.luizkawai.mariposa.domain.model.Character
import com.luizkawai.mariposa.domain.repository.CharacterRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ToggleFavoriteUseCaseTest {
    private val characterRepository = mockk<CharacterRepository>()
    private val toggleFavorite = ToggleFavoriteUseCase(characterRepository)

    @Test
    fun `delegates the toggle operation to the repository`() = runTest {
        coJustRun { characterRepository.toggleFavorite(testCharacter) }

        toggleFavorite(testCharacter)

        coVerify(exactly = 1) { characterRepository.toggleFavorite(testCharacter) }
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
            episodeUrls = emptyList(),
            createdAt = "",
        )
    }
}
