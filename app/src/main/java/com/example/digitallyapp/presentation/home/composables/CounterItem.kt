package com.example.digitallyapp.presentation.home.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.digitallyapp.R
import com.example.digitallyapp.presentation.home.CounterWithCurrentCount
import com.example.digitallyapp.ui.theme.EmptyGray
import com.example.digitallyapp.utils.ResetFrequency
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.PartySystem
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun CounterItem(
    counter: CounterWithCurrentCount,
    incrementCounter: (CounterWithCurrentCount) -> Unit,
    decrementCounter: (CounterWithCurrentCount) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    showTargets: Boolean,
    showConfetti: Boolean
) {

    val resetFrequencyText =
        if (counter.resetFrequency != ResetFrequency.NONE) counter.resetFrequency.label else ""

    val targetText = if (counter.target != null) "/ ${counter.target}" else ""
    val animatedColor by animateColorAsState(
        if (counter.isArchived) EmptyGray else MaterialTheme.colorScheme.onSecondaryContainer,
        label = "color",
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )

    val party = Party(
        speed = 10f,
        maxSpeed = 30f,
        damping = 0.9f,
        angle = Angle.RIGHT - 45,
        spread = 60,
        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
        emitter = Emitter(duration = 1, TimeUnit.SECONDS).perSecond(30),
        position = Position.Relative(0.0, 1.0)
    )

    var confettiVisibility by remember {
        mutableStateOf(false) // Initialize with false
    }

    OutlinedCard(
        modifier = modifier
            .height(130.dp),
        border = if (isSelected) BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.onPrimary
        ) else BorderStroke(0.dp, Color.Transparent),
        elevation =
        CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.padding_small)),
        colors =
        if (isSelected) CardDefaults.cardColors(containerColor = Color.LightGray) else CardDefaults.cardColors()
    ) {
        Box {
            if (showConfetti && confettiVisibility) {
                KonfettiView(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(if (confettiVisibility) 0f else 0f),
                    parties = listOf(
                        party,
                        party.copy(
                            angle = party.angle - 90, // flip angle from right to left
                            position = Position.Relative(1.0, 1.0)
                        ),
                    ),
                    updateListener =
                    object : OnParticleSystemUpdateListener {
                        override fun onParticleSystemEnded(
                            system: PartySystem,
                            activeSystems: Int,
                        ) {
                            if (activeSystems == 0) confettiVisibility = false
                        }
                    },
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimensionResource(R.dimen.padding_medium)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (counter.counterEmoji != "") {
                        Text(
                            text = counter.counterEmoji,
                            modifier = Modifier,
                            fontSize = MaterialTheme.typography.displayMedium.fontSize
                        )
                    }
                    Box {
                        Column(
                            modifier = Modifier.padding(
                                start = if (counter.counterEmoji != "") dimensionResource(R.dimen.padding_small) else 0.dp,
                                top = if (counter.resetFrequency != ResetFrequency.NONE) 20.dp else 0.dp,
                                end = dimensionResource(R.dimen.padding_small)
                            )
                        ) {
                            Text(
                                text = counter.counterName,
                                style = MaterialTheme.typography.headlineSmall,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                                color = animatedColor,
                            )

                            if (resetFrequencyText != "") {
                                Text(
                                    text = resetFrequencyText,
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { decrementCounter(counter) },
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape),
                        contentPadding = PaddingValues(0.dp),
                        enabled = if (!isSelected) counter.currentCount > 0 else false
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.remove_48px),
                            contentDescription = null
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        if (targetText != "" && showTargets) Spacer(modifier = Modifier.height(22.dp))

                        AnimatedContent(
                            targetState = counter.currentCount,
                            transitionSpec = {
                                // Compare the incoming number with the previous number.
                                if (targetState > initialState) {
                                    (slideInHorizontally { width -> width / 2 } + fadeIn()).togetherWith(
                                        slideOutHorizontally { width -> -width / 2 } + fadeOut())
                                } else {
                                    (slideInHorizontally { width -> -width / 2 } + fadeIn()).togetherWith(
                                        slideOutHorizontally { width -> width / 2 } + fadeOut())
                                }.using(
                                    SizeTransform(clip = false)
                                )
                            }, label = ""
                        ) { newCount ->

                            val fontSize = if (newCount.toString().length >= 3) {
                                // Adjust the font size dynamically based on the length of the text
                                MaterialTheme.typography.titleLarge.fontSize
                            } else {
                                MaterialTheme.typography.headlineMedium.fontSize
                            }

                            Text(
                                text = newCount.toString(),
                                modifier = Modifier
                                    .width(70.dp),
//                                .padding(horizontal = 16.dp),
                                fontSize = fontSize,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                            )
                        }
                        if (targetText != "" && showTargets) {
                            Text(
                                text = targetText,
                                modifier = Modifier
                                    .width(70.dp),
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        if (counter.target != null && showConfetti) {
                            if (counter.currentCount + 1 == counter.target) {
                                confettiVisibility = true // Trigger confetti when target is reached
                            }
                        }
                        incrementCounter(counter)
                    },
                    modifier = Modifier
                        .size(35.dp)
                        .clip(CircleShape),
                    contentPadding = PaddingValues(0.dp),
                    enabled = if (!isSelected) counter.currentCount < 9999 else false
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }
        }
    }
}