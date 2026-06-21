package com.hairgrowth.tracker.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hairgrowth.tracker.ui.viewmodel.GrowthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(vm: GrowthViewModel = viewModel()) {
    val state by vm.dashboardState.collectAsState()
    var inputMm by remember { mutableStateOf("") }
    var showLogDialog by remember { mutableStateOf(false) }

    // Animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = state.progressPercent / 100f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "progress"
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("💇 Hair Growth Tracker", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showLogDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text("📏 計測を記録")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Reminder Banner
            if (state.showReminder) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text(
                        "📅 今月の計測リマインダー！実測値を記録してください。",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Days Counter - Hero Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("スタートからの日数", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        "${state.daysSinceStart}",
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("日", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            // Length Cards Row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Current Length
                ElevatedCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("現在の長さ", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${"%,.1f".format(state.estimatedLengthMm)}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("mm", style = MaterialTheme.typography.labelMedium)
                        Text("= ${"%,.1f".format(state.estimatedLengthCm)} cm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                // Goal
                ElevatedCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("目標まで", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (state.daysToGoal == 0L) {
                            Text("🎉達成!", style = MaterialTheme.typography.headlineMedium
                                .copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.tertiary)
                        } else {
                            Text(
                                "${state.daysToGoal ?: "—"}",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(if (state.daysToGoal != null) "日後" else "未設定",
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Progress
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("進捗", style = MaterialTheme.typography.labelLarge)
                        Text("${"%,.0f".format(state.progressPercent)}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth().height(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${state.estimatedLengthMm.let { "%,.0f".format(it) }}mm",
                            style = MaterialTheme.typography.labelSmall)
                        Text(state.goalLengthMm?.let { "${it.toInt()}mm" } ?: "—",
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Growth Rate Cards
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ElevatedCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("今日の伸び", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+${"%,.2f".format(state.todayGrowthMm)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary)
                        Text("mm/日", style = MaterialTheme.typography.labelSmall)
                    }
                }
                ElevatedCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("月間予測", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("+${"%,.1f".format(state.monthlyGrowthMm)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary)
                        Text("mm/月", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }

    // Log Dialog
    if (showLogDialog) {
        AlertDialog(
            onDismissRequest = { showLogDialog = false },
            title = { Text("📏 計測値を記録") },
            text = {
                OutlinedTextField(
                    value = inputMm,
                    onValueChange = { inputMm = it },
                    label = { Text("実測値 (mm)") },
                    suffix = { Text("mm") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    inputMm.toFloatOrNull()?.let { vm.logMeasurement(it) }
                    showLogDialog = false
                    inputMm = ""
                }) { Text("記録") }
            },
            dismissButton = {
                TextButton(onClick = { showLogDialog = false }) { Text("キャンセル") }
            }
        )
    }
}
