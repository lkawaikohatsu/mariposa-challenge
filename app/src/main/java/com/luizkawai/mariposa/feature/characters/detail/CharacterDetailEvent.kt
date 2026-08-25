package com.luizkawai.mariposa.feature.characters.detail

import com.luizkawai.mariposa.domain.model.Character

sealed interface CharacterDetailEvent {
    data class FavoriteUpdated(
        val character: Character,
        val wasFavorite: Boolean,
    ) : CharacterDetailEvent
}
