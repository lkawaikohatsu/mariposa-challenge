package com.luizkawai.mariposa.data.remote.api

import com.luizkawai.mariposa.data.remote.dto.CharacterDto
import com.luizkawai.mariposa.data.remote.dto.CharacterPageDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CharacterApi {
    @GET("character/{id}")
    suspend fun getCharacter(
        @Path("id") id: Int,
    ): CharacterDto

    @GET("character")
    suspend fun getCharacters(
        @Query("page") page: Int,
        @Query("name") name: String?,
    ): CharacterPageDto
}
