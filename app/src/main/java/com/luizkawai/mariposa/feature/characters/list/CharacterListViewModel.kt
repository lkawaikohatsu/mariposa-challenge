package com.luizkawai.mariposa.feature.characters.list

import androidx.lifecycle.SavedStateHandle
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
    private val savedStateHandle: SavedStateHandle,
    private val getCharacters: GetCharactersUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        CharacterListUiState(
            query = savedStateHandle[CHARACTER_LIST_QUERY_KEY] ?: "",
            firstVisibleItemIndex = savedStateHandle[CHARACTER_LIST_FIRST_VISIBLE_ITEM_INDEX_KEY] ?: 0,
            firstVisibleItemScrollOffset = savedStateHandle[
                CHARACTER_LIST_FIRST_VISIBLE_ITEM_SCROLL_OFFSET_KEY
            ] ?: 0,
        ),
    )
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
                savedStateHandle[CHARACTER_LIST_QUERY_KEY] = action.query
                _uiState.value = _uiState.value.copy(query = action.query)
            }

            is CharacterListAction.ListPositionChanged -> {
                savedStateHandle[CHARACTER_LIST_FIRST_VISIBLE_ITEM_INDEX_KEY] =
                    action.firstVisibleItemIndex
                savedStateHandle[CHARACTER_LIST_FIRST_VISIBLE_ITEM_SCROLL_OFFSET_KEY] =
                    action.firstVisibleItemScrollOffset
                _uiState.value = _uiState.value.copy(
                    firstVisibleItemIndex = action.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = action.firstVisibleItemScrollOffset,
                )
            }
        }
    }
}

internal const val CHARACTER_SEARCH_DEBOUNCE_MILLIS = 300L
internal const val CHARACTER_LIST_QUERY_KEY = "character_list_query"
internal const val CHARACTER_LIST_FIRST_VISIBLE_ITEM_INDEX_KEY = "character_list_first_visible_item_index"
internal const val CHARACTER_LIST_FIRST_VISIBLE_ITEM_SCROLL_OFFSET_KEY =
    "character_list_first_visible_item_scroll_offset"
private const val NETWORK_PAGE_SIZE = 20
