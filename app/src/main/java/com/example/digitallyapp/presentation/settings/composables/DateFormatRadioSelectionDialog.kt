package com.example.digitallyapp.presentation.settings.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.window.Dialog
import com.example.digitallyapp.R
import com.example.digitallyapp.utils.DateFormats
import com.example.digitallyapp.utils.getDateFormatString

@Composable
fun DateFormatRadioSelectionDialog(
    onDismissRequest: () -> Unit,
    selectedFormat: DateFormats,
    updateDateFormatPreference: (DateFormats) -> Unit
) {

    Dialog(onDismissRequest = onDismissRequest) {
        ElevatedCard(modifier = Modifier.fillMaxWidth(.75f)) {
            Column(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
//                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Date format",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
                // Iterate through enum values
                DateFormats.values().forEach { format ->
                    val isSelected: Boolean = selectedFormat == format
                    val animatedColor by animateColorAsState(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else CardDefaults.cardColors().containerColor,
                        label = "color",
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    )
                    Card(
                        modifier = Modifier
                            .padding(vertical = dimensionResource(id = R.dimen.padding_extra_small))
                            .clickable { updateDateFormatPreference(format) },
                        colors = CardDefaults.cardColors(containerColor = animatedColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = getDateFormatString(format),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            RadioButton(
                                selected = isSelected, // Check selection state
                                onClick = { updateDateFormatPreference(format) } // Update and handle selection,
                            )
                        }
                    }
                }
            }
        }
    }
}