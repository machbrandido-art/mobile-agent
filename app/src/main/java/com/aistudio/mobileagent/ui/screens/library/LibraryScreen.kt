package com.aistudio.mobileagent.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.mobileagent.data.model.SavedPrompt
import com.aistudio.mobileagent.data.model.WorkspaceDocument
import com.aistudio.mobileagent.data.repository.AppRepository
import com.aistudio.mobileagent.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBack: () -> Unit,
    onUsePromptInChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Saved Prompts", "Workspace Files", "Artifacts")

    val prompts by AppRepository.promptsFlow.collectAsState()
    val documents by AppRepository.docsFlow.collectAsState()

    var showAddPromptDialog by remember { mutableStateOf(false) }
    var viewingDocument by remember { mutableStateOf<WorkspaceDocument?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library & Artifacts", fontWeight = FontWeight.Bold, color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = { showAddPromptDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Prompt", tint = ElegantBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureBlack)
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
            // Tab Selector (Material Black)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkCardBlack,
                contentColor = ElegantBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ElegantBlue
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) ElegantBlue else TextLightGray
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Saved Prompts Tab
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(prompts) { prompt ->
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
                                        Text(
                                            text = prompt.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = TextWhite,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            color = DarkElevatedCard,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = prompt.category,
                                                fontSize = 11.sp,
                                                color = ElegantBlue,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = prompt.content,
                                        fontSize = 13.sp,
                                        color = TextLightGray,
                                        lineHeight = 18.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        IconButton(
                                            onClick = { AppRepository.deleteSavedPrompt(prompt.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = RoseError,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                onUsePromptInChat(prompt.content)
                                                onBack()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ElegantBlue,
                                                contentColor = PureBlack
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Use in Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Workspace Files Tab
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(documents) { doc ->
                            Surface(
                                color = DarkCardBlack,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, DarkBorderBlack, RoundedCornerShape(12.dp))
                                    .clickable { viewingDocument = doc }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when {
                                            doc.name.endsWith(".md") -> Icons.Default.Description
                                            doc.name.endsWith(".json") -> Icons.Default.DataObject
                                            else -> Icons.Default.Code
                                        },
                                        contentDescription = null,
                                        tint = ElegantBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = doc.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextWhite
                                        )
                                        Text(
                                            text = "${doc.path} • ${doc.sizeBytes} bytes",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = "View",
                                        tint = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Artifacts Tab
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Surface(
                                color = DarkCardBlack,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, DarkBorderBlack, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberWarning)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Generated Application Artifacts", fontWeight = FontWeight.Bold, color = TextWhite)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Mobile Agent compiles and preserves code files, execution summaries, and system logs locally on your device storage with complete zero-telemetry privacy.",
                                        fontSize = 13.sp,
                                        color = TextLightGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Prompt Dialog
    if (showAddPromptDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newContent by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf("Coding") }

        AlertDialog(
            onDismissRequest = { showAddPromptDialog = false },
            title = { Text("Save New Prompt", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ElegantBlue,
                            unfocusedBorderColor = DarkBorderBlack
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("Category") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ElegantBlue,
                            unfocusedBorderColor = DarkBorderBlack
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        label = { Text("Prompt instructions") },
                        maxLines = 4,
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
                        if (newTitle.isNotBlank() && newContent.isNotBlank()) {
                            AppRepository.addSavedPrompt(newTitle, newContent, newCategory)
                        }
                        showAddPromptDialog = false
                    }
                ) {
                    Text("Save", color = ElegantBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPromptDialog = false }) {
                    Text("Cancel", color = TextLightGray)
                }
            },
            containerColor = DarkCardBlack
        )
    }

    // View Document Dialog
    if (viewingDocument != null) {
        val doc = viewingDocument!!
        AlertDialog(
            onDismissRequest = { viewingDocument = null },
            title = {
                Text(
                    text = doc.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            },
            text = {
                Surface(
                    color = PureBlack,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                        .border(1.dp, DarkBorderBlack, RoundedCornerShape(8.dp))
                ) {
                    LazyColumn(modifier = Modifier.padding(12.dp)) {
                        item {
                            Text(
                                text = doc.content,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = EmeraldSuccess,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewingDocument = null }) {
                    Text("Close", color = ElegantBlue)
                }
            },
            containerColor = DarkCardBlack
        )
    }
}
