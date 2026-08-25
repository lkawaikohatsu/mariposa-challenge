package com.luizkawai.mariposa.feature.characters.list

import app.cash.turbine.test
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
            val viewModel = CharacterListViewModel(getCharacters)

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
}
