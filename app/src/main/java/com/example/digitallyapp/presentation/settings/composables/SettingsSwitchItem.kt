package com.example.digitallyapp.presentation.settings.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsSwitchItem(
    text: String,
    description: String? = null,
    switchState: Boolean,
    onSwitchChange: ((Boolean) -> Unit),
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onSwitchChange.invoke(!switchState) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .sizeIn(minHeight = 50.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(.75f)) {
            // Label Text
            Text(text = text, style = MaterialTheme.typography.titleMedium)

            if (description != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        switchState.let { state ->
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = state,
                onCheckedChange = { onSwitchChange.invoke(it) } // Call the onSwitchChange listener if provided
            )
        }
    }
}