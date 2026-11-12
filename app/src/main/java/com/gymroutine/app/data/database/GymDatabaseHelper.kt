package com.gymroutine.app.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.gymroutine.app.data.model.DayWorkoutPlan
import com.gymroutine.app.data.model.Exercise
import com.gymroutine.app.data.model.WorkoutSection

class GymDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "gym_routine.db"
        const val DATABASE_VERSION = 2

        // Table Workout Sections
        const val TABLE_SECTIONS = "workout_sections"
        const val COL_SEC_ID = "id"
        const val COL_SEC_DAY = "day_of_week"
        const val COL_SEC_TITLE = "title"
        const val COL_SEC_SORT_ORDER = "sort_order"

        // Table Exercises
        const val TABLE_EXERCISES = "exercises"
        const val COL_EX_ID = "id"
        const val COL_EX_SEC_ID = "section_id"
        const val COL_EX_DAY = "day_of_week"
        const val COL_EX_NAME = "name"
        const val COL_EX_SETS = "sets"
        const val COL_EX_REPS = "reps"
        const val COL_EX_NOTES = "notes"
        const val COL_EX_SORT_ORDER = "sort_order"

        // Table Completions
        const val TABLE_COMPLETIONS = "exercise_completions"
        const val COL_COMP_DATE_KEY = "date_key"
        const val COL_COMP_EX_ID = "exercise_id"
        const val COL_COMP_IS_COMPLETED = "is_completed"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createSectionsTable = """
            CREATE TABLE $TABLE_SECTIONS (
                $COL_SEC_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SEC_DAY INTEGER NOT NULL,
                $COL_SEC_TITLE TEXT NOT NULL,
                $COL_SEC_SORT_ORDER INTEGER NOT NULL
            )
        """.trimIndent()

        val createExercisesTable = """
            CREATE TABLE $TABLE_EXERCISES (
                $COL_EX_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_EX_SEC_ID INTEGER NOT NULL,
                $COL_EX_DAY INTEGER NOT NULL,
                $COL_EX_NAME TEXT NOT NULL,
                $COL_EX_SETS INTEGER NOT NULL,
                $COL_EX_REPS TEXT NOT NULL,
                $COL_EX_NOTES TEXT,
                $COL_EX_SORT_ORDER INTEGER NOT NULL
            )
        """.trimIndent()

        val createCompletionsTable = """
            CREATE TABLE $TABLE_COMPLETIONS (
                $COL_COMP_DATE_KEY TEXT NOT NULL,
                $COL_COMP_EX_ID INTEGER NOT NULL,
                $COL_COMP_IS_COMPLETED INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY ($COL_COMP_DATE_KEY, $COL_COMP_EX_ID)
            )
        """.trimIndent()

        db.execSQL(createSectionsTable)
        db.execSQL(createExercisesTable)
        db.execSQL(createCompletionsTable)

        seedSingleExample(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMPLETIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXERCISES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SECTIONS")
        db.execSQL("DROP TABLE IF EXISTS workout_plans")
        onCreate(db)
    }

    fun resetToDefaults() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_COMPLETIONS, null, null)
            db.delete(TABLE_EXERCISES, null, null)
            db.delete(TABLE_SECTIONS, null, null)
            seedSingleExample(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun seedSingleExample(db: SQLiteDatabase) {
        // Seed ONLY ONE clean example on Monday:
        // Section 1: "Shoulders"
        // Section 2: "Abs"
        val mondayDayIndex = 1

        val secShouldersValues = ContentValues().apply {
            put(COL_SEC_DAY, mondayDayIndex)
            put(COL_SEC_TITLE, "Shoulders")
            put(COL_SEC_SORT_ORDER, 0)
        }
        val secShouldersId = db.insert(TABLE_SECTIONS, null, secShouldersValues)

        val shoulderExercises = listOf(
            Triple("Overhead Barbell Press", 3 to "8-10", "Full lockout overhead"),
            Triple("Dumbbell Lateral Raises", 3 to "12-15", "Controlled tempo, slight forward lean")
        )

        shoulderExercises.forEachIndexed { idx, (name, setsReps, notes) ->
            val exValues = ContentValues().apply {
                put(COL_EX_SEC_ID, secShouldersId)
                put(COL_EX_DAY, mondayDayIndex)
                put(COL_EX_NAME, name)
                put(COL_EX_SETS, setsReps.first)
                put(COL_EX_REPS, setsReps.second)
                put(COL_EX_NOTES, notes)
                put(COL_EX_SORT_ORDER, idx)
            }
            db.insert(TABLE_EXERCISES, null, exValues)
        }

        val secAbsValues = ContentValues().apply {
            put(COL_SEC_DAY, mondayDayIndex)
            put(COL_SEC_TITLE, "Abs")
            put(COL_SEC_SORT_ORDER, 1)
        }
        val secAbsId = db.insert(TABLE_SECTIONS, null, secAbsValues)

        val absExercises = listOf(
            Triple("Hanging Knee / Leg Raises", 3 to "12-15", "Control lower abs contraction"),
            Triple("Plank Hold", 3 to "45-60s", "Hold rigid plank position")
        )

        absExercises.forEachIndexed { idx, (name, setsReps, notes) ->
            val exValues = ContentValues().apply {
                put(COL_EX_SEC_ID, secAbsId)
                put(COL_EX_DAY, mondayDayIndex)
                put(COL_EX_NAME, name)
                put(COL_EX_SETS, setsReps.first)
                put(COL_EX_REPS, setsReps.second)
                put(COL_EX_NOTES, notes)
                put(COL_EX_SORT_ORDER, idx)
            }
            db.insert(TABLE_EXERCISES, null, exValues)
        }
    }

    fun getDayWorkoutPlan(dayOfWeek: Int): DayWorkoutPlan {
        val db = readableDatabase
        val sections = mutableListOf<WorkoutSection>()

        val secCursor = db.query(
            TABLE_SECTIONS,
            arrayOf(COL_SEC_ID, COL_SEC_DAY, COL_SEC_TITLE, COL_SEC_SORT_ORDER),
            "$COL_SEC_DAY = ?",
            arrayOf(dayOfWeek.toString()),
            null, null,
            "$COL_SEC_SORT_ORDER ASC, $COL_SEC_ID ASC"
        )

        secCursor.use { sCursor ->
            while (sCursor.moveToNext()) {
                val secId = sCursor.getLong(sCursor.getColumnIndexOrThrow(COL_SEC_ID))
                val secTitle = sCursor.getString(sCursor.getColumnIndexOrThrow(COL_SEC_TITLE))
                val secSort = sCursor.getInt(sCursor.getColumnIndexOrThrow(COL_SEC_SORT_ORDER))

                val exercises = mutableListOf<Exercise>()
                val exCursor = db.query(
                    TABLE_EXERCISES,
                    arrayOf(COL_EX_ID, COL_EX_SEC_ID, COL_EX_DAY, COL_EX_NAME, COL_EX_SETS, COL_EX_REPS, COL_EX_NOTES, COL_EX_SORT_ORDER),
                    "$COL_EX_SEC_ID = ?",
                    arrayOf(secId.toString()),
                    null, null,
                    "$COL_EX_SORT_ORDER ASC, $COL_EX_ID ASC"
                )

                exCursor.use { eCursor ->
                    while (eCursor.moveToNext()) {
                        exercises.add(
                            Exercise(
                                id = eCursor.getLong(eCursor.getColumnIndexOrThrow(COL_EX_ID)),
                                sectionId = secId,
                                dayOfWeek = dayOfWeek,
                                name = eCursor.getString(eCursor.getColumnIndexOrThrow(COL_EX_NAME)),
                                sets = eCursor.getInt(eCursor.getColumnIndexOrThrow(COL_EX_SETS)),
                                reps = eCursor.getString(eCursor.getColumnIndexOrThrow(COL_EX_REPS)),
                                notes = eCursor.getString(eCursor.getColumnIndexOrThrow(COL_EX_NOTES)) ?: "",
                                sortOrder = eCursor.getInt(eCursor.getColumnIndexOrThrow(COL_EX_SORT_ORDER))
                            )
                        )
                    }
                }

                sections.add(
                    WorkoutSection(
                        id = secId,
                        dayOfWeek = dayOfWeek,
                        title = secTitle,
                        sortOrder = secSort,
                        exercises = exercises
                    )
                )
            }
        }

        return DayWorkoutPlan(
            dayOfWeek = dayOfWeek,
            sections = sections
        )
    }

    fun saveDaySections(dayOfWeek: Int, sections: List<WorkoutSection>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Delete old exercises and sections for this day
            db.delete(TABLE_EXERCISES, "$COL_EX_DAY = ?", arrayOf(dayOfWeek.toString()))
            db.delete(TABLE_SECTIONS, "$COL_SEC_DAY = ?", arrayOf(dayOfWeek.toString()))

            sections.forEachIndexed { secIndex, section ->
                val secValues = ContentValues().apply {
                    put(COL_SEC_DAY, dayOfWeek)
                    put(COL_SEC_TITLE, section.title.trim().ifBlank { "Workout Section" })
                    put(COL_SEC_SORT_ORDER, secIndex)
                }
                val newSecId = db.insert(TABLE_SECTIONS, null, secValues)

                section.exercises.forEachIndexed { exIndex, ex ->
                    val exValues = ContentValues().apply {
                        put(COL_EX_SEC_ID, newSecId)
                        put(COL_EX_DAY, dayOfWeek)
                        put(COL_EX_NAME, ex.name.trim())
                        put(COL_EX_SETS, ex.sets)
                        put(COL_EX_REPS, ex.reps.trim().ifBlank { "10-12" })
                        put(COL_EX_NOTES, ex.notes.trim())
                        put(COL_EX_SORT_ORDER, exIndex)
                    }
                    db.insert(TABLE_EXERCISES, null, exValues)
                }
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getCompletedExerciseIds(dateKey: String): Set<Long> {
        val completed = mutableSetOf<Long>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_COMPLETIONS,
            arrayOf(COL_COMP_EX_ID),
            "$COL_COMP_DATE_KEY = ? AND $COL_COMP_IS_COMPLETED = 1",
            arrayOf(dateKey),
            null, null, null
        )

        cursor.use {
            while (it.moveToNext()) {
                completed.add(it.getLong(it.getColumnIndexOrThrow(COL_COMP_EX_ID)))
            }
        }
        return completed
    }

    fun setExerciseCompletion(dateKey: String, exerciseId: Long, isCompleted: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_COMP_DATE_KEY, dateKey)
            put(COL_COMP_EX_ID, exerciseId)
            put(COL_COMP_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        db.insertWithOnConflict(
            TABLE_COMPLETIONS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun resetDailyCompletions(dateKey: String) {
        val db = writableDatabase
        db.delete(TABLE_COMPLETIONS, "$COL_COMP_DATE_KEY = ?", arrayOf(dateKey))
    }
}
