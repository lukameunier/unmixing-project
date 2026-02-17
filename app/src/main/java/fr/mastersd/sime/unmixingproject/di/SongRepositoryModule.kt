package fr.mastersd.sime.unmixingproject.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.mastersd.sime.unmixingproject.repository.FakeSongRepositoryImpl
import fr.mastersd.sime.unmixingproject.repository.SongRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class SongRepositoryModule {

    @Binds
    abstract fun bindSongRepository(songRepositoryImpl : FakeSongRepositoryImpl) : SongRepository
}