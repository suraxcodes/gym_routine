package com.gymroutine.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymroutine.app.ui.theme.BgDark
import com.gymroutine.app.ui.theme.BorderHighlight
import com.gymroutine.app.ui.theme.BorderMuted
import com.gymroutine.app.ui.theme.DangerRed
import com.gymroutine.app.ui.theme.ElectricLime
import com.gymroutine.app.ui.theme.SurfaceCard
import com.gymroutine.app.ui.theme.SurfaceDark
import com.gymroutine.app.ui.theme.SurfaceElevated
import com.gymroutine.app.ui.theme.TextLight
import com.gymroutine.app.ui.theme.TextMuted
import com.gymroutine.app.ui.theme.TextSecondary
import com.gymroutine.app.ui.theme.TextWhite

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onResetTodayCompletions: () -> Unit,
    onRestoreDefaults: () -> Unit
) {
    var showConfirmRestore by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = ElectricLime,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Settings & Routine",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Local Offline Gym Planner",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderMuted, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "APP INFORMATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricLime,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Gym Routine v1.0",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        Text(
                            text = "All workout routines and completion tracking are stored 100% locally on your device.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Actions Section
                Text(
                    text = "DATA MANAGEMENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricLime,
                    letterSpacing = 0.5.sp
                )

                // Clear today completions button
                OutlinedButton(
                    onClick = {
                        onResetTodayCompletions()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderHighlight)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = TextLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Reset Today's Checkmarks",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Restore default routine button
                OutlinedButton(
                    onClick = { showConfirmRestore = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Restore Default Routine Plans",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = DangerRed
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricLime,
                    contentColor = BgDark
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Close", fontWeight = FontWeight.Bold)
            }
        }
    )

    if (showConfirmRestore) {
        AlertDialog(
            onDismissRequest = { showConfirmRestore = false },
            containerColor = SurfaceDark,
            title = {
                Text(
                    text = "Restore Defaults?",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This deletes ALL your workout plans, their workouts and all checkmarks, and creates one fresh starter plan. Your diet plan is kept. This cannot be undone.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreDefaults()
                        showConfirmRestore = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerRed,
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Reset All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRestore = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

