package com.aistudio.mobileagent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.mobileagent.data.model.Conversation
import com.aistudio.mobileagent.data.repository.AppRepository
import com.aistudio.mobileagent.ui.theme.*

@Composable
fun AppDrawerContent(
    currentConversationId: String,
    onSelectConversation: (String) -> Unit,
    onNewChat: () -> Unit,
    onNavigateToCombos: () -> Unit,
    onNavigateToOmniRouteDashboard: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToTerminal: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val conversations by AppRepository.conversationsFlow.collectAsState()
    var showRenameDialog by remember { mutableStateOf<Conversation?>(null) }
    var renameText by remember { mutableStateOf("") }

    val pinnedList = conversations.filter { it.isPinned }
    val regularList = conversations.filter { !it.isPinned }

    ModalDrawerSheet(
        modifier = modifier.width(320.dp),
        drawerContainerColor = PureBlack
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Header (Material Black)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ElegantBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "App Icon",
                        tint = PureBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mobile Agent",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = OmniRouteOrange.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "OMNI",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = OmniRouteOrange,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "OmniRoute & On-Device Gateway",
                        fontSize = 11.sp,
                        color = TextLightGray
                    )
                }
            }

            // New Chat Button
            Button(
                onClick = {
                    onNewChat()
                    onCloseDrawer()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElegantBlue,
                    contentColor = PureBlack
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("drawer_new_chat_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Chat", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Chat", fontWeight = FontWeight.Bold)
            }

            Divider(color = DarkBorderBlack, modifier = Modifier.padding(vertical = 8.dp))

            // Conversation Lists
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                if (pinnedList.isNotEmpty()) {
                    item {
                        Text(
                            text = "PINNED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantBlue,
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                        )
                    }
                    items(pinnedList) { conv ->
                        ConversationDrawerItem(
                            conversation = conv,
                            isSelected = conv.id == currentConversationId,
                            onSelect = {
                                onSelectConversation(conv.id)
                                onCloseDrawer()
                            },
                            onTogglePin = { AppRepository.togglePinConversation(conv.id) },
                            onRename = {
                                renameText = conv.title
                                showRenameDialog = conv
                            },
                            onDelete = { AppRepository.deleteConversation(conv.id) }
                        )
                    }
                }

                item {
                    Text(
                        text = "RECENT CHATS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp, start = 8.dp)
                    )
                }

                items(regularList) { conv ->
                    ConversationDrawerItem(
                        conversation = conv,
                        isSelected = conv.id == currentConversationId,
                        onSelect = {
                            onSelectConversation(conv.id)
                            onCloseDrawer()
                        },
                        onTogglePin = { AppRepository.togglePinConversation(conv.id) },
                        onRename = {
                            renameText = conv.title
                            showRenameDialog = conv
                        },
                        onDelete = { AppRepository.deleteConversation(conv.id) }
                    )
                }
            }

            Divider(color = DarkBorderBlack)

            // Bottom Navigation Links (Including OmniRoute Ported Features)
            Column(modifier = Modifier.padding(8.dp)) {
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.AltRoute, contentDescription = null, tint = ElegantBlue) },
                    label = { Text("OmniRoute Combos", color = TextWhite, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        onCloseDrawer()
                        onNavigateToCombos()
                    },
                    modifier = Modifier.padding(vertical = 1.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Hub, contentDescription = null, tint = OmniRouteOrange) },
                    label = { Text("OmniRoute Gateway Hub", color = TextWhite, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        onCloseDrawer()
                        onNavigateToOmniRouteDashboard()
                    },
                    modifier = Modifier.padding(vertical = 1.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Folder, contentDescription = null, tint = ElegantPurple) },
                    label = { Text("Library & Artifacts", color = TextLightGray) },
                    selected = false,
                    onClick = {
                        onCloseDrawer()
                        onNavigateToLibrary()
                    },
                    modifier = Modifier.padding(vertical = 1.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Terminal, contentDescription = null, tint = EmeraldSuccess) },
                    label = { Text("Terminal Console", color = TextLightGray) },
                    selected = false,
                    onClick = {
                        onCloseDrawer()
                        onNavigateToTerminal()
                    },
                    modifier = Modifier.padding(vertical = 1.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = TextLightGray) },
                    label = { Text("Settings & Hub", color = TextLightGray) },
                    selected = false,
                    onClick = {
                        onCloseDrawer()
                        onNavigateToSettings()
                    },
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog != null) {
        val target = showRenameDialog!!
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Chat", color = TextWhite) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = ElegantBlue,
                        unfocusedBorderColor = DarkBorderBlack
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            AppRepository.renameConversation(target.id, renameText)
                        }
                        showRenameDialog = null
                    }
                ) {
                    Text("Save", color = ElegantBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) {
                    Text("Cancel", color = TextLightGray)
                }
            },
            containerColor = DarkCardBlack
        )
    }
}

@Composable
fun ConversationDrawerItem(
    conversation: Conversation,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onTogglePin: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = if (isSelected) DarkElevatedCard else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .border(1.dp, if (isSelected) DarkBorderBlack else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (conversation.isPinned) Icons.Default.PushPin else Icons.Default.ChatBubbleOutline,
                contentDescription = null,
                tint = if (isSelected) ElegantBlue else TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = conversation.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp,
                color = if (isSelected) TextWhite else TextLightGray,
                modifier = Modifier.weight(1f)
            )

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(DarkCardBlack)
                ) {
                    DropdownMenuItem(
                        text = { Text(if (conversation.isPinned) "Unpin" else "Pin", color = TextWhite) },
                        leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = TextLightGray) },
                        onClick = {
                            showMenu = false
                            onTogglePin()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename", color = TextWhite) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TextLightGray) },
                        onClick = {
                            showMenu = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = RoseError) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = RoseError) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
