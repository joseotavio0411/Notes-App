package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ProductivityDao
import com.example.data.local.entity.DeadlineNoteEntity
import com.example.data.local.entity.DeadlineTaskEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity

@Database(
    entities = [
        NoteEntity::class,
        TaskEntity::class,
        DeadlineTaskEntity::class,
        DeadlineNoteEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productivityDao(): ProductivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN fontSize INTEGER NOT NULL DEFAULT 16")
                db.execSQL("ALTER TABLE notes ADD COLUMN fontFamily TEXT NOT NULL DEFAULT 'DEFAULT'")
                db.execSQL("ALTER TABLE deadline_notes ADD COLUMN fontSize INTEGER NOT NULL DEFAULT 16")
                db.execSQL("ALTER TABLE deadline_notes ADD COLUMN fontFamily TEXT NOT NULL DEFAULT 'DEFAULT'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "productivity_database"
                )
                .addMigrations(MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
