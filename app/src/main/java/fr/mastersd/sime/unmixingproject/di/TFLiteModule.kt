package fr.mastersd.sime.unmixingproject.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.mastersd.sime.unmixingproject.tflite.TFLiteModelRunner
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TFLiteModule {

    @Provides
    @Singleton
    fun provideTFLiteModelRunner(
        @ApplicationContext context: Context
    ): TFLiteModelRunner = TFLiteModelRunner(context)
}