package com.weighttracker.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Weight::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WeightDatabase : RoomDatabase() {
    abstract fun weightDao(): WeightDao

    companion object {
        @Volatile
        private var INSTANCE: WeightDatabase? = null

        // 数据库迁移：版本1到版本2，添加日期唯一索引
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建唯一索引，确保每天只能有一个体重记录
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_weights_date ON weights(date)")
                
                // 处理可能存在的重复记录：保留每天最新的记录
                database.execSQL("""
                    DELETE FROM weights 
                    WHERE id NOT IN (
                        SELECT MAX(id) 
                        FROM weights 
                        GROUP BY date(date/1000, 'unixepoch')
                    )
                """)
            }
        }

        fun getDatabase(context: Context): WeightDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeightDatabase::class.java,
                    "weight_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 