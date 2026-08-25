package com.luizkawai.mariposa.feature.characters.detail

import com.luizkawai.mariposa.domain.model.Character

data class CharacterDetailUiState(
    val isLoading: Boolean = false,
    val character: Character? = null,
    val isFavorite: Boolean = false,
    val hasError: Boolean = false,
    val hasFavoriteOperationError: Boolean = false,
)
