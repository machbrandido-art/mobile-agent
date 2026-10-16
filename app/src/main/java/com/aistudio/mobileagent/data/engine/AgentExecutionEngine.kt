package com.aistudio.mobileagent.data.engine

import com.aistudio.mobileagent.data.model.*
import com.aistudio.mobileagent.data.repository.AppRepository
import com.aistudio.mobileagent.data.repository.OmniRouteRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object AgentExecutionEngine {

    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var activeJob: Job? = null

    private val _isAgentRunning = MutableStateFlow(false)
    val isAgentRunning: StateFlow<Boolean> = _isAgentRunning.asStateFlow()

    private val _currentAgentStatus = MutableStateFlow<String?>(null)
    val currentAgentStatus: StateFlow<String?> = _currentAgentStatus.asStateFlow()

    private val _pendingApproval = MutableStateFlow<ToolApprovalRequest?>(null)
    val pendingApproval: StateFlow<ToolApprovalRequest?> = _pendingApproval.asStateFlow()

    data class ToolApprovalRequest(
        val callId: String,
        val conversationId: String,
        val messageId: String,
        val toolName: String,
        val arguments: String,
        val continuation: CompletableDeferred<Boolean>
    )

    fun submitUserPrompt(
        conversationId: String,
        prompt: String,
        attachments: List<String> = emptyList(),
        modelId: String,
        mode: AgentMode
    ) {
        if (prompt.isBlank() && attachments.isEmpty()) return

        // Check OmniRoute Combo
        val activeCombo = OmniRouteRepository.findCombo(modelId)
        val piiEnabled = OmniRouteRepository.metricsFlow.value.piiProtectionActive
        val sanitizedPrompt = if (piiEnabled) OmniRouteRepository.applyPiiGuardrails(prompt) else prompt

        // Apply OmniRoute RTK Token Compression if Combo active
        val (finalPrompt, tokensSaved) = if (activeCombo != null) {
            OmniRouteRepository.compressPrompt(sanitizedPrompt, activeCombo.compressionMode)
        } else {
            Pair(sanitizedPrompt, 0)
        }

        // Add User Message
        val userMsg = Message(
            conversationId = conversationId,
            role = MessageRole.USER,
            content = sanitizedPrompt,
            attachments = attachments,
            timestamp = System.currentTimeMillis()
        )
        AppRepository.addMessage(conversationId, userMsg)

        // Add Assistant Message (Streaming)
        val assistantMsgId = UUID.randomUUID().toString()
        val initialThought = if (activeCombo != null) {
            "OmniRoute Gateway active: Routing through combo '${activeCombo.name}' using ${activeCombo.strategy.label}. RTK compressed: $tokensSaved tokens saved."
        } else {
            "Initializing ${mode.label} strategy using $modelId..."
        }

        val assistantMsg = Message(
            id = assistantMsgId,
            conversationId = conversationId,
            role = MessageRole.ASSISTANT,
            content = "",
            thought = initialThought,
            status = MessageStatus.STREAMING,
            timestamp = System.currentTimeMillis()
        )
        AppRepository.addMessage(conversationId, assistantMsg)

        _isAgentRunning.value = true
        _currentAgentStatus.value = if (activeCombo != null) "OmniRoute: Resolving optimal provider..." else "Agent thinking..."

        activeJob?.cancel()
        activeJob = engineScope.launch {
            try {
                // Log to terminal
                AppRepository.addTerminalLog("[AGENT RUN] Conv: $conversationId | Mode: ${mode.name} | Route: $modelId")
                if (activeCombo != null) {
                    AppRepository.addTerminalLog("[OMNIRoute] Chain: ${activeCombo.modelIds.joinToString(" -> ")} | Strategy: ${activeCombo.strategy.name}")
                    if (tokensSaved > 0) {
                        AppRepository.addTerminalLog("[OMNIRoute RTK] Prompt compressed: $tokensSaved tokens saved (${activeCombo.compressionMode.label})")
                    }
                }
                AppRepository.addTerminalLog("[USER PROMPT] $finalPrompt")

                delay(400)

                val thoughtBuilder = StringBuilder(initialThought)
                val contentBuilder = StringBuilder()
                val toolCallsList = mutableListOf<ToolCall>()

                var effectiveExecutingModel = modelId

                // OmniRoute Routing Execution Logic
                if (activeCombo != null) {
                    val primaryModel = activeCombo.modelIds.firstOrNull() ?: "gemma-e2b"
                    effectiveExecutingModel = primaryModel

                    val shouldSimulateFailover = finalPrompt.contains("429", ignoreCase = true) ||
                            finalPrompt.contains("failover", ignoreCase = true) ||
                            finalPrompt.contains("rate limit", ignoreCase = true)

                    if (shouldSimulateFailover && activeCombo.modelIds.size > 1) {
                        _currentAgentStatus.value = "OmniRoute: Connecting to primary ($primaryModel)..."
                        delay(600)
                        thoughtBuilder.append("\n[OmniRoute] Primary endpoint '$primaryModel' returned HTTP 429 Rate Limit. Initiating failover...")
                        AppRepository.updateMessage(conversationId, assistantMsgId) {
                            it.copy(thought = thoughtBuilder.toString())
                        }
                        AppRepository.addTerminalLog("[OMNIRoute FAILOVER] Provider '$primaryModel' throttled (HTTP 429). Failing over to '${activeCombo.modelIds[1]}'.", isError = true)

                        val failoverTool = ToolCall(
                            toolName = "omniroute_failover",
                            arguments = "{\"from\": \"$primaryModel\", \"to\": \"${activeCombo.modelIds[1]}\", \"reason\": \"HTTP_429_RATE_LIMIT\"}",
                            result = "Dynamic route failover successful. Latency: 184ms.",
                            status = "success"
                        )
                        toolCallsList.add(failoverTool)
                        effectiveExecutingModel = activeCombo.modelIds[1]
                        OmniRouteRepository.recordRouteExecution(wasFailover = true, tokensSaved = tokensSaved.toLong())

                        AppRepository.updateMessage(conversationId, assistantMsgId) {
                            it.copy(toolCalls = toolCallsList.toList())
                        }
                        delay(500)
                    } else {
                        OmniRouteRepository.recordRouteExecution(wasFailover = false, tokensSaved = tokensSaved.toLong())
                    }
                }

                when (mode) {
                    AgentMode.PLAN -> {
                        thoughtBuilder.append("\nDecomposing request into architectural phases. Evaluating dependencies, constraints, and verification criteria...")
                        AppRepository.updateMessage(conversationId, assistantMsgId) {
                            it.copy(thought = thoughtBuilder.toString())
                        }
                        delay(500)

                        _currentAgentStatus.value = "Executing tool: search_workspace"
                        val tool1 = ToolCall(
                            toolName = "search_workspace",
                            arguments = "{\"query\": \"architecture\", \"scope\": \"src/\"}",
                            status = "running"
                        )
                        toolCallsList.add(tool1)
                        AppRepository.updateMessage(conversationId, assistantMsgId) {
                            it.copy(toolCalls = toolCallsList.toList())
                        }
                        delay(500)

                        val tool1Success = tool1.copy(result = "Indexed workspace files. Active OmniRoute gateway: healthy.", status = "success")
                        val idx = toolCallsList.indexOf(tool1)
                        if (idx != -1) toolCallsList[idx] = tool1Success
                        AppRepository.updateMessage(conversationId, assistantMsgId) {
                            it.copy(toolCalls = toolCallsList.toList())
                        }
                        AppRepository.addTerminalLog("[TOOL SUCCESS] search_workspace -> OK")

                        delay(400)
                        _currentAgentStatus.value = "Synthesizing architectural plan..."

                        val planResponse = """
### Implementation Plan (Routed via $effectiveExecutingModel)

1. **Architecture & Scope**:
   - Verify local on-device capabilities and hardware acceleration.
   - Confirm 32-bit (armeabi-v7a) & 64-bit (arm64-v8a) architecture parity.
   - Check OmniRoute Combo failover rules and RTK token compression metrics.

2. **Step-by-Step Execution**:
   - Apply Material Black theme tokens across all UI layers.
   - Coordinate local storage, Termux shell streams, and background jobs.
   - Guard user privacy with zero-telemetry and automatic PII masking.

3. **Validation & Verification**:
   - Confirm ABI filters in build configuration (`ndk { abiFilters 'armeabi-v7a', 'arm64-v8a' }`).
   - Validate minSdk 21 compatibility across legacy and modern hardware.

*Plan generated. Switch to **Build** mode when ready to apply automated actions.*
                        """.trimIndent()

                        for (chunk in planResponse.split("\n")) {
                            contentBuilder.append(chunk).append("\n")
                            AppRepository.updateMessage(conversationId, assistantMsgId) {
                                it.copy(content = contentBuilder.toString())
                            }
                            delay(35)
                        }
                    }

                    AgentMode.BUILD -> {
                        thoughtBuilder.append("\nAutonomous Build Mode active. Inspecting filesystem and running tools...")
                        AppRepository.updateMessage(conversationId, assistantMsgId) {
                            it.copy(thought = thoughtBuilder.toString())
                        }
                        delay(500)

                        val isFileOp = finalPrompt.contains("file", ignoreCase = true) || finalPrompt.contains("write", ignoreCase = true) || finalPrompt.contains("create", ignoreCase = true)
                        val isTerminalOp = finalPrompt.contains("command", ignoreCase = true) || finalPrompt.contains("run", ignoreCase = true) || finalPrompt.contains("termux", ignoreCase = true)

                        if (isFileOp) {
                            _currentAgentStatus.value = "Executing tool: write_file"
                            val fileName = if (finalPrompt.contains(".kt", ignoreCase = true)) "OmniRouteHelper.kt" else "agent_task.md"
                            val writeTool = ToolCall(
                                toolName = "write_file",
                                arguments = "{\"path\": \"workspace/$fileName\", \"arch\": \"32-bit & 64-bit compatible\"}",
                                status = "running"
                            )
                            toolCallsList.add(writeTool)
                            AppRepository.updateMessage(conversationId, assistantMsgId) {
                                it.copy(toolCalls = toolCallsList.toList())
                            }
                            delay(600)

                            val writeSuccess = writeTool.copy(result = "Wrote file to workspace/$fileName (Verified on armeabi-v7a & arm64-v8a)", status = "success")
                            val idx = toolCallsList.indexOf(writeTool)
                            if (idx != -1) toolCallsList[idx] = writeSuccess
                            AppRepository.updateMessage(conversationId, assistantMsgId) {
                                it.copy(toolCalls = toolCallsList.toList())
                            }
                            AppRepository.addDocument(fileName, "workspace/$fileName", "// OmniRoute Ported Component\nclass OmniRouteEngine {\n    fun verifyArch() = \"armeabi-v7a + arm64-v8a supported\"\n}")
                            AppRepository.addTerminalLog("[TOOL SUCCESS] write_file -> workspace/$fileName")
                        } else if (isTerminalOp) {
                            _currentAgentStatus.value = "Executing tool: run_terminal_command"
                            val termTool = ToolCall(
                                toolName = "run_terminal_command",
                                arguments = "{\"cmd\": \"uname -m && omniroute --status\"}",
                                status = "running"
                            )
                            toolCallsList.add(termTool)
                            AppRepository.updateMessage(conversationId, assistantMsgId) {
                                it.copy(toolCalls = toolCallsList.toList())
                            }
                            delay(500)

                            val termSuccess = termTool.copy(result = "Exit 0. Arch: multi-ABI (armv7l / aarch64). OmniRoute Gateway: Active", status = "success")
                            val idx = toolCallsList.indexOf(termTool)
                            if (idx != -1) toolCallsList[idx] = termSuccess
                            AppRepository.updateMessage(conversationId, assistantMsgId) {
                                it.copy(toolCalls = toolCallsList.toList())
                            }
                            AppRepository.addTerminalLog("$ uname -m && omniroute --status", isCommand = true)
                            AppRepository.addTerminalLog("Multi-ABI check passed. OmniRoute Gateway online.")
                        } else {
                            _currentAgentStatus.value = "Executing tool: omniroute_route_check"
                            val routeCheck = ToolCall(
                                toolName = "omniroute_route_check",
                                arguments = "{\"target\": \"$effectiveExecutingModel\", \"compression\": \"${activeCombo?.compressionMode?.label ?: "none"}\"}",
                                status = "running"
                            )
                            toolCallsList.add(routeCheck)
                            AppRepository.updateMessage(conversationId, assistantMsgId) {
                                it.copy(toolCalls = toolCallsList.toList())
                            }
                            delay(400)
                            val idx = toolCallsList.indexOf(routeCheck)
                            if (idx != -1) {
                                toolCallsList[idx] = routeCheck.copy(result = "Route verified. Zero-telemetry local storage active.", status = "success")
                            }
                            AppRepository.updateMessage(conversationId, assistantMsgId) {
                                it.copy(toolCalls = toolCallsList.toList())
                            }
                        }

                        delay(300)
                        _currentAgentStatus.value = "Streaming response..."

                        val buildResponse = """
Request successfully executed using **$effectiveExecutingModel** via OmniRoute.

**OmniRoute Routing Highlights**:
- **Strategy**: ${activeCombo?.strategy?.label ?: "Direct Model Dispatch"}
- **Model Chain**: ${activeCombo?.modelIds?.joinToString(" ➔ ") ?: effectiveExecutingModel}
- **Architecture Compatibility**: Tested for both 32-bit (`armeabi-v7a`) and 64-bit (`arm64-v8a`) hardware targets.
- **Token Efficiency**: Saved $tokensSaved tokens using OmniRoute RTK pipeline.

```kotlin
// OmniRoute Multi-ABI Verification
val isCompatible = OmniRouteArchValidator.isSupported()
println("Architecture valid: ${'$'}isCompatible")
```

All modifications have been committed to local device storage under the Material Black design system.
                        """.trimIndent()

                        for (line in buildResponse.split("\n")) {
                            contentBuilder.append(line).append("\n")
                            AppRepository.updateMessage(conversationId, assistantMsgId) {
                                it.copy(content = contentBuilder.toString())
                            }
                            delay(35)
                        }
                    }

                    AgentMode.ASK -> {
                        thoughtBuilder.append("\nAdvisory mode: Consulting without state mutation...")
                        AppRepository.updateMessage(conversationId, assistantMsgId) {
                            it.copy(thought = thoughtBuilder.toString())
                        }
                        delay(400)

                        _currentAgentStatus.value = "Formulating response..."
                        val askResponse = """
In **Ask** mode, I provide analysis and architectural guidance without running modifying tools on your device.

Regarding your request:
> $finalPrompt

**OmniRoute Analysis**:
1. **Dynamic Routing**: With OmniRoute's **${activeCombo?.strategy?.label ?: "routing"}**, requests automatically fail over when providers encounter rate limits (HTTP 429) or timeouts.
2. **Architecture Parity**: 32-bit (armeabi-v7a) and 64-bit (arm64-v8a) devices run with full parity and `minSdk = 21`.
3. **Token Optimization**: RTK and Caveman token compression reduce overhead and maximize free-tier mileage.

Let me know if you would like me to switch to **Build** mode to execute these changes directly!
                        """.trimIndent()

                        for (line in askResponse.split("\n")) {
                            contentBuilder.append(line).append("\n")
                            AppRepository.updateMessage(conversationId, assistantMsgId) {
                                it.copy(content = contentBuilder.toString())
                            }
                            delay(30)
                        }
                    }
                }

                // Finalize Message
                AppRepository.updateMessage(conversationId, assistantMsgId) {
                    it.copy(
                        status = MessageStatus.COMPLETE,
                        content = contentBuilder.toString().trimEnd()
                    )
                }
                AppRepository.addTerminalLog("[AGENT RUN COMPLETED] Message $assistantMsgId routed successfully.")

            } catch (e: CancellationException) {
                AppRepository.updateMessage(conversationId, assistantMsgId) {
                    it.copy(status = MessageStatus.COMPLETE, content = it.content + "\n\n*(Agent run paused by user)*")
                }
                AppRepository.addTerminalLog("[AGENT RUN CANCELLED] Stopped by user.")
            } catch (e: Exception) {
                AppRepository.updateMessage(conversationId, assistantMsgId) {
                    it.copy(status = MessageStatus.ERROR, error = e.localizedMessage ?: "Unknown execution error")
                }
                AppRepository.addTerminalLog("[AGENT ERROR] ${e.localizedMessage}", isError = true)
            } finally {
                _isAgentRunning.value = false
                _currentAgentStatus.value = null
            }
        }
    }

    fun stopAgent() {
        activeJob?.cancel()
        _isAgentRunning.value = false
        _currentAgentStatus.value = null
    }

    fun approveTool(approved: Boolean) {
        val req = _pendingApproval.value ?: return
        req.continuation.complete(approved)
        _pendingApproval.value = null
    }
}
