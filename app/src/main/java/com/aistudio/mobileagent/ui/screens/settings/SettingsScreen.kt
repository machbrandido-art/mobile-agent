package com.aistudio.mobileagent.ui.screens.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.mobileagent.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToCombos: () -> Unit,
    onNavigateToOmniRouteDashboard: () -> Unit,
    onNavigateToProviders: () -> Unit,
    onNavigateToAgents: () -> Unit,
    onNavigateToSkills: () -> Unit,
    onNavigateToMcp: () -> Unit,
    onNavigateToJobs: () -> Unit,
    onNavigateToMemory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Hub", fontWeight = FontWeight.Bold, color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureBlack)
            )
        },
        containerColor = PureBlack,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Text(
                    text = "OMNIRoute GATEWAY & COMBOS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElegantBlue,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            item {
                SettingsCategoryCard(
                    title = "OmniRoute Combos & Chains",
                    description = "Chained model routes with automatic HTTP 429 failover & RTK compression",
                    icon = Icons.Default.AltRoute,
                    accentColor = ElegantBlue,
                    onClick = onNavigateToCombos
                )
            }

            item {
                SettingsCategoryCard(
                    title = "OmniRoute Gateway & Quotas",
                    description = "Real-time routing telemetry, 32/64-bit parity, and RPM/TPM trackers",
                    icon = Icons.Default.Hub,
                    accentColor = OmniRouteOrange,
                    onClick = onNavigateToOmniRouteDashboard
                )
            }

            item {
                Text(
                    text = "INTELLIGENCE & MODELS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElegantBlue,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                SettingsCategoryCard(
                    title = "AI Providers & Inference",
                    description = "On-Device LiteRT (Gemma, Qwen), Google Gemini, OpenAI, Claude, Ollama",
                    icon = Icons.Default.Memory,
                    onClick = onNavigateToProviders
                )
            }

            item {
                SettingsCategoryCard(
                    title = "Agents & Personas",
                    description = "Manage specialized personas: Software Engineer, Architect, Hacker",
                    icon = Icons.Default.SmartToy,
                    onClick = onNavigateToAgents
                )
            }

            item {
                Text(
                    text = "EXTENSIBILITY & PROTOCOLS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElegantBlue,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                SettingsCategoryCard(
                    title = "Skills Catalog",
                    description = "Modular capability packages (android-cli, saf-storage, termux, web-search)",
                    icon = Icons.Default.Extension,
                    onClick = onNavigateToSkills
                )
            }

            item {
                SettingsCategoryCard(
                    title = "MCP Servers (Model Context Protocol)",
                    description = "Connect external servers over SSE and Stdio pipes",
                    icon = Icons.Default.Dns,
                    onClick = onNavigateToMcp
                )
            }

            item {
                Text(
                    text = "AUTOMATION & SYSTEM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElegantBlue,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                )
            }

            item {
                SettingsCategoryCard(
                    title = "Scheduled Background Jobs",
                    description = "Autonomous cron tasks and background maintenance alarms",
                    icon = Icons.Default.Schedule,
                    onClick = onNavigateToJobs
                )
            }

            item {
                SettingsCategoryCard(
                    title = "Persistent Agent Memory",
                    description = "Facts, preferences, and hardware constraints stored across sessions",
                    icon = Icons.Default.Psychology,
                    onClick = onNavigateToMemory
                )
            }

            item {
                Surface(
                    color = DarkCardBlack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .border(1.dp, DarkBorderBlack, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mobile Agent + OmniRoute v1.1.0", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Autonomous AI agent running natively on Android with OmniRoute model chaining and full 32-bit (armeabi-v7a) & 64-bit (arm64-v8a) architecture support under Material Black.",
                            fontSize = 12.sp,
                            color = TextLightGray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCategoryCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color = ElegantBlue,
    onClick: () -> Unit
) {
    Surface(
        color = DarkCardBlack,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorderBlack, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = DarkElevatedCard,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextWhite)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, fontSize = 12.sp, color = TextLightGray, lineHeight = 16.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}
