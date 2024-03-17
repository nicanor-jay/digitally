package com.example.digitallyapp.presentation.home.composables

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.digitallyapp.R
import com.example.digitallyapp.presentation.home.CounterWithCurrentCount
import com.example.digitallyapp.presentation.home.SelectionState
import com.example.digitallyapp.presentation.home.SortDropdownValues
import com.example.digitallyapp.presentation.home.UserSettingsPreferences

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CounterItemList(
    listState: LazyListState,
    counterList: List<CounterWithCurrentCount>,
    incrementCounter: (CounterWithCurrentCount) -> Unit,
    decrementCounter: (CounterWithCurrentCount) -> Unit,
    navigateToDetails: (Int) -> Unit,
    toggleSelectedCounterItem: (CounterWithCurrentCount) -> Unit,

    // 'Mode' (Selecting or not selecting)
    selectionState: SelectionState,

    // Preferences
    userPreferences: UserSettingsPreferences,

    ) {
    if (counterList.isEmpty() ||
        (counterList.all { it.isArchived } && !userPreferences.isShowingArchived)
    ) {

        Log.d("HOMESCREEN LOGGING", "Empty CounterList")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.no_counters_description),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
            items(
                if (userPreferences.sortOrderDirection) {
                    counterList.sortedWith(
                        compareBy {
                            when (userPreferences.selectedSortOption) {
                                SortDropdownValues.DateCreated -> it.counterId
                                SortDropdownValues.Name -> it.counterName
                                SortDropdownValues.ResetFrequency -> it.resetFrequency
                            }
                        }
                    )
                } else {
                    counterList.sortedWith(
                        compareBy {
                            when (userPreferences.selectedSortOption) {
                                SortDropdownValues.DateCreated -> it.counterId
                                SortDropdownValues.Name -> it.counterName
                                SortDropdownValues.ResetFrequency -> it.resetFrequency
                            }
                        }
                    ).reversed()
                }, key = { it.counterId }) { item ->
                val density = LocalDensity.current
                AnimatedVisibility(
                    modifier = Modifier.animateItemPlacement(
                        animationSpec = tween(
                            durationMillis = 100,
                            easing = FastOutSlowInEasing
                        )
                    ),
                    visible = if (item.isArchived) userPreferences.isShowingArchived else true,
                    enter = slideInVertically {
                        // Slide in from 40 dp from the top.
                        with(density) { -40.dp.roundToPx() }
                    } + fadeIn(
                        // Fade in with the initial alpha of 0.3f.
                        initialAlpha = 0.3f
                    ),
                    exit = slideOutVertically() + fadeOut()
                ) {

                    CounterItem(
                        counter = item,
                        incrementCounter = { incrementCounter(item) },
                        decrementCounter = { decrementCounter(item) },
                        modifier = Modifier
                            .padding(
                                horizontal = dimensionResource(id = R.dimen.padding_medium),
                                vertical = dimensionResource(id = R.dimen.padding_small)
                            )
                            .combinedClickable(
                                onClick = {
                                    if (selectionState.isSelecting) toggleSelectedCounterItem(item) else navigateToDetails(
                                        item.counterId.toInt()
                                    )
                                },
                                onLongClick = {
                                    toggleSelectedCounterItem(item)
                                }
                            ),
                        isSelected = selectionState.selectedCounters.contains(item),
                        showTargets = userPreferences.showTargets,
                        showConfetti = userPreferences.showConfetti
                    )
                }
            }
        }
        LaunchedEffect(userPreferences.selectedSortOption, userPreferences.sortOrderDirection) {
            listState.scrollToItem(0)
        }
    }
}