package com.luizkawai.mariposa.domain.usecase

import com.luizkawai.mariposa.domain.model.Character
import com.luizkawai.mariposa.domain.repository.CharacterRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveFavoritesUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    operator fun invoke(): Flow<List<Character>> = characterRepository.observeFavorites()
}
