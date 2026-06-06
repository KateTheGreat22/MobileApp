package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannerDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: Task)

    @Update
    fun updateTask(task: Task)

    @Query("SELECT * FROM graduation_steps")
    fun getAllGraduationSteps(): Flow<List<GraduationStep>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGraduationStep(step: GraduationStep)

    @Update
    fun updateGraduationStep(step: GraduationStep)

    @Query("SELECT * FROM post_grad_plans")
    fun getAllPostGradPlans(): Flow<List<PostGradPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPostGradPlan(plan: PostGradPlan)

    @Query("SELECT * FROM rewards")
    fun getAllRewards(): Flow<List<Reward>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReward(reward: Reward)
}
