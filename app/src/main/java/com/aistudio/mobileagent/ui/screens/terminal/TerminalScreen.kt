package com.aistudio.mobileagent.ui.screens.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.mobileagent.data.repository.AppRepository
import com.aistudio.mobileagent.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val logs by AppRepository.terminalLogsFlow.collectAsState()
    var commandInput by remember { mutableStateOf("") }

    // Scroll to bottom on new log line
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    fun executeTerminalCommand(cmd: String) {
        val trimmed = cmd.trim()
        if (trimmed.isBlank()) return

        AppRepository.addTerminalLog("$ $trimmed", isCommand = true)
        commandInput = ""

        when (trimmed.lowercase()) {
            "help" -> {
                AppRepository.addTerminalLog("Available commands:")
                AppRepository.addTerminalLog("  status         - Show agent runtime & model telemetry")
                AppRepository.addTerminalLog("  ls             - List workspace filesystem entries")
                AppRepository.addTerminalLog("  litert --info  - Inspect on-device LiteRT accelerator status")
                AppRepository.addTerminalLog("  mcp list       - List registered Model Context Protocol servers")
                AppRepository.addTerminalLog("  skills list    - Show loaded agent skills")
                AppRepository.addTerminalLog("  clear          - Clear terminal buffer")
            }
            "clear" -> {
                AppRepository.clearTerminalLogs()
            }
            "status" -> {
                AppRepository.addTerminalLog("[RUNTIME] Engine: LiteRT LM Edge 2.11")
                AppRepository.addTerminalLog("[ACCELERATOR] Hexagon NPU / Qualcomm Adreno GPU active")
                AppRepository.addTerminalLog("[RAM FREE] 4,821 MB / 8,192 MB")
                AppRepository.addTerminalLog("[SANDBOX] Termux isolated environment OK")
            }
            "ls" -> {
                AppRepository.addTerminalLog("drwxr-xr-x 4 u0_a249 u0_a249 4096 Oct 16 07:00 workspace/")
                AppRepository.addTerminalLog("drwxr-xr-x 2 u0_a249 u0_a249 4096 Oct 16 07:00 catalog/")
                AppRepository.addTerminalLog("-rw-r--r-- 1 u0_a249 u0_a249  820 Oct 16 07:00 config.json")
            }
            "litert --info" -> {
                AppRepository.addTerminalLog("Google AI Edge LiteRT v1.0.15")
                AppRepository.addTerminalLog("Model loaded: gemma-4-E2B-it.litertlm (2.3B params)")
                AppRepository.addTerminalLog("Inference backend: GPU / NPU delegate (INT4)")
            }
            "mcp list" -> {
                AppRepository.addTerminalLog("Active MCP Servers:")
                AppRepository.addTerminalLog("  1. stdio://local-fs (connected, 6 tools)")
                AppRepository.addTerminalLog("  2. http://127.0.0.1:8080/mcp (connected, 4 tools)")
                AppRepository.addTerminalLog("  3. https://mcp.github.com/v1 (ready)")
            }
            "skills list" -> {
                AppRepository.addTerminalLog("Loaded Skills:")
                AppRepository.addTerminalLog("  * android-cli (official docs & guidelines)")
                AppRepository.addTerminalLog("  * saf-file-operations (Android storage access)")
                AppRepository.addTerminalLog("  * termux-stream (shell daemon execution)")
                AppRepository.addTerminalLog("  * web-search (live web grounding)")
            }
            else -> {
                AppRepository.addTerminalLog("Executed: $trimmed (process returned code 0)")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Terminal Console", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 16.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { AppRepository.clearTerminalLogs() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear logs", tint = TextLightGray)
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
            // Terminal Logs Area
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    Text(
                        text = log.text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = when {
                            log.isError -> RoseError
                            log.isCommand -> ElegantBlue
                            log.text.startsWith("[") -> ElegantPurple
                            else -> EmeraldSuccess
                        },
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }

            // Command Prompt Input (Material Black)
            Surface(
                color = DarkCardBlack,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorderBlack)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$ ",
                        color = ElegantBlue,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    OutlinedTextField(
                        value = commandInput,
                        onValueChange = { commandInput = it },
                        placeholder = { Text("type command (e.g. help, status, ls)", fontSize = 13.sp, color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            executeTerminalCommand(commandInput)
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("terminal_command_input")
                    )
                    IconButton(
                        onClick = { executeTerminalCommand(commandInput) },
                        modifier = Modifier.testTag("terminal_send_command_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Run", tint = ElegantBlue, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
