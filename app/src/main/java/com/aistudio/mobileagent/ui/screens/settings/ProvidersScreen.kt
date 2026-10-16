package com.aistudio.mobileagent.ui.screens.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.mobileagent.data.model.ProviderSetting
import com.aistudio.mobileagent.data.repository.AppRepository
import com.aistudio.mobileagent.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val providers by AppRepository.providersFlow.collectAsState()
    var editingProvider by remember { mutableStateOf<ProviderSetting?>(null) }
    var apiKeyText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Providers", fontWeight = FontWeight.Bold, color = TextWhite) },
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
                Surface(
                    color = DarkCardBlack,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorderBlack, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("On-Device & OmniRoute Multi-Provider", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Mobile Agent defaults to local execution on Qualcomm Adreno GPU & Hexagon NPU using Google AI Edge LiteRT, keeping your agent runs 100% private and offline on both 32-bit and 64-bit devices.",
                            fontSize = 12.sp,
                            color = TextLightGray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            items(providers) { provider ->
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
                                Icon(
                                    imageVector = if (provider.isLocal) Icons.Default.Memory else Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = if (provider.isLocal) EmeraldSuccess else ElegantBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = provider.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = if (provider.isLocal) "Offline / On-Device" else "Cloud API",
                                        fontSize = 11.sp,
                                        color = if (provider.isLocal) EmeraldSuccess else ElegantBlue
                                    )
                                }
                            }

                            if (!provider.isLocal) {
                                Button(
                                    onClick = {
                                        editingProvider = provider
                                        apiKeyText = provider.apiKey
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkElevatedCard,
                                        contentColor = ElegantBlue
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(if (provider.isConfigured) "Configured" else "Set Key", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = provider.description,
                            fontSize = 12.sp,
                            color = TextLightGray,
                            lineHeight = 16.sp
                        )

                        if (!provider.baseUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Endpoint: ${provider.baseUrl}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }

    if (editingProvider != null) {
        val prov = editingProvider!!
        AlertDialog(
            onDismissRequest = { editingProvider = null },
            title = { Text("${prov.name} API Key", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter API Key for ${prov.name}. Stored securely on your device.", fontSize = 12.sp, color = TextLightGray)
                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                        placeholder = { Text("sk-...", color = TextMuted) },
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
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        AppRepository.updateProviderApiKey(prov.id, apiKeyText)
                        editingProvider = null
                    }
                ) {
                    Text("Save", color = ElegantBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingProvider = null }) {
                    Text("Cancel", color = TextLightGray)
                }
            },
            containerColor = DarkCardBlack
        )
    }
}
