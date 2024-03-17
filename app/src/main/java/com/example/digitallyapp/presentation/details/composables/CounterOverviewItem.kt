package com.example.digitallyapp.presentation.details.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.digitallyapp.presentation.details.OverviewStats

@Composable
fun CounterOverviewItem(modifier: Modifier = Modifier, overviewItem: OverviewStats) {
    Column(modifier = Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = overviewItem.value,
            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = overviewItem.text,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}