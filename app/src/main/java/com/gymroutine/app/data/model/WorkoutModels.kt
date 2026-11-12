package com.gymroutine.app.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    val isSelected: Boolean,
    val dateKey: String = ""   // yyyy-MM-dd of this day in the current week
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
    val sections: List<WorkoutSection> = emptyList(),
    /** Title the user typed for this day's card. Empty = build the title from the section names. */
    val customTitle: String = ""
) {
    /** What the big card at the top shows. */
    val displayTitle: String
        get() = when {
            customTitle.isNotBlank() -> customTitle
            sections.isEmpty() -> "No Workout Scheduled"
            else -> sections.joinToString(" & ") { it.title }
        }

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

/**
 * A workout plan that is valid for a period of dates.
 * Dates are stored as "yyyy-MM-dd". endDate = null means "no end date" (runs until you set one).
 */
data class WorkoutPlan(
    val id: Long = 0,
    val name: String,
    val startDate: String,
    val endDate: String? = null
) {
    fun coversDate(dateKey: String): Boolean =
        startDate <= dateKey && (endDate.isNullOrBlank() || endDate >= dateKey)

    val periodLabel: String
        get() = if (endDate.isNullOrBlank()) {
            "From ${prettyDate(startDate)} • no end date"
        } else {
            "${prettyDate(startDate)} → ${prettyDate(endDate)}"
        }
}

/** One line of the diet page, e.g. name = "Eggs", quantity = "3 daily", recipe = "Boil 10 min". */
data class DietItem(
    val id: Long = 0,
    val name: String,
    val quantity: String = "",
    val recipe: String = ""
)

/** "2026-11-30" -> "30 Nov 2026". Returns the input unchanged if it cannot be parsed. */
fun prettyDate(dateKey: String): String {
    return try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateKey) ?: return dateKey
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(parsed)
    } catch (e: Exception) {
        dateKey
    }
}
