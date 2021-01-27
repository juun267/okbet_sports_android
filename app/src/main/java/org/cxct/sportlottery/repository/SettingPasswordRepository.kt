package org.cxct.sportlottery.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.db.dao.UserInfoDao

@Deprecated("和 UserInfoRepository 整合")
class SettingPasswordRepository(private val userInfoDao: UserInfoDao) {
    suspend fun updatePayPwFlag(userId: Long){
        withContext(Dispatchers.IO){
            userInfoDao.updatePayPw(userId, 0)
        }
    }
}