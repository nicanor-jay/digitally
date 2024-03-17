package com.example.digitallyapp.presentation.details.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.digitallyapp.R
import com.example.digitallyapp.presentation.details.OverviewStats

@Composable
fun CounterOverview(modifier: Modifier = Modifier, overviewStats: List<OverviewStats>) {
    ElevatedCard(
        modifier = modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_medium)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.padding_extra_small)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    dimensionResource(R.dimen.padding_medium)
                ),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.overview_label),
                modifier = Modifier
                    .align(alignment = Alignment.Start)
                    .padding(bottom = dimensionResource(R.dimen.padding_small)),
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround
            ) {
                overviewStats.forEach { overviewItem ->
                    CounterOverviewItem(overviewItem = overviewItem)
                }
            }
        }

    }
}