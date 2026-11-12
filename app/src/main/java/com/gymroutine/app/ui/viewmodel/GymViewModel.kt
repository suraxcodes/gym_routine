package com.gymroutine.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gymroutine.app.data.database.GymDatabaseHelper
import com.gymroutine.app.data.model.DayScheduleItem
import com.gymroutine.app.data.model.DayWorkoutPlan
import com.gymroutine.app.data.model.DietItem
import com.gymroutine.app.data.model.ExerciseWithStatus
import com.gymroutine.app.data.model.SectionWithStatus
import com.gymroutine.app.data.model.Weekday
import com.gymroutine.app.data.model.WorkoutPlan
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
    val todayDateKey: String = "",
    val selectedDateKey: String = "",
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
    val isLoading: Boolean = true,

    // Workout plans with date periods
    val plans: List<WorkoutPlan> = emptyList(),
    /** Plan that the calendar gives for the selected day (null = no plan covers that date). */
    val activePlan: WorkoutPlan? = null,
    /** Plan that is active on today's real date (used for the "ACTIVE" badge). */
    val todayPlanId: Long? = null,
    /** Set while the user is editing a plan picked from the Plans page (it may be a future plan). */
    val previewPlan: WorkoutPlan? = null,

    // Diet page
    val dietItems: List<DietItem> = emptyList()
) {
    /** The plan whose workouts are on screen right now. */
    val shownPlan: WorkoutPlan?
        get() = previewPlan ?: activePlan
}

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
                todayDateKey = repository.getTodayDateKey(),
                currentFormattedDate = formattedDate,
                greeting = greeting,
                isSundayRestDay = (todayWeekday == Weekday.SUNDAY)
            )
        }

        refreshScheduleItems(todayWeekday, todayWeekday)
        loadDayData(todayWeekday)
    }

    /** Call when the app comes back to the screen: if the date changed (after midnight), start the new day. */
    fun onAppResumed() {
        val state = _uiState.value
        if (state.todayDateKey.isNotEmpty() && state.todayDateKey != repository.getTodayDateKey()) {
            initializeToday()
        }
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
        val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        for (weekday in Weekday.entries) {
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val formattedDate = fullDateFormat.format(calendar.time)
            items.add(
                DayScheduleItem(
                    weekday = weekday,
                    dayOfMonth = dayOfMonth,
                    formattedDate = formattedDate,
                    isToday = (weekday == today),
                    isSelected = (weekday == selected),
                    dateKey = keyFormat.format(calendar.time)
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val selectedKey = items.firstOrNull { it.weekday == selected }?.dateKey
            ?: repository.getTodayDateKey()

        _uiState.update { it.copy(scheduleItems = items, selectedDateKey = selectedKey) }
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
            val before = _uiState.value
            val dateKey = before.scheduleItems.firstOrNull { it.weekday == weekday }?.dateKey
                ?: repository.getTodayDateKey()

            // Lists used by the Plans page and the Diet page
            val plans = repository.getPlans()
            val dietItems = repository.getDietItems()
            val todayPlanId = repository.getActivePlan(repository.getTodayDateKey())?.id

            // Which plan belongs to the selected date?
            val activePlan = repository.getActivePlan(dateKey)
            // Keep the preview plan fresh (it may have been renamed or deleted)
            val previewPlan = before.previewPlan?.let { p -> plans.firstOrNull { it.id == p.id } }
            val shownPlan = previewPlan ?: activePlan

            if (weekday == Weekday.SUNDAY || shownPlan == null) {
                _uiState.update {
                    it.copy(
                        plans = plans,
                        dietItems = dietItems,
                        todayPlanId = todayPlanId,
                        activePlan = activePlan,
                        previewPlan = previewPlan,
                        currentPlan = null,
                        sectionsWithStatus = emptyList(),
                        completedCount = 0,
                        totalCount = 0,
                        completionPercentage = 0f,
                        isSundayRestDay = (weekday == Weekday.SUNDAY),
                        isLoading = false
                    )
                }
                return@launch
            }

            val dayPlan = repository.getDayWorkoutPlan(shownPlan.id, weekday.dayIndex)
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
                    plans = plans,
                    dietItems = dietItems,
                    todayPlanId = todayPlanId,
                    activePlan = activePlan,
                    previewPlan = previewPlan,
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

    // ---- Checkmarks: saved for the date of the day that is selected ----

    private fun selectedDateKey(): String =
        _uiState.value.selectedDateKey.ifEmpty { repository.getTodayDateKey() }

    fun toggleExerciseCompletion(exerciseId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.setExerciseCompletion(selectedDateKey(), exerciseId, isCompleted)
        }
    }

    fun resetTodayCompletions() {
        viewModelScope.launch {
            repository.resetDailyCompletions(selectedDateKey())
        }
    }

    // ---- Editing the workouts of one weekday ----

    fun saveDaySections(sections: List<WorkoutSection>) {
        val state = _uiState.value
        val currentDay = state.selectedWeekday
        val plan = state.shownPlan
        if (currentDay == Weekday.SUNDAY || plan == null) {
            _uiState.update { it.copy(isEditingPlan = false) }
            return
        }

        viewModelScope.launch {
            repository.saveDaySections(plan.id, currentDay.dayIndex, sections)
            _uiState.update { it.copy(isEditingPlan = false) }
        }
    }

    fun resetToDefaults() {
        _uiState.update { it.copy(previewPlan = null) }
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

    // ---- Workout plans (date periods) ----

    /** Create (id == 0) or update a plan. For a new plan, [copyFromPlanId] copies that plan's workouts into it. */
    fun savePlan(plan: WorkoutPlan, copyFromPlanId: Long? = null) {
        viewModelScope.launch {
            repository.savePlan(plan, copyFromPlanId)
        }
    }

    fun deletePlan(plan: WorkoutPlan) {
        if (_uiState.value.previewPlan?.id == plan.id) {
            _uiState.update { it.copy(previewPlan = null) }
        }
        viewModelScope.launch {
            repository.deletePlan(plan.id)
        }
    }

    /** Show this plan on the home screen so its workouts can be edited, even if its dates are in the future. */
    fun startPlanPreview(plan: WorkoutPlan) {
        _uiState.update { it.copy(previewPlan = plan) }
        loadDayData(_uiState.value.selectedWeekday)
    }

    fun stopPlanPreview() {
        _uiState.update { it.copy(previewPlan = null) }
        loadDayData(_uiState.value.selectedWeekday)
    }

    // ---- Diet ----

    fun saveDietItem(item: DietItem) {
        if (item.name.isBlank()) return
        viewModelScope.launch {
            repository.saveDietItem(item)
        }
    }

    fun deleteDietItem(item: DietItem) {
        viewModelScope.launch {
            repository.deleteDietItem(item.id)
        }
    }
}
