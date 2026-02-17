package fr.mastersd.sime.unmixingproject.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.mastersd.sime.unmixingproject.repository.FakeSongRepositoryImpl
import fr.mastersd.sime.unmixingproject.repository.SongRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSongRepository(
        impl: FakeSongRepositoryImpl
    ): SongRepository
}