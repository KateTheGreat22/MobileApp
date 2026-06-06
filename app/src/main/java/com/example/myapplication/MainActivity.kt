package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.myapplication.data.PlannerDatabase
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val database by lazy { PlannerDatabase.getDatabase(this) }
    private val viewModel: PlannerViewModel by viewModels {
        PlannerViewModel.provideFactory(database.plannerDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PlannerApp(viewModel)
            }
        }
    }
}
