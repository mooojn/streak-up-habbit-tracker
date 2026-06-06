package com.example.streakup_habbit_tracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits")
    fun getAllHabits(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :habitId LIMIT 1")
    fun getHabitById(habitId: String): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHabit(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHabits(habits: List<HabitEntity>)

    @Update
    fun updateHabit(habit: HabitEntity)

    @Delete
    fun deleteHabit(habit: HabitEntity)
    
    @Query("DELETE FROM habits WHERE id = :habitId")
    fun deleteHabitById(habitId: String)
    
    @Query("DELETE FROM habits")
    fun deleteAll()
}
