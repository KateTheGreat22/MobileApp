package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent

@Composable
fun PlannerApp(viewModel: PlannerViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Graduation") },
                    label = { Text("Graduation") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Post-Grad") },
                    label = { Text("Post-Grad") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Tasks") },
                    label = { Text("Tasks") }
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                0 -> GraduationStepsScreen(viewModel)
                1 -> PostGradScreen(viewModel)
                2 -> TasksScreen(viewModel)
            }
        }
    }
}

@Composable
fun GraduationStepsScreen(viewModel: PlannerViewModel) {
    val steps by viewModel.graduationSteps.collectAsState()
    
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item { 
            Text("Graduation Checklist", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(steps) { step ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = step.isCompleted,
                    onCheckedChange = { viewModel.toggleStep(step.id) }
                )
                Column {
                    Text(step.title, style = MaterialTheme.typography.titleMedium)
                    Text(step.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun PostGradScreen(viewModel: PlannerViewModel) {
    val plans by viewModel.postGradPlans.collectAsState()
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Post-Graduation Planning", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Plan Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { 
                if (title.isNotBlank()) {
                    viewModel.addPostGradPlan(title, desc)
                    title = ""
                    desc = ""
                }
            },
            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
        ) {
            Text("Add Plan")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        LazyColumn {
            items(plans) { plan ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(plan.title, style = MaterialTheme.typography.titleMedium)
                        Text(plan.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun TasksScreen(viewModel: PlannerViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val points by viewModel.points.collectAsState()
    val rewards by viewModel.rewards.collectAsState()
    var newTaskTitle by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Daily Tasks & Rewards", style = MaterialTheme.typography.headlineMedium)
            Text("Points: $points", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    label = { Text("New Task") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        viewModel.addTask(newTaskTitle)
                        newTaskTitle = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }

        item { Text("Tasks", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp)) }
        
        items(tasks) { task ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = task.isCompleted, onCheckedChange = { viewModel.toggleTask(task.id) })
                Text(task.title)
                Spacer(Modifier.weight(1f))
                Text("${task.points} pts", style = MaterialTheme.typography.bodySmall)
            }
        }

        item { Text("Rewards", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 24.dp)) }
        
        items(rewards) { reward ->
            val canAfford = points >= reward.pointsRequired
            ListItem(
                headlineContent = { Text(reward.title) },
                supportingContent = { Text("${reward.pointsRequired} points required") },
                trailingContent = {
                    if (canAfford) {
                        Text("Available!", color = Color(0xFF4CAF50))
                    } else {
                        Text("Locked", color = Color.Gray)
                    }
                }
            )
        }
        
        item {
            val context = LocalContext.current
            Button(
                onClick = {
                    val exportText = viewModel.generateExportText()
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, exportText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Export Planner")
                    context.startActivity(shareIntent)
                },
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
            ) {
                Text("Save & Export for Printing")
            }
        }
    }
}
