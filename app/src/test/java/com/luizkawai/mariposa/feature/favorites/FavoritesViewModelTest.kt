package com.luizkawai.mariposa.feature.favorites

import com.luizkawai.mariposa.domain.model.Character
import com.luizkawai.mariposa.domain.usecase.ObserveFavoritesUseCase
import com.luizkawai.mariposa.domain.usecase.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    private val observeFavorites = mockk<ObserveFavoritesUseCase>()
    private val toggleFavorite = mockk<ToggleFavoriteUseCase>()

    @Test
    fun `retains the character for retry when removing a favorite fails`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        every { observeFavorites() } returns flowOf(listOf(testCharacter))
        coEvery { toggleFavorite(testCharacter) } throws IOException()

        try {
            val viewModel = FavoritesViewModel(
                observeFavorites = observeFavorites,
                toggleFavorite = toggleFavorite,
            )
            runCurrent()

            viewModel.onAction(FavoritesAction.FavoriteClicked(testCharacter))
            runCurrent()

            assertEquals(testCharacter, viewModel.uiState.value.favoriteOperationError)
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
