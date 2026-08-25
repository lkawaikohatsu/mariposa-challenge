package com.luizkawai.mariposa.core.navigation

object AppDestination {
    const val CHARACTERS = "characters"
    const val CHARACTER_ID_ARGUMENT = "characterId"
    const val CHARACTER_DETAIL = "characterDetail/{$CHARACTER_ID_ARGUMENT}"
    const val FAVORITES = "favorites"

    fun characterDetail(characterId: Int): String = "characterDetail/$characterId"
}
