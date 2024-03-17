package com.example.digitallyapp.presentation.details.composables

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.digitallyapp.R
import com.example.digitallyapp.data.Note
import com.example.digitallyapp.presentation.details.ProcessedHistoricCountEntry
import com.example.digitallyapp.utils.isDifferentDay
import java.util.Calendar

@Composable
fun CounterDayDetails(
    modifier: Modifier = Modifier,
    //Day Info Section
    currentEntry: ProcessedHistoricCountEntry?,
    //Note section
    currentNote: Note?,
    toggleIsNoteEditDialogOpen: () -> Unit,
    // Entry section
    toggleIsEntryEditDialogOpen: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_medium)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.padding_extra_small)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    dimensionResource(R.dimen.padding_medium)
                ),
        ) {
            if (currentEntry == null) {
                Text("null")
                return@ElevatedCard
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(R.dimen.padding_small)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentEntry.label!!,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isDifferentDay(currentEntry.dateTime, Calendar.getInstance().timeInMillis)) {
                    //Cannot edit the day you are currently on
                    IconButton(modifier = modifier
                        .size(20.dp)
                        .aspectRatio(1f), onClick = {
                        toggleIsEntryEditDialogOpen()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_counter_title)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(R.dimen.padding_small)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = if (currentEntry.target == null) Arrangement.Center else Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentEntry.count.toString(),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontSize = 40.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Log.d(
                        "DetailsScreen",
                        "Count: ${currentEntry.count}, Target: ${currentEntry.target}"
                    )
                    if (currentEntry.target != null) {
                        Text(
                            text = "/" + currentEntry.target.toString(),
                            modifier = Modifier.width(70.dp),
                            fontSize = MaterialTheme.typography.titleLarge.fontSize,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            Spacer(modifier = modifier.padding(dimensionResource(R.dimen.padding_small)))
            if (currentNote?.text == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,

                    ) {
                    Button(onClick = {
                        toggleIsNoteEditDialogOpen()
                    }) {
                        Text("+ Add Note")
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = modifier.padding(end = dimensionResource(R.dimen.padding_small)),
                        text = stringResource(R.string.note_label),
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                    IconButton(modifier = modifier
                        .size(20.dp)
                        .aspectRatio(1f), onClick = {
                        toggleIsNoteEditDialogOpen()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_counter_title)
                        )
                    }
                }
                Text(
                    text = currentNote.text,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Justify
                )
            }
        }
    }
}