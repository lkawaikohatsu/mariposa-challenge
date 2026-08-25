package com.luizkawai.mariposa.feature.favorites

import com.luizkawai.mariposa.domain.model.Character

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val favorites: List<Character> = emptyList(),
    val hasError: Boolean = false,
    val favoriteOperationError: Character? = null,
)
