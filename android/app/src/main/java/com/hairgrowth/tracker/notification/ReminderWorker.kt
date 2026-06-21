package com.hairgrowth.tracker.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.hairgrowth.tracker.R
import com.hairgrowth.tracker.data.GrowthRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

const val CHANNEL_ID = "hair_reminder"
const val NOTIFICATION_ID = 1001

class ReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val repo = GrowthRepository(applicationContext)
        val reminderDay = repo.reminderDay.first()
        val today = LocalDate.now()

        if (today.dayOfMonth != reminderDay) return Result.success()

        val logs = repo.allRecords.first()
        val alreadyLoggedToday = logs.any { it.dateEpochDay == today.toEpochDay() }
        if (alreadyLoggedToday) return Result.success()

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💇 髪の計測リマインダー")
            .setContentText("今月の髪の長さを計測してください！")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder().build())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "hair_reminder",
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }
}
