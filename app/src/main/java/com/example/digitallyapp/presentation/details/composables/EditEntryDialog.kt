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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.Dialog
import com.example.digitallyapp.R
import com.example.digitallyapp.presentation.details.EntryDetails

@Composable
fun EditEntryDialog(
    onEditConfirm: () -> Unit,
    onEditReset: () -> Unit,
    onEditCancel: () -> Unit,
    modifier: Modifier = Modifier,
    updateUiState: (EntryDetails) -> Unit = {},
    entryDetails: EntryDetails,
    originalCount: Int?
) {
    val focusRequester = remember { FocusRequester() }

    // For focus requestor to work
    val entryTextFieldValue =
        remember {
            mutableStateOf(
                TextFieldValue(
                    entryDetails.count.toString(),
                    TextRange(entryDetails.count?.toString()?.length ?: 0)
                )
            )
        }


    Log.d("CounterDetailsScreen", "Current Entry Text: ${entryDetails.count}")

    Dialog(onDismissRequest = { onEditCancel() }) {
        ElevatedCard {
            Box(
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_large))
            ) {
                Column {
                    Row {
                        Text(
                            "Edit entry",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
                    Row {
                        OutlinedTextField(
                            modifier = modifier
                                .focusRequester(focusRequester)
                                .fillMaxWidth(),
                            value = entryTextFieldValue.value,
                            onValueChange = {
                                updateUiState(entryDetails.copy(count = it.text.toIntOrNull()))
                                entryTextFieldValue.value = it
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                            ),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_small)))
                    Button(
                        onClick =
                        {
                            onEditConfirm()
//                            onEditCancel()
                        },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = entryDetails.count != null
                    ) {
                        Text(stringResource(R.string.save_action))
                    }
                    OutlinedButton(
                        onClick =
                        {
                            onEditReset()
                            entryTextFieldValue.value = TextFieldValue(originalCount.toString())
                        },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = entryDetails.count != originalCount
                    ) {
                        Text(stringResource(R.string.reset_count_label))
                    }
                }
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        }
    }
}