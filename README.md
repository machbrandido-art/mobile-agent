# Mobile Agent (Android)

Mobile Agent is an open-source, autonomous AI agent application built natively for Android using Kotlin and Jetpack Compose. It runs on-device models with Google AI Edge LiteRT, supports external cloud reasoning providers, connects to Model Context Protocol (MCP) servers, and executes modular skills with full Android filesystem and process tools.

## Key Features

- **On-Device & Cloud Intelligence**: Run local neural models (Google Gemma 4 E2B, Gemma 4 E4B, Qwen3 0.6B) completely offline via hardware-accelerated NPU/GPU inference, or connect to cloud models (Google Gemini 2.5 Flash, Anthropic Claude 3.5 Sonnet, OpenAI GPT-4o, Ollama).
- **Three Operational Agent Modes**:
  - **Build**: Full autonomous execution with tools for file operations, workspace inspection, and code generation.
  - **Plan**: Deep architectural decomposition, breaking problems down into structured planning steps before taking action.
  - **Ask**: Pure advisory, diagnostic, and code review mode without state mutation.
- **Model Context Protocol (MCP)**: Native integration for MCP servers across `stdio` pipes and Server-Sent Events (`sse`), exposing tools and contextual resources directly to the agent.
- **Skills System**: Extensible skills catalog (including `android-cli`, `saf-file-operations`, `termux-stream`, `shadcn`, and `web-search`) providing dynamic system prompts and execution recipes.
- **Execution Terminal & Console**: Live streaming console for agent diagnostics, system checks, and interactive shell execution.
- **Persistent Agent Memory**: Retains user preferences, hardware specs, and project conventions across conversations.
- **Scheduled Background Jobs**: Background agent tasks managed via Android AlarmManager and WorkManager.
- **Library & Artifacts**: Reusable prompt templates, workspace document viewer, and generated application artifacts.

## Tech Stack

- **Platform**: Android SDK 35/36 (Kotlin 2.0.21)
- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Architecture**: MVVM with Kotlin Coroutines and reactive StateFlow
- **Inference Runtimes**: Google AI Edge LiteRT LM & Gemini Cloud
- **Build System**: Gradle 9.3.1 (Kotlin DSL) with Android Gradle Plugin (AGP) 8.8.2

## License

This project is licensed under the MIT License.
