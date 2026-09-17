package com.tk.quicksearch.tools.termux

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tk.quicksearch.R
import com.tk.quicksearch.search.core.TermuxCommandState
import com.tk.quicksearch.search.core.TermuxCommandStatus
import com.tk.quicksearch.search.core.TermuxExecutionMode
import com.tk.quicksearch.search.searchScreen.shared.InformationCard
import com.tk.quicksearch.shared.ui.theme.DesignTokens

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TermuxCommandResult(
    commandState: TermuxCommandState,
    showWallpaperBackground: Boolean = false,
    onExecute: () -> Unit = {},
    onGrantPermission: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingSmall),
    ) {
        InformationCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            showWallpaperBackground = showWallpaperBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingSmall),
                ) {
                    Icon(
                        Icons.Rounded.Terminal,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(DesignTokens.IconSizeSmall),
                    )
                    Text(
                        text = stringResource(R.string.termux_command_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = "\$ ${commandState.command.orEmpty()}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                when (commandState.status) {
                    TermuxCommandStatus.Idle -> {
                        Button(
                            onClick = onExecute,
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text(stringResource(R.string.termux_execute_button))
                        }
                    }

                    TermuxCommandStatus.Loading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingMedium),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                            Text(
                                text = stringResource(R.string.termux_executing),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    TermuxCommandStatus.Success -> {
                        val stdout = commandState.stdout.orEmpty()
                        if (stdout.isNotBlank()) {
                            Text(
                                text = stdout,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                            )
                        }
                        if (!commandState.stderr.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(DesignTokens.SpacingSmall))
                            Text(
                                text = commandState.stderr,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                ),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                            )
                        }

                        if (commandState.exitCode != null) {
                            Spacer(modifier = Modifier.height(DesignTokens.SpacingSmall))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DesignTokens.SpacingSmall),
                            ) {
                                val exitLabel = stringResource(R.string.termux_exit_code, commandState.exitCode)
                                val exitColor =
                                    if (commandState.exitCode == 0) {
                                        Color(0xFF4CAF50)
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                Text(
                                    text = exitLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = exitColor,
                                )
                            }
                        }
                    }

                    TermuxCommandStatus.Error -> {
                        Text(
                            text = commandState.errorMessage
                                ?: stringResource(R.string.termux_error_generic),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    TermuxCommandStatus.PermissionError -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingSmall),
                        ) {
                            Text(
                                text = stringResource(R.string.termux_permission_error),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Text(
                                text = stringResource(R.string.termux_setup_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Button(
                                onClick = onGrantPermission,
                                modifier = Modifier.align(Alignment.End),
                            ) {
                                Text(stringResource(R.string.termux_grant_permission))
                            }
                        }
                    }

                    TermuxCommandStatus.NotInstalled -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingSmall),
                        ) {
                            Text(
                                text = stringResource(R.string.termux_not_installed),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(R.string.termux_not_installed_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
