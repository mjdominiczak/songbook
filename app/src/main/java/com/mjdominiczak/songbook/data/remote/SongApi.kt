package com.mjdominiczak.songbook.data.remote

import com.mjdominiczak.songbook.data.Song
import retrofit2.http.GET
import retrofit2.http.Path

interface SongApi {

    @GET("/songs")
    suspend fun getAllSongs(): List<Song>

    @GET("/songs/{songId}")
    suspend fun getSongById(@Path("songId") id: Int): Song

}