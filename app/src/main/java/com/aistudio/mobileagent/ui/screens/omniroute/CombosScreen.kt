package com.aistudio.mobileagent.ui.screens.omniroute

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.mobileagent.data.model.ModelCombo
import com.aistudio.mobileagent.data.repository.AppRepository
import com.aistudio.mobileagent.data.repository.OmniRouteRepository
import com.aistudio.mobileagent.ui.components.ComboListItem
import com.aistudio.mobileagent.ui.components.CreateComboDialog
import com.aistudio.mobileagent.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombosScreen(
    onBack: () -> Unit,
    onSelectComboForChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val combos by OmniRouteRepository.combosFlow.collectAsState()
    val selectedRouteId by OmniRouteRepository.selectedRouteId.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AltRoute, contentDescription = null, tint = ElegantBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("OmniRoute Combos", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 16.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Combo", tint = ElegantBlue)
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
                Surface(
                    color = DarkCardBlack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorderBlack, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Hub, contentDescription = null, tint = OmniRouteOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("The OmniRoute Combo Engine", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Combos chain multiple AI providers (e.g. Gemini, Claude, GPT, LiteRT On-Device) into a unified resilient route. If any provider returns HTTP 429 or quota exhaustion, OmniRoute automatically fails over to the next model in the chain without interrupting your agent.",
                            fontSize = 12.sp,
                            color = TextLightGray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text(
                        text = "REGISTERED COMBOS (${combos.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElegantBlue
                    )
                    Text(
                        text = "Tap to make active",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            items(combos, key = { it.id }) { combo ->
                val isSelected = combo.id == selectedRouteId
                Column {
                    ComboListItem(
                        combo = combo,
                        isSelected = isSelected,
                        onClick = {
                            OmniRouteRepository.selectRoute(combo.id)
                            onSelectComboForChat(combo.id)
                        }
                    )

                    if (!combo.isBuiltIn) {
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, end = 4.dp)
                        ) {
                            TextButton(
                                onClick = { OmniRouteRepository.deleteCombo(combo.id) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = RoseError, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete Combo", fontSize = 11.sp, color = RoseError)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateComboDialog(
            availableModels = AppRepository.availableModels,
            onDismiss = { showCreateDialog = false },
            onCreate = { newCombo ->
                OmniRouteRepository.selectRoute(newCombo.id)
                onSelectComboForChat(newCombo.id)
                showCreateDialog = false
            }
        )
    }
}
