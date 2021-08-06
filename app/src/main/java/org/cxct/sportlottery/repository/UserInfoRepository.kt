package org.cxct.sportlottery.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.db.dao.UserInfoDao
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.user.info.UserInfoData
import org.cxct.sportlottery.network.user.info.UserInfoResult
import retrofit2.Response

class UserInfoRepository(private val userInfoDao: UserInfoDao) {

    val userInfo: Flow<UserInfo?>
        get() = userInfoDao.getUserInfo().map {
            if (it.isNotEmpty()) {
                return@map it[0]
            }
            return@map null
        }

    suspend fun getUserInfo(): Response<UserInfoResult> {
        val userInfoResponse = OneBoSportApi.userService.getUserInfo()

        if (userInfoResponse.isSuccessful) {
            userInfoResponse.body()?.let {
                updateUserInfo(it.userInfoData)
            }
        }
        return userInfoResponse
    }

    @WorkerThread
    suspend fun updateUserInfo(userInfoData: UserInfoData?) {
        userInfoData?.let {
            val userInfo = transform(it)

            withContext(Dispatchers.IO) {
                userInfoDao.upsert(userInfo)
            }
        }
    }

    suspend fun updatePayPwFlag(userId: Long) {
        withContext(Dispatchers.IO) {
            userInfoDao.updatePayPw(userId, 0)
        }
    }

    suspend fun updateIconUrl(userId: Long, iconUrl: String) {
        withContext(Dispatchers.IO) {
            userInfoDao.updateIconUrl(userId, iconUrl)
        }
    }

    suspend fun updateNickname(userId: Long, nickname: String) {
        withContext(Dispatchers.IO) {
            userInfoDao.updateNickname(userId, nickname)
        }
    }

    suspend fun updateFullName(userId: Long, fullName: String){
        withContext(Dispatchers.IO){
            userInfoDao.updateFullName(userId, fullName)
        }
    }

    suspend fun updateQQ(userId: Long, qq: String){
        withContext(Dispatchers.IO){
            userInfoDao.updateQQ(userId, qq)
        }
    }

    suspend fun updateEmail(userId: Long, email: String){
        withContext(Dispatchers.IO){
            userInfoDao.updateEmail(userId, email)
        }
    }

    suspend fun updatePhone(userId: Long, phone: String){
        withContext(Dispatchers.IO){
            userInfoDao.updatePhone(userId, phone)
        }
    }

    suspend fun updateWeChat(userId: Long, wechat: String){
        withContext(Dispatchers.IO){
            userInfoDao.updateWeChat(userId, wechat)
        }
    }

    //是否设置过昵称 0单标未设置过 1代表设置过
    suspend fun updateSetted(userId: Long, setted: Int) {
        withContext(Dispatchers.IO) {
            userInfoDao.updateSetted(userId, setted)
        }
    }

    private fun transform(userInfoData: UserInfoData) =
        UserInfo(
            userInfoData.userId,
            fullName = userInfoData.fullName,
            iconUrl = userInfoData.iconUrl,
            lastLoginIp = userInfoData.lastLoginIp,
            loginIp = userInfoData.loginIp,
            nickName = userInfoData.nickName,
            platformId = userInfoData.platformId,
            testFlag = userInfoData.testFlag,
            userName = userInfoData.userName,
            userType = userInfoData.userType,
            email = userInfoData.email,
            qq = userInfoData.qq,
            phone = userInfoData.phone,
            wechat = userInfoData.wechat,
            updatePayPw = userInfoData.updatePayPw,
            setted = userInfoData.setted,
            userRebateList = userInfoData.userRebateList
        )
}