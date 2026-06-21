package com.hairgrowth.tracker.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.material3.ColorProviders
import androidx.glance.text.*
import com.hairgrowth.tracker.MainActivity
import com.hairgrowth.tracker.data.GrowthRepository
import com.hairgrowth.tracker.ui.theme.LightColors
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class HairGrowthWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = GrowthRepository(context)

        // Fetch data
        val startEpoch = repo.startDateEpoch.first()
        val startMm = repo.startLengthMm.first()
        val goalMm = repo.goalLengthMm.first()
        val dailyRate = repo.dailyRateMm.first()
        val logs = repo.allRecords.first()

        val today = LocalDate.now()
        val daysSince = today.toEpochDay() - startEpoch

        val estimatedMm = if (logs.isNotEmpty()) {
            val latest = logs.maxByOrNull { it.dateEpochDay }!!
            val daysSinceLatest = today.toEpochDay() - latest.dateEpochDay
            latest.measuredLengthMm + daysSinceLatest * dailyRate
        } else {
            startMm + daysSince * dailyRate
        }

        val daysToGoal = goalMm?.let {
            if (estimatedMm >= it) 0L
            else ((it - estimatedMm) / dailyRate).toLong()
        }

        provideContent {
            WidgetContent(
                daysSince = daysSince,
                estimatedMm = estimatedMm,
                daysToGoal = daysToGoal,
                dailyRate = dailyRate
            )
        }
    }
}

@Composable
fun WidgetContent(
    daysSince: Long,
    estimatedMm: Float,
    daysToGoal: Long?,
    dailyRate: Float
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFFEADDFF))
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "💇 Hair Tracker",
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFF6750A4)))
        )
        Spacer(GlanceModifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$daysSince日目",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFF21005D)))
            )
            Spacer(GlanceModifier.width(8.dp))
            Text(
                "${"%,.1f".format(estimatedMm)}mm",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFF6750A4)))
            )
        }
        Text(
            "= ${"%,.1f".format(estimatedMm / 10f)} cm  +${"%,.2f".format(dailyRate)}mm/日",
            style = TextStyle(fontSize = 11.sp, color = ColorProvider(Color(0xFF49454F)))
        )
        if (daysToGoal != null) {
            Text(
                if (daysToGoal == 0L) "🎉 目標達成!" else "目標まで ${daysToGoal}日",
                style = TextStyle(fontSize = 11.sp, color = ColorProvider(Color(0xFF7D5260)))
            )
        }
    }
}
