package com.mjdominiczak.songbook.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mjdominiczak.songbook.common.Constants
import com.mjdominiczak.songbook.data.Section
import com.mjdominiczak.songbook.data.SongRepositoryImpl
import com.mjdominiczak.songbook.data.local.RoomSongLocalDataSource
import com.mjdominiczak.songbook.data.local.SongDao
import com.mjdominiczak.songbook.data.local.SongDatabase
import com.mjdominiczak.songbook.data.local.SongLocalDataSource
import com.mjdominiczak.songbook.data.remote.SongApi
import com.mjdominiczak.songbook.domain.SongRepository
import com.mjdominiczak.songbook.json.SectionTypeAdapter
import com.mjdominiczak.songbook.resolvers.PreferencesResolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(Section::class.java, SectionTypeAdapter())
        .create()

    @Provides
    @Singleton
    fun provideSongApi(gson: Gson): SongApi = Retrofit.Builder()
        .baseUrl(Constants.API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
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
    fun provideSongDatabase(@ApplicationContext context: Context): SongDatabase =
        Room.databaseBuilder(
            context,
            SongDatabase::class.java,
            "songbook.db",
        ).build()

    @Provides
    @Singleton
    fun provideSongDao(database: SongDatabase): SongDao = database.songDao

    @Provides
    @Singleton
    fun provideSongLocalDataSource(dataSource: RoomSongLocalDataSource): SongLocalDataSource =
        dataSource

    @Provides
    @Singleton
    fun provideSongRepository(api: SongApi, localDataSource: SongLocalDataSource): SongRepository =
        SongRepositoryImpl(api, localDataSource)

    @Provides
    @Singleton
    fun providePreferencesResolver(@ApplicationContext context: Context) =
        PreferencesResolver(context)
}
