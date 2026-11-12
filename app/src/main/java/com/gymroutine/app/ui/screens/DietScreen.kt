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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.runtime.getValue
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
import com.gymroutine.app.ui.theme.BgDark
import com.gymroutine.app.ui.theme.BorderHighlight
import com.gymroutine.app.ui.theme.BorderMuted
import com.gymroutine.app.ui.theme.ElectricLime
import com.gymroutine.app.ui.theme.ElectricLimeSubtle
import com.gymroutine.app.ui.theme.SurfaceCard
import com.gymroutine.app.ui.theme.SurfaceElevated
import com.gymroutine.app.ui.theme.TextMuted
import com.gymroutine.app.ui.theme.TextSecondary
import com.gymroutine.app.ui.theme.TextWhite

data class DietItem(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val calories: String,
    val protein: String,
    val recipe: String,
    val mealType: String  // Breakfast, Lunch, Dinner, Snack
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietScreen(onBack: () -> Unit) {

    val dietItems = remember {
        mutableStateListOf(
            DietItem(
                name = "Boiled Eggs",
                calories = "155 kcal",
                protein = "13g",
                mealType = "Breakfast",
                recipe = "1. Boil water in a pot.\n2. Add eggs and cook for 10–12 min.\n3. Cool under cold water, peel and serve.\n4. Season with salt & pepper."
            ),
            DietItem(
                name = "Soya Bean Curry",
                calories = "290 kcal",
                protein = "28g",
                mealType = "Lunch",
                recipe = "1. Soak soya chunks in hot water for 20 min, drain.\n2. Sauté onion, tomato, ginger-garlic paste.\n3. Add turmeric, chili, coriander powder.\n4. Add soya chunks, salt and cook 15 min.\n5. Garnish with coriander leaves."
            ),
            DietItem(
                name = "Grilled Chicken Breast",
                calories = "165 kcal",
                protein = "31g",
                mealType = "Dinner",
                recipe = "1. Marinate chicken with lemon, olive oil, garlic, salt & pepper for 30 min.\n2. Preheat grill on medium-high.\n3. Grill each side for 6–7 min until cooked through.\n4. Rest for 5 min before serving."
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }

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
                onClick = { showAddDialog = true },
                containerColor = ElectricLime,
                contentColor = BgDark,
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

            // Group by meal type
            val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
            mealTypes.forEach { mealType ->
                val items = dietItems.filter { it.mealType == mealType }
                if (items.isNotEmpty()) {
                    item(key = "header_$mealType") {
                        MealTypeHeader(mealType = mealType)
                    }
                    items(items = items, key = { it.id }) { item ->
                        DietItemCard(
                            item = item,
                            onDelete = { dietItems.remove(item) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AddDietItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newItem ->
                dietItems.add(newItem)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun MealTypeHeader(mealType: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(ElectricLime)
        )
        Text(
            text = mealType.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = ElectricLime,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun DietItemCard(item: DietItem, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderHighlight, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            // Top row: name + expand/delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MacroBadge(label = "Cal", value = item.calories)
                        MacroBadge(label = "Protein", value = item.protein)
                    }
                }
                Row {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Recipe",
                            tint = ElectricLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Expandable recipe section
            if (expanded && item.recipe.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Recipe",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricLime,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.recipe,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroBadge(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ElectricLimeSubtle)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$label: $value",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = ElectricLime
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDietItemDialog(onDismiss: () -> Unit, onAdd: (DietItem) -> Unit) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var recipe by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("Breakfast") }

    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ElectricLime,
        unfocusedBorderColor = BorderMuted,
        focusedLabelColor = ElectricLime,
        unfocusedLabelColor = TextMuted,
        cursorColor = ElectricLime,
        focusedTextColor = TextWhite,
        unfocusedTextColor = TextWhite
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCard,
        title = {
            Text("Add Food Item", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Food Name (e.g. Eggs, Soya Bean)") },
                        colors = fieldColors,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    // Meal type selector
                    Text("Meal Type", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        mealTypes.forEach { type ->
                            val selected = mealType == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) ElectricLime else SurfaceElevated)
                                    .border(1.dp, if (selected) ElectricLime else BorderMuted, RoundedCornerShape(8.dp))
                                    .clickable { mealType = type }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selected) BgDark else TextSecondary
                                )
                            }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = calories,
                            onValueChange = { calories = it },
                            label = { Text("Calories") },
                            colors = fieldColors,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = protein,
                            onValueChange = { protein = it },
                            label = { Text("Protein") },
                            colors = fieldColors,
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = recipe,
                        onValueChange = { recipe = it },
                        label = { Text("Recipe / Preparation Steps") },
                        colors = fieldColors,
                        minLines = 3,
                        maxLines = 6,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(
                            DietItem(
                                name = name.trim(),
                                calories = calories.ifBlank { "—" },
                                protein = protein.ifBlank { "—" },
                                recipe = recipe.trim(),
                                mealType = mealType
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = BgDark),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
