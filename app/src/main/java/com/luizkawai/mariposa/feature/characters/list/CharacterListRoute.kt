package com.luizkawai.mariposa.feature.characters.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun CharacterListRoute(
    onCharacterClick: (Int) -> Unit,
    onFavoritesClick: () -> Unit,
    viewModel: CharacterListViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val characters = viewModel.characters.collectAsLazyPagingItems()
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = uiState.value.firstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = uiState.value.firstVisibleItemScrollOffset,
    )

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                viewModel.onAction(
                    CharacterListAction.ListPositionChanged(
                        firstVisibleItemIndex = index,
                        firstVisibleItemScrollOffset = offset,
                    ),
                )
            }
    }

    CharacterListScreen(
        uiState = uiState.value,
        characters = characters,
        listState = listState,
        onAction = viewModel::onAction,
        onCharacterClick = onCharacterClick,
        onFavoritesClick = onFavoritesClick,
    )
}
