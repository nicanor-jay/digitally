package com.example.digitallyapp.presentation.details.composables

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.Dialog
import com.example.digitallyapp.R

@Composable
fun EditNoteDialog(
    onEditConfirm: () -> Unit,
    onEditCancel: () -> Unit,
    modifier: Modifier = Modifier,
    noteText: String,
    onValueChange: (String) -> Unit = {},
    isAddingOrEditing: Boolean
) {
    val focusRequester = remember { FocusRequester() }

    // For focus requestor to work
    val noteTextFieldValue =
        remember { mutableStateOf(TextFieldValue(noteText, TextRange(noteText.length))) }

    Log.d("CounterDetailsScreen", "Current Note Text: $noteText")

    Dialog(onDismissRequest = { onEditCancel() }) {
        ElevatedCard {
            Box(
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_large))
            ) {
                Column {
                    Row {
                        Text(
                            if (isAddingOrEditing) "Add note" else "Edit note",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
                    Row {
                        OutlinedTextField(
                            modifier = modifier
                                .focusRequester(focusRequester)
                                .fillMaxWidth(),
                            value = noteTextFieldValue.value,
                            onValueChange = {
                                if (it.text.length <= 200) {
                                    noteTextFieldValue.value = it
                                    onValueChange(it.text)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                keyboardType = KeyboardType.Text
                            ),
                            label = { Text("Note") }
                        )

                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                    Row {
                        Button(
                            onClick = {
                                onEditConfirm()
                            },
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.save_action))
                        }
                    }
                }
            }
        }
    }
}