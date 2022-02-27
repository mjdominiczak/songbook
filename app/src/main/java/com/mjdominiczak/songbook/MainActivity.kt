package com.mjdominiczak.songbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mjdominiczak.songbook.presentation.SongDetailScreen
import com.mjdominiczak.songbook.presentation.SongListScreen
import com.mjdominiczak.songbook.presentation.theme.SongbookTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
}

@Composable
fun SongbookNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "songs") {
        composable("songs") {
            SongListScreen(navController = navController)
        }
        composable(
            route = "songs/{songId}",
            arguments = listOf(navArgument("songId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) {
            SongDetailScreen()
        }
    }
}