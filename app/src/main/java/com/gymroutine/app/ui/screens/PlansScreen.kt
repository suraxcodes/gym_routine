package com.gymroutine.app.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymroutine.app.data.model.WorkoutPlan
import com.gymroutine.app.data.model.prettyDate
import com.gymroutine.app.ui.theme.AmberGold
import com.gymroutine.app.ui.theme.BgDark
import com.gymroutine.app.ui.theme.BorderHighlight
import com.gymroutine.app.ui.theme.BorderMuted
import com.gymroutine.app.ui.theme.DangerRed
import com.gymroutine.app.ui.theme.ElectricLime
import com.gymroutine.app.ui.theme.ElectricLimeSubtle
import com.gymroutine.app.ui.theme.SurfaceCard
import com.gymroutine.app.ui.theme.SurfaceDark
import com.gymroutine.app.ui.theme.SurfaceElevated
import com.gymroutine.app.ui.theme.TextLight
import com.gymroutine.app.ui.theme.TextMuted
import com.gymroutine.app.ui.theme.TextSecondary
import com.gymroutine.app.ui.theme.TextWhite
import com.gymroutine.app.ui.viewmodel.GymViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Workout Plans page.
 * Each plan has a start date and an optional end date. On any day the app shows the plan
 * whose period covers that date. A plan without an end date keeps showing until you set one
 * or until a newer plan starts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    viewModel: GymViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val plans = uiState.plans
    val todayKey = uiState.todayDateKey

    var showDialog by remember { mutableStateOf(false) }
    var editingPlan by remember { mutableStateOf<WorkoutPlan?>(null) }
    var deletingPlan by remember { mutableStateOf<WorkoutPlan?>(null) }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Workout Plans",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDark,
                    titleContentColor = TextWhite
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingPlan = null
                    showDialog = true
                },
                containerColor = ElectricLime,
                contentColor = BgDark,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "New Plan") },
                text = { Text("New Plan", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Each plan is shown only during its dates. A plan with no end date keeps showing every week until you set an end date or a newer plan starts.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (plans.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderMuted, RoundedCornerShape(16.dp))
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No plans yet. Tap \"New Plan\" to create one.",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            items(items = plans, key = { it.id }) { plan ->
                val status = when {
                    plan.id == uiState.todayPlanId -> PlanStatus.ACTIVE
                    todayKey.isNotEmpty() && plan.startDate > todayKey -> PlanStatus.UPCOMING
                    todayKey.isNotEmpty() && !plan.endDate.isNullOrBlank() && plan.endDate < todayKey -> PlanStatus.FINISHED
                    else -> PlanStatus.WAITING
                }
                PlanCard(
                    plan = plan,
                    status = status,
                    onEditWorkouts = {
                        viewModel.startPlanPreview(plan)
                        onBack()
                    },
                    onEditDates = {
                        editingPlan = plan
                        showDialog = true
                    },
                    onDelete = { deletingPlan = plan }
                )
            }

            item { Spacer(modifier = Modifier.height(88.dp)) }
        }
    }

    if (showDialog) {
        val copySource = plans.firstOrNull { it.id == uiState.todayPlanId } ?: plans.lastOrNull()
        PlanDialog(
            initial = editingPlan,
            todayKey = todayKey,
            copySource = if (editingPlan == null) copySource else null,
            onDismiss = { showDialog = false },
            onSave = { plan, copyFromId ->
                viewModel.savePlan(plan, copyFromId)
                showDialog = false
            }
        )
    }

    deletingPlan?.let { plan ->
        AlertDialog(
            onDismissRequest = { deletingPlan = null },
            containerColor = SurfaceDark,
            title = { Text("Delete plan?", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "\"${plan.name}\" and all workouts inside it will be deleted. This cannot be undone.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePlan(plan)
                        deletingPlan = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = TextWhite),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingPlan = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

private enum class PlanStatus(val label: String) {
    ACTIVE("ACTIVE NOW"),
    UPCOMING("UPCOMING"),
    FINISHED("FINISHED"),
    WAITING("NOT SHOWN TODAY")
}

@Composable
private fun PlanCard(
    plan: WorkoutPlan,
    status: PlanStatus,
    onEditWorkouts: () -> Unit,
    onEditDates: () -> Unit,
    onDelete: () -> Unit
) {
    val isActive = status == PlanStatus.ACTIVE
    val badgeColor = when (status) {
        PlanStatus.ACTIVE -> ElectricLime
        PlanStatus.UPCOMING -> AmberGold
        else -> TextMuted
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, if (isActive) ElectricLime else BorderMuted, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isActive) ElectricLimeSubtle else SurfaceElevated)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = status.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor,
                letterSpacing = 0.5.sp
            )
        }

        Text(
            text = plan.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(text = plan.periodLabel, fontSize = 13.sp, color = TextLight)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onEditWorkouts,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLimeSubtle, contentColor = ElectricLime),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Workouts", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            IconButton(onClick = onEditDates) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit name and dates",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete plan",
                    tint = DangerRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PlanDialog(
    initial: WorkoutPlan?,
    todayKey: String,
    copySource: WorkoutPlan?,
    onDismiss: () -> Unit,
    onSave: (plan: WorkoutPlan, copyFromPlanId: Long?) -> Unit
) {
    val context = LocalContext.current
    val fallbackToday = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time) }

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var startDate by remember { mutableStateOf(initial?.startDate ?: todayKey.ifEmpty { fallbackToday }) }
    var endDate by remember { mutableStateOf(initial?.endDate) }
    var copyWorkouts by remember { mutableStateOf(false) }

    val end = endDate
    val datesValid = end.isNullOrBlank() || end >= startDate

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextWhite,
        unfocusedTextColor = TextWhite,
        focusedBorderColor = ElectricLime,
        unfocusedBorderColor = BorderHighlight,
        focusedLabelColor = ElectricLime,
        unfocusedLabelColor = TextMuted,
        cursorColor = ElectricLime
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(
                text = if (initial == null) "New Workout Plan" else "Edit Plan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Plan name (e.g. Bulk Phase 1)") },
                    singleLine = true,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("STARTS ON", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp)
                DateButton(
                    text = prettyDate(startDate),
                    onClick = { pickDate(context, startDate) { startDate = it } }
                )

                Text("ENDS ON", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp)
                DateButton(
                    text = if (end.isNullOrBlank()) "No end date (keeps showing)" else prettyDate(end),
                    onClick = { pickDate(context, if (end.isNullOrBlank()) startDate else end) { endDate = it } }
                )
                if (!end.isNullOrBlank()) {
                    TextButton(onClick = { endDate = null }) {
                        Text("Remove end date", color = TextSecondary, fontSize = 13.sp)
                    }
                }

                if (!datesValid) {
                    Text(
                        text = "End date must be on or after the start date.",
                        fontSize = 12.sp,
                        color = DangerRed
                    )
                }

                if (copySource != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = copyWorkouts,
                            onCheckedChange = { copyWorkouts = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = ElectricLime,
                                checkmarkColor = BgDark,
                                uncheckedColor = TextMuted
                            )
                        )
                        Text(
                            text = "Copy workouts from \"${copySource.name}\"",
                            fontSize = 13.sp,
                            color = TextLight
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        WorkoutPlan(
                            id = initial?.id ?: 0,
                            name = name.trim(),
                            startDate = startDate,
                            endDate = end?.takeIf { it.isNotBlank() }
                        ),
                        if (copyWorkouts) copySource?.id else null
                    )
                },
                enabled = name.isNotBlank() && datesValid,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = BgDark),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
private fun DateButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
        border = BorderStroke(1.dp, BorderHighlight)
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = ElectricLime,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

/** Opens the system calendar picker. [current] and the result are "yyyy-MM-dd". */
private fun pickDate(context: Context, current: String, onPicked: (String) -> Unit) {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val calendar = Calendar.getInstance()
    try {
        format.parse(current)?.let { calendar.time = it }
    } catch (e: Exception) {
        // keep today
    }
    DatePickerDialog(
        context,
        { _, year, month, day ->
            val picked = Calendar.getInstance()
            picked.set(year, month, day)
            onPicked(format.format(picked.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
