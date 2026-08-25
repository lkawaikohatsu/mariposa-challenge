package com.luizkawai.mariposa.feature.favorites

import com.luizkawai.mariposa.domain.model.Character

sealed interface FavoritesEvent {
    data class FavoriteRemoved(val character: Character) : FavoritesEvent
}
