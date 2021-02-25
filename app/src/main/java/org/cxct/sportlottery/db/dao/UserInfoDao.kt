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
    suspend fun deleteAll()

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(vararg userInfo: UserInfo)

    @Query("UPDATE user_info_table SET update_pay_pw=:updatePayPw WHERE user_id=:userId")
    suspend fun updatePayPw(userId: Long, updatePayPw: Int)

    @Query("UPDATE user_info_table SET icon_url=:iconUrl WHERE user_id=:userId")
    suspend fun updateIconUrl(userId: Long, iconUrl: String)

    @Query("UPDATE user_info_table SET nick_name=:nickname WHERE user_id=:userId")
    suspend fun updateNickname(userId: Long, nickname: String)

    @Query("UPDATE user_info_table SET full_name=:fullName WHERE user_id=:userId")
    suspend fun updateFullName(userId: Long, fullName: String)

    @Query("UPDATE user_info_table SET qq=:qq WHERE user_id=:userId")
    suspend fun updateQQ(userId: Long, qq: String)

    @Query("UPDATE user_info_table SET email=:email WHERE user_id=:userId")
    suspend fun updateEmail(userId: Long, email: String)

    @Query("UPDATE user_info_table SET phone=:phone WHERE user_id=:userId")
    suspend fun updatePhone(userId: Long, phone: String)

    @Query("UPDATE user_info_table SET wechat=:wechat WHERE user_id=:userId")
    suspend fun updateWeChat(userId: Long, wechat: String)

    @Query("UPDATE user_info_table SET setted=:setted WHERE user_id=:userId")
    suspend fun updateSetted(userId: Long, setted: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userInfo: UserInfo)

    @Transaction
    suspend fun upsert(userInfo: UserInfo) {
        try {
            insert(userInfo)
        } catch (e: SQLiteConstraintException) {
            e.printStackTrace()
            update(userInfo)
        }
    }
}