# Follow-up Task: Tool Registration Framework

## Context
Phase 1 Group A added 4 built-in tools (Base64, Hash, URL Codec, Timestamp Converter). Each tool required changes across ~15 files:
- SearchModels.kt, SearchToolCoordinator.kt, SearchHandlerContainer.kt, SearchViewModel.kt
- SearchQueryCoordinator.kt, SearchPreferencesDelegate.kt, SearchStateExtractor.kt
- SearchStartupLifecycleDelegate.kt, SearchStateModels.kt, SearchViewModelPreferencesApi.kt
- UiPreferences.kt, UserAppPreferences.kt
- AliasHandler.kt, ToolSettingsRegistry.kt, AppSettingsRepository.kt
- SettingsDetailLevel2Screen.kt, SettingsCommands.kt, SettingsCallbacksBuilder.kt
- SettingsDataModels.kt, SettingsStateMappers.kt
- AiSearchResultShared.kt, strings.xml

This boilerplate is error-prone and makes adding new tools tedious.

## Proposed Solution
Create a **Tool Registration Framework** that centralizes tool integration:

```kotlin
// Core interfaces
interface SearchTool {
    val type: SearchToolType
    val preferenceKey: String
    val aliasFeatureId: String
    val strings: ToolStrings
    val handlerFactory: (UserAppPreferences) -> ToolHandler
    val isCandidate: (String) -> Boolean
    val detectAndProcess: (String) -> ToolResult?
}

data class ToolStrings(
    val titleResId: Int,
    val descriptionResId: Int,
    val keywords: List<String>
)

sealed class ToolResult {
    data class Success(val input: String, val output: String, val metadata: Map<String, String> = emptyMap()) : ToolResult()
    object NoMatch : ToolResult()
}

abstract class BaseToolHandler<T>(
    protected val userPreferences: UserAppPreferences,
    protected val isEnabled: (UserAppPreferences) -> Boolean,
    protected val detectAndProcess: (String) -> ToolResult?,
    protected val toolType: SearchToolType,
    protected val modeFlag: CalculatorState.() -> Unit
) {
    fun processQuery(query: String, forceMode: Boolean = false): CalculatorState { ... }
}

// Registry
object ToolRegistry {
    private val tools = mutableMapOf<SearchToolType, SearchTool>()
    
    fun register(tool: SearchTool) {
        tools[tool.type] = tool
        // Auto-wire: preferences, settings, aliases, attribution, etc.
    }
    
    fun getTool(type: SearchToolType): SearchTool? = tools[type]
    fun allTools(): List<SearchTool> = tools.values.toList()
}

// Usage for new tools
ToolRegistry.register(object : SearchTool {
    override val type = SearchToolType.TIMESTAMP_CONVERTER
    override val preferenceKey = "timestamp_converter_enabled"
    override val aliasFeatureId = "timestamp_converter_mode"
    override val strings = ToolStrings(
        titleResId = R.string.timestamp_converter_toggle_title,
        descriptionResId = R.string.timestamp_converter_toggle_desc,
        keywords = listOf("epoch", "iso", "timestamp", "date")
    )
    override val handlerFactory = { prefs -> TimestampConverterHandler(prefs) }
    override val isCandidate = TimestampConverterUtils::isCandidate
    override val detectAndProcess = TimestampConverterUtils::detectAndProcess
})
```

## Benefits
- New tool integration: ~3 files (Utils, Handler, Registration) instead of ~15
- Consistent behavior enforced by base classes
- Single source of truth for tool metadata
- Easier testing (mock registry)
- Centralized enable/disable logic

## Files to Create/Modify
1. `search/core/ToolRegistry.kt` - Registration logic
2. `search/core/SearchTool.kt` - Interfaces
3. `search/core/BaseToolHandler.kt` - Shared handler logic
4. Update existing tools to use framework
5. Update SearchToolCoordinator to use registry
6. Update SearchHandlerContainer to use registry
7. Update settings to read from registry

## Priority
Medium - Technical debt reduction for future tool additions