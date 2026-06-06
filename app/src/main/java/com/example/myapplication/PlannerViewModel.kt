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
    val graduationSteps: StateFlow<List<GraduationStep>> = dao.getAllGraduationSteps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val postGradPlans: StateFlow<List<PostGradPlan>> = dao.getAllPostGradPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<Task>> = dao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rewards: StateFlow<List<Reward>> = dao.getAllRewards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val points: StateFlow<Int> = tasks.map { taskList ->
        taskList.filter { it.isCompleted }.sumOf { it.points }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        initializeDataIfEmpty()
    }

    private fun initializeDataIfEmpty() {
        viewModelScope.launch {
            val steps = dao.getAllGraduationSteps().first()
            if (steps.isEmpty()) {
                withContext(Dispatchers.IO) {
                    dao.insertGraduationStep(GraduationStep(UUID.randomUUID().toString(), "Complete Credits", "Earn all required credits for your major.", true, false))
                    dao.insertGraduationStep(GraduationStep(UUID.randomUUID().toString(), "Apply for Graduation", "Submit the graduation application form.", true, false))
                    dao.insertGraduationStep(GraduationStep(UUID.randomUUID().toString(), "Clear Fees", "Pay any outstanding library fines or tuition fees.", true, false))
                    
                    dao.insertReward(Reward(UUID.randomUUID().toString(), "Extra hour of gaming", 50, false))
                    dao.insertReward(Reward(UUID.randomUUID().toString(), "Favorite takeout dinner", 100, false))
                    dao.insertReward(Reward(UUID.randomUUID().toString(), "Weekend trip", 500, false))
                }
            }
        }
    }

    fun toggleStep(id: String) {
        viewModelScope.launch {
            val step = graduationSteps.value.find { it.id == id }
            step?.let {
                withContext(Dispatchers.IO) {
                    dao.updateGraduationStep(it.copy(isCompleted = !it.isCompleted))
                }
            }
        }
    }

    fun addTask(title: String) {
        viewModelScope.launch {
            val newTask = Task(UUID.randomUUID().toString(), title, false, 10)
            withContext(Dispatchers.IO) {
                dao.insertTask(newTask)
            }
        }
    }

    fun toggleTask(id: String) {
        viewModelScope.launch {
            val task = tasks.value.find { it.id == id }
            task?.let {
                withContext(Dispatchers.IO) {
                    dao.updateTask(it.copy(isCompleted = !it.isCompleted))
                }
            }
        }
    }

    fun addPostGradPlan(title: String, description: String) {
        viewModelScope.launch {
            val newPlan = PostGradPlan(UUID.randomUUID().toString(), title, description, null)
            withContext(Dispatchers.IO) {
                dao.insertPostGradPlan(newPlan)
            }
        }
    }
    
    fun generateExportText(): String {
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
        tasks.value.filter { it.isCompleted }.forEach { 
            sb.append("- ${it.title}\n")
        }
        
        sb.append("\nTotal Points Earned: ${points.value}\n")
        
        return sb.toString()
    }

    companion object {
        fun provideFactory(dao: PlannerDao): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlannerViewModel(dao) as T
            }
        }
    }
}
