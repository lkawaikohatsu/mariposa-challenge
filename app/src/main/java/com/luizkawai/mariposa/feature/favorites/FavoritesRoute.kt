package com.luizkawai.mariposa.feature.favorites

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FavoritesRoute(
    onBack: () -> Unit,
    onCharacterClick: (Int) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    FavoritesScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        onBack = onBack,
        onCharacterClick = onCharacterClick,
    )
}
