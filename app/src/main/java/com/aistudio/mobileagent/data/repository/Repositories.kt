package com.aistudio.mobileagent.data.repository

import com.aistudio.mobileagent.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object AppRepository {

    // --- CONVERSATIONS & MESSAGES ---
    private val initialConversations = mutableListOf(
        Conversation(
            id = "conv-welcome",
            title = "Mobile Agent Setup & On-Device Run",
            providerId = "on-device",
            modelId = "gemma-e2b",
            agentMode = AgentMode.BUILD,
            agentId = "swe",
            isPinned = true,
            createdAt = System.currentTimeMillis() - 3600000,
            updatedAt = System.currentTimeMillis() - 3600000
        ),
        Conversation(
            id = "conv-terminal",
            title = "Termux & MCP Environment Check",
            providerId = "on-device",
            modelId = "qwen3-0.6b",
            agentMode = AgentMode.PLAN,
            agentId = "hacker",
            isPinned = false,
            createdAt = System.currentTimeMillis() - 7200000,
            updatedAt = System.currentTimeMillis() - 7200000
        )
    )

    private val initialMessages = mutableMapOf(
        "conv-welcome" to mutableListOf(
            Message(
                id = "msg-1",
                conversationId = "conv-welcome",
                role = MessageRole.USER,
                content = "Hello! Can you inspect our local project structure and verify which on-device AI models and skills are available?",
                timestamp = System.currentTimeMillis() - 3500000
            ),
            Message(
                id = "msg-2",
                conversationId = "conv-welcome",
                role = MessageRole.ASSISTANT,
                thought = "I need to check the active workspace environment, verify the installed on-device LiteRT models (Gemma 4 E2B, Qwen 3 0.6B), and query the active skills.",
                content = "I've checked the environment. Mobile Agent is running in full standalone mode with direct filesystem access and native tool execution enabled.",
                toolCalls = listOf(
                    ToolCall(
                        id = "call-1",
                        toolName = "list_files",
                        arguments = "{\"path\": \"/sdcard/mobile-agent/\"}",
                        result = "Found: catalog/, modules/, workspace/, config.json",
                        status = "success"
                    ),
                    ToolCall(
                        id = "call-2",
                        toolName = "query_model_status",
                        arguments = "{\"engine\": \"litert-lm\"}",
                        result = "Gemma 4 E2B loaded (2.3B parameters, mixed 2/4/8-bit quantization, RAM: 8GB min)",
                        status = "success"
                    )
                ),
                status = MessageStatus.COMPLETE,
                timestamp = System.currentTimeMillis() - 3400000
            )
        ),
        "conv-terminal" to mutableListOf(
            Message(
                id = "msg-t1",
                conversationId = "conv-terminal",
                role = MessageRole.USER,
                content = "Plan how to connect an MCP filesystem server via Stdio transport.",
                timestamp = System.currentTimeMillis() - 7000000
            ),
            Message(
                id = "msg-t2",
                conversationId = "conv-terminal",
                role = MessageRole.ASSISTANT,
                thought = "Planning the MCP connection steps: 1. Configure JSON manifest. 2. Verify SAF file permissions. 3. Establish stdio pipe over Termux or local socket.",
                content = "Here is the architectural plan for the MCP connection:\n\n1. **Protocol Discovery**: Initialize JSON-RPC 2.0 handshake.\n2. **Capability Negotiation**: Enable `tools/list` and `resources/read`.\n3. **Safe Execution**: Sandbox all write operations behind user approval.",
                toolCalls = listOf(
                    ToolCall(
                        id = "call-p1",
                        toolName = "mcp_handshake",
                        arguments = "{\"server\": \"local-filesystem\", \"transport\": \"stdio\"}",
                        result = "Handshake OK - 5 tools exposed",
                        status = "success"
                    )
                ),
                status = MessageStatus.COMPLETE,
                timestamp = System.currentTimeMillis() - 6900000
            )
        )
    )

    private val _conversationsFlow = MutableStateFlow<List<Conversation>>(initialConversations)
    val conversationsFlow: StateFlow<List<Conversation>> = _conversationsFlow.asStateFlow()

    private val _selectedConversationId = MutableStateFlow("conv-welcome")
    val selectedConversationId: StateFlow<String> = _selectedConversationId.asStateFlow()

    private val _messagesFlow = MutableStateFlow<List<Message>>(initialMessages["conv-welcome"] ?: emptyList())
    val messagesFlow: StateFlow<List<Message>> = _messagesFlow.asStateFlow()

    // --- AGENTS ---
    private val defaultAgents = listOf(
        AgentProfile(
            id = "general",
            name = "General Agent",
            description = "All-around mobile AI assistant with system and web tools",
            systemPrompt = "You are Mobile Agent, an autonomous assistant running directly on mobile hardware. You are direct, accurate, and proactive.",
            iconName = "SmartToy",
            isBuiltIn = true,
            defaultMode = AgentMode.BUILD
        ),
        AgentProfile(
            id = "swe",
            name = "Software Engineer",
            description = "Specialized in code writing, debugging, refactoring, and AST analysis",
            systemPrompt = "You are an expert software engineer. Write pristine, modular code, handle edge cases, and inspect files before editing.",
            iconName = "Code",
            isBuiltIn = true,
            defaultMode = AgentMode.BUILD
        ),
        AgentProfile(
            id = "planner",
            name = "System Architect",
            description = "Focuses on deep planning, task decomposition, and specifications",
            systemPrompt = "You are a software architect. Focus on high-level architecture, flowcharts, schemas, and trade-off analysis.",
            iconName = "AccountTree",
            isBuiltIn = true,
            defaultMode = AgentMode.PLAN
        ),
        AgentProfile(
            id = "hacker",
            name = "Terminal & DevOps",
            description = "Expert in shell commands, Termux automation, and bash scripts",
            systemPrompt = "You are a Linux & Android DevOps specialist. You operate the terminal with precision and safety.",
            iconName = "Terminal",
            isBuiltIn = true,
            defaultMode = AgentMode.BUILD
        )
    )
    private val _agentsFlow = MutableStateFlow<List<AgentProfile>>(defaultAgents)
    val agentsFlow: StateFlow<List<AgentProfile>> = _agentsFlow.asStateFlow()

    // --- SKILLS ---
    private val defaultSkills = listOf(
        SkillItem(
            id = "android-cli",
            label = "android-cli",
            description = "Searches and fetches official Android developer guides, SDK docs, and Kotlin Compose recipes.",
            author = "Google AI Studio",
            sourceUrl = "https://developer.android.com",
            instructions = "Use this skill to look up modern Android practices, Compose guidelines, and Jetpack APIs.",
            enabled = true,
            isBundled = true
        ),
        SkillItem(
            id = "file-manager",
            label = "saf-file-operations",
            description = "Storage Access Framework tools for reading, writing, and querying mobile device storage.",
            author = "Mobile Agent",
            sourceUrl = "https://github.com/machbrandido-art/mobile-agent",
            instructions = "Directly inspect device directories, read source code, and save exported artifacts.",
            enabled = true,
            isBundled = true
        ),
        SkillItem(
            id = "termux-stream",
            label = "termux-stream",
            description = "Runs local shell tasks and background daemon jobs via Termux integration.",
            author = "Mobile Agent",
            sourceUrl = "https://termux.dev",
            instructions = "Stream live bash output, monitor running background processes, and run compilers.",
            enabled = true,
            isBundled = true
        ),
        SkillItem(
            id = "shadcn",
            label = "shadcn",
            description = "Manages modern UI component structures, styling rules, and layout tokens.",
            author = "shadcn",
            sourceUrl = "https://ui.shadcn.com",
            instructions = "Reference design systems and token structures for clean mobile aesthetics.",
            enabled = false,
            isBundled = true
        ),
        SkillItem(
            id = "web-search",
            label = "web-search",
            description = "Queries real-time web information and extracts clean Markdown summaries.",
            author = "Community",
            sourceUrl = "https://duckduckgo.com",
            instructions = "Perform live web searches to look up up-to-date documentation and news.",
            enabled = true,
            isBundled = false
        )
    )
    private val _skillsFlow = MutableStateFlow<List<SkillItem>>(defaultSkills)
    val skillsFlow: StateFlow<List<SkillItem>> = _skillsFlow.asStateFlow()

    // --- MCP SERVERS ---
    private val defaultMcpServers = listOf(
        McpServerItem(
            id = "mcp-fs",
            label = "Local Filesystem MCP",
            url = "stdio://local-fs",
            transport = "stdio",
            status = "connected",
            toolCount = 6,
            instructions = "Provides read_file, write_file, list_directory, and stat."
        ),
        McpServerItem(
            id = "mcp-sqlite",
            label = "Device SQLite DB",
            url = "http://127.0.0.1:8080/mcp",
            transport = "sse",
            status = "connected",
            toolCount = 4,
            instructions = "Direct SQL query execution and schema inspection."
        ),
        McpServerItem(
            id = "mcp-github",
            label = "GitHub API MCP",
            url = "https://mcp.github.com/v1",
            transport = "sse",
            authMode = "bearer",
            status = "untested",
            toolCount = 12,
            instructions = "Issues, PRs, and repo code search."
        )
    )
    private val _mcpServersFlow = MutableStateFlow<List<McpServerItem>>(defaultMcpServers)
    val mcpServersFlow: StateFlow<List<McpServerItem>> = _mcpServersFlow.asStateFlow()

    // --- MODELS ---
    val availableModels = listOf(
        AiModelInfo(
            id = "gemma-e2b",
            name = "Gemma 4 E2B (On-Device)",
            provider = "LiteRT Edge",
            parameterCount = "2.3B",
            isLocal = true,
            sizeBytes = 2588147712L,
            isDownloaded = true,
            contextWindow = 32000,
            description = "Google Gemma 4 mobile-optimized 2.3B parameter model with mixed 2/4/8-bit quantization. Fast and offline.",
            supportsTools = true,
            supportsReasoning = true
        ),
        AiModelInfo(
            id = "gemma-e4b",
            name = "Gemma 4 E4B (On-Device)",
            provider = "LiteRT Edge",
            parameterCount = "4.5B",
            isLocal = true,
            sizeBytes = 3659530240L,
            isDownloaded = false,
            contextWindow = 32000,
            description = "Higher capacity 4.5B parameter Gemma model for deeper code synthesis and reasoning.",
            supportsTools = true,
            supportsReasoning = true
        ),
        AiModelInfo(
            id = "qwen3-0.6b",
            name = "Qwen3 0.6B Ultra-Light",
            provider = "LiteRT Edge",
            parameterCount = "0.6B",
            isLocal = true,
            sizeBytes = 497664000L,
            isDownloaded = true,
            contextWindow = 2048,
            description = "Ultra compact 600M model designed to run with minimal battery and RAM impact.",
            supportsTools = true,
            supportsReasoning = false
        ),
        AiModelInfo(
            id = "gemini-2.5-flash",
            name = "Gemini 2.5 Flash",
            provider = "Google AI",
            parameterCount = "Cloud",
            isLocal = false,
            sizeBytes = 0L,
            isDownloaded = true,
            contextWindow = 1000000,
            description = "High-speed multimodal AI with million-token context and live web groundings.",
            supportsTools = true,
            supportsReasoning = true
        ),
        AiModelInfo(
            id = "claude-3-5-sonnet",
            name = "Claude 3.5 Sonnet",
            provider = "Anthropic",
            parameterCount = "Cloud",
            isLocal = false,
            sizeBytes = 0L,
            isDownloaded = true,
            contextWindow = 200000,
            description = "Industry standard for advanced coding and logical instruction following.",
            supportsTools = true,
            supportsReasoning = true
        ),
        AiModelInfo(
            id = "gpt-4o",
            name = "GPT-4o",
            provider = "OpenAI",
            parameterCount = "Cloud",
            isLocal = false,
            sizeBytes = 0L,
            isDownloaded = true,
            contextWindow = 128000,
            description = "Flagship multimodal intelligence for code, vision, and natural conversation.",
            supportsTools = true,
            supportsReasoning = true
        ),
        AiModelInfo(
            id = "ollama-local",
            name = "Ollama Local (LAN/Host)",
            provider = "Ollama",
            parameterCount = "Variable",
            isLocal = true,
            sizeBytes = 0L,
            isDownloaded = true,
            contextWindow = 8192,
            description = "Connects to any local Ollama instance running on localhost:11434 or LAN.",
            supportsTools = true,
            supportsReasoning = true
        )
    )

    // --- PROVIDERS ---
    private val defaultProviders = listOf(
        ProviderSetting(
            id = "on-device",
            name = "LiteRT On-Device",
            description = "Execute weights directly on device NPU/GPU using Google AI Edge LiteRT runtime",
            isConfigured = true,
            isLocal = true,
            enabled = true
        ),
        ProviderSetting(
            id = "google",
            name = "Google Gemini",
            description = "Gemini 2.5 Flash & Pro via Google AI API key",
            isConfigured = true,
            baseUrl = "https://generativelanguage.googleapis.com",
            enabled = true
        ),
        ProviderSetting(
            id = "anthropic",
            name = "Anthropic",
            description = "Claude 3.5 Sonnet & Haiku models",
            isConfigured = false,
            baseUrl = "https://api.anthropic.com",
            enabled = false
        ),
        ProviderSetting(
            id = "openai",
            name = "OpenAI",
            description = "GPT-4o, o3-mini models",
            isConfigured = false,
            baseUrl = "https://api.openai.com/v1",
            enabled = false
        ),
        ProviderSetting(
            id = "ollama",
            name = "Ollama",
            description = "Local or networked Ollama server",
            isConfigured = true,
            baseUrl = "http://10.0.2.2:11434",
            isLocal = true,
            enabled = true
        )
    )
    private val _providersFlow = MutableStateFlow<List<ProviderSetting>>(defaultProviders)
    val providersFlow: StateFlow<List<ProviderSetting>> = _providersFlow.asStateFlow()

    // --- SAVED PROMPTS ---
    private val defaultPrompts = listOf(
        SavedPrompt(
            id = "p-1",
            title = "Refactor into Jetpack Compose",
            content = "Refactor the following component to use modern Jetpack Compose with Material 3, ViewModel, and clean StateFlow patterns:",
            category = "Coding"
        ),
        SavedPrompt(
            id = "p-2",
            title = "Code Review & Security Audit",
            content = "Perform a thorough security and performance audit of this code. Check for memory leaks, unhandled exceptions, and concurrency race conditions.",
            category = "Security"
        ),
        SavedPrompt(
            id = "p-3",
            title = "Explain Architecture & Dataflow",
            content = "Provide an architectural overview of this module including state flow, error handling boundaries, and lifecycle management.",
            category = "Architecture"
        ),
        SavedPrompt(
            id = "p-4",
            title = "Generate Unit Test Suite",
            content = "Write a comprehensive Robolectric/JUnit test suite with edge cases, happy paths, and error scenarios for:",
            category = "Testing"
        )
    )
    private val _promptsFlow = MutableStateFlow<List<SavedPrompt>>(defaultPrompts)
    val promptsFlow: StateFlow<List<SavedPrompt>> = _promptsFlow.asStateFlow()

    // --- MEMORIES ---
    private val defaultMemories = listOf(
        MemoryFact(
            id = "m-1",
            content = "User prefers Kotlin and Jetpack Compose over XML layouts.",
            category = "Coding Preferences"
        ),
        MemoryFact(
            id = "m-2",
            content = "Target device has 8GB RAM, suitable for Gemma 4 E2B on-device.",
            category = "Hardware"
        ),
        MemoryFact(
            id = "m-3",
            content = "Always verify file paths using relative project references.",
            category = "Agent Policy"
        )
    )
    private val _memoriesFlow = MutableStateFlow<List<MemoryFact>>(defaultMemories)
    val memoriesFlow: StateFlow<List<MemoryFact>> = _memoriesFlow.asStateFlow()

    // --- SCHEDULED JOBS ---
    private val defaultJobs = listOf(
        ScheduledJob(
            id = "j-1",
            title = "Daily Git & Security Sync",
            prompt = "Check repo commits, scan dependencies for vulnerabilities, and summarize findings.",
            cronExpression = "0 8 * * *",
            enabled = true,
            lastRunAt = System.currentTimeMillis() - 86400000L,
            nextRunAt = System.currentTimeMillis() + 43200000L
        ),
        ScheduledJob(
            id = "j-2",
            title = "Hourly Workspace Cleanup",
            prompt = "Clean ephemeral cache files and rotate logs.",
            cronExpression = "0 * * * *",
            enabled = true,
            lastRunAt = System.currentTimeMillis() - 3600000L,
            nextRunAt = System.currentTimeMillis() + 1800000L
        )
    )
    private val _jobsFlow = MutableStateFlow<List<ScheduledJob>>(defaultJobs)
    val jobsFlow: StateFlow<List<ScheduledJob>> = _jobsFlow.asStateFlow()

    // --- WORKSPACE DOCUMENTS ---
    private val defaultDocs = listOf(
        WorkspaceDocument(
            id = "d-1",
            name = "app_architecture.md",
            path = "docs/app_architecture.md",
            content = "# Mobile Agent Architecture\n\n- Runtime: On-Device LiteRT + Gemini Cloud\n- Storage: SQLite & Jetpack Compose\n- Tool System: SAF & Termux stdio",
            mimeType = "text/markdown",
            sizeBytes = 240L
        ),
        WorkspaceDocument(
            id = "d-2",
            name = "mcp_config.json",
            path = "config/mcp_config.json",
            content = "{\n  \"servers\": {\n    \"filesystem\": {\n      \"command\": \"npx\",\n      \"args\": [\"-y\", \"@modelcontextprotocol/server-filesystem\", \".\"]\n    }\n  }\n}",
            mimeType = "application/json",
            sizeBytes = 180L
        ),
        WorkspaceDocument(
            id = "d-3",
            name = "benchmark_results.log",
            path = "logs/benchmark_results.log",
            content = "[INFO] Gemma 4 E2B inference: 24.5 tokens/sec on Qualcomm Adreno GPU\n[INFO] Time to first token: 142ms\n[INFO] RAM usage: 3.1GB",
            mimeType = "text/plain",
            sizeBytes = 320L
        )
    )
    private val _docsFlow = MutableStateFlow<List<WorkspaceDocument>>(defaultDocs)
    val docsFlow: StateFlow<List<WorkspaceDocument>> = _docsFlow.asStateFlow()

    // --- TERMINAL LOGS ---
    private val defaultLogs = listOf(
        TerminalLog(text = "Mobile Agent Shell v1.0.0 (Linux aarch64 Android)", isCommand = false),
        TerminalLog(text = "$ termux-info", isCommand = true),
        TerminalLog(text = "Android 15 (API 35), Termux sandbox: active", isCommand = false),
        TerminalLog(text = "$ litert-lm --check-accelerator", isCommand = true),
        TerminalLog(text = "Found OpenCL & Hexagon NPU backend. Quantization support: INT4, INT8, FP16.", isCommand = false),
        TerminalLog(text = "$ mcp-server --status", isCommand = true),
        TerminalLog(text = "[MCP] 3 active servers listening on stdio / sse.", isCommand = false)
    )
    private val _terminalLogsFlow = MutableStateFlow<List<TerminalLog>>(defaultLogs)
    val terminalLogsFlow: StateFlow<List<TerminalLog>> = _terminalLogsFlow.asStateFlow()

    // --- ACTIONS ---

    fun selectConversation(id: String) {
        _selectedConversationId.value = id
        _messagesFlow.value = initialMessages[id] ?: emptyList()
    }

    fun createConversation(title: String = "New Agent Chat", modelId: String = "gemma-e2b", mode: AgentMode = AgentMode.BUILD): Conversation {
        val newConv = Conversation(
            title = title,
            modelId = modelId,
            agentMode = mode
        )
        initialConversations.add(0, newConv)
        initialMessages[newConv.id] = mutableListOf(
            Message(
                conversationId = newConv.id,
                role = MessageRole.ASSISTANT,
                content = "Ready to assist in **${mode.label}** mode with **$modelId**. Ask a question or assign a software task."
            )
        )
        _conversationsFlow.value = initialConversations.toList()
        selectConversation(newConv.id)
        return newConv
    }

    fun togglePinConversation(id: String) {
        val index = initialConversations.indexOfFirst { it.id == id }
        if (index != -1) {
            val conv = initialConversations[index]
            initialConversations[index] = conv.copy(isPinned = !conv.isPinned)
            _conversationsFlow.value = initialConversations.toList()
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        val index = initialConversations.indexOfFirst { it.id == id }
        if (index != -1) {
            initialConversations[index] = initialConversations[index].copy(title = newTitle, updatedAt = System.currentTimeMillis())
            _conversationsFlow.value = initialConversations.toList()
        }
    }

    fun deleteConversation(id: String) {
        initialConversations.removeAll { it.id == id }
        initialMessages.remove(id)
        _conversationsFlow.value = initialConversations.toList()
        if (_selectedConversationId.value == id) {
            val next = initialConversations.firstOrNull()?.id ?: createConversation().id
            selectConversation(next)
        }
    }

    fun updateConversationMode(id: String, mode: AgentMode) {
        val index = initialConversations.indexOfFirst { it.id == id }
        if (index != -1) {
            initialConversations[index] = initialConversations[index].copy(agentMode = mode)
            _conversationsFlow.value = initialConversations.toList()
        }
    }

    fun updateConversationModel(id: String, modelId: String) {
        val index = initialConversations.indexOfFirst { it.id == id }
        if (index != -1) {
            initialConversations[index] = initialConversations[index].copy(modelId = modelId)
            _conversationsFlow.value = initialConversations.toList()
        }
    }

    fun addMessage(conversationId: String, message: Message) {
        val list = initialMessages.getOrPut(conversationId) { mutableListOf() }
        list.add(message)
        if (_selectedConversationId.value == conversationId) {
            _messagesFlow.value = list.toList()
        }
        val convIndex = initialConversations.indexOfFirst { it.id == conversationId }
        if (convIndex != -1) {
            initialConversations[convIndex] = initialConversations[convIndex].copy(updatedAt = System.currentTimeMillis())
            _conversationsFlow.value = initialConversations.toList()
        }
    }

    fun updateMessage(conversationId: String, messageId: String, transform: (Message) -> Message) {
        val list = initialMessages[conversationId] ?: return
        val idx = list.indexOfFirst { it.id == messageId }
        if (idx != -1) {
            list[idx] = transform(list[idx])
            if (_selectedConversationId.value == conversationId) {
                _messagesFlow.value = list.toList()
            }
        }
    }

    fun addTerminalLog(text: String, isCommand: Boolean = false, isError: Boolean = false) {
        val log = TerminalLog(text = text, isCommand = isCommand, isError = isError)
        _terminalLogsFlow.value = _terminalLogsFlow.value + log
    }

    fun clearTerminalLogs() {
        _terminalLogsFlow.value = emptyList()
    }

    fun toggleSkill(id: String) {
        _skillsFlow.value = _skillsFlow.value.map {
            if (it.id == id) it.copy(enabled = !it.enabled) else it
        }
    }

    fun addSkill(skill: SkillItem) {
        _skillsFlow.value = _skillsFlow.value + skill
    }

    fun toggleMcpServer(id: String) {
        _mcpServersFlow.value = _mcpServersFlow.value.map {
            if (it.id == id) it.copy(enabled = !it.enabled) else it
        }
    }

    fun addMcpServer(server: McpServerItem) {
        _mcpServersFlow.value = _mcpServersFlow.value + server
    }

    fun addSavedPrompt(title: String, content: String, category: String) {
        _promptsFlow.value = _promptsFlow.value + SavedPrompt(title = title, content = content, category = category)
    }

    fun deleteSavedPrompt(id: String) {
        _promptsFlow.value = _promptsFlow.value.filterNot { it.id == id }
    }

    fun addMemory(content: String, category: String) {
        _memoriesFlow.value = _memoriesFlow.value + MemoryFact(content = content, category = category)
    }

    fun toggleMemory(id: String) {
        _memoriesFlow.value = _memoriesFlow.value.map {
            if (it.id == id) it.copy(enabled = !it.enabled) else it
        }
    }

    fun deleteMemory(id: String) {
        _memoriesFlow.value = _memoriesFlow.value.filterNot { it.id == id }
    }

    fun addJob(title: String, prompt: String, cron: String) {
        _jobsFlow.value = _jobsFlow.value + ScheduledJob(title = title, prompt = prompt, cronExpression = cron)
    }

    fun toggleJob(id: String) {
        _jobsFlow.value = _jobsFlow.value.map {
            if (it.id == id) it.copy(enabled = !it.enabled) else it
        }
    }

    fun deleteJob(id: String) {
        _jobsFlow.value = _jobsFlow.value.filterNot { it.id == id }
    }

    fun addDocument(name: String, path: String, content: String, mimeType: String = "text/plain") {
        _docsFlow.value = _docsFlow.value + WorkspaceDocument(
            name = name,
            path = path,
            content = content,
            mimeType = mimeType,
            sizeBytes = content.toByteArray().size.toLong()
        )
    }

    fun deleteDocument(id: String) {
        _docsFlow.value = _docsFlow.value.filterNot { it.id == id }
    }

    fun updateProviderApiKey(id: String, apiKey: String) {
        _providersFlow.value = _providersFlow.value.map {
            if (it.id == id) it.copy(apiKey = apiKey, isConfigured = apiKey.isNotBlank()) else it
        }
    }
}
