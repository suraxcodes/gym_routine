package com.gymroutine.app.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.gymroutine.app.data.model.DayWorkoutPlan
import com.gymroutine.app.data.model.DietItem
import com.gymroutine.app.data.model.Exercise
import com.gymroutine.app.data.model.WorkoutPlan
import com.gymroutine.app.data.model.WorkoutSection
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GymDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "gym_routine.db"
        const val DATABASE_VERSION = 3

        // Table Workout Plans (a plan = a full Mon-Sat routine valid for a date period)
        const val TABLE_PLANS = "workout_plans"
        const val COL_PLAN_ID = "id"
        const val COL_PLAN_NAME = "name"
        const val COL_PLAN_START = "start_date"
        const val COL_PLAN_END = "end_date"

        // Table Workout Sections
        const val TABLE_SECTIONS = "workout_sections"
        const val COL_SEC_ID = "id"
        const val COL_SEC_PLAN_ID = "plan_id"
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

        // Table Diet Items
        const val TABLE_DIET = "diet_items"
        const val COL_DIET_ID = "id"
        const val COL_DIET_NAME = "name"
        const val COL_DIET_QUANTITY = "quantity"
        const val COL_DIET_RECIPE = "recipe"

        const val DEFAULT_PLAN_NAME = "My Workout Plan"
    }

    // ---------------------------------------------------------------------
    // Schema
    // ---------------------------------------------------------------------

    private fun createPlansTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_PLANS (
                $COL_PLAN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PLAN_NAME TEXT NOT NULL,
                $COL_PLAN_START TEXT NOT NULL,
                $COL_PLAN_END TEXT
            )
            """.trimIndent()
        )
    }

    private fun createDietTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_DIET (
                $COL_DIET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DIET_NAME TEXT NOT NULL,
                $COL_DIET_QUANTITY TEXT NOT NULL DEFAULT '',
                $COL_DIET_RECIPE TEXT NOT NULL DEFAULT ''
            )
            """.trimIndent()
        )
    }

    override fun onCreate(db: SQLiteDatabase) {
        createPlansTable(db)

        db.execSQL(
            """
            CREATE TABLE $TABLE_SECTIONS (
                $COL_SEC_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SEC_PLAN_ID INTEGER NOT NULL DEFAULT 0,
                $COL_SEC_DAY INTEGER NOT NULL,
                $COL_SEC_TITLE TEXT NOT NULL,
                $COL_SEC_SORT_ORDER INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
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
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_COMPLETIONS (
                $COL_COMP_DATE_KEY TEXT NOT NULL,
                $COL_COMP_EX_ID INTEGER NOT NULL,
                $COL_COMP_IS_COMPLETED INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY ($COL_COMP_DATE_KEY, $COL_COMP_EX_ID)
            )
            """.trimIndent()
        )

        createDietTable(db)

        val planId = insertDefaultPlan(db)
        seedSingleExample(db, planId)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Very old layout: nothing worth keeping, rebuild everything.
            db.execSQL("DROP TABLE IF EXISTS $TABLE_COMPLETIONS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_EXERCISES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_SECTIONS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_PLANS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_DIET")
            onCreate(db)
            return
        }

        if (oldVersion < 3) {
            // v2 -> v3: KEEP the user's existing workouts.
            // They are moved into one default plan with no end date.
            db.execSQL("DROP TABLE IF EXISTS $TABLE_PLANS")
            createPlansTable(db)
            val planId = insertDefaultPlan(db)
            db.execSQL("ALTER TABLE $TABLE_SECTIONS ADD COLUMN $COL_SEC_PLAN_ID INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE $TABLE_SECTIONS SET $COL_SEC_PLAN_ID = $planId")
            createDietTable(db)
        }
    }

    /** Monday of the current week as yyyy-MM-dd, so the whole visible week is covered. */
    private fun mondayOfThisWeek(): String {
        val cal = Calendar.getInstance()
        val daysBack = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // MON -> 0 ... SUN -> 6
        cal.add(Calendar.DAY_OF_YEAR, -daysBack)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    private fun insertDefaultPlan(db: SQLiteDatabase): Long {
        val values = ContentValues().apply {
            put(COL_PLAN_NAME, DEFAULT_PLAN_NAME)
            put(COL_PLAN_START, mondayOfThisWeek())
            putNull(COL_PLAN_END)
        }
        return db.insert(TABLE_PLANS, null, values)
    }

    fun resetToDefaults() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_COMPLETIONS, null, null)
            db.delete(TABLE_EXERCISES, null, null)
            db.delete(TABLE_SECTIONS, null, null)
            db.delete(TABLE_PLANS, null, null)
            val planId = insertDefaultPlan(db)
            seedSingleExample(db, planId)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun seedSingleExample(db: SQLiteDatabase, planId: Long) {
        // Seed ONLY ONE clean example on Monday: "Shoulders" and "Abs"
        val mondayDayIndex = 1

        val examples = listOf(
            "Shoulders" to listOf(
                Triple("Overhead Barbell Press", 3 to "8-10", "Full lockout overhead"),
                Triple("Dumbbell Lateral Raises", 3 to "12-15", "Controlled tempo, slight forward lean")
            ),
            "Abs" to listOf(
                Triple("Hanging Knee / Leg Raises", 3 to "12-15", "Control lower abs contraction"),
                Triple("Plank Hold", 3 to "45-60s", "Hold rigid plank position")
            )
        )

        examples.forEachIndexed { secIdx, (title, exercises) ->
            val secValues = ContentValues().apply {
                put(COL_SEC_PLAN_ID, planId)
                put(COL_SEC_DAY, mondayDayIndex)
                put(COL_SEC_TITLE, title)
                put(COL_SEC_SORT_ORDER, secIdx)
            }
            val secId = db.insert(TABLE_SECTIONS, null, secValues)

            exercises.forEachIndexed { idx, (name, setsReps, notes) ->
                val exValues = ContentValues().apply {
                    put(COL_EX_SEC_ID, secId)
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
    }

    // ---------------------------------------------------------------------
    // Workout plans (date periods)
    // ---------------------------------------------------------------------

    private fun readPlans(selection: String?, args: Array<String>?, orderBy: String, limit: String?): List<WorkoutPlan> {
        val plans = mutableListOf<WorkoutPlan>()
        val cursor = readableDatabase.query(
            TABLE_PLANS,
            arrayOf(COL_PLAN_ID, COL_PLAN_NAME, COL_PLAN_START, COL_PLAN_END),
            selection, args, null, null, orderBy, limit
        )
        cursor.use {
            while (it.moveToNext()) {
                val endIdx = it.getColumnIndexOrThrow(COL_PLAN_END)
                val end = if (it.isNull(endIdx)) null else it.getString(endIdx)
                plans.add(
                    WorkoutPlan(
                        id = it.getLong(it.getColumnIndexOrThrow(COL_PLAN_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(COL_PLAN_NAME)),
                        startDate = it.getString(it.getColumnIndexOrThrow(COL_PLAN_START)),
                        endDate = end?.takeIf { e -> e.isNotBlank() }
                    )
                )
            }
        }
        return plans
    }

    fun getPlans(): List<WorkoutPlan> =
        readPlans(null, null, "$COL_PLAN_START ASC, $COL_PLAN_ID ASC", null)

    /**
     * The plan to show on [dateKey] (yyyy-MM-dd).
     * A plan counts if it has started and has not ended (no end date = never ends).
     * If two plans overlap, the one that started most recently wins.
     */
    fun getActivePlan(dateKey: String): WorkoutPlan? =
        readPlans(
            "$COL_PLAN_START <= ? AND ($COL_PLAN_END IS NULL OR $COL_PLAN_END = '' OR $COL_PLAN_END >= ?)",
            arrayOf(dateKey, dateKey),
            "$COL_PLAN_START DESC, $COL_PLAN_ID DESC",
            "1"
        ).firstOrNull()

    /** Insert (id == 0) or update a plan. Returns the plan id. */
    fun savePlan(plan: WorkoutPlan): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PLAN_NAME, plan.name.trim().ifBlank { DEFAULT_PLAN_NAME })
            put(COL_PLAN_START, plan.startDate)
            if (plan.endDate.isNullOrBlank()) putNull(COL_PLAN_END) else put(COL_PLAN_END, plan.endDate)
        }
        return if (plan.id > 0) {
            db.update(TABLE_PLANS, values, "$COL_PLAN_ID = ?", arrayOf(plan.id.toString()))
            plan.id
        } else {
            db.insert(TABLE_PLANS, null, values)
        }
    }

    fun deletePlan(planId: Long) {
        val db = writableDatabase
        val arg = arrayOf(planId.toString())
        db.beginTransaction()
        try {
            db.delete(
                TABLE_EXERCISES,
                "$COL_EX_SEC_ID IN (SELECT $COL_SEC_ID FROM $TABLE_SECTIONS WHERE $COL_SEC_PLAN_ID = ?)",
                arg
            )
            db.delete(TABLE_SECTIONS, "$COL_SEC_PLAN_ID = ?", arg)
            db.delete(TABLE_PLANS, "$COL_PLAN_ID = ?", arg)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** Copies every section and exercise (all weekdays) from one plan into another. */
    fun copyPlanContents(fromPlanId: Long, toPlanId: Long) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (day in 1..7) {
                val source = getDayWorkoutPlan(fromPlanId, day)
                source.sections.forEachIndexed { secIndex, section ->
                    val secValues = ContentValues().apply {
                        put(COL_SEC_PLAN_ID, toPlanId)
                        put(COL_SEC_DAY, day)
                        put(COL_SEC_TITLE, section.title)
                        put(COL_SEC_SORT_ORDER, secIndex)
                    }
                    val newSecId = db.insert(TABLE_SECTIONS, null, secValues)
                    section.exercises.forEachIndexed { exIndex, ex ->
                        val exValues = ContentValues().apply {
                            put(COL_EX_SEC_ID, newSecId)
                            put(COL_EX_DAY, day)
                            put(COL_EX_NAME, ex.name)
                            put(COL_EX_SETS, ex.sets)
                            put(COL_EX_REPS, ex.reps)
                            put(COL_EX_NOTES, ex.notes)
                            put(COL_EX_SORT_ORDER, exIndex)
                        }
                        db.insert(TABLE_EXERCISES, null, exValues)
                    }
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // ---------------------------------------------------------------------
    // Sections + exercises of one weekday inside one plan
    // ---------------------------------------------------------------------

    fun getDayWorkoutPlan(planId: Long, dayOfWeek: Int): DayWorkoutPlan {
        val db = readableDatabase
        val sections = mutableListOf<WorkoutSection>()

        val secCursor = db.query(
            TABLE_SECTIONS,
            arrayOf(COL_SEC_ID, COL_SEC_DAY, COL_SEC_TITLE, COL_SEC_SORT_ORDER),
            "$COL_SEC_PLAN_ID = ? AND $COL_SEC_DAY = ?",
            arrayOf(planId.toString(), dayOfWeek.toString()),
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

        return DayWorkoutPlan(dayOfWeek = dayOfWeek, sections = sections)
    }

    fun saveDaySections(planId: Long, dayOfWeek: Int, sections: List<WorkoutSection>) {
        val db = writableDatabase
        val args = arrayOf(planId.toString(), dayOfWeek.toString())
        db.beginTransaction()
        try {
            // Delete old exercises and sections for this day of THIS plan only
            db.delete(
                TABLE_EXERCISES,
                "$COL_EX_SEC_ID IN (SELECT $COL_SEC_ID FROM $TABLE_SECTIONS WHERE $COL_SEC_PLAN_ID = ? AND $COL_SEC_DAY = ?)",
                args
            )
            db.delete(TABLE_SECTIONS, "$COL_SEC_PLAN_ID = ? AND $COL_SEC_DAY = ?", args)

            sections.forEachIndexed { secIndex, section ->
                val secValues = ContentValues().apply {
                    put(COL_SEC_PLAN_ID, planId)
                    put(COL_SEC_DAY, dayOfWeek)
                    put(COL_SEC_TITLE, section.title.trim().ifBlank { "Workout Section" })
                    put(COL_SEC_SORT_ORDER, secIndex)
                }
                val newSecId = db.insert(TABLE_SECTIONS, null, secValues)

                section.exercises.forEachIndexed { exIndex, ex ->
                    val exValues = ContentValues().apply {
                        // Keep the old id of an existing exercise so its checkmarks survive an edit
                        if (ex.id > 0) put(COL_EX_ID, ex.id)
                        put(COL_EX_SEC_ID, newSecId)
                        put(COL_EX_DAY, dayOfWeek)
                        put(COL_EX_NAME, ex.name.trim())
                        put(COL_EX_SETS, ex.sets)
                        put(COL_EX_REPS, ex.reps.trim().ifBlank { "10-12" })
                        put(COL_EX_NOTES, ex.notes.trim())
                        put(COL_EX_SORT_ORDER, exIndex)
                    }
                    db.insertWithOnConflict(TABLE_EXERCISES, null, exValues, SQLiteDatabase.CONFLICT_REPLACE)
                }
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // ---------------------------------------------------------------------
    // Completions (checkmarks per date)
    // ---------------------------------------------------------------------

    fun getCompletedExerciseIds(dateKey: String): Set<Long> {
        val completed = mutableSetOf<Long>()
        val cursor = readableDatabase.query(
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
        val values = ContentValues().apply {
            put(COL_COMP_DATE_KEY, dateKey)
            put(COL_COMP_EX_ID, exerciseId)
            put(COL_COMP_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        writableDatabase.insertWithOnConflict(TABLE_COMPLETIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun resetDailyCompletions(dateKey: String) {
        writableDatabase.delete(TABLE_COMPLETIONS, "$COL_COMP_DATE_KEY = ?", arrayOf(dateKey))
    }

    // ---------------------------------------------------------------------
    // Diet items
    // ---------------------------------------------------------------------

    fun getDietItems(): List<DietItem> {
        val items = mutableListOf<DietItem>()
        val cursor = readableDatabase.query(
            TABLE_DIET,
            arrayOf(COL_DIET_ID, COL_DIET_NAME, COL_DIET_QUANTITY, COL_DIET_RECIPE),
            null, null, null, null,
            "$COL_DIET_ID ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                items.add(
                    DietItem(
                        id = it.getLong(it.getColumnIndexOrThrow(COL_DIET_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(COL_DIET_NAME)),
                        quantity = it.getString(it.getColumnIndexOrThrow(COL_DIET_QUANTITY)) ?: "",
                        recipe = it.getString(it.getColumnIndexOrThrow(COL_DIET_RECIPE)) ?: ""
                    )
                )
            }
        }
        return items
    }

    /** Insert (id == 0) or update a diet item. */
    fun saveDietItem(item: DietItem) {
        val values = ContentValues().apply {
            put(COL_DIET_NAME, item.name.trim())
            put(COL_DIET_QUANTITY, item.quantity.trim())
            put(COL_DIET_RECIPE, item.recipe.trim())
        }
        val db = writableDatabase
        if (item.id > 0) {
            db.update(TABLE_DIET, values, "$COL_DIET_ID = ?", arrayOf(item.id.toString()))
        } else {
            db.insert(TABLE_DIET, null, values)
        }
    }

    fun deleteDietItem(itemId: Long) {
        writableDatabase.delete(TABLE_DIET, "$COL_DIET_ID = ?", arrayOf(itemId.toString()))
    }
}
