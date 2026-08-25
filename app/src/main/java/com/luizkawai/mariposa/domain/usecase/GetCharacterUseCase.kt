package com.luizkawai.mariposa.domain.usecase

import com.luizkawai.mariposa.domain.model.Character
import com.luizkawai.mariposa.domain.repository.CharacterRepository
import javax.inject.Inject

class GetCharacterUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    suspend operator fun invoke(id: Int): Character = characterRepository.getCharacter(id)
}
