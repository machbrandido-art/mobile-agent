package com.aistudio.mobileagent.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.mobileagent.data.model.AiModelInfo
import com.aistudio.mobileagent.data.model.ModelCombo
import com.aistudio.mobileagent.data.model.RoutingStrategy
import com.aistudio.mobileagent.data.model.TokenCompressionMode
import com.aistudio.mobileagent.data.repository.AppRepository
import com.aistudio.mobileagent.data.repository.OmniRouteRepository
import com.aistudio.mobileagent.ui.theme.*

/**
 * OmniRoute ComboBox Component
 * Replicates the authentic Combo dropdown from OmniRoute with:
 * - Searchable model & combo list
 * - Chained model visual pills [Model A -> Model B -> Model C]
 * - Routing strategy indicator chips (Priority Failover, Round Robin, Cost Optimized, etc.)
 * - Architecture compatibility badges (32-bit & 64-bit support)
 * - In-place New Combo creation
 * - Material Black AMOLED styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniRouteComboBox(
    selectedRouteId: String,
    onSelectRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val combos by OmniRouteRepository.combosFlow.collectAsState()
    val availableModels = AppRepository.availableModels

    val activeCombo = combos.find { it.id == selectedRouteId }
    val activeSingleModel = availableModels.find { it.id == selectedRouteId }

    var showCreateComboDialog by remember { mutableStateOf(false) }

    // Dropdown Trigger Pill
    Surface(
        color = DarkCardBlack,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .border(1.dp, if (activeCombo != null) ElegantBlue.copy(alpha = 0.5f) else DarkBorderBlack, RoundedCornerShape(10.dp))
            .clickable { expanded = true }
            .testTag("omniroute_combobox_trigger")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = if (activeCombo != null) Icons.Default.AltRoute else Icons.Default.Tune,
                contentDescription = null,
                tint = if (activeCombo != null) ElegantBlue else TextLightGray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = activeCombo?.name ?: activeSingleModel?.name ?: selectedRouteId,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (activeCombo != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = ElegantBlue.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "COMBO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantBlue,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                if (activeCombo != null) {
                    Text(
                        text = "${activeCombo.strategy.label} • ${activeCombo.modelIds.size} models",
                        fontSize = 10.sp,
                        color = TextLightGray
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Expand Combo",
                tint = TextLightGray,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    // Modal Bottom Sheet with OmniRoute Combo List
    if (expanded) {
        var searchQuery by remember { mutableStateOf("") }
        var filterTab by remember { mutableStateOf(0) } // 0: All, 1: Combos, 2: On-Device, 3: Cloud
        val tabs = listOf("All", "Combos", "On-Device", "Cloud")

        ModalBottomSheet(
            onDismissRequest = { expanded = false },
            containerColor = PureBlack,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(DarkBorderBlack)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AltRoute, contentDescription = null, tint = ElegantBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OmniRoute Combos & Models",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                        Text(
                            text = "Intelligent multi-model chains & 32/64-bit failover routing",
                            fontSize = 11.sp,
                            color = TextLightGray
                        )
                    }

                    // Create Combo Button
                    Button(
                        onClick = {
                            expanded = false
                            showCreateComboDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantBlue,
                            contentColor = PureBlack
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Combo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search models, combos, providers...", fontSize = 13.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = ElegantBlue,
                        unfocusedBorderColor = DarkBorderBlack,
                        focusedContainerColor = DarkSurfaceBlack,
                        unfocusedContainerColor = DarkSurfaceBlack
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tabs.indices.toList()) { idx ->
                        Surface(
                            color = if (filterTab == idx) ElegantBlue else DarkCardBlack,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .border(1.dp, if (filterTab == idx) Color.Transparent else DarkBorderBlack, RoundedCornerShape(8.dp))
                                .clickable { filterTab = idx }
                        ) {
                            Text(
                                text = tabs[idx],
                                fontSize = 12.sp,
                                fontWeight = if (filterTab == idx) FontWeight.Bold else FontWeight.Normal,
                                color = if (filterTab == idx) PureBlack else TextWhite,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Combo & Model List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .padding(bottom = 16.dp)
                ) {
                    // Show Combos
                    if (filterTab == 0 || filterTab == 1) {
                        val filteredCombos = combos.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                                    it.description.contains(searchQuery, ignoreCase = true) ||
                                    it.strategy.label.contains(searchQuery, ignoreCase = true)
                        }

                        if (filteredCombos.isNotEmpty()) {
                            item {
                                Text(
                                    text = "OMNIRoute COMBOS (CHAINS)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantBlue,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            items(filteredCombos) { combo ->
                                val isSelected = combo.id == selectedRouteId
                                ComboListItem(
                                    combo = combo,
                                    isSelected = isSelected,
                                    onClick = {
                                        onSelectRoute(combo.id)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Show Individual Models
                    if (filterTab != 1) {
                        val filteredModels = availableModels.filter { model ->
                            val matchSearch = model.name.contains(searchQuery, ignoreCase = true) ||
                                    model.provider.contains(searchQuery, ignoreCase = true)
                            val matchTab = when (filterTab) {
                                2 -> model.isLocal
                                3 -> !model.isLocal
                                else -> true
                            }
                            matchSearch && matchTab
                        }

                        if (filteredModels.isNotEmpty()) {
                            item {
                                Text(
                                    text = "STANDALONE MODELS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextLightGray,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }

                            items(filteredModels) { model ->
                                val isSelected = model.id == selectedRouteId
                                SingleModelListItem(
                                    model = model,
                                    isSelected = isSelected,
                                    onClick = {
                                        onSelectRoute(model.id)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create New Combo Dialog
    if (showCreateComboDialog) {
        CreateComboDialog(
            availableModels = availableModels,
            onDismiss = { showCreateComboDialog = false },
            onCreate = { newCombo ->
                onSelectRoute(newCombo.id)
                showCreateComboDialog = false
            }
        )
    }
}

@Composable
fun ComboListItem(
    combo: ModelCombo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) DarkElevatedCard else DarkCardBlack,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) ElegantBlue else DarkBorderBlack,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AltRoute, contentDescription = null, tint = ElegantBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = combo.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextWhite
                    )
                }

                Surface(
                    color = when (combo.strategy) {
                        RoutingStrategy.PRIORITY_FAILOVER -> OmniRouteOrange.copy(alpha = 0.2f)
                        RoutingStrategy.COST_OPTIMIZED -> OmniRouteGreen.copy(alpha = 0.2f)
                        RoutingStrategy.LOWEST_LATENCY -> OmniRouteBlue.copy(alpha = 0.2f)
                        else -> ElegantPurple.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = combo.strategy.label.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (combo.strategy) {
                            RoutingStrategy.PRIORITY_FAILOVER -> OmniRouteOrange
                            RoutingStrategy.COST_OPTIMIZED -> OmniRouteGreen
                            RoutingStrategy.LOWEST_LATENCY -> OmniRouteBlue
                            else -> ElegantPurple
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = combo.description,
                fontSize = 11.sp,
                color = TextLightGray,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Visual Model Chain
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Chain: ",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    items(combo.modelIds.indices.toList()) { idx ->
                        val mId = combo.modelIds[idx]
                        Surface(
                            color = PureBlack,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.border(1.dp, DarkBorderBlack, RoundedCornerShape(4.dp))
                        ) {
                            Text(
                                text = mId,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ElegantBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (idx < combo.modelIds.size - 1) {
                            Text(text = "➔", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (combo.fallbackOnHttp429) {
                    BadgeChip(label = "429 Failover")
                }
                if (combo.compressionMode != TokenCompressionMode.NONE) {
                    BadgeChip(label = "RTK: ${combo.compressionMode.label}", highlight = true)
                }
                BadgeChip(label = "32/64-bit ABI")
            }
        }
    }
}

@Composable
fun SingleModelListItem(
    model: AiModelInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) DarkElevatedCard else DarkCardBlack,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) ElegantCyan else DarkBorderBlack,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (model.isLocal) Icons.Default.Memory else Icons.Default.Cloud,
                contentDescription = null,
                tint = if (model.isLocal) EmeraldSuccess else ElegantCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextWhite
                )
                Text(
                    text = "${model.provider} • ${model.parameterCount} params",
                    fontSize = 11.sp,
                    color = TextLightGray
                )
            }
            Surface(
                color = if (model.isLocal) EmeraldSuccess.copy(alpha = 0.2f) else DarkBorderBlack,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (model.isLocal) "LOCAL" else "CLOUD",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (model.isLocal) EmeraldSuccess else TextLightGray,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun CreateComboDialog(
    availableModels: List<AiModelInfo>,
    onDismiss: () -> Unit,
    onCreate: (ModelCombo) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedStrategy by remember { mutableStateOf(RoutingStrategy.PRIORITY_FAILOVER) }
    var selectedCompression by remember { mutableStateOf(TokenCompressionMode.LIGHT_RTK) }
    val selectedModelList = remember { mutableStateListOf("gemini-2.5-flash", "gemma-e2b") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AltRoute, contentDescription = null, tint = ElegantBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create OmniRoute Combo", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Combo Name (e.g. Code Beast)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ElegantBlue,
                            unfocusedBorderColor = DarkBorderBlack
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Routing Goal") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ElegantBlue,
                            unfocusedBorderColor = DarkBorderBlack
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Routing Strategy:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElegantBlue)
                    RoutingStrategy.values().forEach { strat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStrategy = strat }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedStrategy == strat,
                                onClick = { selectedStrategy = strat },
                                colors = RadioButtonDefaults.colors(selectedColor = ElegantBlue)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(strat.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                                Text(strat.description, fontSize = 10.sp, color = TextLightGray)
                            }
                        }
                    }
                }

                item {
                    Text("Select Models in Failover Chain:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElegantBlue)
                    availableModels.forEach { m ->
                        val isChecked = selectedModelList.contains(m.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) {
                                        if (selectedModelList.size > 1) selectedModelList.remove(m.id)
                                    } else {
                                        selectedModelList.add(m.id)
                                    }
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { check ->
                                    if (check) selectedModelList.add(m.id)
                                    else if (selectedModelList.size > 1) selectedModelList.remove(m.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = ElegantBlue)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(m.name, fontSize = 12.sp, color = TextWhite)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && selectedModelList.isNotEmpty()) {
                        val created = OmniRouteRepository.createCombo(
                            name = name,
                            description = description.ifBlank { "Custom OmniRoute combo with ${selectedStrategy.label}" },
                            strategy = selectedStrategy,
                            modelIds = selectedModelList.toList(),
                            compressionMode = selectedCompression
                        )
                        onCreate(created)
                    }
                }
            ) {
                Text("Create & Route", color = ElegantBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextLightGray)
            }
        },
        containerColor = DarkCardBlack
    )
}
