package fr.mastersd.sime.unmixingproject.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.mastersd.sime.unmixingproject.pytorch.PyTorchModelRunner
import fr.mastersd.sime.unmixingproject.pytorch.UnmixingPipeline
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PyTorchModule {

    @Provides
    @Singleton
    fun providePyTorchModelRunner(
        @ApplicationContext context: Context
    ): PyTorchModelRunner = PyTorchModelRunner(context)

    @Provides
    @Singleton
    fun provideUnmixingPipeline(
        modelRunner: PyTorchModelRunner
    ): UnmixingPipeline = UnmixingPipeline(modelRunner)
}