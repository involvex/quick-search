package com.tk.quicksearch.search.apps

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HorizontalSplit
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image as IconImage
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PinEnd
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tk.quicksearch.R
import com.tk.quicksearch.search.core.AppIconShape
import com.tk.quicksearch.search.data.AppShortcutRepository.StaticShortcut
import com.tk.quicksearch.search.data.AppsRepository
import com.tk.quicksearch.search.data.TodayAppUsage
import com.tk.quicksearch.search.data.AppShortcutRepository.rememberShortcutIcon
import com.tk.quicksearch.search.data.AppShortcutRepository.shortcutDisplayName
import com.tk.quicksearch.search.models.AppInfo
import com.tk.quicksearch.pinnedNotifications.PinnedNotifications
import com.tk.quicksearch.search.apps.speedBump.SpeedBump
import com.tk.quicksearch.search.apps.speedBump.SpeedBumpExplainerDialog
import com.tk.quicksearch.shared.ui.components.ItemMenuPopup
import com.tk.quicksearch.shared.ui.components.ItemMenuRow
import com.tk.quicksearch.shared.ui.components.ItemMenuTile
import com.tk.quicksearch.shared.ui.theme.AppColors
import com.tk.quicksearch.widgets.customButtonsWidget.CustomWidgetButtonAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val ShortcutGridIconSize = 24.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppItemDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    isPinned: Boolean,
    showUninstall: Boolean,
    hasNickname: Boolean,
    hasTrigger: Boolean,
    shortcuts: List<StaticShortcut>,
    appInfo: AppInfo,
    iconPackPackage: String?,
    appIconShape: AppIconShape,
    onShortcutClick: (StaticShortcut) -> Unit,
    onAppInfoClick: () -> Unit,
    onHideApp: () -> Unit,
    onPinApp: () -> Unit,
    onUnpinApp: () -> Unit,
    onUninstallClick: () -> Unit,
    onNicknameClick: () -> Unit,
    onTriggerClick: () -> Unit,
    onAddToHome: () -> Unit,
    onOpenInSplitScreen: () -> Unit,
) {
    val context = LocalContext.current
    val todayUsage by produceState<TodayAppUsage?>(
        initialValue = null,
        key1 = expanded,
        key2 = appInfo.packageName,
    ) {
        if (expanded) {
            value = withContext(Dispatchers.IO) {
                AppsRepository(context.applicationContext).getTodayAppUsage(appInfo.packageName)
            }
        }
    }
    val isCurrentApp = appInfo.packageName == context.packageName
    val isLaunchableApp = appInfo.hasLaunchIntent
    val notificationAction =
        CustomWidgetButtonAction.App(
            packageName = appInfo.packageName,
            appName = appInfo.appName,
            userHandleId = appInfo.userHandleId,
        )
    val isPinnedToNotifications = PinnedNotifications.isPinned(context, notificationAction)
    val showIconPicker = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var speedBumpEnabled by remember(appInfo.packageName, expanded) {
        mutableStateOf(SpeedBump.isEnabled(context, appInfo.packageName))
    }
    // Non-null while the explainer is up; true when it followed the user first turning it on.
    var speedBumpExplainerJustEnabled by remember { mutableStateOf<Boolean?>(null) }
    val isOtherLaunchableApp = !isCurrentApp && isLaunchableApp
    val actions = buildList {
        if (isOtherLaunchableApp) {
            add(ItemMenuTile(
                label = stringResource(if (isPinned) R.string.action_unpin_app else R.string.action_pin_app),
                icon = {
                    Icon(
                        painter = painterResource(if (isPinned) R.drawable.ic_unpin else R.drawable.ic_pin),
                        contentDescription = null,
                    )
                },
                onClick = { onDismiss(); if (isPinned) onUnpinApp() else onPinApp() },
            ))
        }
        if (!isCurrentApp) {
            add(ItemMenuTile(
                label = stringResource(if (hasTrigger) R.string.action_edit_trigger else R.string.action_add_trigger),
                icon = { Icon(imageVector = Icons.Rounded.Bolt, contentDescription = null) },
                onClick = { onDismiss(); onTriggerClick() },
            ))
        }
        if (!isCurrentApp) {
            add(ItemMenuTile(
                label = stringResource(if (hasNickname) R.string.action_edit_nickname else R.string.common_nickname),
                icon = { Icon(imageVector = Icons.Rounded.Edit, contentDescription = null) },
                onClick = { onDismiss(); onNicknameClick() },
            ))
        }
        add(ItemMenuTile(
            label = stringResource(R.string.action_exclude_generic),
            icon = { Icon(imageVector = Icons.Rounded.VisibilityOff, contentDescription = null) },
            onClick = { onDismiss(); onHideApp() },
        ))
    }

    val appearanceRows = buildList {
        if (isOtherLaunchableApp) {
            add(ItemMenuRow(
                label = stringResource(R.string.speed_bump_title),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Spa,
                        contentDescription = null,
                        tint = if (speedBumpEnabled) AppColors.ActionPhone else LocalContentColor.current,
                    )
                },
                trailingText = stringResource(
                    if (speedBumpEnabled) R.string.app_menu_value_on else R.string.app_menu_value_off,
                ),
                onClick = {
                    speedBumpEnabled = SpeedBump.toggle(context, appInfo.packageName)
                    if (!SpeedBump.hasSeenExplainer(context)) {
                        SpeedBump.markExplainerSeen(context)
                        onDismiss()
                        speedBumpExplainerJustEnabled = true
                    }
                    // Otherwise the menu stays open so the new state can be seen.
                },
                onLongClick = { onDismiss(); speedBumpExplainerJustEnabled = false },
            ))
        }
        if (!isCurrentApp) {
            add(ItemMenuRow(
                label = stringResource(R.string.action_change_icon),
                icon = { Icon(imageVector = Icons.Rounded.IconImage, contentDescription = null) },
                onClick = { onDismiss(); showIconPicker.value = true },
            ))
        }
        if (isLaunchableApp) {
            add(ItemMenuRow(
                label = stringResource(
                    if (isPinnedToNotifications) R.string.action_unpin_from_notifications
                    else R.string.action_pin_to_notifications,
                ),
                icon = {
                    if (isPinnedToNotifications) {
                        Icon(painter = painterResource(R.drawable.ic_unpin), contentDescription = null)
                    } else {
                        Icon(imageVector = Icons.Rounded.PinEnd, contentDescription = null)
                    }
                },
                onClick = { onDismiss(); PinnedNotifications.toggle(context, notificationAction) },
            ))
        }
    }

    val launchRows = buildList {
        if (isLaunchableApp) {
            if (appInfo.userHandleId == null) {
                add(ItemMenuRow(
                    label = stringResource(R.string.action_open_in_split_screen),
                    icon = { Icon(imageVector = Icons.Rounded.HorizontalSplit, contentDescription = null) },
                    onClick = { onDismiss(); onOpenInSplitScreen() },
                ))
            }
            add(ItemMenuRow(
                label = stringResource(R.string.action_add_to_home),
                icon = { Icon(imageVector = Icons.Rounded.Home, contentDescription = null) },
                onClick = { onDismiss(); onAddToHome() },
            ))
        }
    }

    val footerButtons = buildList {
        add(ItemMenuRow(
            label = stringResource(R.string.action_app_info),
            icon = { Icon(imageVector = Icons.Rounded.Info, contentDescription = null) },
            onClick = { onDismiss(); onAppInfoClick() },
        ))
        if (showUninstall) {
            add(ItemMenuRow(
                label = stringResource(R.string.action_uninstall_app),
                icon = { Icon(imageVector = Icons.Rounded.Delete, contentDescription = null) },
                onClick = { onDismiss(); onUninstallClick() },
                destructive = true,
            ))
        }
    }

    val density = LocalDensity.current
    val shortcutIconSizePx = remember(density) {
        with(density) { ShortcutGridIconSize.roundToPx().coerceAtLeast(1) }
    }
    val iconResult = rememberAppIcon(
        packageName = appInfo.packageName,
        iconPackPackage = iconPackPackage,
        userHandleId = appInfo.userHandleId,
        forceCircularMask = appIconShape == AppIconShape.CIRCLE,
    )

    if (expanded) {
        val shortcutTiles = shortcuts.map { shortcut ->
            val displayName = shortcutDisplayName(shortcut)
            val iconBitmap = rememberShortcutIcon(shortcut, shortcutIconSizePx)
            ItemMenuTile(
                label = displayName,
                icon = {
                    if (iconBitmap != null) {
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = displayName,
                            modifier = Modifier.size(ShortcutGridIconSize),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Text(
                            text = displayName.trim().take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                },
                onClick = { onShortcutClick(shortcut); onDismiss() },
                enableMarquee = true,
            )
        }
        ItemMenuPopup(
            onDismiss = onDismiss,
            leadingContent = {
                iconResult.bitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = appInfo.appName,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit,
                    )
                }
            },
            title = {
                Column {
                    Text(
                        text = appInfo.appName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    todayUsage
                        ?.takeIf { it.openedCount > 0 }
                        ?.let { usage ->
                            Text(
                                text = stringResource(
                                    R.string.app_menu_usage_today,
                                    formatUsageDuration(usage.foregroundTimeMillis),
                                    pluralStringResource(
                                        R.plurals.app_menu_opened_count,
                                        usage.openedCount,
                                        usage.openedCount,
                                    ),
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                }
            },
            shortcutsTitle = stringResource(R.string.app_menu_section_shortcuts),
            shortcuts = shortcutTiles,
            actionsTitle = stringResource(R.string.app_menu_section_actions),
            actions = actions,
            rows = appearanceRows + launchRows,
            footer = footerButtons,
        )
    }

    speedBumpExplainerJustEnabled?.let { justEnabled ->
        SpeedBumpExplainerDialog(
            justEnabledForAppName = appInfo.appName.takeIf { justEnabled },
            onDismiss = { speedBumpExplainerJustEnabled = null },
        )
    }

    if (showIconPicker.value) {
        AppIconOverrideDrawer(
            packageName = appInfo.packageName,
            appName = appInfo.appName,
            onDismiss = { showIconPicker.value = false },
        )
    }
}

@Composable
private fun formatUsageDuration(durationMillis: Long): String {
    val totalMinutes = (durationMillis / 60_000L).toInt().coerceAtLeast(1)
    val halfHours = totalMinutes / 30
    val hours = halfHours / 2
    return when {
        totalMinutes >= 60 && halfHours % 2 == 1 ->
            stringResource(R.string.app_menu_usage_hours_decimal, "$hours.5")
        totalMinutes >= 60 -> pluralStringResource(R.plurals.app_menu_usage_hours, hours, hours)
        else -> pluralStringResource(R.plurals.app_menu_usage_minutes, totalMinutes, totalMinutes)
    }
}
