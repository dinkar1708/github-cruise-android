package com.jetpack.compose.github.github.cruise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jetpack.compose.github.github.cruise.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User operations
 *
 * Provides reactive database operations with Flow
 */
@Dao
interface UserDao {

    /**
     * Get user by login (reactive)
     */
    @Query("SELECT * FROM users WHERE login = :login")
    fun getUserByLogin(login: String): Flow<UserEntity?>

    /**
     * Get user by login (one-shot)
     */
    @Query("SELECT * FROM users WHERE login = :login")
    suspend fun getUserByLoginOneShot(login: String): UserEntity?

    /**
     * Insert or update user
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    /**
     * Insert or update multiple users
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    /**
     * Delete user
     */
    @Query("DELETE FROM users WHERE login = :login")
    suspend fun deleteUser(login: String)

    /**
     * Delete all users
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    /**
     * Get all cached users
     */
    @Query("SELECT * FROM users ORDER BY cachedAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    /**
     * Delete old cached users (older than 7 days)
     */
    @Query("DELETE FROM users WHERE cachedAt < :timestamp")
    suspend fun deleteOldUsers(timestamp: Long)
}
