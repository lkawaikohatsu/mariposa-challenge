package com.luizkawai.mariposa.feature.characters.list

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems

@Composable
fun CharacterListRoute(
    onCharacterClick: (Int) -> Unit,
    onFavoritesClick: () -> Unit,
    viewModel: CharacterListViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val characters = viewModel.characters.collectAsLazyPagingItems()

    CharacterListScreen(
        uiState = uiState.value,
        characters = characters,
        onAction = viewModel::onAction,
        onCharacterClick = onCharacterClick,
        onFavoritesClick = onFavoritesClick,
    )
}
