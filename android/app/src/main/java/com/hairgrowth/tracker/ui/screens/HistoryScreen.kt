package com.hairgrowth.tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hairgrowth.tracker.data.GrowthRecord
import com.hairgrowth.tracker.ui.viewmodel.GrowthViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(vm: GrowthViewModel = viewModel()) {
    val records by vm.records.collectAsState()
    val sortedDesc = records.sortedByDescending { it.dateEpochDay }
    val fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("計測履歴", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        if (sortedDesc.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("記録がありません\n💇 計測を始めましょう!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("📊 計測ログ (${sortedDesc.size}件)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp))
                }
                items(sortedDesc) { record ->
                    HistoryItem(record, fmt)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(record: GrowthRecord, fmt: DateTimeFormatter) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    LocalDate.ofEpochDay(record.dateEpochDay).format(fmt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (record.note.isNotEmpty()) {
                    Text(record.note, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${"%,.1f".format(record.measuredLengthMm)} mm",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "= ${"%,.1f".format(record.measuredLengthMm / 10f)} cm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
