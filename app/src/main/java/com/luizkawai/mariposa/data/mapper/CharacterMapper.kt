package com.luizkawai.mariposa.data.mapper

import com.luizkawai.mariposa.data.remote.dto.CharacterDto
import com.luizkawai.mariposa.data.remote.dto.CharacterPageDto
import com.luizkawai.mariposa.domain.model.Character
import com.luizkawai.mariposa.domain.model.CharacterPage

fun CharacterDto.toDomain(): Character = Character(
    id = id,
    name = name,
    status = status,
    species = species,
    type = type,
    gender = gender,
    originName = origin.name,
    locationName = location.name,
    imageUrl = imageUrl,
    episodeUrls = episodeUrls,
    createdAt = createdAt,
)

fun CharacterPageDto.toDomain(): CharacterPage = CharacterPage(
    characters = results.map { it.toDomain() },
    previousPage = info.previousPageUrl?.pageNumber(),
    nextPage = info.nextPageUrl?.pageNumber(),
)

private fun String.pageNumber(): Int? =
    substringAfter("page=", missingDelimiterValue = "")
        .substringBefore('&')
        .toIntOrNull()
