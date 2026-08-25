package com.luizkawai.mariposa.domain.usecase

import com.luizkawai.mariposa.domain.model.CharacterPage
import com.luizkawai.mariposa.domain.repository.CharacterRepository
import javax.inject.Inject

class GetCharactersUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    suspend operator fun invoke(
        query: String,
        page: Int,
    ): CharacterPage = characterRepository.getCharacters(query, page)
}
