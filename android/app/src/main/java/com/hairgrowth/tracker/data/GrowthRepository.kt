package com.hairgrowth.tracker.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

val Context.dataStore by preferencesDataStore(name = "hair_settings")

object PrefsKeys {
    val START_DATE_EPOCH = longPreferencesKey("start_date_epoch")
    val START_LENGTH_MM = floatPreferencesKey("start_length_mm")
    val GOAL_LENGTH_MM = floatPreferencesKey("goal_length_mm")
    val DAILY_RATE_MM = floatPreferencesKey("daily_rate_mm")
    val REMINDER_DAY = intPreferencesKey("reminder_day")
    val SCREEN_PPI = floatPreferencesKey("screen_ppi")
}

class GrowthRepository(private val context: Context) {
    private val dao = AppDatabase.getInstance(context).growthDao()

    val allRecords: Flow<List<GrowthRecord>> = dao.getAllRecords()

    suspend fun insertRecord(record: GrowthRecord) = dao.insertRecord(record)
    suspend fun deleteRecord(record: GrowthRecord) = dao.deleteRecord(record)
    suspend fun getLatestRecord() = dao.getLatestRecord()

    // Settings via DataStore
    val startDateEpoch: Flow<Long> = context.dataStore.data.map {
        it[PrefsKeys.START_DATE_EPOCH] ?: LocalDate.now().toEpochDay()
    }
    val startLengthMm: Flow<Float> = context.dataStore.data.map { it[PrefsKeys.START_LENGTH_MM] ?: 0f }
    val goalLengthMm: Flow<Float?> = context.dataStore.data.map { it[PrefsKeys.GOAL_LENGTH_MM] }
    val dailyRateMm: Flow<Float> = context.dataStore.data.map { it[PrefsKeys.DAILY_RATE_MM] ?: 0.37f }
    val reminderDay: Flow<Int> = context.dataStore.data.map { it[PrefsKeys.REMINDER_DAY] ?: 1 }
    val screenPpi: Flow<Float> = context.dataStore.data.map { it[PrefsKeys.SCREEN_PPI] ?: 160f }

    suspend fun saveSettings(
        startDateEpoch: Long? = null,
        startLengthMm: Float? = null,
        goalLengthMm: Float? = null,
        dailyRateMm: Float? = null,
        reminderDay: Int? = null,
        screenPpi: Float? = null
    ) {
        context.dataStore.edit { prefs ->
            startDateEpoch?.let { prefs[PrefsKeys.START_DATE_EPOCH] = it }
            startLengthMm?.let { prefs[PrefsKeys.START_LENGTH_MM] = it }
            goalLengthMm?.let { prefs[PrefsKeys.GOAL_LENGTH_MM] = it }
            dailyRateMm?.let { prefs[PrefsKeys.DAILY_RATE_MM] = it }
            reminderDay?.let { prefs[PrefsKeys.REMINDER_DAY] = it }
            screenPpi?.let { prefs[PrefsKeys.SCREEN_PPI] = it }
        }
    }
}
