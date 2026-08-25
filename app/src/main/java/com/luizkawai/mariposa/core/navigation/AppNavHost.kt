package com.luizkawai.mariposa.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.luizkawai.mariposa.feature.characters.detail.CharacterDetailRoute
import com.luizkawai.mariposa.feature.characters.list.CharacterListRoute
import com.luizkawai.mariposa.feature.favorites.FavoritesRoute

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestination.CHARACTERS,
        modifier = modifier,
    ) {
        composable(AppDestination.CHARACTERS) {
            CharacterListRoute(
                onCharacterClick = { characterId ->
                    navController.navigate(AppDestination.characterDetail(characterId))
                },
                onFavoritesClick = {
                    navController.navigate(AppDestination.FAVORITES)
                },
            )
        }
        composable(
            route = AppDestination.CHARACTER_DETAIL,
            arguments = listOf(
                navArgument(AppDestination.CHARACTER_ID_ARGUMENT) {
                    type = NavType.IntType
                },
            ),
        ) {
            CharacterDetailRoute(onBack = { navController.navigateUp() })
        }
        composable(AppDestination.FAVORITES) {
            FavoritesRoute(
                onBack = { navController.navigateUp() },
                onCharacterClick = { characterId ->
                    navController.navigate(AppDestination.characterDetail(characterId))
                },
            )
        }
    }
}
