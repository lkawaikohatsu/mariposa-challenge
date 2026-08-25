package com.luizkawai.mariposa.feature.favorites

import com.luizkawai.mariposa.domain.model.Character

sealed interface FavoritesAction {
    data class FavoriteClicked(val character: Character) : FavoritesAction
    data class UndoFavoriteClicked(val character: Character) : FavoritesAction
    data object RetryFavoriteClicked : FavoritesAction
    data object DismissFavoriteError : FavoritesAction
    data object RetryClicked : FavoritesAction
}
