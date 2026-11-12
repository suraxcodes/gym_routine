package com.gymroutine.app.data.model

import java.util.Calendar

enum class Weekday(val dayIndex: Int, val shortLabel: String, val fullTitle: String) {
    MONDAY(1, "MON", "Monday"),
    TUESDAY(2, "TUE", "Tuesday"),
    WEDNESDAY(3, "WED", "Wednesday"),
    THURSDAY(4, "THU", "Thursday"),
    FRIDAY(5, "FRI", "Friday"),
    SATURDAY(6, "SAT", "Saturday"),
    SUNDAY(7, "SUN", "Sunday");

    companion object {
        fun fromDayIndex(index: Int): Weekday {
            return entries.firstOrNull { it.dayIndex == index } ?: MONDAY
        }

        fun fromCalendarDay(calendarDay: Int): Weekday {
            return when (calendarDay) {
                Calendar.MONDAY -> MONDAY
                Calendar.TUESDAY -> TUESDAY
                Calendar.WEDNESDAY -> WEDNESDAY
                Calendar.THURSDAY -> THURSDAY
                Calendar.FRIDAY -> FRIDAY
                Calendar.SATURDAY -> SATURDAY
                Calendar.SUNDAY -> SUNDAY
                else -> MONDAY
            }
        }
    }
}

data class DayScheduleItem(
    val weekday: Weekday,
    val dayOfMonth: Int,
    val formattedDate: String,
    val isToday: Boolean,
    val isSelected: Boolean
)

data class Exercise(
    val id: Long = 0,
    val sectionId: Long = 0,
    val dayOfWeek: Int,
    val name: String,
    val sets: Int,
    val reps: String,
    val notes: String = "",
    val sortOrder: Int = 0
)

data class WorkoutSection(
    val id: Long = 0,
    val dayOfWeek: Int,
    val title: String,
    val sortOrder: Int = 0,
    val exercises: List<Exercise> = emptyList()
) {
    val totalSets: Int
        get() = exercises.sumOf { it.sets }

    val exerciseCount: Int
        get() = exercises.size
}

data class DayWorkoutPlan(
    val dayOfWeek: Int,
    val sections: List<WorkoutSection> = emptyList()
) {
    val totalExercises: Int
        get() = sections.sumOf { it.exerciseCount }

    val totalSets: Int
        get() = sections.sumOf { it.totalSets }

    val sectionTitlesSummary: String
        get() = if (sections.isEmpty()) "No Workout Scheduled" else sections.joinToString(" • ") { it.title }
}

data class ExerciseWithStatus(
    val exercise: Exercise,
    val isCompleted: Boolean = false
)

data class SectionWithStatus(
    val section: WorkoutSection,
    val exercisesWithStatus: List<ExerciseWithStatus> = emptyList()
)
