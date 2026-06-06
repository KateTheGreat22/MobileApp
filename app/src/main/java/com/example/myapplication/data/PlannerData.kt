package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    val title: String,
    val isCompleted: Boolean,
    val points: Int
)

@Entity(tableName = "graduation_steps")
data class GraduationStep(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isRequired: Boolean,
    val isCompleted: Boolean
)

@Entity(tableName = "post_grad_plans")
data class PostGradPlan(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val targetDate: String?
)

@Entity(tableName = "rewards")
data class Reward(
    @PrimaryKey val id: String,
    val title: String,
    val pointsRequired: Int,
    val isUnlocked: Boolean
)
