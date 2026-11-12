package com.gymroutine.app

import com.gymroutine.app.data.model.DayWorkoutPlan
import com.gymroutine.app.data.model.Exercise
import com.gymroutine.app.data.model.Weekday
import com.gymroutine.app.data.model.WorkoutSection
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class WorkoutModelTest {

    @Test
    fun testWeekdayMapping() {
        assertEquals(Weekday.MONDAY, Weekday.fromDayIndex(1))
        assertEquals(Weekday.TUESDAY, Weekday.fromDayIndex(2))
        assertEquals(Weekday.WEDNESDAY, Weekday.fromDayIndex(3))
        assertEquals(Weekday.THURSDAY, Weekday.fromDayIndex(4))
        assertEquals(Weekday.FRIDAY, Weekday.fromDayIndex(5))
        assertEquals(Weekday.SATURDAY, Weekday.fromDayIndex(6))
        assertEquals(Weekday.SUNDAY, Weekday.fromDayIndex(7))

        assertEquals(Weekday.MONDAY, Weekday.fromCalendarDay(Calendar.MONDAY))
        assertEquals(Weekday.SUNDAY, Weekday.fromCalendarDay(Calendar.SUNDAY))
        assertEquals(Weekday.THURSDAY, Weekday.fromCalendarDay(Calendar.THURSDAY))
    }

    @Test
    fun testDayWorkoutPlanWithSections() {
        val shoulderExercises = listOf(
            Exercise(id = 1, sectionId = 10, dayOfWeek = 1, name = "Overhead Barbell Press", sets = 3, reps = "8-10"),
            Exercise(id = 2, sectionId = 10, dayOfWeek = 1, name = "Lateral Raises", sets = 3, reps = "12-15")
        )

        val absExercises = listOf(
            Exercise(id = 3, sectionId = 11, dayOfWeek = 1, name = "Hanging Knee Raises", sets = 3, reps = "12-15"),
            Exercise(id = 4, sectionId = 11, dayOfWeek = 1, name = "Plank", sets = 3, reps = "45-60s")
        )

        val section1 = WorkoutSection(
            id = 10,
            dayOfWeek = 1,
            title = "Shoulders",
            sortOrder = 0,
            exercises = shoulderExercises
        )

        val section2 = WorkoutSection(
            id = 11,
            dayOfWeek = 1,
            title = "Abs",
            sortOrder = 1,
            exercises = absExercises
        )

        val dayPlan = DayWorkoutPlan(
            dayOfWeek = 1,
            sections = listOf(section1, section2)
        )

        assertEquals(2, dayPlan.sections.size)
        assertEquals(4, dayPlan.totalExercises)
        assertEquals(12, dayPlan.totalSets) // 3 + 3 + 3 + 3
        assertEquals("Shoulders • Abs", dayPlan.sectionTitlesSummary)
    }
}
