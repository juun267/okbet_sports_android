package org.cxct.sportlottery.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.dao.UserInfoDao
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.network.uploadImg.UploadImgResult
import org.cxct.sportlottery.network.user.iconUrl.IconUrlRequest
import org.cxct.sportlottery.network.user.iconUrl.IconUrlResult
import org.cxct.sportlottery.util.Event
import retrofit2.Response

class AvatarRepository(private val androidContext: Context, private val userInfoDao: UserInfoDao) {
    private val _editIconUrlResult = MutableLiveData<Event<IconUrlResult?>>()
    val editIconUrlResult: LiveData<Event<IconUrlResult?>>
        get() = _editIconUrlResult

    private val _voucherUrlResult = MutableLiveData<Event<String>>()
    val voucherUrlResult: LiveData<Event<String>>
        get() = _voucherUrlResult

    //UploadImage API 在另一個伺服器，上傳成功後得到的 url，再透過 editIconUrl API 去更新，下次登入的 LoginData 裡面的 iconUrl 才會更新
    suspend fun uploadImage(uploadImgRequest: UploadImgRequest): Response<UploadImgResult> {
        val response = OneBoSportApi.uploadImgService.uploadImg(uploadImgRequest.toParts())
        if (response.isSuccessful) {
            val result = response.body()
            when {
                result == null -> _editIconUrlResult.postValue(
                    Event(
                        IconUrlResult(
                            -1,
                            androidContext.getString(R.string.unknown_error),
                            false,
                            null
                        )
                    )
                )
                result.success -> {
                    val userId = uploadImgRequest.userId.toLong()
                    val path = result.imgData?.path ?: ""
                    val iconUrl = sConfigData?.resServerHost + path
                    userInfoDao.updateIconUrl(userId, iconUrl) //更新 DB iconUrl 資料
                    editIconUrl(IconUrlRequest(path))
                }
                else -> {
                    val error = IconUrlResult(result.code, result.msg, result.success, null)
                    _editIconUrlResult.postValue(Event(error))
                }
            }
        }
        return response
    }

    private suspend fun editIconUrl(iconUrlRequest: IconUrlRequest) {
        val response = OneBoSportApi.userService.editIconUrl(iconUrlRequest)
        if (response.isSuccessful) {
            response.body()?.let { result ->
                _editIconUrlResult.postValue(Event(result))
            }
        }
    }

    suspend fun uploadVoucher(uploadImgRequest: UploadImgRequest): Response<UploadImgResult> {
        val response = OneBoSportApi.uploadImgService.uploadImg(uploadImgRequest.toParts())
        if (response.isSuccessful) {
            val result = response.body()
            when {
                result == null -> _editIconUrlResult.postValue(
                    Event(
                        IconUrlResult(
                            -1,
                            androidContext.getString(R.string.unknown_error),
                            false,
                            null
                        )
                    )
                )
                result.success -> {
                    val path = result.imgData?.path ?: ""
                    val iconUrl = sConfigData?.resServerHost + path
                    _voucherUrlResult.postValue(Event(iconUrl))

                }
            }
        }
        return response
    }

}