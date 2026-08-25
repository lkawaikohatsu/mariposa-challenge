package com.luizkawai.mariposa.feature.characters.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.luizkawai.mariposa.domain.model.Character
import com.luizkawai.mariposa.domain.usecase.GetCharactersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class CharacterListViewModel @Inject constructor(
    private val getCharacters: GetCharactersUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CharacterListUiState())
    val uiState = _uiState.asStateFlow()

    internal val searchQueries: Flow<String> = uiState
        .map { state -> state.query }
        .distinctUntilChanged()
        .debounce(CHARACTER_SEARCH_DEBOUNCE_MILLIS)

    val characters: Flow<PagingData<Character>> = searchQueries
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(
                    pageSize = NETWORK_PAGE_SIZE,
                    enablePlaceholders = false,
                ),
                pagingSourceFactory = {
                    CharacterPagingSource(
                        query = query,
                        getCharacters = getCharacters,
                    )
                },
            ).flow
        }
        .cachedIn(viewModelScope)

    fun onAction(action: CharacterListAction) {
        when (action) {
            is CharacterListAction.QueryChanged -> {
                _uiState.value = _uiState.value.copy(query = action.query)
            }
        }
    }
}

internal const val CHARACTER_SEARCH_DEBOUNCE_MILLIS = 300L
private const val NETWORK_PAGE_SIZE = 20
