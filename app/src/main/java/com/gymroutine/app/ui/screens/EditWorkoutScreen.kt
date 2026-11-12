package com.gymroutine.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymroutine.app.data.model.DayWorkoutPlan
import com.gymroutine.app.data.model.Exercise
import com.gymroutine.app.data.model.Weekday
import com.gymroutine.app.data.model.WorkoutSection
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkoutScreen(
    weekday: Weekday,
    initialPlan: DayWorkoutPlan?,
    onSaveSections: (sections: List<WorkoutSection>) -> Unit,
    onCancel: () -> Unit
) {
    // List of sections for this day
    val sectionList = remember {
        mutableStateListOf<WorkoutSection>().apply {
            addAll(initialPlan?.sections ?: emptyList())
        }
    }

    // Dialog state for adding or editing a Section Title
    var showSectionDialog by remember { mutableStateOf(false) }
    var editingSectionIndex by remember { mutableIntStateOf(-1) }
    var sectionDialogTitle by remember { mutableStateOf("") }

    // Dialog state for adding or editing an Exercise within a Section
    var showExerciseDialog by remember { mutableStateOf(false) }
    var targetSectionIndex by remember { mutableIntStateOf(-1) }
    var editingExerciseIndex by remember { mutableIntStateOf(-1) }
    var dialogExName by remember { mutableStateOf("") }
    var dialogExSets by remember { mutableIntStateOf(3) }
    var dialogExReps by remember { mutableStateOf("10-12") }
    var dialogExNotes by remember { mutableStateOf("") }

    fun openAddSectionDialog() {
        editingSectionIndex = -1
        sectionDialogTitle = ""
        showSectionDialog = true
    }

    fun openEditSectionDialog(secIndex: Int) {
        editingSectionIndex = secIndex
        sectionDialogTitle = sectionList[secIndex].title
        showSectionDialog = true
    }

    fun openAddExerciseDialog(secIndex: Int) {
        targetSectionIndex = secIndex
        editingExerciseIndex = -1
        dialogExName = ""
        dialogExSets = 3
        dialogExReps = "10-12"
        dialogExNotes = ""
        showExerciseDialog = true
    }

    fun openEditExerciseDialog(secIndex: Int, exIndex: Int) {
        val ex = sectionList[secIndex].exercises[exIndex]
        targetSectionIndex = secIndex
        editingExerciseIndex = exIndex
        dialogExName = ex.name
        dialogExSets = ex.sets
        dialogExReps = ex.reps
        dialogExNotes = ex.notes
        showExerciseDialog = true
    }

    fun moveSection(from: Int, to: Int) {
        if (from in sectionList.indices && to in sectionList.indices) {
            val item = sectionList.removeAt(from)
            sectionList.add(to, item)
        }
    }

    fun moveExercise(secIndex: Int, from: Int, to: Int) {
        val sec = sectionList[secIndex]
        val exList = sec.exercises.toMutableList()
        if (from in exList.indices && to in exList.indices) {
            val item = exList.removeAt(from)
            exList.add(to, item)
            sectionList[secIndex] = sec.copy(exercises = exList)
        }
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Edit ${weekday.fullTitle}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "${sectionList.size} Workout Sections",
                            fontSize = 12.sp,
                            color = ElectricLime
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val sortedSections = sectionList.mapIndexed { sIdx, sec ->
                                val sortedExercises = sec.exercises.mapIndexed { eIdx, ex ->
                                    ex.copy(dayOfWeek = weekday.dayIndex, sortOrder = eIdx)
                                }
                                sec.copy(dayOfWeek = weekday.dayIndex, sortOrder = sIdx, exercises = sortedExercises)
                            }
                            onSaveSections(sortedSections)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricLime,
                            contentColor = BgDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WORKOUT SECTIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricLime,
                        letterSpacing = 1.sp
                    )

                    Button(
                        onClick = { openAddSectionDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceElevated,
                            contentColor = ElectricLime
                        ),
                        border = BorderStroke(1.dp, BorderHighlight),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Section",
                            tint = ElectricLime,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Section",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricLime
                        )
                    }
                }
            }

            if (sectionList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderMuted, RoundedCornerShape(16.dp))
                            .padding(32.dp),
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
                                text = "No Workout Sections Yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = "Add a title (e.g. 'Shoulders' or 'Abs') to start building your routine.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { openAddSectionDialog() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricLime,
                                    contentColor = BgDark
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = "Create First Section", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(sectionList) { secIndex, section ->
                    EditableSectionCard(
                        secIndex = secIndex,
                        totalSections = sectionList.size,
                        section = section,
                        onMoveUp = { moveSection(secIndex, secIndex - 1) },
                        onMoveDown = { moveSection(secIndex, secIndex + 1) },
                        onEditTitle = { openEditSectionDialog(secIndex) },
                        onDeleteSection = { sectionList.removeAt(secIndex) },
                        onAddExercise = { openAddExerciseDialog(secIndex) },
                        onMoveExerciseUp = { exIdx -> moveExercise(secIndex, exIdx, exIdx - 1) },
                        onMoveExerciseDown = { exIdx -> moveExercise(secIndex, exIdx, exIdx + 1) },
                        onEditExercise = { exIdx -> openEditExerciseDialog(secIndex, exIdx) },
                        onDeleteExercise = { exIdx ->
                            val updatedExercises = section.exercises.toMutableList()
                            updatedExercises.removeAt(exIdx)
                            sectionList[secIndex] = section.copy(exercises = updatedExercises)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Dialog for Adding / Editing a Section Title
    if (showSectionDialog) {
        AlertDialog(
            onDismissRequest = { showSectionDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = if (editingSectionIndex >= 0) "Edit Section Title" else "New Workout Section",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter a title for this workout section (e.g. Shoulders, Abs, Chest, Triceps, Cardio):",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = sectionDialogTitle,
                        onValueChange = { sectionDialogTitle = it },
                        label = { Text("Section Title *", color = TextSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricLime,
                            unfocusedBorderColor = BorderMuted,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val validTitle = sectionDialogTitle.trim()
                        if (validTitle.isNotBlank()) {
                            if (editingSectionIndex >= 0) {
                                sectionList[editingSectionIndex] = sectionList[editingSectionIndex].copy(title = validTitle)
                            } else {
                                sectionList.add(
                                    WorkoutSection(
                                        dayOfWeek = weekday.dayIndex,
                                        title = validTitle,
                                        sortOrder = sectionList.size,
                                        exercises = emptyList()
                                    )
                                )
                            }
                            showSectionDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricLime,
                        contentColor = BgDark
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (editingSectionIndex >= 0) "Update Title" else "Create Section",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSectionDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Dialog for Adding / Editing an Exercise
    if (showExerciseDialog) {
        val currentSec = sectionList.getOrNull(targetSectionIndex)
        val secTitle = currentSec?.title ?: "Workout"

        AlertDialog(
            onDismissRequest = { showExerciseDialog = false },
            containerColor = SurfaceDark,
            title = {
                Column {
                    Text(
                        text = if (editingExerciseIndex >= 0) "Edit Exercise" else "Add Exercise",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Under '$secTitle'",
                        color = ElectricLime,
                        fontSize = 12.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Exercise Name
                    OutlinedTextField(
                        value = dialogExName,
                        onValueChange = { dialogExName = it },
                        label = { Text("Exercise Name *", color = TextSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricLime,
                            unfocusedBorderColor = BorderMuted,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Sets Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sets: $dialogExSets",
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { if (dialogExSets > 1) dialogExSets-- },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated)
                            ) {
                                Text(text = "-", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                            Text(
                                text = "$dialogExSets",
                                color = ElectricLime,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            IconButton(
                                onClick = { if (dialogExSets < 20) dialogExSets++ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated)
                            ) {
                                Text(text = "+", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                    }

                    // Reps Range
                    Column {
                        Text(
                            text = "Reps Range",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Common preset chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("8-10", "10-12", "12-15", "Failure").forEach { preset ->
                                val isSelected = dialogExReps == preset
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ElectricLime else SurfaceElevated)
                                        .clickable { dialogExReps = preset }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = preset,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) BgDark else TextLight
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = dialogExReps,
                            onValueChange = { dialogExReps = it },
                            label = { Text("Custom Reps (e.g. 10-12 or 45s)", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricLime,
                                unfocusedBorderColor = BorderMuted,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = SurfaceCard,
                                unfocusedContainerColor = SurfaceCard
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Optional Notes
                    OutlinedTextField(
                        value = dialogExNotes,
                        onValueChange = { dialogExNotes = it },
                        label = { Text("Exercise Notes (Optional)", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricLime,
                            unfocusedBorderColor = BorderMuted,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val validName = dialogExName.trim()
                        if (validName.isNotBlank() && targetSectionIndex in sectionList.indices) {
                            val sec = sectionList[targetSectionIndex]
                            val exList = sec.exercises.toMutableList()

                            val newEx = Exercise(
                                id = if (editingExerciseIndex >= 0) exList[editingExerciseIndex].id else 0,
                                sectionId = sec.id,
                                dayOfWeek = weekday.dayIndex,
                                name = validName,
                                sets = dialogExSets,
                                reps = dialogExReps.trim().ifBlank { "10-12" },
                                notes = dialogExNotes.trim(),
                                sortOrder = if (editingExerciseIndex >= 0) editingExerciseIndex else exList.size
                            )

                            if (editingExerciseIndex >= 0) {
                                exList[editingExerciseIndex] = newEx
                            } else {
                                exList.add(newEx)
                            }

                            sectionList[targetSectionIndex] = sec.copy(exercises = exList)
                            showExerciseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricLime,
                        contentColor = BgDark
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (editingExerciseIndex >= 0) "Update" else "Add Exercise",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showExerciseDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun EditableSectionCard(
    secIndex: Int,
    totalSections: Int,
    section: WorkoutSection,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEditTitle: () -> Unit,
    onDeleteSection: () -> Unit,
    onAddExercise: () -> Unit,
    onMoveExerciseUp: (Int) -> Unit,
    onMoveExerciseDown: (Int) -> Unit,
    onEditExercise: (Int) -> Unit,
    onDeleteExercise: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderHighlight, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Section Title Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(ElectricLimeSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${secIndex + 1}",
                            color = ElectricLime,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = section.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    IconButton(
                        onClick = onEditTitle,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Section Title",
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Section order controls & delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = secIndex > 0,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Move Section Up",
                            tint = if (secIndex > 0) TextLight else TextMuted.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = secIndex < totalSections - 1,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Move Section Down",
                            tint = if (secIndex < totalSections - 1) TextLight else TextMuted.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteSection,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Section",
                            tint = DangerRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Exercise items under this section
            if (section.exercises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No exercises in this section yet.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            } else {
                section.exercises.forEachIndexed { exIdx, exercise ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderMuted, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "${exercise.sets} Sets | ${exercise.reps} Reps",
                                    fontSize = 12.sp,
                                    color = ElectricLime
                                )
                                if (exercise.notes.isNotBlank()) {
                                    Text(
                                        text = exercise.notes,
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Exercise Controls
                            IconButton(
                                onClick = { onMoveExerciseUp(exIdx) },
                                enabled = exIdx > 0,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Move Up",
                                    tint = if (exIdx > 0) TextLight else TextMuted.copy(alpha = 0.3f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            IconButton(
                                onClick = { onMoveExerciseDown(exIdx) },
                                enabled = exIdx < section.exercises.size - 1,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Move Down",
                                    tint = if (exIdx < section.exercises.size - 1) TextLight else TextMuted.copy(alpha = 0.3f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            IconButton(
                                onClick = { onEditExercise(exIdx) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDeleteExercise(exIdx) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = DangerRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Add Exercise button for this section
            OutlinedButton(
                onClick = onAddExercise,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ElectricLime.copy(alpha = 0.4f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricLime),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Exercise",
                    tint = ElectricLime,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add Exercise to ${section.title}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ElectricLime
                )
            }
        }
    }
}
