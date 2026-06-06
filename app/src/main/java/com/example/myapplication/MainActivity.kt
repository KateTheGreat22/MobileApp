package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.myapplication.data.PlannerDatabase
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    // Create a single database instance when it is first needed.
    private val database by lazy { PlannerDatabase.getDatabase(this) }

    // Create and provide the ViewModel with access to the DAO
    // so it can interact with the database.
    private val viewModel: PlannerViewModel by viewModels {
        PlannerViewModel.provideFactory(database.plannerDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allow app content to draw behind system bars
        // for a modern edge-to-edge layout.
        enableEdgeToEdge()

        // Set up the Jetpack Compose user interface.
        setContent {
            MyApplicationTheme {

                // Launch the main planner screen and provide
                // the ViewModel for state management and data access.
                PlannerApp(viewModel)
            }
        }
    }
}
