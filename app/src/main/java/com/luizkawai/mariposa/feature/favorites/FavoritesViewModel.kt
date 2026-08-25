package com.luizkawai.mariposa.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luizkawai.mariposa.domain.model.Character
import com.luizkawai.mariposa.domain.usecase.ObserveFavoritesUseCase
import com.luizkawai.mariposa.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val observeFavorites: ObserveFavoritesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<FavoritesEvent>()
    val events = _events.asSharedFlow()

    init {
        observeFavoritesFromDatabase()
    }

    fun onAction(action: FavoritesAction) {
        when (action) {
            is FavoritesAction.FavoriteClicked -> {
                toggleFavoriteCharacter(action.character, showFeedback = true)
            }

            is FavoritesAction.UndoFavoriteClicked -> {
                toggleFavoriteCharacter(action.character, showFeedback = false)
            }

            FavoritesAction.RetryFavoriteClicked -> {
                uiState.value.favoriteOperationError?.let { character ->
                    toggleFavoriteCharacter(character, showFeedback = false)
                }
            }

            FavoritesAction.DismissFavoriteError -> {
                _uiState.update { it.copy(favoriteOperationError = null) }
            }

            FavoritesAction.RetryClicked -> observeFavoritesFromDatabase()
        }
    }

    private fun observeFavoritesFromDatabase() {
        viewModelScope.launch {
            observeFavorites()
                .catch {
                    _uiState.value = FavoritesUiState(
                        isLoading = false,
                        hasError = true,
                    )
                }
                .collect { favorites ->
                    _uiState.value = FavoritesUiState(
                        isLoading = false,
                        favorites = favorites,
                    )
                }
        }
    }

    private fun toggleFavoriteCharacter(
        character: Character,
        showFeedback: Boolean,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(favoriteOperationError = null) }
            runCatching { toggleFavorite(character) }
                .onSuccess {
                    if (showFeedback) {
                        _events.emit(FavoritesEvent.FavoriteRemoved(character))
                    }
                }
                .onFailure { exception ->
                    if (exception is CancellationException) throw exception
                    _uiState.update { it.copy(favoriteOperationError = character) }
                }
        }
    }
}
