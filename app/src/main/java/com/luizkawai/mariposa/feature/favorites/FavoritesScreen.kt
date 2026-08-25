package com.luizkawai.mariposa.feature.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luizkawai.mariposa.R
import com.luizkawai.mariposa.core.designsystem.CharacterArtwork
import com.luizkawai.mariposa.core.designsystem.theme.MariposaTheme
import com.luizkawai.mariposa.domain.model.Character

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    onAction: (FavoritesAction) -> Unit,
    onBack: () -> Unit,
    onCharacterClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val favoriteErrorMessage = stringResource(R.string.unable_to_update_favorite)
    val retryLabel = stringResource(R.string.retry)

    LaunchedEffect(uiState.favoriteOperationError?.id) {
        if (uiState.favoriteOperationError != null) {
            when (
                snackbarHostState.showSnackbar(
                    message = favoriteErrorMessage,
                    actionLabel = retryLabel,
                )
            ) {
                SnackbarResult.ActionPerformed -> onAction(FavoritesAction.RetryFavoriteClicked)
                SnackbarResult.Dismissed -> onAction(FavoritesAction.DismissFavoriteError)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.favorites_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> FavoritesLoadingState(Modifier.padding(paddingValues))
            uiState.hasError -> FavoritesErrorState(
                modifier = Modifier.padding(paddingValues),
                onRetry = { onAction(FavoritesAction.RetryClicked) },
            )
            uiState.favorites.isEmpty() -> FavoritesEmptyState(Modifier.padding(paddingValues))
            else -> FavoritesList(
                favorites = uiState.favorites,
                onAction = onAction,
                onCharacterClick = onCharacterClick,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun FavoritesList(
    favorites: List<Character>,
    onAction: (FavoritesAction) -> Unit,
    onCharacterClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(
            count = favorites.size,
            key = { index -> favorites[index].id },
        ) { index ->
            val character = favorites[index]
            FavoritesListItem(
                character = character,
                onRemove = { onAction(FavoritesAction.FavoriteClicked(character)) },
                onClick = { onCharacterClick(character.id) },
            )
        }
    }
}

@Composable
private fun FavoritesListItem(
    character: Character,
    onRemove: () -> Unit,
    onClick: () -> Unit,
) {
    val openDetailsDescription = stringResource(R.string.open_character_details, character.name)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = openDetailsDescription }
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CharacterArtwork(
                imageUrl = character.imageUrl,
                contentDescription = stringResource(R.string.portrait_of, character.name),
                modifier = Modifier.size(76.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(
                        R.string.character_summary,
                        character.status,
                        character.species,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = stringResource(R.string.remove),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun FavoritesLoadingState(modifier: Modifier = Modifier) {
    val loadingDescription = stringResource(R.string.loading_description)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.semantics { contentDescription = loadingDescription },
        )
    }
}

@Composable
private fun FavoritesEmptyState(modifier: Modifier = Modifier) {
    val emptyDescription = stringResource(R.string.empty_favorites_list_description)

    Column(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = emptyDescription },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(52.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(text = stringResource(R.string.no_favorite_characters))
    }
}

@Composable
private fun FavoritesErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = stringResource(R.string.unable_to_load_favorites))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    MariposaTheme {
        FavoritesScreen(
            uiState = FavoritesUiState(isLoading = false),
            onAction = {},
            onBack = {},
            onCharacterClick = {},
        )
    }
}
