package com.tk.quicksearch.settings.settingsDetailScreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.ui.graphics.vector.ImageVector
import com.tk.quicksearch.R
import com.tk.quicksearch.search.appSettings.AppSettingsToggleKey
import com.tk.quicksearch.searchEngines.AliasHandler

enum class ToolSettingId {
    CALCULATOR,
    UNIT_CONVERTER,
    DATE_CALCULATOR,
    CURRENCY_CONVERTER,
    COLOR_VISUALIZER,
    BASE64_CODEC,
    HASH_GENERATOR,
    URL_CODEC,
    TIMESTAMP_CONVERTER,
    WORD_CLOCK,
    DICTIONARY,
    WEATHER,
}

data class ToolSettingDefinition(
    val id: ToolSettingId,
    val aliasFeatureId: String? = null,
    val titleResId: Int,
    val defaultDescriptionResId: Int,
    val requiresGeminiApiKey: Boolean = false,
    val requiresGeminiDescriptionResId: Int? = null,
    val icon: ImageVector,
    val toggleKey: AppSettingsToggleKey? = null,
    val infoDestination: SettingsDetailType? = null,
    val aiBackedModelConfigurable: Boolean = false,
)

data class ToolSettingUiState(
    val enabled: Boolean,
    val aliasCode: String,
)

object ToolSettingsRegistry {
    val definitions: List<ToolSettingDefinition> =
        listOf(
            ToolSettingDefinition(
                id = ToolSettingId.CALCULATOR,
                aliasFeatureId = AliasHandler.CALCULATOR_ALIAS_FEATURE_ID,
                titleResId = R.string.calculator_toggle_title,
                defaultDescriptionResId = R.string.calculator_toggle_desc,
                icon = Icons.Rounded.Calculate,
                toggleKey = AppSettingsToggleKey.CALCULATOR,
            ),
            ToolSettingDefinition(
                id = ToolSettingId.UNIT_CONVERTER,
                aliasFeatureId = AliasHandler.UNIT_CONVERTER_ALIAS_FEATURE_ID,
                titleResId = R.string.unit_converter_info_title,
                defaultDescriptionResId = R.string.date_calculator_toggle_desc,
                icon = Icons.Rounded.Straighten,
                toggleKey = AppSettingsToggleKey.UNIT_CONVERTER,
                infoDestination = SettingsDetailType.UNIT_CONVERTER_INFO,
            ),
            ToolSettingDefinition(
                id = ToolSettingId.DATE_CALCULATOR,
                aliasFeatureId = AliasHandler.DATE_CALCULATOR_ALIAS_FEATURE_ID,
                titleResId = R.string.date_calculator_info_title,
                defaultDescriptionResId = R.string.date_calculator_toggle_desc,
                icon = Icons.Rounded.CalendarMonth,
                toggleKey = AppSettingsToggleKey.DATE_CALCULATOR,
                infoDestination = SettingsDetailType.DATE_CALCULATOR_INFO,
            ),
            ToolSettingDefinition(
                id = ToolSettingId.CURRENCY_CONVERTER,
                aliasFeatureId = AliasHandler.CURRENCY_CONVERTER_ALIAS_FEATURE_ID,
                titleResId = R.string.currency_converter_toggle_title,
                defaultDescriptionResId = R.string.currency_converter_toggle_desc,
                icon = Icons.Rounded.CurrencyExchange,
            ),
            ToolSettingDefinition(
                id = ToolSettingId.COLOR_VISUALIZER,
                titleResId = R.string.color_visualizer_toggle_title,
                defaultDescriptionResId = R.string.color_visualizer_toggle_desc,
                icon = Icons.Rounded.Palette,
                toggleKey = AppSettingsToggleKey.COLOR_VISUALIZER,
            ),
            ToolSettingDefinition(
                id = ToolSettingId.BASE64_CODEC,
                aliasFeatureId = AliasHandler.BASE64_CODEC_ALIAS_FEATURE_ID,
                titleResId = R.string.base64_codec_toggle_title,
                defaultDescriptionResId = R.string.base64_codec_toggle_desc,
                icon = Icons.Rounded.Calculate,
                toggleKey = AppSettingsToggleKey.BASE64_CODEC,
            ),
            ToolSettingDefinition(
                id = ToolSettingId.HASH_GENERATOR,
                aliasFeatureId = AliasHandler.HASH_GENERATOR_ALIAS_FEATURE_ID,
                titleResId = R.string.hash_generator_toggle_title,
                defaultDescriptionResId = R.string.hash_generator_toggle_desc,
                icon = Icons.Rounded.Calculate,
                toggleKey = AppSettingsToggleKey.HASH_GENERATOR,
            ),
            ToolSettingDefinition(
                id = ToolSettingId.URL_CODEC,
                aliasFeatureId = AliasHandler.URL_CODEC_ALIAS_FEATURE_ID,
                titleResId = R.string.url_codec_toggle_title,
                defaultDescriptionResId = R.string.url_codec_toggle_desc,
                icon = Icons.Rounded.Calculate,
                toggleKey = AppSettingsToggleKey.URL_CODEC,
            ),
            ToolSettingDefinition(
                id = ToolSettingId.TIMESTAMP_CONVERTER,
                aliasFeatureId = AliasHandler.TIMESTAMP_CONVERTER_ALIAS_FEATURE_ID,
                titleResId = R.string.timestamp_converter_toggle_title,
                defaultDescriptionResId = R.string.timestamp_converter_toggle_desc,
                icon = Icons.Rounded.AccessTime,
                toggleKey = AppSettingsToggleKey.TIMESTAMP_CONVERTER,
            ),
            ToolSettingDefinition(
                id = ToolSettingId.WORD_CLOCK,
                aliasFeatureId = AliasHandler.WORD_CLOCK_ALIAS_FEATURE_ID,
                titleResId = R.string.world_clock_toggle_title,
                defaultDescriptionResId = R.string.world_clock_toggle_desc,
                requiresGeminiApiKey = true,
                requiresGeminiDescriptionResId = R.string.currency_converter_requires_gemini_key,
                icon = Icons.Rounded.AccessTime,
                aiBackedModelConfigurable = true,
            ),
            ToolSettingDefinition(
                id = ToolSettingId.DICTIONARY,
                aliasFeatureId = AliasHandler.DICTIONARY_ALIAS_FEATURE_ID,
                titleResId = R.string.dictionary_toggle_title,
                defaultDescriptionResId = R.string.dictionary_toggle_desc,
                requiresGeminiApiKey = true,
                requiresGeminiDescriptionResId = R.string.currency_converter_requires_gemini_key,
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                toggleKey = AppSettingsToggleKey.DICTIONARY,
                aiBackedModelConfigurable = true,
            ),
            ToolSettingDefinition(
                id = ToolSettingId.WEATHER,
                aliasFeatureId = AliasHandler.WEATHER_ALIAS_FEATURE_ID,
                titleResId = R.string.weather_toggle_title,
                defaultDescriptionResId = R.string.weather_toggle_desc,
                requiresGeminiApiKey = true,
                requiresGeminiDescriptionResId = R.string.currency_converter_requires_gemini_key,
                icon = Icons.Rounded.Cloud,
                toggleKey = AppSettingsToggleKey.WEATHER,
                aiBackedModelConfigurable = true,
            ),
        )

    fun definitionFor(id: ToolSettingId): ToolSettingDefinition? =
        definitions.firstOrNull { it.id == id }
}
