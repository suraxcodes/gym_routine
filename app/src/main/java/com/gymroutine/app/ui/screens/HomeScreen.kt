package com.gymroutine.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.gymroutine.app.ui.components.DaySelector
import com.gymroutine.app.ui.components.ExerciseCard
import com.gymroutine.app.ui.components.HomeHeader
import com.gymroutine.app.ui.components.RestDayView
import com.gymroutine.app.ui.components.WorkoutHeaderCard
import com.gymroutine.app.ui.theme.BgDark
import com.gymroutine.app.ui.theme.BorderMuted
import com.gymroutine.app.ui.theme.ElectricLime
import com.gymroutine.app.ui.theme.ElectricLimeSubtle
import com.gymroutine.app.ui.theme.SurfaceCard
import com.gymroutine.app.ui.theme.SurfaceElevated
import com.gymroutine.app.ui.theme.TextMuted
import com.gymroutine.app.ui.theme.TextSecondary
import com.gymroutine.app.ui.theme.TextWhite
import com.gymroutine.app.ui.viewmodel.GymViewModel

@Composable
fun HomeScreen(
    viewModel: GymViewModel,
    navController: androidx.navigation.NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = uiState.isEditingPlan) {
        viewModel.setEditing(false)
    }

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            // Bottom buttons: Diet Plan + Workout Plans
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgDark)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { navController.navigate("diet") },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLimeSubtle, contentColor = ElectricLime),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = "Diet",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Diet Plan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Button(
                    onClick = { navController.navigate("plans") },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLimeSubtle, contentColor = ElectricLime),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Workout Plans",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Workout Plans", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        },
        floatingActionButton = {
            if (!uiState.isSundayRestDay && !uiState.isEditingPlan && uiState.shownPlan != null) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.setEditing(true) },
                    containerColor = ElectricLime,
                    contentColor = BgDark,
                    shape = RoundedCornerShape(16.dp),
                    icon = {
                        Icon(
                            imageVector = if (uiState.sectionsWithStatus.isNotEmpty()) Icons.Default.Edit else Icons.Default.Add,
                            contentDescription = "Edit Routine"
                        )
                    },
                    text = {
                        Text(
                            text = if (uiState.sectionsWithStatus.isNotEmpty()) "Edit Routine" else "Add Workout",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header: Greeting, Date, Settings Icon
            HomeHeader(
                greeting = uiState.greeting,
                todayDate = uiState.currentFormattedDate,
                onSettingsClick = { viewModel.setSettingsOpen(true) }
            )

            // Weekly Schedule Selector: Mon-Sun
            DaySelector(
                scheduleItems = uiState.scheduleItems,
                onDaySelected = { weekday -> viewModel.selectWeekday(weekday) }
            )

            // Which plan is on screen (name + dates), or the "editing a plan" banner
            val shownPlan = uiState.shownPlan
            if (uiState.previewPlan != null && shownPlan != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ElectricLimeSubtle)
                        .padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "EDITING PLAN: ${shownPlan.name}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricLime
                        )
                        Text(
                            text = shownPlan.periodLabel,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    TextButton(onClick = { viewModel.stopPlanPreview() }) {
                        Text("Done", fontWeight = FontWeight.Bold, color = ElectricLime)
                    }
                }
            } else if (shownPlan != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = ElectricLime,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${shownPlan.name}  •  ${shownPlan.periodLabel}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Content Area
            if (uiState.isSundayRestDay) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        RestDayView()
                    }
                }
            } else if (uiState.shownPlan == null) {
                // No plan covers this date (the last plan ended and no new one has started)
                if (!uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderMuted, RoundedCornerShape(16.dp))
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "No workout plan for this date",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = "Your last plan has ended or the next one has not started yet. Create a new plan or change the dates.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { navController.navigate("plans") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricLime,
                                    contentColor = BgDark
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = "Workout Plans", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Today's / Selected Day's Workout Overview Card
                    item {
                        WorkoutHeaderCard(
                            weekday = uiState.selectedWeekday,
                            plan = uiState.currentPlan,
                            completedCount = uiState.completedCount,
                            totalCount = uiState.totalCount,
                            progressPercentage = uiState.completionPercentage,
                            onEditClick = { viewModel.setEditing(true) },
                            onResetTodayClick = { viewModel.resetTodayCompletions() }
                        )
                    }

                    // Render Each Workout Section and its Exercises
                    if (uiState.sectionsWithStatus.isNotEmpty()) {
                        uiState.sectionsWithStatus.forEach { secWithStatus ->
                            val section = secWithStatus.section

                            // Section Title Header
                            item(key = "sec_header_${section.id}_${section.title}") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(18.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(ElectricLime)
                                        )
                                        Text(
                                            text = section.title.uppercase(),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SurfaceElevated)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${secWithStatus.exercisesWithStatus.size} exercises",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = ElectricLime
                                        )
                                    }
                                }
                            }

                            // Exercise Cards under this Section
                            if (secWithStatus.exercisesWithStatus.isEmpty()) {
                                item(key = "sec_empty_${section.id}") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SurfaceCard)
                                            .border(1.dp, BorderMuted, RoundedCornerShape(12.dp))
                                            .padding(14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No exercises yet in '${section.title}'",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            } else {
                                items(
                                    items = secWithStatus.exercisesWithStatus,
                                    key = { "ex_${it.exercise.id}" }
                                ) { itemWithStatus ->
                                    ExerciseCard(
                                        item = itemWithStatus,
                                        onToggleCompletion = { isCompleted ->
                                            viewModel.toggleExerciseCompletion(itemWithStatus.exercise.id, isCompleted)
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        // Empty State for weekday without any sections
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceCard)
                                    .border(1.dp, BorderMuted, RoundedCornerShape(16.dp))
                                    .padding(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = "No Workout Scheduled",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "Add sections like 'Shoulders' or 'Abs' to organize your ${uiState.selectedWeekday.fullTitle} routine.",
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(
                                        onClick = { viewModel.setEditing(true) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ElectricLime,
                                            contentColor = BgDark
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(text = "Add Workout", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Edit Workout Screen Overlay
    AnimatedVisibility(
        visible = uiState.isEditingPlan,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        EditWorkoutScreen(
            weekday = uiState.selectedWeekday,
            initialPlan = uiState.currentPlan,
            onSaveSections = { sections ->
                viewModel.saveDaySections(sections)
            },
            onCancel = { viewModel.setEditing(false) }
        )
    }

    // Settings / Info Dialog
    if (uiState.isSettingsOpen) {
        SettingsDialog(
            onDismiss = { viewModel.setSettingsOpen(false) },
            onResetTodayCompletions = { viewModel.resetTodayCompletions() },
            onRestoreDefaults = { viewModel.resetToDefaults() }
        )
    }
}
