package com.example.healyn.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Medicine::class, Appointment::class, SymptomLog::class],
    version = 3,
    exportSchema = false
)
abstract class HealynDatabase : RoomDatabase() {

    abstract fun healynDao(): HealynDao

    companion object {
        @Volatile
        private var INSTANCE: HealynDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE medicines ADD COLUMN remainingDays INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE medicines ADD COLUMN remainingPills INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS symptom_logs (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "symptom TEXT NOT NULL, " +
                            "timestamp INTEGER NOT NULL)"
                )
            }
        }

        fun getDatabase(context: Context): HealynDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealynDatabase::class.java,
                    "healyn_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
                INSTANCE = instance
                instance
            }
        }
    }
}