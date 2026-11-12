package com.gymroutine.app.data.repository

import com.gymroutine.app.data.database.GymDatabaseHelper
import com.gymroutine.app.data.model.DayWorkoutPlan
import com.gymroutine.app.data.model.WorkoutSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GymRepository(private val dbHelper: GymDatabaseHelper) {

    private val _dataUpdates = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    val dataUpdates: SharedFlow<Unit> = _dataUpdates.asSharedFlow()

    fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun getDayWorkoutPlan(dayOfWeek: Int): DayWorkoutPlan = withContext(Dispatchers.IO) {
        dbHelper.getDayWorkoutPlan(dayOfWeek)
    }

    suspend fun saveDaySections(dayOfWeek: Int, sections: List<WorkoutSection>) = withContext(Dispatchers.IO) {
        dbHelper.saveDaySections(dayOfWeek, sections)
        _dataUpdates.emit(Unit)
    }

    suspend fun getCompletedExerciseIds(dateKey: String): Set<Long> = withContext(Dispatchers.IO) {
        dbHelper.getCompletedExerciseIds(dateKey)
    }

    suspend fun setExerciseCompletion(dateKey: String, exerciseId: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        dbHelper.setExerciseCompletion(dateKey, exerciseId, isCompleted)
        _dataUpdates.emit(Unit)
    }

    suspend fun resetDailyCompletions(dateKey: String) = withContext(Dispatchers.IO) {
        dbHelper.resetDailyCompletions(dateKey)
        _dataUpdates.emit(Unit)
    }

    suspend fun resetToDefaults() = withContext(Dispatchers.IO) {
        dbHelper.resetToDefaults()
        _dataUpdates.emit(Unit)
    }
}
