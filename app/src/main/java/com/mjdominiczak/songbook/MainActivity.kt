package com.mjdominiczak.songbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mjdominiczak.songbook.data.local.SongDatabase
import com.mjdominiczak.songbook.presentation.addedit.AddEditSongScreen
import com.mjdominiczak.songbook.presentation.detail.SongDetailScreen
import com.mjdominiczak.songbook.presentation.list.SongListScreen
import com.mjdominiczak.songbook.presentation.navigation.Routes
import com.mjdominiczak.songbook.presentation.theme.SongbookTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var database: SongDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SongbookTheme {
                Surface {
                    SongbookNavHost()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        database?.close()
    }
}

@Composable
fun SongbookNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.SONGS_LIST) {
        composable(Routes.SONGS_LIST) {
            SongListScreen(navController = navController)
        }
        composable(
            route = Routes.SONG_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_SONG_ID) {
                type = NavType.IntType
                defaultValue = -1
            })
        ) {
            SongDetailScreen(navController)
        }
        composable(
            route = Routes.ADD_SONG
        ) {
            AddEditSongScreen(navController)
        }
    }
}