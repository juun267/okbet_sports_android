package org.cxct.sportlottery.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.cxct.sportlottery.db.dao.UserInfoDao
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bank.my.BankMyResult
import retrofit2.Response

class WithdrawRepository(private val userInfoDao: UserInfoDao) {

    val userInfoFlow: Flow<UserInfo?>
        get() = userInfoDao.getUserInfo().map {
            if (it.isNotEmpty()) {
                return@map it[0]
            }
            return@map null
        }

//    val userInfo by lazy { userInfoFlow.asLiveData() }

    private var _needToUpdateWithdrawPassword = MutableLiveData<Boolean>()
    val needToUpdateWithdrawPassword: LiveData<Boolean> //提款頁面是否需要更新提款密碼 true: 需要, false: 不需要
        get() = _needToUpdateWithdrawPassword

    private var _settingNeedToUpdateWithdrawPassword = MutableLiveData<Boolean>()
    val settingNeedToUpdateWithdrawPassword: LiveData<Boolean> //提款設置頁面是否需要更新提款密碼 true: 需要, false: 不需要
        get() = _settingNeedToUpdateWithdrawPassword

    private var _needToCompleteProfileInfo = MutableLiveData<Boolean>()
    val needToCompleteProfileInfo: LiveData<Boolean> //提款頁面是否需要完善個人資料 true: 需要, false: 不需要
        get() = _needToCompleteProfileInfo

    private var _needToBindBankCard = MutableLiveData<Boolean>()
    val needToBindBankCard: LiveData<Boolean>
        get() = _needToBindBankCard //提款頁面是否需要新增銀行卡 true: 需要, false:不需要

    //TODO simon test
    private suspend fun checkNeedUpdatePassWord(): Boolean? {
        Log.e("simon test", "updatePayPw: ${userInfoFlow.firstOrNull()?.updatePayPw}")
        return when (userInfoFlow.firstOrNull()?.updatePayPw) {
            1 -> true
            0 -> false
            else -> null
        }
    }

    //提款判斷權限
    suspend fun withdrawCheckPermissions() {
        this.checkNeedUpdatePassWord()?.let { _needToUpdateWithdrawPassword.value = it }
    }

    //提款設置判斷權限
    suspend fun settingCheckPermissions() {
        this.checkNeedUpdatePassWord()?.let { _settingNeedToUpdateWithdrawPassword.value = it }
    }

    /**
     * 判斷個人資訊是否完整, 若不完整需要前往個人資訊頁面完善資料.
     * complete true: 個人資訊有缺漏, false: 個人資訊完整
     */
    fun checkProfileInfoComplete() {
//        var complete = false
//        sConfigData?.apply {
//            if (enableWithdrawFullName == FLAG_OPEN && userInfo.value?.fullName.isNullOrBlank() ||
//                enableWithdrawQQ == FLAG_OPEN && userInfo.value?.qq.isNullOrBlank() ||
//                enableWithdrawEmail == FLAG_OPEN && userInfo.value?.email.isNullOrBlank() ||
//                enableWithdrawPhone == FLAG_OPEN && userInfo.value?.phone.isNullOrBlank() ||
//                enableWithdrawWechat == FLAG_OPEN && userInfo.value?.wechat.isNullOrBlank()
//            ) {
//                complete = true
//            }
//        }
//        _needToCompleteProfileInfo.value = complete
    }

    suspend fun checkBankCardPermissions(): Response<BankMyResult> {
        val response = OneBoSportApi.bankService.getBankMy()
        if (response.isSuccessful) {
            response.body()?.let { result ->
                if (result.success) {
                    _needToBindBankCard.value = result.bankCardList.isNullOrEmpty()
                }
            }
        }
        return response
    }

}