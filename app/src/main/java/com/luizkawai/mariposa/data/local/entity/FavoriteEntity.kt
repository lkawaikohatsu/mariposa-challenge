package com.luizkawai.mariposa.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_characters")
data class FavoriteEntity(
    @PrimaryKey
    @ColumnInfo(name = "character_id")
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    @ColumnInfo(name = "origin_name")
    val originName: String,
    @ColumnInfo(name = "location_name")
    val locationName: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String,
    @ColumnInfo(name = "episode_urls")
    val episodeUrls: List<String>,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
)
