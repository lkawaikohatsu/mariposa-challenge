package com.luizkawai.mariposa.feature.characters.detail

sealed interface CharacterDetailAction {
    data object FavoriteClicked : CharacterDetailAction
    data object UndoFavoriteClicked : CharacterDetailAction
    data object RetryFavoriteClicked : CharacterDetailAction
    data object DismissFavoriteError : CharacterDetailAction
    data object RetryClicked : CharacterDetailAction
}
