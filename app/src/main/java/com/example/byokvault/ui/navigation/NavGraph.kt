package com.example.byokvault.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.byokvault.ui.screens.main.MainScreen

/**
 * Главный граф навигации приложения
 */
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main
    ) {
        // Главный экран
        composable<Screen.Main> {
            MainScreen(
                onNavigateToAddKey = { platformId ->
                    navController.navigate(Screen.AddKey(platformId = platformId))
                },
                onNavigateToKeyDetail = { keyId ->
                    navController.navigate(Screen.KeyDetail(keyId))
                },
                onNavigateToPlatformKeys = { platformId ->
                    navController.navigate(Screen.PlatformKeysList(platformId))
                }
            )
        }
        
        // Экран добавления/редактирования ключа
        composable<Screen.AddKey> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.AddKey>()
            // TODO: Реализуем на следующем этапе
        }
        
        // Экран деталей ключа
        composable<Screen.KeyDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.KeyDetail>()
            // TODO: Реализуем на следующем этапе
        }
        
        // Экран списка ключей платформы
        composable<Screen.PlatformKeysList> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.PlatformKeysList>()
            // TODO: Реализуем на следующем этапе
        }
    }
}
