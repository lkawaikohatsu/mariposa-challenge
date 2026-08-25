package com.luizkawai.mariposa.feature.characters.list

data class CharacterListUiState(
    val query: String = "",
    val firstVisibleItemIndex: Int = 0,
    val firstVisibleItemScrollOffset: Int = 0,
)
