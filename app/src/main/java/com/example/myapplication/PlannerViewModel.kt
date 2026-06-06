package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.GraduationStep
import com.example.myapplication.data.PlannerDao
import com.example.myapplication.data.PostGradPlan
import com.example.myapplication.data.Reward
import com.example.myapplication.data.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class PlannerViewModel(private val dao: PlannerDao) : ViewModel() {

    // Provides a live list of graduation checklist items from the database.
    val graduationSteps: StateFlow<List<GraduationStep>> = dao.getAllGraduationSteps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Provides a live list of post-graduation plans.
    val postGradPlans: StateFlow<List<PostGradPlan>> = dao.getAllPostGradPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Provides a live list of tasks.
    val tasks: StateFlow<List<Task>> = dao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Provides a live list of available rewards.
    val rewards: StateFlow<List<Reward>> = dao.getAllRewards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculates the user's total points based on completed tasks.
    val points: StateFlow<Int> = tasks.map { taskList ->
        taskList.filter { it.isCompleted }.sumOf { it.points }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Populate the database with starter data if it is empty.
        initializeDataIfEmpty()
    }

    private fun initializeDataIfEmpty() {
        viewModelScope.launch {

            // Check whether graduation steps already exist.
            val steps = dao.getAllGraduationSteps().first()

            if (steps.isEmpty()) {
                withContext(Dispatchers.IO) {

                    // Insert default graduation checklist items.
                    dao.insertGraduationStep(
                        GraduationStep(
                            UUID.randomUUID().toString(),
                            "Complete Credits",
                            "Earn all required credits for your major.",
                            true,
                            false
                        )
                    )

                    dao.insertGraduationStep(
                        GraduationStep(
                            UUID.randomUUID().toString(),
                            "Apply for Graduation",
                            "Submit the graduation application form.",
                            true,
                            false
                        )
                    )

                    dao.insertGraduationStep(
                        GraduationStep(
                            UUID.randomUUID().toString(),
                            "Clear Fees",
                            "Pay any outstanding library fines or tuition fees.",
                            true,
                            false
                        )
                    )

                    // Insert default rewards for earning points.
                    dao.insertReward(
                        Reward(
                            UUID.randomUUID().toString(),
                            "Extra hour of gaming",
                            50,
                            false
                        )
                    )

                    dao.insertReward(
                        Reward(
                            UUID.randomUUID().toString(),
                            "Favorite takeout dinner",
                            100,
                            false
                        )
                    )

                    dao.insertReward(
                        Reward(
                            UUID.randomUUID().toString(),
                            "Weekend trip",
                            500,
                            false
                        )
                    )
                }
            }
        }
    }

    fun toggleStep(id: String) {
        viewModelScope.launch {

            // Find the selected graduation step.
            val step = graduationSteps.value.find { it.id == id }

            step?.let {
                withContext(Dispatchers.IO) {

                    // Toggle the completion status and update the database.
                    dao.updateGraduationStep(
                        it.copy(isCompleted = !it.isCompleted)
                    )
                }
            }
        }
    }

    fun addTask(title: String) {
        viewModelScope.launch {

            // Create a new task worth 10 points by default.
            val newTask = Task(
                UUID.randomUUID().toString(),
                title,
                false,
                10
            )

            withContext(Dispatchers.IO) {
                dao.insertTask(newTask)
            }
        }
    }

    fun toggleTask(id: String) {
        viewModelScope.launch {

            // Find the selected task.
            val task = tasks.value.find { it.id == id }

            task?.let {
                withContext(Dispatchers.IO) {

                    // Toggle task completion and update the database.
                    dao.updateTask(
                        it.copy(isCompleted = !it.isCompleted)
                    )
                }
            }
        }
    }

    fun addPostGradPlan(title: String, description: String) {
        viewModelScope.launch {

            // Create a new post-graduation plan.
            val newPlan = PostGradPlan(
                UUID.randomUUID().toString(),
                title,
                description,
                null
            )

            withContext(Dispatchers.IO) {
                dao.insertPostGradPlan(newPlan)
            }
        }
    }

    fun generateExportText(): String {

        // Build a formatted text summary that can be shared or printed.
        val sb = StringBuilder()

        sb.append("GRADUATION PLANNER SUMMARY\n")
        sb.append("==========================\n\n")

        sb.append("Graduation Steps:\n")

        graduationSteps.value.forEach {
            val status = if (it.isCompleted) "[X]" else "[ ]"
            sb.append("$status ${it.title}: ${it.description}\n")
        }

        sb.append("\nPost-Graduation Plans:\n")

        postGradPlans.value.forEach {
            sb.append("- ${it.title}: ${it.description}\n")
        }

        sb.append("\nTasks Completed:\n")

        tasks.value
            .filter { it.isCompleted }
            .forEach {
                sb.append("- ${it.title}\n")
            }

        sb.append("\nTotal Points Earned: ${points.value}\n")

        return sb.toString()
    }

    companion object {

        // Factory used to create the ViewModel with a DAO dependency.
        fun provideFactory(dao: PlannerDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PlannerViewModel(dao) as T
                }
            }
    }
}
