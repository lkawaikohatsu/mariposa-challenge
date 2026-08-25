package com.luizkawai.mariposa.feature.characters.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luizkawai.mariposa.R
import com.luizkawai.mariposa.core.designsystem.CharacterArtwork
import com.luizkawai.mariposa.core.designsystem.theme.MariposaTheme
import com.luizkawai.mariposa.domain.model.Character

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    uiState: CharacterDetailUiState,
    onAction: (CharacterDetailAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val favoriteErrorMessage = stringResource(R.string.unable_to_update_favorite)
    val retryLabel = stringResource(R.string.retry)

    LaunchedEffect(uiState.hasFavoriteOperationError) {
        if (uiState.hasFavoriteOperationError) {
            when (
                snackbarHostState.showSnackbar(
                    message = favoriteErrorMessage,
                    actionLabel = retryLabel,
                )
            ) {
                SnackbarResult.ActionPerformed -> onAction(CharacterDetailAction.RetryFavoriteClicked)
                SnackbarResult.Dismissed -> onAction(CharacterDetailAction.DismissFavoriteError)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text(text = stringResource(R.string.character_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    if (uiState.character != null) {
                        IconButton(
                            onClick = { onAction(CharacterDetailAction.FavoriteClicked) },
                        ) {
                            Icon(
                                imageVector = if (uiState.isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Outlined.FavoriteBorder
                                },
                                contentDescription = stringResource(
                                    if (uiState.isFavorite) R.string.unfavorite else R.string.favorite,
                                ),
                            )
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> DetailLoadingState(modifier = Modifier.padding(paddingValues))
            uiState.hasError -> DetailErrorState(
                message = stringResource(
                    if (uiState.isOfflineError) {
                        R.string.offline_character
                    } else {
                        R.string.unable_to_load_character
                    },
                ),
                onRetry = { onAction(CharacterDetailAction.RetryClicked) },
                modifier = Modifier.padding(paddingValues),
            )

            uiState.character != null -> CharacterDetailContent(
                character = uiState.character,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

@Composable
private fun CharacterDetailContent(
    character: Character,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CharacterArtwork(
            imageUrl = character.imageUrl,
            contentDescription = stringResource(R.string.portrait_of, character.name),
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
            cornerRadius = 28.dp,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                DetailAttribute(label = stringResource(R.string.status), value = character.status)
                DetailAttribute(label = stringResource(R.string.species), value = character.species)
                DetailAttribute(
                    label = stringResource(R.string.type),
                    value = character.type.ifBlank { stringResource(R.string.unknown) },
                )
                DetailAttribute(label = stringResource(R.string.gender), value = character.gender)
                DetailAttribute(label = stringResource(R.string.origin), value = character.originName)
                DetailAttribute(label = stringResource(R.string.location), value = character.locationName)
                DetailAttribute(
                    label = stringResource(R.string.episodes),
                    value = character.episodeUrls.size.toString(),
                )
            }
        }
    }
}

@Composable
private fun DetailAttribute(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun DetailLoadingState(modifier: Modifier = Modifier) {
    val loadingDescription = stringResource(R.string.loading_description)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.semantics {
                contentDescription = loadingDescription
            },
        )
    }
}

@Composable
private fun DetailErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterDetailScreenPreview() {
    MariposaTheme {
        CharacterDetailScreen(
            uiState = CharacterDetailUiState(
                character = Character(
                    id = 1,
                    name = "Rick Sanchez",
                    status = "Alive",
                    species = "Human",
                    type = "",
                    gender = "Male",
                    originName = "Earth (C-137)",
                    locationName = "Citadel of Ricks",
                    imageUrl = "",
                    episodeUrls = emptyList(),
                    createdAt = "",
                ),
            ),
            onAction = {},
            onBack = {},
        )
    }
}
