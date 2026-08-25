package com.luizkawai.mariposa.di

import android.content.Context
import androidx.room.Room
import com.luizkawai.mariposa.data.local.dao.FavoriteDao
import com.luizkawai.mariposa.data.local.database.CharacterDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideCharacterDatabase(
        @ApplicationContext context: Context,
    ): CharacterDatabase {
        return Room.databaseBuilder(
            context,
            CharacterDatabase::class.java,
            DATABASE_NAME,
        ).build()
    }

    @Provides
    fun provideFavoriteDao(database: CharacterDatabase): FavoriteDao = database.favoriteDao()

    private const val DATABASE_NAME = "characters.db"
}
