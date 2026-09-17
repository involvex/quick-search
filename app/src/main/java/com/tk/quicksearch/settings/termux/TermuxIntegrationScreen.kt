package com.tk.quicksearch.settings.termux

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.tk.quicksearch.R
import com.tk.quicksearch.search.apps.rememberAppIcon
import com.tk.quicksearch.search.core.TermuxExecutionMode
import com.tk.quicksearch.search.core.TermuxSavedCommand
import com.tk.quicksearch.searchEngines.AliasValidator.hasExactAliasConflict
import com.tk.quicksearch.searchEngines.AliasValidator.isValidGeneralAliasCode
import com.tk.quicksearch.searchEngines.AliasValidator.normalizeShortcutCodeInput
import com.tk.quicksearch.settings.shared.SettingsCard
import com.tk.quicksearch.settings.shared.SettingsToggleRow
import com.tk.quicksearch.shared.ui.components.dialogTextFieldColors
import com.tk.quicksearch.shared.ui.theme.DesignTokens
import com.tk.quicksearch.tools.termux.TermuxVariant

@Composable
fun TermuxIntegrationScreen(
    enabled: Boolean,
    prefix: String,
    defaultExecutionMode: TermuxExecutionMode,
    savedCommands: List<TermuxSavedCommand>,
    existingAliases: Map<String, String>,
    variantPackageOverride: String,
    onSetEnabled: (Boolean) -> Unit,
    onSetPrefix: (String) -> Unit,
    onSetExecutionMode: (TermuxExecutionMode) -> Unit,
    onSetVariantPackage: (String) -> Unit,
    onAddCommand: (alias: String, name: String, command: String, mode: TermuxExecutionMode) -> Unit,
    onDeleteCommand: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var permissionCheckKey by remember { mutableIntStateOf(0) }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            permissionCheckKey++
        }

    val installedVariants = remember { TermuxVariant.installedVariants(context) }
    var selectedOverride by rememberSaveable(variantPackageOverride) {
        mutableStateOf(variantPackageOverride)
    }
    val selectedVariant = remember(selectedOverride, installedVariants) {
        TermuxVariant.resolve(context, selectedOverride)
    }
    val selectedInstalled = remember(selectedVariant, installedVariants) {
        selectedVariant in installedVariants
    }
    // permissionCheckKey is a recompute trigger: bumping it re-reads the grant state.
    val permissionGranted = remember(selectedVariant, permissionCheckKey) {
        selectedVariant.hasRunCommandPermission(context)
    }

    var prefixInput by rememberSaveable(prefix) { mutableStateOf(prefix) }

    var newAlias by rememberSaveable { mutableStateOf("") }
    var newName by rememberSaveable { mutableStateOf("") }
    var newCommand by rememberSaveable { mutableStateOf("") }
    var newMode by rememberSaveable { mutableStateOf(TermuxExecutionMode.BACKGROUND) }
    val normalizedNewAlias = normalizeShortcutCodeInput(newAlias)
    val newAliasConflict = hasExactAliasConflict(newAlias, existingAliases)
    val canAddCommand =
        isValidGeneralAliasCode(newAlias) && !newAliasConflict &&
            newName.isNotBlank() && newCommand.isNotBlank()

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingSmall),
    ) {
        SettingsCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(
                    horizontal = DesignTokens.CardHorizontalPadding,
                    vertical = DesignTokens.CardVerticalPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium),
            ) {
                Text(
                    stringResource(R.string.termux_settings_status_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (installedVariants.isEmpty()) {
                    Text(
                        stringResource(R.string.termux_not_installed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.termux_not_installed_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = selectedVariant.displayName + " · " +
                            stringResource(R.string.termux_settings_installed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium),
                    ) {
                        Text(
                            text = stringResource(R.string.termux_settings_permission_title) + ": " +
                                stringResource(
                                    if (permissionGranted) {
                                        R.string.termux_settings_permission_granted
                                    } else {
                                        R.string.termux_settings_permission_denied
                                    },
                                ),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (permissionGranted) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            modifier = Modifier.weight(1f),
                        )
                        if (selectedInstalled && !permissionGranted) {
                            OutlinedButton(
                                onClick = {
                                    permissionLauncher.launch(selectedVariant.runCommandPermission)
                                },
                            ) {
                                Text(stringResource(R.string.termux_grant_permission))
                            }
                        }
                    }
                    Text(
                        stringResource(R.string.termux_setup_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        SettingsCard(modifier = Modifier.fillMaxWidth()) {
            SettingsToggleRow(
                title = stringResource(R.string.termux_settings_title),
                subtitle = stringResource(R.string.termux_settings_description),
                checked = enabled,
                onCheckedChange = onSetEnabled,
                leadingIcon = Icons.Rounded.Terminal,
                showDivider = false,
            )
        }

        SettingsCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(
                    horizontal = DesignTokens.CardHorizontalPadding,
                    vertical = DesignTokens.CardVerticalPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium),
            ) {
                Text(
                    stringResource(R.string.termux_settings_variant_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Column(modifier = Modifier.selectableGroup()) {
                    TermuxVariantRow(
                        title = stringResource(R.string.termux_settings_variant_auto),
                        subtitle = stringResource(R.string.termux_settings_variant_auto_desc),
                        iconBitmap = null,
                        selected = selectedOverride.isBlank(),
                        onClick = {
                            selectedOverride = ""
                            onSetVariantPackage("")
                        },
                    )
                    TermuxVariant.entries.forEach { variant ->
                        val installed = variant in installedVariants
                        TermuxVariantRow(
                            title = variant.displayName,
                            subtitle = stringResource(
                                if (installed) {
                                    R.string.termux_settings_installed
                                } else {
                                    R.string.termux_settings_not_installed
                                },
                            ),
                            iconBitmap = rememberAppIcon(variant.packageName).bitmap,
                            selected = selectedOverride == variant.packageName,
                            onClick = {
                                selectedOverride = variant.packageName
                                onSetVariantPackage(variant.packageName)
                            },
                        )
                    }
                }
            }
        }

        SettingsCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(
                    horizontal = DesignTokens.CardHorizontalPadding,
                    vertical = DesignTokens.CardVerticalPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium),
            ) {
                Text(
                    stringResource(R.string.termux_settings_prefix_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = prefixInput,
                        onValueChange = { prefixInput = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = dialogTextFieldColors(),
                    )
                    Button(
                        onClick = { onSetPrefix(prefixInput) },
                        enabled = prefixInput != prefix,
                    ) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
                Text(
                    stringResource(R.string.termux_settings_execution_mode_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Column(modifier = Modifier.selectableGroup()) {
                    ExecutionModeRow(
                        mode = TermuxExecutionMode.BACKGROUND,
                        selected = defaultExecutionMode == TermuxExecutionMode.BACKGROUND,
                        onClick = { onSetExecutionMode(TermuxExecutionMode.BACKGROUND) },
                    )
                    ExecutionModeRow(
                        mode = TermuxExecutionMode.FOREGROUND,
                        selected = defaultExecutionMode == TermuxExecutionMode.FOREGROUND,
                        onClick = { onSetExecutionMode(TermuxExecutionMode.FOREGROUND) },
                    )
                }
            }
        }

        SettingsCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(
                    horizontal = DesignTokens.CardHorizontalPadding,
                    vertical = DesignTokens.CardVerticalPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium),
            ) {
                Text(
                    stringResource(R.string.termux_saved_commands_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    stringResource(R.string.termux_add_command_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.termux_name_label)) },
                    singleLine = true,
                    colors = dialogTextFieldColors(),
                )
                OutlinedTextField(
                    value = newAlias,
                    onValueChange = { newAlias = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.settings_custom_tool_alias_label)) },
                    supportingText = if (newAliasConflict) {
                        { Text(stringResource(R.string.termux_alias_conflict)) }
                    } else {
                        null
                    },
                    isError = newAliasConflict,
                    singleLine = true,
                    colors = dialogTextFieldColors(),
                )
                OutlinedTextField(
                    value = newCommand,
                    onValueChange = { newCommand = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.termux_command_label_field)) },
                    singleLine = true,
                    colors = dialogTextFieldColors(),
                )
                Column(modifier = Modifier.selectableGroup()) {
                    ExecutionModeRow(
                        mode = TermuxExecutionMode.BACKGROUND,
                        selected = newMode == TermuxExecutionMode.BACKGROUND,
                        onClick = { newMode = TermuxExecutionMode.BACKGROUND },
                    )
                    ExecutionModeRow(
                        mode = TermuxExecutionMode.FOREGROUND,
                        selected = newMode == TermuxExecutionMode.FOREGROUND,
                        onClick = { newMode = TermuxExecutionMode.FOREGROUND },
                    )
                }
                Button(
                    onClick = {
                        onAddCommand(normalizedNewAlias, newName.trim(), newCommand.trim(), newMode)
                        newAlias = ""
                        newName = ""
                        newCommand = ""
                        newMode = TermuxExecutionMode.BACKGROUND
                    },
                    enabled = canAddCommand,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(stringResource(R.string.termux_add_command_button))
                }
                TermuxPresetSection(
                    savedCommands = savedCommands,
                    onAddPreset = { preset ->
                        onAddCommand(preset.alias, preset.name, preset.command, preset.executionMode)
                    },
                )
            }
        }

        savedCommands.forEach { saved ->
            SettingsCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(
                        horizontal = DesignTokens.CardHorizontalPadding,
                        vertical = DesignTokens.CardVerticalPadding,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium),
                ) {
                    Icon(
                        Icons.Rounded.Terminal,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(DesignTokens.IconSizeSmall),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingXSmall),
                    ) {
                        Text(
                            saved.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            stringResource(
                                R.string.termux_saved_alias,
                                existingAliases[saved.id].orEmpty(),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            saved.command,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { onDeleteCommand(saved.id) }) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.termux_delete_command),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TermuxPresetSection(
    savedCommands: List<TermuxSavedCommand>,
    onAddPreset: (com.tk.quicksearch.tools.termux.TermuxCommandPreset) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingSmall),
    ) {
        Text(
            stringResource(R.string.termux_presets_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        com.tk.quicksearch.tools.termux.TERMUX_COMMAND_PRESETS.forEach { preset ->
            val alreadyAdded =
                remember(savedCommands, preset) {
                    savedCommands.any {
                        it.aliasCode.equals(preset.alias, ignoreCase = true) ||
                            it.command == preset.command
                    }
                }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingXSmall),
                ) {
                    Text(
                        preset.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        (preset.command +
                            if (preset.requiresApiApp) {
                                " · " + stringResource(R.string.termux_preset_requires_api)
                            } else {
                                ""
                            }),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (alreadyAdded) {
                    Text(
                        stringResource(R.string.termux_preset_added),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    TextButton(onClick = { onAddPreset(preset) }) {
                        Text(stringResource(R.string.termux_preset_add))
                    }
                }
            }
        }
    }
}

@Composable
private fun TermuxVariantRow(
    title: String,
    subtitle: String,
    iconBitmap: ImageBitmap?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(
            role = Role.RadioButton,
            onClick = onClick,
        ).padding(vertical = DesignTokens.SpacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium),
    ) {
        RadioButton(selected = selected, onClick = null)
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap,
                contentDescription = null,
                modifier = Modifier.size(DesignTokens.IconSizeSmall),
                contentScale = ContentScale.Fit,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExecutionModeRow(
    mode: TermuxExecutionMode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(
            role = Role.RadioButton,
            onClick = onClick,
        ).padding(vertical = DesignTokens.SpacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = stringResource(
                if (mode == TermuxExecutionMode.BACKGROUND) {
                    R.string.termux_execution_mode_background
                } else {
                    R.string.termux_execution_mode_foreground
                },
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
