package com.gymroutine.app.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymroutine.app.data.model.DietItem
import com.gymroutine.app.ui.theme.BgDark
import com.gymroutine.app.ui.theme.BorderHighlight
import com.gymroutine.app.ui.theme.BorderMuted
import com.gymroutine.app.ui.theme.DangerRed
import com.gymroutine.app.ui.theme.ElectricLime
import com.gymroutine.app.ui.theme.SurfaceCard
import com.gymroutine.app.ui.theme.SurfaceDark
import com.gymroutine.app.ui.theme.TextLight
import com.gymroutine.app.ui.theme.TextMuted
import com.gymroutine.app.ui.theme.TextSecondary
import com.gymroutine.app.ui.theme.TextWhite
import com.gymroutine.app.ui.viewmodel.GymViewModel

/**
 * Simple diet page: a list of foods, each with a quantity and an optional recipe.
 * Everything is saved in the local database, so it stays after closing the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietScreen(
    viewModel: GymViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dietItems = uiState.dietItems

    var showDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<DietItem?>(null) }
    var deletingItem by remember { mutableStateOf<DietItem?>(null) }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Diet Plan",
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
                    editingItem = null
                    showDialog = true
                },
                containerColor = ElectricLime,
                contentColor = BgDark,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add Food") },
                text = { Text("Add Food", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
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
            item { Spacer(modifier = Modifier.height(4.dp)) }

            if (dietItems.isEmpty()) {
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "No diet added yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                            Text(
                                text = "Tap \"Add Food\" and write what you eat.\nExample: Eggs — 3 daily\nPaneer — 100 gm, with recipe",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(items = dietItems, key = { it.id }) { item ->
                    DietItemCard(
                        item = item,
                        onEdit = {
                            editingItem = item
                            showDialog = true
                        },
                        onDelete = { deletingItem = item }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(88.dp)) }
        }
    }

    if (showDialog) {
        DietItemDialog(
            initial = editingItem,
            onDismiss = { showDialog = false },
            onSave = { item ->
                viewModel.saveDietItem(item)
                showDialog = false
            }
        )
    }

    deletingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            containerColor = SurfaceDark,
            title = { Text("Delete food?", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = { Text("Remove \"${item.name}\" from your diet plan?", color = TextSecondary, fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDietItem(item)
                        deletingItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = TextWhite),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingItem = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun DietItemCard(
    item: DietItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderMuted, RoundedCornerShape(14.dp))
            .clickable(onClick = onEdit)
            .padding(start = 14.dp, top = 8.dp, end = 4.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                if (item.quantity.isNotBlank()) {
                    Text(
                        text = item.quantity,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricLime
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = DangerRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (item.recipe.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "RECIPE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.recipe,
                fontSize = 14.sp,
                color = TextLight,
                lineHeight = 20.sp,
                modifier = Modifier.padding(end = 10.dp)
            )
        }
    }
}

@Composable
private fun DietItemDialog(
    initial: DietItem?,
    onDismiss: () -> Unit,
    onSave: (DietItem) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var quantity by remember { mutableStateOf(initial?.quantity ?: "") }
    var recipe by remember { mutableStateOf(initial?.recipe ?: "") }

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
                text = if (initial == null) "Add Food" else "Edit Food",
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
                    label = { Text("Food (e.g. Eggs, Paneer)") },
                    singleLine = true,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("How much (e.g. 3 daily, 100 gm)") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = recipe,
                    onValueChange = { recipe = it },
                    label = { Text("Recipe (optional)") },
                    minLines = 3,
                    maxLines = 8,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        DietItem(
                            id = initial?.id ?: 0,
                            name = name.trim(),
                            quantity = quantity.trim(),
                            recipe = recipe.trim()
                        )
                    )
                },
                enabled = name.isNotBlank(),
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
