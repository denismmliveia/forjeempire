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
import com.forgelegends.presentation.GameViewModel
import com.forgelegends.ui.navigation.NavRoutes
import com.forgelegends.ui.screen.CompletionScreen
import com.forgelegends.ui.screen.ConceptSelectScreen
import com.forgelegends.ui.screen.ForgeScreen
import com.forgelegends.ui.screen.ModelDetailScreen
import com.forgelegends.ui.screen.ShowcaseScreen
import com.forgelegends.ui.screen.SplashScreen
import com.forgelegends.ui.screen.WeaponProgressScreen
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
                val allConcepts by viewModel.allConcepts.collectAsState()
                val activeConcept = viewModel.getActiveConcept()
                val conceptLookup = { id: String -> viewModel.conceptRegistry.getById(id) }

                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.SPLASH
                ) {
                    composable(NavRoutes.SPLASH) {
                        SplashScreen(
                            onTimeout = {
                                val dest = if (gameState.activeConceptId.isEmpty()) {
                                    NavRoutes.CONCEPT_SELECT
                                } else {
                                    NavRoutes.FORGE
                                }
                                navController.navigate(dest) {
                                    popUpTo(NavRoutes.SPLASH) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(NavRoutes.FORGE) {
                        ForgeScreen(
                            gameState = gameState,
                            concept = activeConcept,
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
                            concept = activeConcept,
                            onBack = { navController.popBackStack() },
                            onNavigateToCompletion = {
                                navController.navigate(NavRoutes.COMPLETION)
                            }
                        )
                    }

                    composable(NavRoutes.COMPLETION) {
                        CompletionScreen(
                            gameState = gameState,
                            concept = activeConcept,
                            onNavigateToConceptSelect = {
                                viewModel.archiveCurrentRun()
                                navController.navigate(NavRoutes.CONCEPT_SELECT) {
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
                            conceptLookup = conceptLookup,
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
                        val entryConcept = entry?.let { conceptLookup(it.conceptId) }
                        ModelDetailScreen(
                            entry = entry,
                            concept = entryConcept,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(NavRoutes.CONCEPT_SELECT) {
                        ConceptSelectScreen(
                            concepts = allConcepts,
                            showcaseEntries = showcaseEntries,
                            onSelectConcept = { conceptId ->
                                viewModel.startNewRun(conceptId)
                                navController.navigate(NavRoutes.FORGE) {
                                    popUpTo(NavRoutes.FORGE) { inclusive = true }
                                }
                            },
                            onAddCustomConcept = { name ->
                                viewModel.addCustomConcept(name)
                                val conceptId = name.trim().lowercase()
                                    .replace(Regex("[^a-z0-9]+"), "_")
                                    .trim('_')
                                    .take(40)
                                viewModel.startNewRun(conceptId)
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
