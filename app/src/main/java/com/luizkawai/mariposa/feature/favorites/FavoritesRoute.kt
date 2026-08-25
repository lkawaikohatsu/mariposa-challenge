package com.luizkawai.mariposa.feature.favorites

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
fun FavoritesRoute(
    onBack: () -> Unit,
    onCharacterClick: (Int) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val favoriteRemovedMessage = stringResource(R.string.favorite_removed)
    val undoLabel = stringResource(R.string.undo)

    LaunchedEffect(viewModel, snackbarHostState, favoriteRemovedMessage, undoLabel) {
        viewModel.events.collect { event ->
            when (event) {
                is FavoritesEvent.FavoriteRemoved -> {
                    val result = snackbarHostState.showSnackbar(
                        message = favoriteRemovedMessage,
                        actionLabel = undoLabel,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onAction(FavoritesAction.UndoFavoriteClicked(event.character))
                    }
                }
            }
        }
    }

    FavoritesScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        onBack = onBack,
        onCharacterClick = onCharacterClick,
        snackbarHostState = snackbarHostState,
    )
}
