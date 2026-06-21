package com.hairgrowth.tracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hairgrowth.tracker.data.GrowthRecord
import com.hairgrowth.tracker.data.GrowthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DashboardUiState(
    val daysSinceStart: Long = 0,
    val estimatedLengthMm: Float = 0f,
    val estimatedLengthCm: Float = 0f,
    val todayGrowthMm: Float = 0.37f,
    val monthlyGrowthMm: Float = 11f,
    val goalLengthMm: Float? = null,
    val daysToGoal: Long? = null,
    val progressPercent: Float = 0f,
    val showReminder: Boolean = false
)

class GrowthViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = GrowthRepository(application)

    val records: StateFlow<List<GrowthRecord>> = repo.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardState: StateFlow<DashboardUiState> = combine(
        repo.startDateEpoch,
        repo.startLengthMm,
        repo.goalLengthMm,
        repo.dailyRateMm,
        repo.reminderDay,
        repo.allRecords
    ) { values ->
        val startEpoch = values[0] as Long
        val startMm = values[1] as Float
        val goalMm = values[2] as Float?
        val dailyRate = values[3] as Float
        val reminderDay = values[4] as Int
        @Suppress("UNCHECKED_CAST")
        val logs = values[5] as List<GrowthRecord>

        val today = LocalDate.now()
        val startDate = LocalDate.ofEpochDay(startEpoch)
        val daysSince = today.toEpochDay() - startDate.toEpochDay()

        // Effective rate from logs
        var effectiveRate = dailyRate
        if (logs.size >= 2) {
            val first = logs.first()
            val last = logs.last()
            val daysSpan = last.dateEpochDay - first.dateEpochDay
            if (daysSpan > 0) {
                val computedRate = (last.measuredLengthMm - first.measuredLengthMm) / daysSpan
                if (computedRate > 0) effectiveRate = computedRate
            }
        }

        val estimatedMm = if (logs.isNotEmpty()) {
            val latestLog = logs.last()
            val daysSinceLatest = today.toEpochDay() - latestLog.dateEpochDay
            latestLog.measuredLengthMm + daysSinceLatest * effectiveRate
        } else {
            startMm + daysSince * effectiveRate
        }

        val daysToGoal = goalMm?.let {
            if (estimatedMm >= it) 0L
            else ((it - estimatedMm) / effectiveRate).toLong()
        }

        val progress = if (goalMm != null && goalMm > startMm) {
            ((estimatedMm - startMm) / (goalMm - startMm) * 100f).coerceIn(0f, 100f)
        } else 0f

        val showReminder = today.dayOfMonth == reminderDay &&
            logs.none { it.dateEpochDay == today.toEpochDay() }

        DashboardUiState(
            daysSinceStart = daysSince,
            estimatedLengthMm = estimatedMm,
            estimatedLengthCm = estimatedMm / 10f,
            todayGrowthMm = effectiveRate,
            monthlyGrowthMm = effectiveRate * 30.44f,
            goalLengthMm = goalMm,
            daysToGoal = daysToGoal,
            progressPercent = progress,
            showReminder = showReminder
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun logMeasurement(measuredMm: Float, note: String = "") {
        viewModelScope.launch {
            val today = LocalDate.now()
            repo.insertRecord(
                GrowthRecord(
                    dateEpochDay = today.toEpochDay(),
                    measuredLengthMm = measuredMm,
                    note = note
                )
            )
        }
    }

    fun saveSettings(
        startDateEpoch: Long? = null,
        startLengthMm: Float? = null,
        goalLengthMm: Float? = null,
        dailyRateMm: Float? = null,
        reminderDay: Int? = null,
        screenPpi: Float? = null
    ) {
        viewModelScope.launch {
            repo.saveSettings(startDateEpoch, startLengthMm, goalLengthMm, dailyRateMm, reminderDay, screenPpi)
        }
    }
}
