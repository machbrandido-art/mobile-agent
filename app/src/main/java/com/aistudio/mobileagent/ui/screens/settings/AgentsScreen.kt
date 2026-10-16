package com.aistudio.mobileagent.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.mobileagent.data.repository.AppRepository
import com.aistudio.mobileagent.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val agents by AppRepository.agentsFlow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agents & Personas", fontWeight = FontWeight.Bold, color = TextWhite) },
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
                    text = "Specialized agents fine-tuned for specific software engineering workflows.",
                    fontSize = 13.sp,
                    color = TextLightGray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(agents) { agent ->
                var expanded by remember { mutableStateOf(false) }

                Surface(
                    color = DarkCardBlack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorderBlack, RoundedCornerShape(12.dp))
                        .clickable { expanded = !expanded }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = when (agent.iconName) {
                                    "Code" -> Icons.Default.Code
                                    "Terminal" -> Icons.Default.Terminal
                                    "AccountTree" -> Icons.Default.AccountTree
                                    else -> Icons.Default.SmartToy
                                },
                                contentDescription = null,
                                tint = ElegantBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = agent.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Default Mode: ${agent.defaultMode.label}",
                                    fontSize = 11.sp,
                                    color = ElegantBlue
                                )
                            }
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = TextLightGray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = agent.description,
                            fontSize = 12.sp,
                            color = TextLightGray,
                            lineHeight = 16.sp
                        )

                        AnimatedVisibility(visible = expanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                Text(
                                    text = "System Prompt:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextLightGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = PureBlack,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, DarkBorderBlack, RoundedCornerShape(6.dp))
                                ) {
                                    Text(
                                        text = agent.systemPrompt,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = EmeraldSuccess,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
