package com.luizkawai.mariposa.feature.characters.detail

import androidx.lifecycle.SavedStateHandle
import com.luizkawai.mariposa.core.navigation.AppDestination
import com.luizkawai.mariposa.domain.model.Character
import com.luizkawai.mariposa.domain.usecase.GetCharacterUseCase
import com.luizkawai.mariposa.domain.usecase.ObserveFavoriteIdsUseCase
import com.luizkawai.mariposa.domain.usecase.ToggleFavoriteUseCase
import io.mockk.every
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterDetailViewModelTest {
    private val getCharacter = mockk<GetCharacterUseCase>()
    private val observeFavoriteIds = mockk<ObserveFavoriteIdsUseCase>()
    private val toggleFavorite = mockk<ToggleFavoriteUseCase>()

    @Test
    fun `loads the character identified by the saved navigation argument`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        coEvery { getCharacter(1) } returns testCharacter
        every { observeFavoriteIds() } returns flowOf(emptySet())

        try {
            val viewModel = CharacterDetailViewModel(
                savedStateHandle = SavedStateHandle(
                    mapOf(AppDestination.CHARACTER_ID_ARGUMENT to 1),
                ),
                getCharacter = getCharacter,
                observeFavoriteIds = observeFavoriteIds,
                toggleFavorite = toggleFavorite,
            )

            runCurrent()

            assertEquals(
                CharacterDetailUiState(character = testCharacter),
                viewModel.uiState.value,
            )
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `exposes a favorite operation error when toggling fails`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        coEvery { getCharacter(1) } returns testCharacter
        coEvery { toggleFavorite(testCharacter) } throws IOException()
        every { observeFavoriteIds() } returns flowOf(emptySet())

        try {
            val viewModel = CharacterDetailViewModel(
                savedStateHandle = SavedStateHandle(
                    mapOf(AppDestination.CHARACTER_ID_ARGUMENT to 1),
                ),
                getCharacter = getCharacter,
                observeFavoriteIds = observeFavoriteIds,
                toggleFavorite = toggleFavorite,
            )
            runCurrent()

            viewModel.onAction(CharacterDetailAction.FavoriteClicked)
            runCurrent()

            assertTrue(viewModel.uiState.value.hasFavoriteOperationError)
        } finally {
            Dispatchers.resetMain()
        }
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
