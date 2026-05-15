package com.baseproject.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.baseproject.data.local.database.converter.Converters
import com.baseproject.data.local.database.dao.GreetingDao
import com.baseproject.data.local.database.entity.GreetingEntity

@Database(
    entities = [GreetingEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun greetingDao(): GreetingDao

    companion object {
        const val DATABASE_NAME = "app_database"
    }
}
