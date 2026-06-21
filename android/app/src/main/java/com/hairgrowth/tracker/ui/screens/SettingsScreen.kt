package com.hairgrowth.tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hairgrowth.tracker.ui.viewmodel.GrowthViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: GrowthViewModel = viewModel()) {
    var startDateText by remember { mutableStateOf("") }
    var startLengthText by remember { mutableStateOf("") }
    var goalLengthText by remember { mutableStateOf("") }
    var dailyRateText by remember { mutableStateOf("0.37") }
    var reminderDayText by remember { mutableStateOf("1") }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("設定", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Start Settings
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("📅 スタート設定", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = startDateText,
                        onValueChange = { startDateText = it },
                        label = { Text("開始日 (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(LocalDate.now().toString()) }
                    )
                    OutlinedTextField(
                        value = startLengthText,
                        onValueChange = { startLengthText = it },
                        label = { Text("開始時の長さ") },
                        suffix = { Text("mm") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }

            // Goal Settings
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🎯 目標設定", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = goalLengthText,
                        onValueChange = { goalLengthText = it },
                        label = { Text("目標の長さ") },
                        suffix = { Text("mm") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }

            // Growth Rate
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("📈 成長速度", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = dailyRateText,
                        onValueChange = { dailyRateText = it },
                        label = { Text("1日あたりの成長速度") },
                        suffix = { Text("mm/日") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Text("平均は約0.37mm/日 (11mm/月)。計測ログから自動補正されます。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Reminder
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🔔 リマインダー", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = reminderDayText,
                        onValueChange = { reminderDayText = it },
                        label = { Text("計測リマインダー日 (毎月)") },
                        suffix = { Text("日") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            Button(
                onClick = {
                    val startEpoch = runCatching { LocalDate.parse(startDateText).toEpochDay() }.getOrNull()
                    val startMm = startLengthText.toFloatOrNull()
                    val goalMm = goalLengthText.toFloatOrNull()
                    val daily = dailyRateText.toFloatOrNull()
                    val day = reminderDayText.toIntOrNull()?.coerceIn(1, 28)
                    vm.saveSettings(startEpoch, startMm, goalMm, daily, day)
                    saved = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text("設定を保存", style = MaterialTheme.typography.labelLarge)
            }

            if (saved) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text("✅ 保存しました！",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
