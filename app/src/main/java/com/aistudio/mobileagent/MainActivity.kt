package com.aistudio.mobileagent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aistudio.mobileagent.data.repository.AppRepository
import com.aistudio.mobileagent.data.repository.OmniRouteRepository
import com.aistudio.mobileagent.ui.components.AppDrawerContent
import com.aistudio.mobileagent.ui.navigation.Screen
import com.aistudio.mobileagent.ui.screens.chat.ChatScreen
import com.aistudio.mobileagent.ui.screens.library.LibraryScreen
import com.aistudio.mobileagent.ui.screens.omniroute.CombosScreen
import com.aistudio.mobileagent.ui.screens.omniroute.OmniRouteDashboardScreen
import com.aistudio.mobileagent.ui.screens.settings.*
import com.aistudio.mobileagent.ui.screens.terminal.TerminalScreen
import com.aistudio.mobileagent.ui.theme.MobileAgentTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MobileAgentTheme(darkTheme = true) { // Material Black AMOLED mandatory
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()
                val currentConversationId by AppRepository.selectedConversationId.collectAsState()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppDrawerContent(
                            currentConversationId = currentConversationId,
                            onSelectConversation = { convId ->
                                AppRepository.selectConversation(convId)
                                if (navController.currentDestination?.route != Screen.Chat.route) {
                                    navController.navigate(Screen.Chat.route) {
                                        popUpTo(Screen.Chat.route) { inclusive = true }
                                    }
                                }
                            },
                            onNewChat = {
                                AppRepository.createConversation()
                                if (navController.currentDestination?.route != Screen.Chat.route) {
                                    navController.navigate(Screen.Chat.route) {
                                        popUpTo(Screen.Chat.route) { inclusive = true }
                                    }
                                }
                            },
                            onNavigateToCombos = {
                                navController.navigate(Screen.Combos.route)
                            },
                            onNavigateToOmniRouteDashboard = {
                                navController.navigate(Screen.OmniRouteDashboard.route)
                            },
                            onNavigateToLibrary = {
                                navController.navigate(Screen.Library.route)
                            },
                            onNavigateToTerminal = {
                                navController.navigate(Screen.Terminal.route)
                            },
                            onNavigateToSettings = {
                                navController.navigate(Screen.Settings.route)
                            },
                            onCloseDrawer = {
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                    }
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Chat.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(Screen.Chat.route) {
                            ChatScreen(
                                onOpenDrawer = {
                                    coroutineScope.launch { drawerState.open() }
                                }
                            )
                        }

                        composable(Screen.Combos.route) {
                            BackHandler { navController.popBackStack() }
                            CombosScreen(
                                onBack = { navController.popBackStack() },
                                onSelectComboForChat = { comboId ->
                                    val currentConvId = AppRepository.selectedConversationId.value
                                    AppRepository.updateConversationModel(currentConvId, comboId)
                                    OmniRouteRepository.selectRoute(comboId)
                                    navController.navigate(Screen.Chat.route) {
                                        popUpTo(Screen.Chat.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.OmniRouteDashboard.route) {
                            BackHandler { navController.popBackStack() }
                            OmniRouteDashboardScreen(
                                onBack = { navController.popBackStack() },
                                onNavigateToCombos = { navController.navigate(Screen.Combos.route) }
                            )
                        }

                        composable(Screen.Library.route) {
                            BackHandler { navController.popBackStack() }
                            LibraryScreen(
                                onBack = { navController.popBackStack() },
                                onUsePromptInChat = { promptText ->
                                    navController.navigate(Screen.Chat.route)
                                }
                            )
                        }

                        composable(Screen.Terminal.route) {
                            BackHandler { navController.popBackStack() }
                            TerminalScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Settings.route) {
                            BackHandler { navController.popBackStack() }
                            SettingsScreen(
                                onBack = { navController.popBackStack() },
                                onNavigateToCombos = { navController.navigate(Screen.Combos.route) },
                                onNavigateToOmniRouteDashboard = { navController.navigate(Screen.OmniRouteDashboard.route) },
                                onNavigateToProviders = { navController.navigate(Screen.Providers.route) },
                                onNavigateToAgents = { navController.navigate(Screen.Agents.route) },
                                onNavigateToSkills = { navController.navigate(Screen.Skills.route) },
                                onNavigateToMcp = { navController.navigate(Screen.McpServers.route) },
                                onNavigateToJobs = { navController.navigate(Screen.Jobs.route) },
                                onNavigateToMemory = { navController.navigate(Screen.Memory.route) }
                            )
                        }

                        composable(Screen.Providers.route) {
                            BackHandler { navController.popBackStack() }
                            ProvidersScreen(onBack = { navController.popBackStack() })
                        }

                        composable(Screen.Agents.route) {
                            BackHandler { navController.popBackStack() }
                            AgentsScreen(onBack = { navController.popBackStack() })
                        }

                        composable(Screen.Skills.route) {
                            BackHandler { navController.popBackStack() }
                            SkillsScreen(onBack = { navController.popBackStack() })
                        }

                        composable(Screen.McpServers.route) {
                            BackHandler { navController.popBackStack() }
                            McpScreen(onBack = { navController.popBackStack() })
                        }

                        composable(Screen.Jobs.route) {
                            BackHandler { navController.popBackStack() }
                            JobsScreen(onBack = { navController.popBackStack() })
                        }

                        composable(Screen.Memory.route) {
                            BackHandler { navController.popBackStack() }
                            MemoryScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
