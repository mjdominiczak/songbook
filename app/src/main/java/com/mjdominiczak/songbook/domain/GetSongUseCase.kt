package com.mjdominiczak.songbook.domain

import com.mjdominiczak.songbook.common.Resource
import com.mjdominiczak.songbook.data.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetSongUseCase @Inject constructor(
    private val songRepository: SongRepository
) {
    operator fun invoke(id: Int): Flow<Resource<Song>> = flow {
        try {
            emit(Resource.Loading())
            val song = songRepository.getSongById(id)
            emit(Resource.Success(song))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "Unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error(e.localizedMessage ?: "Unexpected error occured"))
        }
    }
}