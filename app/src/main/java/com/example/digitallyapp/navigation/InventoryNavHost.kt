package com.example.digitallyapp.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.digitallyapp.presentation.counter.CounterAddDestination
import com.example.digitallyapp.presentation.counter.CounterAddScreen
import com.example.digitallyapp.presentation.counter.CounterEditDestination
import com.example.digitallyapp.presentation.counter.CounterEditScreen
import com.example.digitallyapp.presentation.details.DetailsDestination
import com.example.digitallyapp.presentation.details.DetailsScreen
import com.example.digitallyapp.presentation.home.HomeDestination
import com.example.digitallyapp.presentation.home.HomeScreen
import com.example.digitallyapp.presentation.settings.SettingsScreen
import com.example.digitallyapp.presentation.settings.SettingsScreenDestination

@Composable
fun DigitallyNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier,
    ) {
        composable(
            route = HomeDestination.route,
        ) {
            HomeScreen(
                navigateToCounterAdd = { navController.navigate(CounterAddDestination.route) },

                navigateToSettings = { navController.navigate(SettingsScreenDestination.route) },
                navigateToDetails = { navController.navigate("${DetailsDestination.route}/${it}") }
            )
        }

        composable(route = SettingsScreenDestination.route) {
            SettingsScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(
            route = CounterAddDestination.route,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = FastOutSlowInEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    towards = AnimatedContentTransitionScope.SlideDirection.Up
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = FastOutSlowInEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    towards = AnimatedContentTransitionScope.SlideDirection.Down
                )
            },
        ) {
            CounterAddScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() })
        }

        composable(
            route = DetailsDestination.routeWithArgs,
            arguments = listOf(navArgument(DetailsDestination.CounterIdArg) {
                type = NavType.IntType
            }),
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = FastOutSlowInEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = FastOutSlowInEasing
                    )
                )
            },
            popEnterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = FastOutSlowInEasing
                    )
                )
            },
            popExitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = FastOutSlowInEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            },
        ) {
            DetailsScreen(
                navigateToCounterEdit = { navController.navigate("${CounterEditDestination.route}/${it}") },
                navigateBack = { navController.navigateUp() }
            )
        }

        composable(
            route = CounterEditDestination.routeWithArgs,
            arguments = listOf(navArgument(DetailsDestination.CounterIdArg) {
                type = NavType.IntType
            }),
            enterTransition = {
                fadeIn(
                    animationSpec = tween(
                        300, easing = FastOutSlowInEasing
                    )
                ) + slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                fadeOut(
                    animationSpec = tween(
                        300, easing = FastOutSlowInEasing
                    )
                ) + slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            },
        ) {
            CounterEditScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}