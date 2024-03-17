package com.example.digitallyapp.presentation.details.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.example.digitallyapp.R
import com.example.digitallyapp.presentation.details.ProcessedHistoricCountEntry
import com.example.digitallyapp.utils.DateComponent
import com.example.digitallyapp.utils.getDateComponent

@Composable
fun CounterHistory(
    modifier: Modifier = Modifier,
    //History Section
    counterHistory: List<ProcessedHistoricCountEntry>,
    selectedIndex: Int,
    updateSelectedIndex: (Int) -> Unit,
    dateFormatPreference: String,
) {
    val lazyGridState = rememberLazyGridState()
    val dayList = listOf("", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    var effectExecuted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!effectExecuted) {
            lazyGridState.scrollToItem(counterHistory.lastIndex)
            effectExecuted = true
        }
    }

    val numRows = 8
    val spacedBy = 4
    val roundedCornerSize = 4
    val itemSize = 40
    val rowHeight = ((numRows * itemSize) + (spacedBy * (numRows - 1)))

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
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.history_label),
                modifier = Modifier
                    .align(alignment = Alignment.Start)
                    .padding(bottom = dimensionResource(R.dimen.padding_small)),
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )
            Row(
                modifier = Modifier
                    .height(rowHeight.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(numRows),
                    horizontalArrangement = Arrangement.spacedBy(spacedBy.dp),
                    verticalArrangement = Arrangement.spacedBy(spacedBy.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = lazyGridState,
                ) {
                    itemsIndexed(counterHistory) { index, item ->
                        val backgroundColor = if (item.color == Color.Transparent) {
                            MaterialTheme.colorScheme.surfaceColorAtElevation(dimensionResource(id = R.dimen.padding_extra_small))
                        } else {
                            item.color
                        }
                        val backgroundColorInt = backgroundColor.toArgb()
                        val textColor =
                            if (ColorUtils.calculateLuminance(backgroundColorInt) < 0.5) {
                                Color.White
                            } else {
                                Color.Black
                            }
                        val text: String =
                            if (item.label.isNullOrEmpty()) {
                                // Empty spacer box
                                ""
                            } else {
                                val endIndex = if (getDateComponent(
                                        item.label,
                                        dateFormatPreference,
                                        DateComponent.DAY
                                    ).toIntOrNull() == null
                                ) {
                                    // Month Label
                                    3
                                } else {
                                    // Date number
                                    2
                                }
                                item.label.toString().substring(0, endIndex)
                            }

                        if (!item.hasNote) {
                            Text(
                                text = text,
                                color = textColor,
                                modifier = if (index == selectedIndex) {
                                    Modifier
                                        .background(
                                            color = backgroundColor,
                                            shape = RoundedCornerShape(roundedCornerSize.dp)
                                        )
                                        .border(
                                            2.dp,
                                            Color.White,
                                            RoundedCornerShape(roundedCornerSize.dp)
                                        )
                                        .size(itemSize.dp)
                                        .wrapContentSize()
                                } else {
                                    computeModifier(
                                        backgroundColor,
                                        index,
                                        item,
                                        lazyGridState,
                                        updateSelectedIndex,
                                        roundedCornerSize.dp,
                                        itemSize.dp
                                    )
                                }
                            )
                        } else {
                            BadgedBox(modifier = if (index == selectedIndex) {
                                Modifier
                                    .background(
                                        color = backgroundColor,
                                        shape = RoundedCornerShape(roundedCornerSize.dp)
                                    )
                                    .border(
                                        2.dp,
                                        Color.White,
                                        RoundedCornerShape(roundedCornerSize.dp)
                                    )
                                    .size(itemSize.dp)
                                    .wrapContentSize()
                            } else {
                                computeModifier(
                                    backgroundColor,
                                    index,
                                    item,
                                    lazyGridState,
                                    updateSelectedIndex,
                                    roundedCornerSize.dp,
                                    itemSize.dp
                                )
                            }, badge = {
                                Badge(
                                    Modifier
                                        .size(7.dp)
                                        .offset(3.dp, (-3).dp),
                                    containerColor = Color.White
                                )
                            }) {
                                Text(
                                    text = text,
                                    color = textColor,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(spacedBy.dp))

                Column(
                    modifier = Modifier.height(rowHeight.dp),
                    verticalArrangement = Arrangement.spacedBy(spacedBy.dp),
                ) {
                    dayList.forEachIndexed { _, day ->
                        Text(
                            text = day,
                            Modifier
                                .background(
                                    Color.Transparent, RoundedCornerShape(roundedCornerSize.dp)
                                )
                                .size(itemSize.dp)
                                .wrapContentSize(),
                            color = if (ColorUtils.calculateLuminance(MaterialTheme.colorScheme.surfaceVariant.toArgb()) < 0.5) {
                                Color.White
                            } else {
                                Color.Black
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private fun computeModifier(
    backgroundColor: Color,
    index: Int,
    item: ProcessedHistoricCountEntry,
    lazyGridState: LazyGridState,
    updateSelectedIndex: (Int) -> Unit,
    roundedCornerSize: Dp,
    itemSize: Dp
): Modifier {
    var thisModifier = Modifier
        .background(
            color = backgroundColor,
            shape = RoundedCornerShape(roundedCornerSize)
        )
    if (item.isBordered) {
        thisModifier = thisModifier.then(
            Modifier.border(
                2.dp, item.borderColor,
                RoundedCornerShape(roundedCornerSize)
            )
        )
    }
    return thisModifier.then(
        Modifier
            .size(itemSize)
            .wrapContentSize()
            .clickable {
                if (item.color != Color.Transparent)
                    updateSelectedIndex(
                        index
                    )
            }
    )
}