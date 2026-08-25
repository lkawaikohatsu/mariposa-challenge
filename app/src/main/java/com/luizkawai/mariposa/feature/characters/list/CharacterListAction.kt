package com.luizkawai.mariposa.feature.characters.list

sealed interface CharacterListAction {
    data class QueryChanged(val query: String) : CharacterListAction
}
