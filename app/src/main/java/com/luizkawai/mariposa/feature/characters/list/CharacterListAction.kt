package com.luizkawai.mariposa.feature.characters.list

sealed interface CharacterListAction {
    data class QueryChanged(val query: String) : CharacterListAction
    data class ListPositionChanged(
        val firstVisibleItemIndex: Int,
        val firstVisibleItemScrollOffset: Int,
    ) : CharacterListAction
}
