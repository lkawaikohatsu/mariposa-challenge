package com.luizkawai.mariposa.feature.characters.detail

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CharacterDetailRoute(
    onBack: () -> Unit,
    viewModel: CharacterDetailViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    CharacterDetailScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        onBack = onBack,
    )
}
