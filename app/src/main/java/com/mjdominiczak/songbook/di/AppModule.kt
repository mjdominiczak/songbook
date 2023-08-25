package com.mjdominiczak.songbook.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mjdominiczak.songbook.common.Constants
import com.mjdominiczak.songbook.data.Section
import com.mjdominiczak.songbook.data.SongRepositoryImpl
import com.mjdominiczak.songbook.data.local.SectionDto
import com.mjdominiczak.songbook.data.local.SongDatabase
import com.mjdominiczak.songbook.data.local.SongDto
import com.mjdominiczak.songbook.data.remote.SongApi
import com.mjdominiczak.songbook.domain.SongRepository
import com.mjdominiczak.songbook.json.SectionTypeAdapter
import com.mjdominiczak.songbook.resolvers.PreferencesResolver
import com.mjdominiczak.songbook.resolvers.ResourcesResolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Configuration
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
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
    fun provideRealmConfiguration(): Configuration =
        RealmConfiguration.Builder(schema = setOf(SongDto::class, SectionDto::class))
            .deleteRealmIfMigrationNeeded()
            .build()

    @Provides
    @Singleton
    fun provideRealm(config: Configuration): Realm = Realm.open(configuration = config)

    @Provides
    @Singleton
    fun provideSongDatabase(realm: Realm) = SongDatabase(realm)

    @Provides
    @Singleton
    fun provideSongRepository(api: SongApi, db: SongDatabase): SongRepository =
        SongRepositoryImpl(api = api, db = db)

    @Provides
    @Singleton
    fun provideResourcesResolver(@ApplicationContext context: Context) = ResourcesResolver(context)

    @Provides
    @Singleton
    fun providePreferencesResolver(@ApplicationContext context: Context) =
        PreferencesResolver(context)
}