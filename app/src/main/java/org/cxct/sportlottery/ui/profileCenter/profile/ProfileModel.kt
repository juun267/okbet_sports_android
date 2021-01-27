package org.cxct.sportlottery.ui.profileCenter.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.network.user.iconUrl.IconUrlRequest
import org.cxct.sportlottery.network.user.iconUrl.IconUrlResult
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseViewModel

class ProfileModel(
    private val androidContext: Context,
    private val userInfoRepository: UserInfoRepository
) : BaseViewModel() {

    private val _editIconUrlResult = MutableLiveData<IconUrlResult?>()

    val editIconUrlResult: LiveData<IconUrlResult?>
        get() = _editIconUrlResult

    val userInfo: LiveData<UserInfo?> = userInfoRepository.userInfo.asLiveData()

    //UploadImage API 在另一個伺服器，上傳成功後得到的 url，再透過 editIconUrl API 去更新，下次登入的 LoginData 裡面的 iconUrl 才會更新
    fun uploadImage(uploadImgRequest: UploadImgRequest) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.uploadImgService.uploadImg(uploadImgRequest.toParts())
            }

            when {
                result == null -> _editIconUrlResult.postValue(IconUrlResult(-1, androidContext.getString(R.string.unknown_error), false, null))
                result.success -> {
                    val userId = uploadImgRequest.userId.toLong()
                    val path = result.imgData?.path ?: ""
                    val iconUrl = sConfigData?.resServerHost + path
                    userInfoRepository.updateIconUrl(userId, iconUrl) //更新 DB iconUrl 資料
                    editIconUrl(IconUrlRequest(path))
                }
                else -> {
                    val error = IconUrlResult(result.code, result.msg, result.success, null)
                    _editIconUrlResult.postValue(error)
                }
            }
        }
    }

    private fun editIconUrl(iconUrlRequest: IconUrlRequest) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.userService.editIconUrl(iconUrlRequest)
            }

            _editIconUrlResult.postValue(result)
        }
    }
}