package com.hairgrowth.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hairgrowth.tracker.ui.theme.HairGrowthTheme
import com.hairgrowth.tracker.ui.navigation.AppNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HairGrowthTheme {
                AppNavGraph()
            }
        }
    }
}
