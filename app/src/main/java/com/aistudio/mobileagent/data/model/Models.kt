package com.aistudio.mobileagent.data.model

import java.util.UUID

enum class MessageRole {
    USER, ASSISTANT, SYSTEM, TOOL
}

enum class MessageStatus {
    SENT, STREAMING, COMPLETE, ERROR
}

enum class AgentMode(val label: String, val description: String) {
    BUILD("Build", "Autonomous execution: runs tools, edits code, modifies files"),
    PLAN("Plan", "Architectural planning: breaks down problems before execution"),
    ASK("Ask", "Conversational advice: questions, diagnostics, and code review")
}

data class ToolCall(
    val id: String = UUID.randomUUID().toString(),
    val toolName: String,
    val arguments: String,
    val result: String? = null,
    val status: String = "running", // running, approved, denied, success, error
    val requiresApproval: Boolean = false
)

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val role: MessageRole,
    val content: String,
    val thought: String? = null,
    val toolCalls: List<ToolCall> = emptyList(),
    val status: MessageStatus = MessageStatus.COMPLETE,
    val error: String? = null,
    val attachments: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val providerId: String = "on-device",
    val modelId: String = "gemma-e2b",
    val agentMode: AgentMode = AgentMode.BUILD,
    val agentId: String = "general",
    val isPinned: Boolean = false,
    val selectedSkillIds: List<String> = listOf("code-tools", "file-manager"),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class AgentProfile(
    val id: String,
    val name: String,
    val description: String,
    val systemPrompt: String,
    val iconName: String = "SmartToy",
    val isBuiltIn: Boolean = false,
    val enabled: Boolean = true,
    val defaultMode: AgentMode = AgentMode.BUILD
)

data class SkillItem(
    val id: String,
    val label: String,
    val description: String,
    val author: String,
    val sourceUrl: String? = null,
    val instructions: String,
    val enabled: Boolean = true,
    val isBundled: Boolean = true
)

data class McpServerItem(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val url: String,
    val transport: String = "sse", // sse, stdio, http
    val authMode: String = "none", // none, bearer, oauth
    val enabled: Boolean = true,
    val status: String = "connected", // connected, disconnected, testing, error
    val toolCount: Int = 0,
    val instructions: String? = null
)

data class SavedPrompt(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val category: String = "General"
)

data class MemoryFact(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val enabled: Boolean = true,
    val category: String = "Preference",
    val timestamp: Long = System.currentTimeMillis()
)

data class ScheduledJob(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val prompt: String,
    val cronExpression: String, // e.g. "0 9 * * *" or "Every 30m"
    val enabled: Boolean = true,
    val modelId: String = "gemma-e2b",
    val lastRunAt: Long? = null,
    val nextRunAt: Long? = null
)

data class WorkspaceDocument(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val path: String,
    val content: String,
    val mimeType: String = "text/plain",
    val sizeBytes: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)

data class AiModelInfo(
    val id: String,
    val name: String,
    val provider: String,
    val parameterCount: String,
    val isLocal: Boolean,
    val sizeBytes: Long = 0L,
    val isDownloaded: Boolean = false,
    val contextWindow: Int = 8192,
    val description: String,
    val supportsTools: Boolean = true,
    val supportsReasoning: Boolean = true
)

data class ProviderSetting(
    val id: String,
    val name: String,
    val description: String,
    val isConfigured: Boolean,
    val apiKey: String = "",
    val baseUrl: String = "",
    val enabled: Boolean = true,
    val isLocal: Boolean = false
)

data class TerminalLog(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isError: Boolean = false,
    val isCommand: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
