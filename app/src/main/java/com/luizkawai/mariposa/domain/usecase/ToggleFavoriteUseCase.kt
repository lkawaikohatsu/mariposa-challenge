package com.luizkawai.mariposa.domain.usecase

import com.luizkawai.mariposa.domain.model.Character
import com.luizkawai.mariposa.domain.repository.CharacterRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    suspend operator fun invoke(character: Character) {
        characterRepository.toggleFavorite(character)
    }
}
