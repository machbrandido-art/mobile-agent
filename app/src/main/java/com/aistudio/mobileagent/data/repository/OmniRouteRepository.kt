package com.aistudio.mobileagent.data.repository

import com.aistudio.mobileagent.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object OmniRouteRepository {

    // Default Pre-Configured Combos from OmniRoute
    private val defaultCombos = mutableListOf(
        ModelCombo(
            id = "combo-free-rush",
            name = "Free Tier Max & Local Fallback",
            description = "Leverages Google Gemini free quotas first, falling back to local Ollama and on-device Gemma if rate-limited",
            strategy = RoutingStrategy.COST_OPTIMIZED,
            modelIds = listOf("gemini-2.5-flash", "ollama-local", "gemma-e2b"),
            fallbackOnHttp429 = true,
            compressionMode = TokenCompressionMode.LIGHT_RTK,
            isBuiltIn = true
        ),
        ModelCombo(
            id = "combo-code-power",
            name = "Coding Heavy Lifter (Failover)",
            description = "Priority routing for hard software engineering tasks: Claude 3.5 -> GPT-4o -> Gemini 2.5 Flash",
            strategy = RoutingStrategy.PRIORITY_FAILOVER,
            modelIds = listOf("claude-3-5-sonnet", "gpt-4o", "gemini-2.5-flash"),
            fallbackOnHttp429 = true,
            compressionMode = TokenCompressionMode.AGGRESSIVE_RTK,
            isBuiltIn = true
        ),
        ModelCombo(
            id = "combo-speed-rush",
            name = "Ultra-Speed Low Latency",
            description = "Routes to on-device NPU/GPU first for sub-100ms response, falling back to Gemini Flash",
            strategy = RoutingStrategy.LOWEST_LATENCY,
            modelIds = listOf("gemma-e2b", "qwen3-0.6b", "gemini-2.5-flash"),
            fallbackOnHttp429 = true,
            compressionMode = TokenCompressionMode.LIGHT_RTK,
            isBuiltIn = true
        ),
        ModelCombo(
            id = "combo-offline-guardian",
            name = "100% Offline Resilience",
            description = "Pure on-device neural weight balancing: Gemma 4 E2B and Qwen 3 0.6B with zero internet access",
            strategy = RoutingStrategy.AUTO_BALANCE,
            modelIds = listOf("gemma-e2b", "qwen3-0.6b"),
            fallbackOnHttp429 = false,
            compressionMode = TokenCompressionMode.NONE,
            isBuiltIn = true
        )
    )

    private val _combosFlow = MutableStateFlow<List<ModelCombo>>(defaultCombos)
    val combosFlow: StateFlow<List<ModelCombo>> = _combosFlow.asStateFlow()

    private val _metricsFlow = MutableStateFlow(OmniRouteMetrics())
    val metricsFlow: StateFlow<OmniRouteMetrics> = _metricsFlow.asStateFlow()

    private val _quotasFlow = MutableStateFlow(
        listOf(
            ProviderQuota("google", "Google Gemini Free Tier", 412, 1500, currentRpm = 4, maxRpm = 15),
            ProviderQuota("anthropic", "Anthropic Claude Tier 1", 89, 500, currentRpm = 2, maxRpm = 50),
            ProviderQuota("openai", "OpenAI GPT-4o Tier 1", 120, 1000, currentRpm = 3, maxRpm = 60),
            ProviderQuota("on-device", "LiteRT Edge (Unlimited)", 850, 999999, currentRpm = 18, maxRpm = 9999)
        )
    )
    val quotasFlow: StateFlow<List<ProviderQuota>> = _quotasFlow.asStateFlow()

    private val _selectedRouteId = MutableStateFlow("combo-free-rush")
    val selectedRouteId: StateFlow<String> = _selectedRouteId.asStateFlow()

    // --- COMBO MANAGEMENT ---

    fun createCombo(
        name: String,
        description: String,
        strategy: RoutingStrategy,
        modelIds: List<String>,
        compressionMode: TokenCompressionMode
    ): ModelCombo {
        val combo = ModelCombo(
            name = name,
            description = description,
            strategy = strategy,
            modelIds = modelIds,
            compressionMode = compressionMode,
            isBuiltIn = false
        )
        _combosFlow.value = _combosFlow.value + combo
        _metricsFlow.value = _metricsFlow.value.copy(activeCombosCount = _combosFlow.value.size)
        return combo
    }

    fun updateCombo(updated: ModelCombo) {
        _combosFlow.value = _combosFlow.value.map {
            if (it.id == updated.id) updated else it
        }
    }

    fun deleteCombo(id: String) {
        _combosFlow.value = _combosFlow.value.filterNot { it.id == id }
        _metricsFlow.value = _metricsFlow.value.copy(activeCombosCount = _combosFlow.value.size)
    }

    fun selectRoute(routeId: String) {
        _selectedRouteId.value = routeId
    }

    fun findCombo(id: String): ModelCombo? {
        return _combosFlow.value.find { it.id == id }
    }

    // --- TOKEN COMPRESSION (RTK & CAVEMAN) ---

    fun compressPrompt(prompt: String, mode: TokenCompressionMode): Pair<String, Int> {
        val originalWords = prompt.split("\\s+".toRegex()).size
        val compressed = when (mode) {
            TokenCompressionMode.NONE -> prompt
            TokenCompressionMode.LIGHT_RTK -> {
                prompt
                    .replace("(?i)\\b(please|kindly|could you|would you be able to|can you please)\\b".toRegex(), "")
                    .replace("  +".toRegex(), " ")
                    .trim()
            }
            TokenCompressionMode.AGGRESSIVE_RTK -> {
                prompt
                    .replace("(?i)\\b(please|kindly|could you|would you|can you|i would like you to|help me to|in order to)\\b".toRegex(), "")
                    .replace("(?i)\\b(thank you|thanks|regards|sincerely)\\b".toRegex(), "")
                    .replace("\n\n+".toRegex(), "\n")
                    .replace("  +".toRegex(), " ")
                    .trim()
            }
            TokenCompressionMode.CAVEMAN -> {
                // Caveman mode: ultra dense telegraphic format
                prompt
                    .replace("(?i)\\b(please|kindly|could|would|should|can|will|the|a|an|in|on|at|of|for|with)\\b".toRegex(), "")
                    .replace("  +".toRegex(), " ")
                    .trim()
            }
        }
        val compressedWords = compressed.split("\\s+".toRegex()).size
        val tokensSaved = maxOf(0, originalWords - compressedWords)

        if (tokensSaved > 0) {
            _metricsFlow.value = _metricsFlow.value.copy(
                tokensSavedViaRTK = _metricsFlow.value.tokensSavedViaRTK + tokensSaved.toLong()
            )
        }

        return Pair(compressed, tokensSaved)
    }

    // --- PII MASKING & GUARDRAILS ---

    fun applyPiiGuardrails(input: String): String {
        return input
            .replace("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b".toRegex(), "[REDACTED_EMAIL]")
            .replace("\\b(sk-[a-zA-Z0-9]{20,})\\b".toRegex(), "[REDACTED_API_KEY]")
            .replace("\\b(?:\\+?\\d{1,3}[- ]?)?\\(?\\d{3}\\)?[- ]?\\d{3}[- ]?\\d{4}\\b".toRegex(), "[REDACTED_PHONE]")
    }

    // --- ROUTING DISPATCH & METRICS RECORDING ---

    fun recordRouteExecution(wasFailover: Boolean, tokensSaved: Long) {
        _metricsFlow.value = _metricsFlow.value.copy(
            totalRequests = _metricsFlow.value.totalRequests + 1L,
            failoversHandled = _metricsFlow.value.failoversHandled + (if (wasFailover) 1L else 0L),
            tokensSavedViaRTK = _metricsFlow.value.tokensSavedViaRTK + tokensSaved
        )
    }

    fun toggleGateway(active: Boolean) {
        _metricsFlow.value = _metricsFlow.value.copy(isGatewayActive = active)
    }

    fun togglePiiProtection(enabled: Boolean) {
        _metricsFlow.value = _metricsFlow.value.copy(piiProtectionActive = enabled)
    }
}
