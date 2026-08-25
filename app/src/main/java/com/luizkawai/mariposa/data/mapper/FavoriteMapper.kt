package com.luizkawai.mariposa.data.mapper

import com.luizkawai.mariposa.data.local.entity.FavoriteEntity
import com.luizkawai.mariposa.domain.model.Character

fun Character.toFavoriteEntity(): FavoriteEntity = FavoriteEntity(
    id = id,
    name = name,
    status = status,
    species = species,
    type = type,
    gender = gender,
    originName = originName,
    locationName = locationName,
    imageUrl = imageUrl,
    episodeUrls = episodeUrls,
    createdAt = createdAt,
)

fun FavoriteEntity.toDomain(): Character = Character(
    id = id,
    name = name,
    status = status,
    species = species,
    type = type,
    gender = gender,
    originName = originName,
    locationName = locationName,
    imageUrl = imageUrl,
    episodeUrls = episodeUrls,
    createdAt = createdAt,
)
