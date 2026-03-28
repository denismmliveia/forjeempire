package com.forgelegends

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.forgelegends.domain.model.WeaponFamily
import com.forgelegends.presentation.GameViewModel
import com.forgelegends.ui.navigation.NavRoutes
import com.forgelegends.ui.screen.CompletionScreen
import com.forgelegends.ui.screen.ForgeScreen
import com.forgelegends.ui.screen.ModelDetailScreen
import com.forgelegends.ui.screen.ShowcaseScreen
import com.forgelegends.ui.screen.WeaponProgressScreen
import com.forgelegends.ui.screen.WeaponSelectScreen
import com.forgelegends.ui.screen.WorkbenchScreen
import com.forgelegends.ui.theme.ForgeLegendTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ForgeLegendTheme {
                val navController = rememberNavController()
                val viewModel: GameViewModel = hiltViewModel()
                val gameState by viewModel.gameState.collectAsState()
                val showcaseEntries by viewModel.showcaseEntries.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.FORGE
                ) {
                    composable(NavRoutes.FORGE) {
                        ForgeScreen(
                            gameState = gameState,
                            onTap = viewModel::onTap,
                            onNavigateToWorkbench = {
                                navController.navigate(NavRoutes.WORKBENCH)
                            },
                            onNavigateToProgress = {
                                navController.navigate(NavRoutes.WEAPON_PROGRESS)
                            },
                            onNavigateToCompletion = {
                                navController.navigate(NavRoutes.COMPLETION) {
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToShowcase = {
                                navController.navigate(NavRoutes.SHOWCASE)
                            }
                        )
                    }

                    composable(NavRoutes.WORKBENCH) {
                        WorkbenchScreen(
                            gameState = gameState,
                            onPurchaseUpgrade = viewModel::purchaseUpgrade,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(NavRoutes.WEAPON_PROGRESS) {
                        WeaponProgressScreen(
                            gameState = gameState,
                            onBack = { navController.popBackStack() },
                            onNavigateToCompletion = {
                                navController.navigate(NavRoutes.COMPLETION)
                            }
                        )
                    }

                    composable(NavRoutes.COMPLETION) {
                        CompletionScreen(
                            gameState = gameState,
                            onNavigateToWeaponSelect = {
                                viewModel.archiveCurrentRun()
                                navController.navigate(NavRoutes.WEAPON_SELECT) {
                                    popUpTo(NavRoutes.FORGE) { inclusive = false }
                                }
                            },
                            onNavigateToShowcase = {
                                navController.navigate(NavRoutes.SHOWCASE)
                            }
                        )
                    }

                    composable(NavRoutes.SHOWCASE) {
                        ShowcaseScreen(
                            entries = showcaseEntries,
                            onEntryClick = { entryId ->
                                navController.navigate(NavRoutes.modelDetail(entryId))
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = NavRoutes.MODEL_DETAIL,
                        arguments = listOf(navArgument("entryId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val entryId = backStackEntry.arguments?.getString("entryId")
                        val entry = showcaseEntries.find { it.id == entryId }
                        ModelDetailScreen(
                            entry = entry,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(NavRoutes.WEAPON_SELECT) {
                        WeaponSelectScreen(
                            showcaseEntries = showcaseEntries,
                            onSelectWeapon = { family ->
                                viewModel.startNewRun(family)
                                navController.navigate(NavRoutes.FORGE) {
                                    popUpTo(NavRoutes.FORGE) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
