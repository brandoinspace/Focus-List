package com.brandoinspace.focuslist.custom

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import me.zhanghai.compose.preference.LocalPreferenceTheme
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.rememberPreferenceState

// Based on https://github.com/zhanghai/ComposePreference/blob/master/preference/src/commonMain/kotlin/SwitchPreference.kt

inline fun LazyListScope.switchActionPreference(
    key: String,
    defaultValue: Boolean,
    crossinline title: @Composable (Boolean) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    crossinline rememberState: @Composable () -> MutableState<Boolean> = {
        rememberPreferenceState(key, defaultValue)
    },
    crossinline enabled: (Boolean) -> Boolean = { true },
    noinline icon: @Composable ((Boolean) -> Unit)? = null,
    noinline summary: @Composable ((Boolean) -> Unit)? = null,
    noinline action: ((Boolean) -> Unit)? = null,
) {
    item(key = key, contentType = "SwitchActionPreference") {
        val state = rememberState()
        val value by state
        SwitchActionPreference(
            state = state,
            title = { title(value) },
            modifier = modifier,
            enabled = enabled(value),
            icon = icon?.let { { it(value) } },
            summary = summary?.let { { it(value) } },
            action = { action?.let { it(value) } }
        )
    }
}

@Composable
fun SwitchActionPreference(
    state: MutableState<Boolean>,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    action: ((Boolean) -> Unit)? = null,
) {
    var value by state
    SwitchActionPreference(
        value = value,
        onValueChange = { value = it },
        title = title,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        summary = summary,
        action = action,
    )
}

@Composable
fun SwitchActionPreference(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    action: ((Boolean) -> Unit)? = null
) {
    Preference(
        title = title,
        modifier = modifier.toggleable(value, enabled, Role.Switch, onValueChange = {
            onValueChange(it)
            action?.invoke(it)
        }),
        enabled = enabled,
        icon = icon,
        summary = summary,
        widgetContainer = {
            val theme = LocalPreferenceTheme.current
            Switch(
                checked = value,
                onCheckedChange = null,
                modifier = Modifier.padding(theme.padding),
                enabled = enabled
            )
        }
    )
}