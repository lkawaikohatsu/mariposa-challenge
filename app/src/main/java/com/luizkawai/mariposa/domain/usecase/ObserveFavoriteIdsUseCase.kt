package com.luizkawai.mariposa.domain.usecase

import com.luizkawai.mariposa.domain.repository.CharacterRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveFavoriteIdsUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
) {
    operator fun invoke(): Flow<Set<Int>> = characterRepository.observeFavoriteIds()
}
