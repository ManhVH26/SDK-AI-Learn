package com.baseproject.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.baseproject.data.local.database.entity.GreetingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GreetingDao {

    @Query("SELECT * FROM greetings ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<GreetingEntity>>

    @Query("SELECT * FROM greetings WHERE id = :id")
    suspend fun getById(id: Long): GreetingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GreetingEntity): Long

    @Delete
    suspend fun delete(entity: GreetingEntity)

    @Query("DELETE FROM greetings")
    suspend fun deleteAll()
}
