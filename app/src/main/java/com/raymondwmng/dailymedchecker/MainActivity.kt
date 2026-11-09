package com.raymondwmng.dailymedchecker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raymondwmng.dailymedchecker.ui.MainScreen
import com.raymondwmng.dailymedchecker.ui.MedViewModel
import com.raymondwmng.dailymedchecker.ui.theme.DailyMedCheckerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyMedCheckerTheme {
                val medViewModel: MedViewModel = viewModel()
                MainScreen(viewModel = medViewModel)
            }
        }
    }
}

