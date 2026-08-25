package com.luizkawai.mariposa.data.local.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class StringListConverter {
    private val json = Json

    @TypeConverter
    fun fromJson(value: String): List<String> = json.decodeFromString(value)

    @TypeConverter
    fun toJson(value: List<String>): String = json.encodeToString(value)
}
