package com.luizkawai.mariposa.feature.characters.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luizkawai.mariposa.core.navigation.AppDestination
import com.luizkawai.mariposa.domain.usecase.GetCharacterUseCase
import com.luizkawai.mariposa.domain.usecase.ObserveFavoriteIdsUseCase
import com.luizkawai.mariposa.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

@HiltViewModel
class CharacterDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCharacter: GetCharacterUseCase,
    private val observeFavoriteIds: ObserveFavoriteIdsUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {
    private val characterId: Int = checkNotNull(
        savedStateHandle[AppDestination.CHARACTER_ID_ARGUMENT],
    )

    private val _uiState = MutableStateFlow(CharacterDetailUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<CharacterDetailEvent>()
    val events = _events.asSharedFlow()

    init {
        observeFavoriteStatus()
        loadCharacter()
    }

    fun onAction(action: CharacterDetailAction) {
        when (action) {
            CharacterDetailAction.FavoriteClicked -> toggleFavorite(showFeedback = true)
            CharacterDetailAction.UndoFavoriteClicked -> toggleFavorite(showFeedback = false)
            CharacterDetailAction.RetryFavoriteClicked -> toggleFavorite(showFeedback = false)
            CharacterDetailAction.DismissFavoriteError -> {
                _uiState.update { it.copy(hasFavoriteOperationError = false) }
            }
            CharacterDetailAction.RetryClicked -> loadCharacter()
        }
    }

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            observeFavoriteIds().collect { favoriteIds ->
                _uiState.update { state ->
                    state.copy(isFavorite = characterId in favoriteIds)
                }
            }
        }
    }

    private fun toggleFavorite(showFeedback: Boolean) {
        val character = _uiState.value.character ?: return
        val wasFavorite = _uiState.value.isFavorite

        viewModelScope.launch {
            _uiState.update { it.copy(hasFavoriteOperationError = false) }
            runCatching { toggleFavorite(character) }
                .onSuccess {
                    if (showFeedback) {
                        _events.emit(
                            CharacterDetailEvent.FavoriteUpdated(
                                character = character,
                                wasFavorite = wasFavorite,
                            ),
                        )
                    }
                }
                .onFailure { exception ->
                    if (exception is CancellationException) throw exception
                    _uiState.update { it.copy(hasFavoriteOperationError = true) }
                }
        }
    }

    private fun loadCharacter() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    hasError = false,
                    isOfflineError = false,
                )
            }

            runCatching { getCharacter(characterId) }
                .onSuccess { character ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            character = character,
                            hasError = false,
                            isOfflineError = false,
                        )
                    }
                }
                .onFailure { exception ->
                    if (exception is CancellationException) throw exception
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            hasError = true,
                            isOfflineError = exception.isNetworkUnavailable(),
                        )
                    }
                }
        }
    }
}

private fun Throwable.isNetworkUnavailable(): Boolean =
    generateSequence(this) { throwable -> throwable.cause }
        .any { throwable -> throwable is IOException }
