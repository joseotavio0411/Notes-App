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

        private fun migrateToV5(db: SupportSQLiteDatabase) {
            // 1. Ensure all tables exist with base structures
            db.execSQL("CREATE TABLE IF NOT EXISTS `notes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `colorHex` TEXT NOT NULL DEFAULT '#FFFFFF', `isPinned` INTEGER NOT NULL DEFAULT 0, `isLocked` INTEGER NOT NULL DEFAULT 0, `lockPin` TEXT, `imageUri` TEXT, `audioPath` TEXT, `fontSize` INTEGER NOT NULL DEFAULT 16, `fontFamily` TEXT NOT NULL DEFAULT 'DEFAULT', `createdAt` INTEGER NOT NULL DEFAULT 0, `updatedAt` INTEGER NOT NULL DEFAULT 0)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `tasks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL DEFAULT 0, `isPinned` INTEGER NOT NULL DEFAULT 0, `isLocked` INTEGER NOT NULL DEFAULT 0, `lockPin` TEXT, `recurrence` TEXT NOT NULL DEFAULT 'NONE', `visibleFrom` INTEGER NOT NULL DEFAULT 0, `createdAt` INTEGER NOT NULL DEFAULT 0)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `deadline_tasks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `text` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL DEFAULT 0, `deadlineTimestamp` INTEGER NOT NULL DEFAULT 0, `isPinned` INTEGER NOT NULL DEFAULT 0, `isLocked` INTEGER NOT NULL DEFAULT 0, `lockPin` TEXT, `recurrence` TEXT NOT NULL DEFAULT 'NONE', `visibleFrom` INTEGER NOT NULL DEFAULT 0, `createdAt` INTEGER NOT NULL DEFAULT 0)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `deadline_notes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `deadlineTimestamp` INTEGER NOT NULL DEFAULT 0, `colorHex` TEXT NOT NULL DEFAULT '#FFFFFF', `isPinned` INTEGER NOT NULL DEFAULT 0, `isLocked` INTEGER NOT NULL DEFAULT 0, `lockPin` TEXT, `recurrence` TEXT NOT NULL DEFAULT 'NONE', `visibleFrom` INTEGER NOT NULL DEFAULT 0, `imageUri` TEXT, `audioPath` TEXT, `fontSize` INTEGER NOT NULL DEFAULT 16, `fontFamily` TEXT NOT NULL DEFAULT 'DEFAULT', `createdAt` INTEGER NOT NULL DEFAULT 0)")

            // 2. Helper to check existing columns
            fun getExistingColumns(tableName: String): Set<String> {
                val columns = mutableSetOf<String>()
                val cursor = db.query("PRAGMA table_info(`$tableName`)")
                try {
                    val nameIndex = cursor.getColumnIndex("name")
                    while (cursor.moveToNext()) {
                        if (nameIndex != -1) {
                            columns.add(cursor.getString(nameIndex))
                        }
                    }
                } finally {
                    cursor.close()
                }
                return columns
            }

            // 3. Migrate `notes` columns safely
            val notesCols = getExistingColumns("notes")
            if (!notesCols.contains("colorHex")) db.execSQL("ALTER TABLE `notes` ADD COLUMN `colorHex` TEXT NOT NULL DEFAULT '#FFFFFF'")
            if (!notesCols.contains("isPinned")) db.execSQL("ALTER TABLE `notes` ADD COLUMN `isPinned` INTEGER NOT NULL DEFAULT 0")
            if (!notesCols.contains("isLocked")) db.execSQL("ALTER TABLE `notes` ADD COLUMN `isLocked` INTEGER NOT NULL DEFAULT 0")
            if (!notesCols.contains("lockPin")) db.execSQL("ALTER TABLE `notes` ADD COLUMN `lockPin` TEXT")
            if (!notesCols.contains("imageUri")) db.execSQL("ALTER TABLE `notes` ADD COLUMN `imageUri` TEXT")
            if (!notesCols.contains("audioPath")) db.execSQL("ALTER TABLE `notes` ADD COLUMN `audioPath` TEXT")
            if (!notesCols.contains("fontSize")) db.execSQL("ALTER TABLE `notes` ADD COLUMN `fontSize` INTEGER NOT NULL DEFAULT 16")
            if (!notesCols.contains("fontFamily")) db.execSQL("ALTER TABLE `notes` ADD COLUMN `fontFamily` TEXT NOT NULL DEFAULT 'DEFAULT'")
            if (!notesCols.contains("createdAt")) db.execSQL("ALTER TABLE `notes` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
            if (!notesCols.contains("updatedAt")) db.execSQL("ALTER TABLE `notes` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")

            // 4. Migrate `tasks` columns safely
            val tasksCols = getExistingColumns("tasks")
            if (!tasksCols.contains("isCompleted")) db.execSQL("ALTER TABLE `tasks` ADD COLUMN `isCompleted` INTEGER NOT NULL DEFAULT 0")
            if (!tasksCols.contains("isPinned")) db.execSQL("ALTER TABLE `tasks` ADD COLUMN `isPinned` INTEGER NOT NULL DEFAULT 0")
            if (!tasksCols.contains("isLocked")) db.execSQL("ALTER TABLE `tasks` ADD COLUMN `isLocked` INTEGER NOT NULL DEFAULT 0")
            if (!tasksCols.contains("lockPin")) db.execSQL("ALTER TABLE `tasks` ADD COLUMN `lockPin` TEXT")
            if (!tasksCols.contains("recurrence")) db.execSQL("ALTER TABLE `tasks` ADD COLUMN `recurrence` TEXT NOT NULL DEFAULT 'NONE'")
            if (!tasksCols.contains("visibleFrom")) db.execSQL("ALTER TABLE `tasks` ADD COLUMN `visibleFrom` INTEGER NOT NULL DEFAULT 0")
            if (!tasksCols.contains("createdAt")) db.execSQL("ALTER TABLE `tasks` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")

            // 5. Migrate `deadline_tasks` columns safely
            val dlTasksCols = getExistingColumns("deadline_tasks")
            if (!dlTasksCols.contains("isCompleted")) db.execSQL("ALTER TABLE `deadline_tasks` ADD COLUMN `isCompleted` INTEGER NOT NULL DEFAULT 0")
            if (!dlTasksCols.contains("deadlineTimestamp")) db.execSQL("ALTER TABLE `deadline_tasks` ADD COLUMN `deadlineTimestamp` INTEGER NOT NULL DEFAULT 0")
            if (!dlTasksCols.contains("isPinned")) db.execSQL("ALTER TABLE `deadline_tasks` ADD COLUMN `isPinned` INTEGER NOT NULL DEFAULT 0")
            if (!dlTasksCols.contains("isLocked")) db.execSQL("ALTER TABLE `deadline_tasks` ADD COLUMN `isLocked` INTEGER NOT NULL DEFAULT 0")
            if (!dlTasksCols.contains("lockPin")) db.execSQL("ALTER TABLE `deadline_tasks` ADD COLUMN `lockPin` TEXT")
            if (!dlTasksCols.contains("recurrence")) db.execSQL("ALTER TABLE `deadline_tasks` ADD COLUMN `recurrence` TEXT NOT NULL DEFAULT 'NONE'")
            if (!dlTasksCols.contains("visibleFrom")) db.execSQL("ALTER TABLE `deadline_tasks` ADD COLUMN `visibleFrom` INTEGER NOT NULL DEFAULT 0")
            if (!dlTasksCols.contains("createdAt")) db.execSQL("ALTER TABLE `deadline_tasks` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")

            // 6. Migrate `deadline_notes` columns safely
            val dlNotesCols = getExistingColumns("deadline_notes")
            if (!dlNotesCols.contains("deadlineTimestamp")) db.execSQL("ALTER TABLE `deadline_notes` ADD COLUMN `deadlineTimestamp` INTEGER NOT NULL DEFAULT 0")
            if (!dlNotesCols.contains("colorHex")) db.execSQL("ALTER TABLE `deadline_notes` ADD COLUMN `colorHex` TEXT NOT NULL DEFAULT '#FFFFFF'")
            if (!dlNotesCols.contains("isPinned")) db.execSQL("ALTER TABLE `deadline_notes` ADD COLUMN `isPinned` INTEGER NOT NULL DEFAULT 0")
            if (!dlNotesCols.contains("isLocked")) db.execSQL("ALTER TABLE `deadline_notes` ADD COLUMN `isLocked` INTEGER NOT NULL DEFAULT 0")
            if (!dlNotesCols.contains("lockPin")) db.execSQL("ALTER TABLE `deadline_notes` ADD COLUMN `lockPin` TEXT")
            if (!dlNotesCols.contains("recurrence")) db.execSQL("ALTER TABLE `deadline_notes` ADD COLUMN `recurrence` TEXT NOT NULL DEFAULT 'NONE'")
            if (!dlNotesCols.contains("visibleFrom")) db.execSQL("ALTER TABLE `deadline_notes` ADD COLUMN `visibleFrom` INTEGER NOT NULL DEFAULT 0")
            if (!dlNotesCols.contains("imageUri")) db.execSQL("ALTER TABLE `deadline_notes` ADD COLUMN `imageUri` TEXT")
            if (!dlNotesCols.contains("audioPath")) db.execSQL("ALTER TABLE `deadline_notes` ADD COLUMN `audioPath` TEXT")
            if (!dlNotesCols.contains("fontSize")) db.execSQL("ALTER TABLE `deadline_notes` ADD COLUMN `fontSize` INTEGER NOT NULL DEFAULT 16")
            if (!dlNotesCols.contains("fontFamily")) db.execSQL("ALTER TABLE `deadline_notes` ADD COLUMN `fontFamily` TEXT NOT NULL DEFAULT 'DEFAULT'")
            if (!dlNotesCols.contains("createdAt")) db.execSQL("ALTER TABLE `deadline_notes` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
        }

        private val MIGRATION_1_5 = object : Migration(1, 5) { override fun migrate(db: SupportSQLiteDatabase) = migrateToV5(db) }
        private val MIGRATION_2_5 = object : Migration(2, 5) { override fun migrate(db: SupportSQLiteDatabase) = migrateToV5(db) }
        private val MIGRATION_3_5 = object : Migration(3, 5) { override fun migrate(db: SupportSQLiteDatabase) = migrateToV5(db) }
        private val MIGRATION_4_5 = object : Migration(4, 5) { override fun migrate(db: SupportSQLiteDatabase) = migrateToV5(db) }
        private val MIGRATION_1_2 = object : Migration(1, 2) { override fun migrate(db: SupportSQLiteDatabase) = migrateToV5(db) }
        private val MIGRATION_2_3 = object : Migration(2, 3) { override fun migrate(db: SupportSQLiteDatabase) = migrateToV5(db) }
        private val MIGRATION_3_4 = object : Migration(3, 4) { override fun migrate(db: SupportSQLiteDatabase) = migrateToV5(db) }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "productivity_database"
                )
                .addMigrations(
                    MIGRATION_1_5,
                    MIGRATION_2_5,
                    MIGRATION_3_5,
                    MIGRATION_4_5,
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4
                )
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
