package com.luizkawai.mariposa.feature.characters.list

import app.cash.turbine.test
import androidx.lifecycle.SavedStateHandle
import com.luizkawai.mariposa.domain.usecase.GetCharactersUseCase
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterListViewModelTest {
    private val getCharacters = mockk<GetCharactersUseCase>()

    @Test
    fun `search uses only the latest query after the debounce interval`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val viewModel = CharacterListViewModel(
                savedStateHandle = SavedStateHandle(),
                getCharacters = getCharacters,
            )

            viewModel.searchQueries.test {
                advanceTimeBy(CHARACTER_SEARCH_DEBOUNCE_MILLIS)
                runCurrent()
                assertEquals("", awaitItem())

                viewModel.onAction(CharacterListAction.QueryChanged("r"))
                viewModel.onAction(CharacterListAction.QueryChanged("ri"))
                viewModel.onAction(CharacterListAction.QueryChanged("rick"))
                assertEquals("rick", viewModel.uiState.value.query)

                advanceTimeBy(CHARACTER_SEARCH_DEBOUNCE_MILLIS - 1)
                runCurrent()
                expectNoEvents()

                advanceTimeBy(1)
                runCurrent()
                assertEquals("rick", awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `restores the query and list position from saved state`() {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                CHARACTER_LIST_QUERY_KEY to "morty",
                CHARACTER_LIST_FIRST_VISIBLE_ITEM_INDEX_KEY to 12,
                CHARACTER_LIST_FIRST_VISIBLE_ITEM_SCROLL_OFFSET_KEY to 40,
            ),
        )

        val viewModel = CharacterListViewModel(
            savedStateHandle = savedStateHandle,
            getCharacters = getCharacters,
        )

        assertEquals("morty", viewModel.uiState.value.query)
        assertEquals(12, viewModel.uiState.value.firstVisibleItemIndex)
        assertEquals(40, viewModel.uiState.value.firstVisibleItemScrollOffset)
    }
}
