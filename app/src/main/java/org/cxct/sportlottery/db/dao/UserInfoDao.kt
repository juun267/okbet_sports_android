package org.cxct.sportlottery.db.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.cxct.sportlottery.db.entity.UserInfo

@Dao
interface UserInfoDao {
    @Query("SELECT * FROM user_info_table")
    fun getUserInfo(): Flow<List<UserInfo>>

    @Query("SELECT * FROM user_info_table WHERE user_id = :userId")
    fun getUserInfo(userId: Long): Flow<UserInfo?>

    @Query("DELETE FROM user_info_table")
    fun deleteAll()

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg userInfo: UserInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userInfo: UserInfo)

    @Transaction
    suspend fun upsert(userInfo: UserInfo) {
        try {
            insert(userInfo)
        } catch (e: SQLiteConstraintException) {
            update(userInfo)
        }
    }
}