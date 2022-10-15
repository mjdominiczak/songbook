package com.mjdominiczak.songbook.di

import com.google.gson.GsonBuilder
import com.mjdominiczak.songbook.common.Constants
import com.mjdominiczak.songbook.data.Section
import com.mjdominiczak.songbook.data.SongRepositoryImpl
import com.mjdominiczak.songbook.data.remote.SongApi
import com.mjdominiczak.songbook.domain.SongRepository
import com.mjdominiczak.songbook.json.SectionTypeAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSongApi(): SongApi = Retrofit.Builder()
        .baseUrl(Constants.API_BASE_URL)
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder()
                    .registerTypeAdapter(Section::class.java, SectionTypeAdapter())
                    .create()
            )
        )
        .client(
            OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    }
                )
                .build()
        )
        .build()
        .create(SongApi::class.java)

    @Provides
    @Singleton
    fun provideSongRepository(api: SongApi): SongRepository = SongRepositoryImpl(api)
}