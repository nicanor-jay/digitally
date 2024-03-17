package com.example.digitallyapp.presentation.counter.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.emoji2.emojipicker.EmojiPickerView
import com.example.digitallyapp.R
import com.example.digitallyapp.presentation.counter.EntryFormCounterDetails
import kotlinx.coroutines.delay

@Composable
fun CounterInputForm(
    modifier: Modifier = Modifier,
    counterDetails: EntryFormCounterDetails,
    onValueChange: (EntryFormCounterDetails) -> Unit = {},
    isDropdownOpen: Boolean,
    toggleDropDown: () -> Unit,
    toggleEmojiPicker: () -> Unit,
    dropdownOptions: List<String>,
    onDropdownItemSelected: (String) -> Unit,
    isAddingNewCounter: Boolean,
    onClick: () -> Unit,
    saveEnabled: Boolean,
    showEmojiPicker: Boolean,
    emojiPickerView: EmojiPickerView?,
) {

    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {

        Column(modifier = Modifier.height(150.dp)) {
            // Add Icon section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(120.dp)
                ) {
                    if (counterDetails.emojiCon == "") {
                        Button(
                            onClick = {
                                toggleEmojiPicker()
                            },
                            modifier = Modifier
//                                .size(100.dp)
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_add_reaction_24),
                                contentDescription = null,
                                modifier = Modifier.size(60.dp)
                            )
                        }

                    } else {
                        ClickableText(
                            onClick = {
                                toggleEmojiPicker()
                            },
                            text = AnnotatedString(counterDetails.emojiCon),
                            modifier = Modifier.fillMaxSize(),
                            style = TextStyle(fontSize = 100.sp)
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier
                    .height(dimensionResource(id = R.dimen.padding_small))
                    .weight(1f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (counterDetails.emojiCon == "") {
                    ClickableText(
                        onClick = {
                            toggleEmojiPicker()
                        },
                        text = AnnotatedString(stringResource(R.string.add_emoji)),
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                        ),
                    )

                } else {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    ClickableText(
                        onClick = {
                            toggleEmojiPicker()
                        },
                        text = AnnotatedString(stringResource(R.string.change)),
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                        )
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    ClickableText(
                        onClick = { onValueChange(counterDetails.copy(emojiCon = "")) },
                        text = AnnotatedString(stringResource(R.string.remove)),
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_small)))
        }

        // Emoji Picker
        if (showEmojiPicker && emojiPickerView != null) {
            BasicAlertDialog(
                modifier = Modifier
                    .fillMaxHeight(0.75f)
                    .fillMaxWidth(1f),
                onDismissRequest = { toggleEmojiPicker() },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Card(
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.padding_medium))
                )
                {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(dimensionResource(id = R.dimen.padding_small)),
                        factory = { emojiPickerView }
                    )
                }
            }
        }
        val focusRequester = remember { FocusRequester() }

        // Name field
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = counterDetails.name,
                onValueChange = {
                    if (it.length <= 20) {
                        onValueChange(counterDetails.copy(name = it))
                    }
                },
                label = { Text(stringResource(R.string.counter_name_req)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )
        }

        LaunchedEffect(Unit) {
            if (isAddingNewCounter) {
                delay(100)
                focusRequester.requestFocus()
            }
        }

        // Reset Freq
        ExposedDropdownMenuBox(
            expanded = isDropdownOpen && isAddingNewCounter,
            onExpandedChange = {
                toggleDropDown()
            })
        {
            OutlinedTextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = counterDetails.resetFrequency,
                onValueChange = {},
                label = { Text(stringResource(R.string.counter_resetfreq_req)) },
                enabled = isAddingNewCounter, //Cannot change reset freq,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownOpen)
                }
            )
            ExposedDropdownMenu(
                expanded = isDropdownOpen && isAddingNewCounter,
                onDismissRequest = { toggleDropDown() },
            ) {
                dropdownOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onDropdownItemSelected(selectionOption)
                            toggleDropDown()
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        // Target
        OutlinedTextField(
            value = counterDetails.target?.toString() ?: "",
            onValueChange = { onValueChange(counterDetails.copy(target = it.toIntOrNull())) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            ),
            label = {
                if (counterDetails.resetFrequency != stringResource(R.string.none)) {
                    Text(
                        counterDetails.resetFrequency + " " + stringResource(
                            R.string.counter_target_req
                        )
                    )
                } else {
                    Text(stringResource(R.string.counter_target_req))
                }
            },
            singleLine = true,
        )
        Text(
            text = stringResource(R.string.required_fields),
            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium))
        )
        Button(
            onClick = onClick,
            enabled = saveEnabled,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_action))
        }
    }
}