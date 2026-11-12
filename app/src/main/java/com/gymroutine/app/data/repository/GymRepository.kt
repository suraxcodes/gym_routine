package com.gymroutine.app.data.repository

import com.gymroutine.app.data.database.GymDatabaseHelper
import com.gymroutine.app.data.model.DayWorkoutPlan
import com.gymroutine.app.data.model.DietItem
import com.gymroutine.app.data.model.WorkoutPlan
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
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    // ---- Workout plans (date periods) ----

    suspend fun getPlans(): List<WorkoutPlan> = withContext(Dispatchers.IO) {
        dbHelper.getPlans()
    }

    suspend fun getActivePlan(dateKey: String): WorkoutPlan? = withContext(Dispatchers.IO) {
        dbHelper.getActivePlan(dateKey)
    }

    suspend fun savePlan(plan: WorkoutPlan, copyFromPlanId: Long? = null) = withContext(Dispatchers.IO) {
        val isNew = plan.id <= 0
        val planId = dbHelper.savePlan(plan)
        if (isNew && copyFromPlanId != null && planId > 0) {
            dbHelper.copyPlanContents(copyFromPlanId, planId)
        }
        _dataUpdates.emit(Unit)
    }

    suspend fun deletePlan(planId: Long) = withContext(Dispatchers.IO) {
        dbHelper.deletePlan(planId)
        _dataUpdates.emit(Unit)
    }

    // ---- Sections + exercises ----

    suspend fun getDayWorkoutPlan(planId: Long, dayOfWeek: Int): DayWorkoutPlan = withContext(Dispatchers.IO) {
        dbHelper.getDayWorkoutPlan(planId, dayOfWeek)
    }

    suspend fun saveDaySections(planId: Long, dayOfWeek: Int, sections: List<WorkoutSection>) = withContext(Dispatchers.IO) {
        dbHelper.saveDaySections(planId, dayOfWeek, sections)
        _dataUpdates.emit(Unit)
    }

    suspend fun setDayTitle(planId: Long, dayOfWeek: Int, title: String) = withContext(Dispatchers.IO) {
        dbHelper.setDayTitle(planId, dayOfWeek, title)
        _dataUpdates.emit(Unit)
    }

    // ---- Completions ----

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

    // ---- Diet ----

    suspend fun getDietItems(): List<DietItem> = withContext(Dispatchers.IO) {
        dbHelper.getDietItems()
    }

    suspend fun saveDietItem(item: DietItem) = withContext(Dispatchers.IO) {
        dbHelper.saveDietItem(item)
        _dataUpdates.emit(Unit)
    }

    suspend fun deleteDietItem(itemId: Long) = withContext(Dispatchers.IO) {
        dbHelper.deleteDietItem(itemId)
        _dataUpdates.emit(Unit)
    }
}
