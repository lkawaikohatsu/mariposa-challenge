package com.luizkawai.mariposa.di

import com.luizkawai.mariposa.data.repository.CharacterRepositoryImpl
import com.luizkawai.mariposa.domain.repository.CharacterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCharacterRepository(
        characterRepositoryImpl: CharacterRepositoryImpl,
    ): CharacterRepository
}
