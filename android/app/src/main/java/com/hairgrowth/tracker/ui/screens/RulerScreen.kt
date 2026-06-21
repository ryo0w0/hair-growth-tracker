package com.hairgrowth.tracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hairgrowth.tracker.ui.viewmodel.GrowthViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulerScreen(vm: GrowthViewModel = viewModel()) {
    val dashState by vm.dashboardState.collectAsState()
    val density = LocalDensity.current
    // Assume ~160dpi default, can be overridden
    var ppiInput by remember { mutableStateOf("160") }
    val ppi = ppiInput.toFloatOrNull() ?: 160f
    val pxPerMm = ppi / 25.4f

    val lengthMm = dashState.estimatedLengthMm
    val rulerMm = (lengthMm + 20f).coerceAtLeast(50f)

    Scaffold(
        topBar = { TopAppBar(title = { Text("📐 画面定規", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PPI設定", style = MaterialTheme.typography.labelLarge)
                    OutlinedTextField(
                        value = ppiInput,
                        onValueChange = { ppiInput = it },
                        label = { Text("画面PPI") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("現在の推定長さ: ${"%,.1f".format(lengthMm)} mm (${"%,.1f".format(lengthMm/10)} cm)",
                        style = MaterialTheme.typography.bodySmall)
                }
            }

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("実寸定規", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        val canvasWidthPx = (rulerMm * pxPerMm).roundToInt()
                        val canvasHeightDp = 90.dp

                        Canvas(
                            modifier = Modifier
                                .width(with(density) { canvasWidthPx.toDp() })
                                .height(canvasHeightDp)
                        ) {
                            val h = size.height
                            // Background
                            drawRect(Color(0xFFFFF9C4))

                            // Ticks
                            for (mm in 0..rulerMm.toInt()) {
                                val x = mm * pxPerMm
                                val isCm = mm % 10 == 0
                                val is5 = mm % 5 == 0
                                val tickH = when {
                                    isCm -> h * 0.5f
                                    is5 -> h * 0.35f
                                    else -> h * 0.2f
                                }
                                drawLine(
                                    color = if (isCm) Color(0xFF5D4037) else Color(0xFF8D6E63),
                                    start = Offset(x, h),
                                    end = Offset(x, h - tickH),
                                    strokeWidth = if (isCm) 2f else 1f
                                )
                                if (isCm) {
                                    drawContext.canvas.nativeCanvas.drawText(
                                        "${mm / 10}",
                                        x,
                                        h - tickH - 8f,
                                        android.graphics.Paint().apply {
                                            textSize = 28f
                                            color = android.graphics.Color.rgb(62, 39, 35)
                                            textAlign = android.graphics.Paint.Align.CENTER
                                            isFakeBoldText = true
                                        }
                                    )
                                }
                            }

                            // Hair length marker
                            val markerX = lengthMm * pxPerMm
                            drawLine(
                                color = Color(0xFFE53935),
                                start = Offset(markerX, 0f),
                                end = Offset(markerX, h),
                                strokeWidth = 3f
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "${"%,.1f".format(lengthMm)}mm",
                                markerX + 8f,
                                32f,
                                android.graphics.Paint().apply {
                                    textSize = 26f
                                    color = android.graphics.Color.rgb(229, 57, 53)
                                    isFakeBoldText = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
