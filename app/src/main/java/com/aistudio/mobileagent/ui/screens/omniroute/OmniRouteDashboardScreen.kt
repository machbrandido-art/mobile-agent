package com.aistudio.mobileagent.ui.screens.omniroute

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.mobileagent.data.model.ProviderQuota
import com.aistudio.mobileagent.data.model.TokenCompressionMode
import com.aistudio.mobileagent.data.repository.OmniRouteRepository
import com.aistudio.mobileagent.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniRouteDashboardScreen(
    onBack: () -> Unit,
    onNavigateToCombos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics by OmniRouteRepository.metricsFlow.collectAsState()
    val quotas by OmniRouteRepository.quotasFlow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AltRoute, contentDescription = null, tint = ElegantBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("OmniRoute Gateway Hub", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 16.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCombos) {
                        Icon(Icons.Default.Tune, contentDescription = "Manage Combos", tint = ElegantBlue)
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Gateway Status Card
            item {
                Surface(
                    color = DarkCardBlack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorderBlack, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(if (metrics.isGatewayActive) OmniRouteGreen else RoseError, RoundedCornerShape(5.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (metrics.isGatewayActive) "Local Gateway Online" else "Gateway Paused",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextWhite
                                )
                            }
                            Switch(
                                checked = metrics.isGatewayActive,
                                onCheckedChange = { OmniRouteRepository.toggleGateway(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PureBlack,
                                    checkedTrackColor = ElegantBlue
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Universal OpenAI-Compatible API: http://127.0.0.1:${metrics.gatewayPort}/v1",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = ElegantBlue
                        )
                    }
                }
            }

            // 32-bit & 64-bit Architecture Parity Card
            item {
                Surface(
                    color = DarkElevatedCard,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorderBlack, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = OmniRouteGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dual-Architecture Engine (32-bit & 64-bit)",
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Configured with ndk { abiFilters 'armeabi-v7a', 'arm64-v8a' } and minSdk 21. All data flow, Combo evaluations, and token calculations run uniformly across legacy 32-bit and modern 64-bit hardware.",
                            fontSize = 11.sp,
                            color = TextLightGray,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(color = OmniRouteGreen.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                Text("armeabi-v7a (32-bit)", fontSize = 10.sp, color = OmniRouteGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Surface(color = OmniRouteBlue.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                Text("arm64-v8a (64-bit)", fontSize = 10.sp, color = OmniRouteBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Surface(color = ElegantPurple.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                Text("minSdk 21", fontSize = 10.sp, color = ElegantPurple, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }

            // Metrics Telemetry
            item {
                Text(
                    text = "ROUTING TELEMETRY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElegantBlue,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricBox(
                        title = "Requests Routed",
                        value = "${metrics.totalRequests}",
                        icon = Icons.Default.Speed,
                        tint = ElegantBlue,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "429 Failovers",
                        value = "${metrics.failoversHandled}",
                        icon = Icons.Default.SwapCalls,
                        tint = OmniRouteOrange,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "RTK Tokens Saved",
                        value = "${metrics.tokensSavedViaRTK}",
                        icon = Icons.Default.Compress,
                        tint = OmniRouteGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Provider Quota & Throttling Tracker
            item {
                Text(
                    text = "PROVIDER QUOTAS & RATE LIMITS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElegantBlue,
                    modifier = Modifier.padding(start = 2.dp, top = 6.dp)
                )
            }

            items(quotas) { quota ->
                Surface(
                    color = DarkCardBlack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorderBlack, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = quota.providerName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextWhite
                            )
                            Text(
                                text = "${quota.currentRpm}/${quota.maxRpm} RPM",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = if (quota.currentRpm > quota.maxRpm * 0.8) OmniRouteOrange else OmniRouteGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        val progress = if (quota.requestsLimitToday > 0) quota.requestsUsedToday.toFloat() / quota.requestsLimitToday else 0.1f
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            color = if (progress > 0.85f) RoseError else ElegantBlue,
                            trackColor = DarkSurfaceBlack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Daily Requests: ${quota.requestsUsedToday} / ${quota.requestsLimitToday}",
                                fontSize = 10.sp,
                                color = TextLightGray
                            )
                            Text(
                                text = "Auto-Failover: Enabled",
                                fontSize = 10.sp,
                                color = OmniRouteGreen
                            )
                        }
                    }
                }
            }

            // Security & Privacy Guardrails
            item {
                Text(
                    text = "SECURITY & PRIVACY GUARDRAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElegantBlue,
                    modifier = Modifier.padding(start = 2.dp, top = 6.dp)
                )
            }

            item {
                Surface(
                    color = DarkCardBlack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorderBlack, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PII Protection & Secret Masking",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Redacts emails, phone numbers, and API keys before transmission",
                                    fontSize = 11.sp,
                                    color = TextLightGray
                                )
                            }
                            Switch(
                                checked = metrics.piiProtectionActive,
                                onCheckedChange = { OmniRouteRepository.togglePiiProtection(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PureBlack,
                                    checkedTrackColor = ElegantBlue
                                )
                            )
                        }

                        Divider(color = DarkBorderBlack, modifier = Modifier.padding(vertical = 10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = OmniRouteGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Zero Cloud Telemetry: 100% of routing and metrics stay on-device",
                                fontSize = 11.sp,
                                color = TextLightGray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkCardBlack,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, DarkBorderBlack, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextWhite
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = TextLightGray,
                maxLines = 1
            )
        }
    }
}
