package com.tk.quicksearch.tools.termux

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import com.tk.quicksearch.R
import com.tk.quicksearch.search.core.TermuxSavedCommand
import com.tk.quicksearch.search.searchScreen.shared.InformationCard
import com.tk.quicksearch.shared.ui.theme.DesignTokens

/**
 * Global-search matches for saved Termux commands. Tapping a row locks the
 * command and shows the standard [TermuxCommandResult] card via the existing
 * detected-termux-command flow.
 */
@Composable
fun TermuxSuggestionList(
    suggestions: List<TermuxSavedCommand>,
    showWallpaperBackground: Boolean = false,
    onSuggestionClick: (String) -> Unit = {},
) {
    if (suggestions.isEmpty()) return
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.SpacingSmall),
    ) {
        suggestions.forEach { saved ->
            InformationCard(
                modifier = Modifier.fillMaxWidth(),
                showWallpaperBackground = showWallpaperBackground,
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSuggestionClick(saved.id) }
                            .padding(
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
                            text = saved.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (saved.aliasCode.isNotBlank()) {
                            Text(
                                text = stringResource(R.string.termux_saved_alias, saved.aliasCode),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = saved.command,
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(R.string.termux_execute_button),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(DesignTokens.IconSizeSmall),
                    )
                }
            }
        }
    }
}
