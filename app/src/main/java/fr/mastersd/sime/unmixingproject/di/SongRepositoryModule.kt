package fr.mastersd.sime.unmixingproject.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.mastersd.sime.unmixingproject.repository.SongRepository
import fr.mastersd.sime.unmixingproject.repository.SongRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SongRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSongRepository(songRepositoryImpl: SongRepositoryImpl): SongRepository
}