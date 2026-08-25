package com.luizkawai.mariposa.domain.model

data class CharacterPage(
    val characters: List<Character>,
    val previousPage: Int?,
    val nextPage: Int?,
)
