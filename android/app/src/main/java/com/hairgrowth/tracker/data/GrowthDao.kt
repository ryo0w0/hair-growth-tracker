package com.hairgrowth.tracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GrowthDao {
    @Query("SELECT * FROM growth_records ORDER BY dateEpochDay ASC")
    fun getAllRecords(): Flow<List<GrowthRecord>>

    @Query("SELECT * FROM growth_records ORDER BY dateEpochDay DESC LIMIT 1")
    suspend fun getLatestRecord(): GrowthRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GrowthRecord)

    @Delete
    suspend fun deleteRecord(record: GrowthRecord)

    @Query("DELETE FROM growth_records WHERE dateEpochDay = :epochDay")
    suspend fun deleteByDate(epochDay: Long)
}
