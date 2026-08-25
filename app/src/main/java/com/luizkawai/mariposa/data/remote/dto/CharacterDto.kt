package com.luizkawai.mariposa.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CharacterPageDto(
    val info: PageInfoDto,
    val results: List<CharacterDto>,
)

@Serializable
data class PageInfoDto(
    val count: Int,
    val pages: Int,
    @SerialName("next") val nextPageUrl: String? = null,
    @SerialName("prev") val previousPageUrl: String? = null,
)

@Serializable
data class CharacterDto(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: PlaceDto,
    val location: PlaceDto,
    @SerialName("image") val imageUrl: String,
    @SerialName("episode") val episodeUrls: List<String>,
    @SerialName("created") val createdAt: String,
)

@Serializable
data class PlaceDto(
    val name: String,
    val url: String,
)
