package com.gymroutine.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymroutine.app.data.model.DayScheduleItem
import com.gymroutine.app.data.model.Weekday
import com.gymroutine.app.ui.theme.BgDark
import com.gymroutine.app.ui.theme.BorderHighlight
import com.gymroutine.app.ui.theme.BorderMuted
import com.gymroutine.app.ui.theme.ElectricLime
import com.gymroutine.app.ui.theme.ElectricLimeGlow
import com.gymroutine.app.ui.theme.ElectricLimeSubtle
import com.gymroutine.app.ui.theme.SurfaceCard
import com.gymroutine.app.ui.theme.SurfaceCardHighlight
import com.gymroutine.app.ui.theme.TextMuted
import com.gymroutine.app.ui.theme.TextSecondary
import com.gymroutine.app.ui.theme.TextWhite

@Composable
fun DaySelector(
    scheduleItems: List<DayScheduleItem>,
    onDaySelected: (Weekday) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        scheduleItems.forEach { item ->
            DayCard(
                item = item,
                onClick = { onDaySelected(item.weekday) }
            )
        }
    }
}

@Composable
fun DayCard(
    item: DayScheduleItem,
    onClick: () -> Unit
) {
    val isSelected = item.isSelected
    val isToday = item.isToday

    val cardBg by animateColorAsState(
        targetValue = when {
            isSelected -> SurfaceCardHighlight
            isToday -> SurfaceCard
            else -> SurfaceCard.copy(alpha = 0.7f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "dayCardBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> ElectricLime
            isToday -> ElectricLime.copy(alpha = 0.5f)
            else -> BorderMuted
        },
        animationSpec = tween(durationMillis = 200),
        label = "dayCardBorder"
    )

    Box(
        modifier = Modifier
            .width(62.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                width = if (isSelected) 1.8.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Day Label (MON, TUE...)
            Text(
                text = item.weekday.shortLabel,
                fontSize = 11.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    isSelected -> ElectricLime
                    isToday -> ElectricLime.copy(alpha = 0.9f)
                    else -> TextMuted
                },
                letterSpacing = 0.5.sp
            )

            // Date Number (12, 13...)
            Text(
                text = item.dayOfMonth.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isSelected -> TextWhite
                    isToday -> TextWhite
                    else -> TextSecondary
                }
            )

            // Today Badge or Rest indicator
            if (isToday) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) ElectricLime else ElectricLimeSubtle)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "TODAY",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) BgDark else ElectricLime,
                        letterSpacing = 0.5.sp
                    )
                }
            } else if (item.weekday == Weekday.SUNDAY) {
                Text(
                    text = "REST",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
            } else {
                // Placeholder dot for consistent card height
                Box(modifier = Modifier.padding(vertical = 3.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) ElectricLime else BorderHighlight)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

