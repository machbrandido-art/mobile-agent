package com.aistudio.mobileagent.data.model

import java.util.UUID

enum class RoutingStrategy(val label: String, val description: String) {
    PRIORITY_FAILOVER("Priority Failover", "Routes to 1st model; immediately fails over to 2nd if rate-limited (HTTP 429) or offline"),
    ROUND_ROBIN("Round Robin", "Distributes requests sequentially across all models in the chain"),
    COST_OPTIMIZED("Cost Optimized", "Routes to free tier / lowest cost per token provider first"),
    LOWEST_LATENCY("Lowest Latency", "Favors the fastest responding provider, falling back on slow spikes"),
    AUTO_BALANCE("Auto-Balance", "Dynamically scores models by quota headroom, RPM health, and success rate")
}

enum class TokenCompressionMode(val label: String, val reductionEstimate: String) {
    NONE("None", "0%"),
    LIGHT_RTK("Light RTK", "~20% reduction"),
    AGGRESSIVE_RTK("Aggressive RTK", "~50% reduction"),
    CAVEMAN("Caveman Mode", "~75% reduction (ultra-dense prompt)")
}

data class ModelCombo(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val strategy: RoutingStrategy = RoutingStrategy.PRIORITY_FAILOVER,
    val modelIds: List<String>, // Chain order: e.g. ["gemini-2.5-flash", "claude-3-5-sonnet", "gemma-e2b"]
    val fallbackOnHttp429: Boolean = true,
    val timeoutMs: Long = 15000L,
    val compressionMode: TokenCompressionMode = TokenCompressionMode.LIGHT_RTK,
    val isEnabled: Boolean = true,
    val isBuiltIn: Boolean = false
)

data class RouteExecutionStep(
    val stepIndex: Int,
    val modelId: String,
    val provider: String,
    val status: String, // "ATTEMPTING", "FAILED_429", "FAILED_TIMEOUT", "SUCCESS"
    val latencyMs: Long,
    val message: String
)

data class OmniRouteMetrics(
    val totalRequests: Long = 142L,
    val failoversHandled: Long = 18L,
    val tokensSavedViaRTK: Long = 48290L,
    val activeCombosCount: Int = 4,
    val gatewayPort: Int = 8080,
    val isGatewayActive: Boolean = true,
    val piiProtectionActive: Boolean = true
)

data class ProviderQuota(
    val providerId: String,
    val providerName: String,
    val requestsUsedToday: Int,
    val requestsLimitToday: Int,
    val currentRpm: Int,
    val maxRpm: Int,
    val isThrottled: Boolean = false
)
