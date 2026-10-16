package com.aistudio.mobileagent.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.mobileagent.data.engine.AgentExecutionEngine
import com.aistudio.mobileagent.data.model.AgentMode
import com.aistudio.mobileagent.data.repository.AppRepository
import com.aistudio.mobileagent.data.repository.OmniRouteRepository
import com.aistudio.mobileagent.ui.components.ChatMessageItem
import com.aistudio.mobileagent.ui.components.OmniRouteComboBox
import com.aistudio.mobileagent.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val conversations by AppRepository.conversationsFlow.collectAsState()
    val selectedId by AppRepository.selectedConversationId.collectAsState()
    val messages by AppRepository.messagesFlow.collectAsState()
    val isRunning by AgentExecutionEngine.isAgentRunning.collectAsState()
    val agentStatus by AgentExecutionEngine.currentAgentStatus.collectAsState()
    val selectedRouteId by OmniRouteRepository.selectedRouteId.collectAsState()

    val currentConversation = conversations.find { it.id == selectedId } ?: conversations.firstOrNull()

    var promptInput by remember { mutableStateOf("") }
    var selectedAttachments by remember { mutableStateOf<List<String>>(emptyList()) }
    var showModeDropdown by remember { mutableStateOf(false) }
    var isVoiceRecording by remember { mutableStateOf(false) }

    // Auto scroll to bottom when messages update
    LaunchedEffect(messages.size, messages.lastOrNull()?.content?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentConversation?.title ?: "Mobile Agent",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            maxLines = 1
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            // Mode Pill Dropdown
                            Box {
                                Surface(
                                    color = when (currentConversation?.agentMode) {
                                        AgentMode.BUILD -> EmeraldSuccess.copy(alpha = 0.2f)
                                        AgentMode.PLAN -> ElegantPurple.copy(alpha = 0.2f)
                                        else -> ElegantBlue.copy(alpha = 0.2f)
                                    },
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.clickable { showModeDropdown = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = currentConversation?.agentMode?.label ?: "Build",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (currentConversation?.agentMode) {
                                                AgentMode.BUILD -> EmeraldSuccess
                                                AgentMode.PLAN -> ElegantPurple
                                                else -> ElegantBlue
                                            }
                                        )
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = TextLightGray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showModeDropdown,
                                    onDismissRequest = { showModeDropdown = false },
                                    modifier = Modifier.background(DarkCardBlack)
                                ) {
                                    AgentMode.values().forEach { mode ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(mode.label, fontWeight = FontWeight.Bold, color = TextWhite)
                                                    Text(mode.description, fontSize = 11.sp, color = TextLightGray)
                                                }
                                            },
                                            onClick = {
                                                currentConversation?.let {
                                                    AppRepository.updateConversationMode(it.id, mode)
                                                }
                                                showModeDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // OmniRoute ComboBox Trigger directly in Header
                            OmniRouteComboBox(
                                selectedRouteId = currentConversation?.modelId ?: selectedRouteId,
                                onSelectRoute = { newRoute ->
                                    currentConversation?.let {
                                        AppRepository.updateConversationModel(it.id, newRoute)
                                    }
                                    OmniRouteRepository.selectRoute(newRoute)
                                }
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("chat_open_drawer_button")
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Drawer", tint = TextWhite)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val activeRoute = currentConversation?.modelId ?: selectedRouteId
                        AppRepository.createConversation(modelId = activeRoute)
                    }) {
                        Icon(Icons.Default.AddComment, contentDescription = "New Chat", tint = ElegantBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureBlack
                )
            )
        },
        containerColor = PureBlack,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Running Banner
            AnimatedVisibility(visible = isRunning) {
                Surface(
                    color = DarkCardBlack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorderBlack)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp),
                                color = ElegantBlue
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = agentStatus ?: "Agent thinking...",
                                fontSize = 12.sp,
                                color = ElegantBlue
                            )
                        }

                        Button(
                            onClick = { AgentExecutionEngine.stopAgent() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoseError.copy(alpha = 0.2f),
                                contentColor = RoseError
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Stop", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Message Scroller List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatMessageItem(message = msg)
                }
            }

            // Composer Section (Material Black)
            Surface(
                color = DarkCardBlack,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorderBlack, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Quick Tips & Prompts
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        val tips = listOf(
                            "Simulate HTTP 429 failover",
                            "Verify 32-bit & 64-bit parity",
                            "Inspect active OmniRoute combo",
                            "Compress prompt via RTK"
                        )
                        items(tips) { tip ->
                            Surface(
                                color = DarkElevatedCard,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .border(1.dp, DarkBorderBlack, RoundedCornerShape(12.dp))
                                    .clickable {
                                        promptInput = tip
                                    }
                            ) {
                                Text(
                                    text = tip,
                                    fontSize = 11.sp,
                                    color = TextWhite,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Attached Files Chips
                    if (selectedAttachments.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            selectedAttachments.forEach { att ->
                                Surface(
                                    color = ElegantBlue.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(att, fontSize = 11.sp, color = ElegantBlue)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = ElegantBlue,
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clickable {
                                                    selectedAttachments = selectedAttachments - att
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Voice recording indicator
                    if (isVoiceRecording) {
                        Surface(
                            color = RoseError.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = RoseError
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Listening via Whisper on-device...", fontSize = 12.sp, color = RoseError)
                            }
                        }
                    }

                    // Input bar row
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Attach file button
                        IconButton(
                            onClick = {
                                if (!selectedAttachments.contains("omniroute_manifest.json")) {
                                    selectedAttachments = selectedAttachments + "omniroute_manifest.json"
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = "Attach file",
                                tint = TextLightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Voice STT button
                        IconButton(
                            onClick = {
                                isVoiceRecording = !isVoiceRecording
                                if (isVoiceRecording) {
                                    promptInput = "Simulate 429 rate limit failover across OmniRoute combo"
                                    isVoiceRecording = false
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Voice input",
                                tint = if (isVoiceRecording) RoseError else TextLightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Text input field
                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            placeholder = {
                                Text(
                                    text = if (currentConversation?.agentMode == AgentMode.PLAN) "Describe task to plan..." else "Ask or assign a task...",
                                    fontSize = 14.sp,
                                    color = TextMuted
                                )
                            },
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ElegantBlue,
                                unfocusedBorderColor = DarkBorderBlack,
                                focusedContainerColor = PureBlack,
                                unfocusedContainerColor = PureBlack
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                                .testTag("composer_text_input")
                        )

                        // Send / Stop button
                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isRunning) RoseError
                                    else if (promptInput.isNotBlank()) ElegantBlue
                                    else DarkElevatedCard
                                )
                                .clickable {
                                    if (isRunning) {
                                        AgentExecutionEngine.stopAgent()
                                    } else if (promptInput.isNotBlank()) {
                                        currentConversation?.let { conv ->
                                            AgentExecutionEngine.submitUserPrompt(
                                                conversationId = conv.id,
                                                prompt = promptInput,
                                                attachments = selectedAttachments,
                                                modelId = conv.modelId,
                                                mode = conv.agentMode
                                            )
                                            promptInput = ""
                                            selectedAttachments = emptyList()
                                        }
                                    }
                                }
                                .testTag("composer_send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.ArrowUpward,
                                contentDescription = if (isRunning) "Stop" else "Send",
                                tint = if (isRunning || promptInput.isNotBlank()) PureBlack else TextLightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
