package com.gymroutine.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gymroutine.app.data.database.GymDatabaseHelper
import com.gymroutine.app.data.model.DayScheduleItem
import com.gymroutine.app.data.model.DayWorkoutPlan
import com.gymroutine.app.data.model.ExerciseWithStatus
import com.gymroutine.app.data.model.SectionWithStatus
import com.gymroutine.app.data.model.Weekday
import com.gymroutine.app.data.model.WorkoutSection
import com.gymroutine.app.data.repository.GymRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class GymUiState(
    val todayWeekday: Weekday = Weekday.MONDAY,
    val selectedWeekday: Weekday = Weekday.MONDAY,
    val currentFormattedDate: String = "",
    val greeting: String = "Good Day",
    val scheduleItems: List<DayScheduleItem> = emptyList(),
    val currentPlan: DayWorkoutPlan? = null,
    val sectionsWithStatus: List<SectionWithStatus> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val completionPercentage: Float = 0f,
    val isSundayRestDay: Boolean = false,
    val isEditingPlan: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val isLoading: Boolean = true
)

class GymViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GymRepository(GymDatabaseHelper(application))

    private val _uiState = MutableStateFlow(GymUiState())
    val uiState: StateFlow<GymUiState> = _uiState.asStateFlow()

    init {
        initializeToday()
        observeDataChanges()
    }

    private fun initializeToday() {
        val calendar = Calendar.getInstance()
        val calendarDay = calendar.get(Calendar.DAY_OF_WEEK)
        val todayWeekday = Weekday.fromCalendarDay(calendarDay)

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Night Routine"
        }

        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        val formattedDate = dateFormat.format(Date())

        _uiState.update {
            it.copy(
                todayWeekday = todayWeekday,
                selectedWeekday = todayWeekday,
                currentFormattedDate = formattedDate,
                greeting = greeting,
                isSundayRestDay = (todayWeekday == Weekday.SUNDAY)
            )
        }

        refreshScheduleItems(todayWeekday, todayWeekday)
        loadDayData(todayWeekday)
    }

    private fun observeDataChanges() {
        viewModelScope.launch {
            repository.dataUpdates.collectLatest {
                loadDayData(_uiState.value.selectedWeekday)
            }
        }
    }

    private fun refreshScheduleItems(today: Weekday, selected: Weekday) {
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = when (currentDayOfWeek) {
            Calendar.SUNDAY -> 6
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            else -> 0
        }
        calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)

        val items = mutableListOf<DayScheduleItem>()
        val fullDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        for (weekday in Weekday.entries) {
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val formattedDate = fullDateFormat.format(calendar.time)
            items.add(
                DayScheduleItem(
                    weekday = weekday,
                    dayOfMonth = dayOfMonth,
                    formattedDate = formattedDate,
                    isToday = (weekday == today),
                    isSelected = (weekday == selected)
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        _uiState.update { it.copy(scheduleItems = items) }
    }

    fun selectWeekday(weekday: Weekday) {
        if (_uiState.value.selectedWeekday == weekday) return

        _uiState.update {
            it.copy(
                selectedWeekday = weekday,
                isSundayRestDay = (weekday == Weekday.SUNDAY)
            )
        }
        refreshScheduleItems(_uiState.value.todayWeekday, weekday)
        loadDayData(weekday)
    }

    private fun loadDayData(weekday: Weekday) {
        viewModelScope.launch {
            if (weekday == Weekday.SUNDAY) {
                _uiState.update {
                    it.copy(
                        currentPlan = null,
                        sectionsWithStatus = emptyList(),
                        completedCount = 0,
                        totalCount = 0,
                        completionPercentage = 0f,
                        isSundayRestDay = true,
                        isLoading = false
                    )
                }
                return@launch
            }

            val dayPlan = repository.getDayWorkoutPlan(weekday.dayIndex)
            val dateKey = repository.getTodayDateKey()
            val completedIds = repository.getCompletedExerciseIds(dateKey)

            val sectionsWithStatus = dayPlan.sections.map { section ->
                val exercisesWithStatus = section.exercises.map { exercise ->
                    ExerciseWithStatus(
                        exercise = exercise,
                        isCompleted = completedIds.contains(exercise.id)
                    )
                }
                SectionWithStatus(
                    section = section,
                    exercisesWithStatus = exercisesWithStatus
                )
            }

            val totalExercises = sectionsWithStatus.sumOf { it.exercisesWithStatus.size }
            val completedExercises = sectionsWithStatus.sumOf { sec ->
                sec.exercisesWithStatus.count { it.isCompleted }
            }
            val percentage = if (totalExercises > 0) completedExercises.toFloat() / totalExercises.toFloat() else 0f

            _uiState.update {
                it.copy(
                    currentPlan = dayPlan,
                    sectionsWithStatus = sectionsWithStatus,
                    completedCount = completedExercises,
                    totalCount = totalExercises,
                    completionPercentage = percentage,
                    isSundayRestDay = false,
                    isLoading = false
                )
            }
        }
    }

    fun toggleExerciseCompletion(exerciseId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            val dateKey = repository.getTodayDateKey()
            repository.setExerciseCompletion(dateKey, exerciseId, isCompleted)
        }
    }

    fun resetTodayCompletions() {
        viewModelScope.launch {
            val dateKey = repository.getTodayDateKey()
            repository.resetDailyCompletions(dateKey)
        }
    }

    fun saveDaySections(sections: List<WorkoutSection>) {
        val currentDay = _uiState.value.selectedWeekday
        if (currentDay == Weekday.SUNDAY) return

        viewModelScope.launch {
            repository.saveDaySections(currentDay.dayIndex, sections)
            _uiState.update { it.copy(isEditingPlan = false) }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetToDefaults()
        }
    }

    fun setEditing(isEditing: Boolean) {
        _uiState.update { it.copy(isEditingPlan = isEditing) }
    }

    fun setSettingsOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = isOpen) }
    }
}
