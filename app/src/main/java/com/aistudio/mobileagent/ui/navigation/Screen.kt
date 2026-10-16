package com.aistudio.mobileagent.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Chat : Screen("chat", "Chat")
    object Library : Screen("library", "Library")
    object Terminal : Screen("terminal", "Terminal")
    object Settings : Screen("settings", "Settings")
    object Providers : Screen("settings_providers", "AI Providers")
    object Agents : Screen("settings_agents", "Agents")
    object Skills : Screen("settings_skills", "Skills")
    object McpServers : Screen("settings_mcp", "MCP Servers")
    object Jobs : Screen("settings_jobs", "Scheduled Jobs")
    object Memory : Screen("settings_memory", "Memory")

    // OmniRoute Ported Screens
    object Combos : Screen("omniroute_combos", "OmniRoute Combos")
    object OmniRouteDashboard : Screen("omniroute_dashboard", "OmniRoute Gateway Hub")
}
