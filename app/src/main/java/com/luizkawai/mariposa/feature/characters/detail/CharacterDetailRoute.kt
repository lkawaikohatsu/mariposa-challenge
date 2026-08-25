package com.luizkawai.mariposa.feature.characters.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.luizkawai.mariposa.R

@Composable
fun CharacterDetailRoute(
    onBack: () -> Unit,
    viewModel: CharacterDetailViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val favoriteAddedMessage = stringResource(R.string.favorite_added)
    val favoriteRemovedMessage = stringResource(R.string.favorite_removed)
    val undoLabel = stringResource(R.string.undo)

    LaunchedEffect(viewModel, snackbarHostState, favoriteAddedMessage, favoriteRemovedMessage, undoLabel) {
        viewModel.events.collect { event ->
            when (event) {
                is CharacterDetailEvent.FavoriteUpdated -> {
                    val result = snackbarHostState.showSnackbar(
                        message = if (event.wasFavorite) favoriteRemovedMessage else favoriteAddedMessage,
                        actionLabel = undoLabel,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onAction(CharacterDetailAction.UndoFavoriteClicked)
                    }
                }
            }
        }
    }

    CharacterDetailScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
    )
}
