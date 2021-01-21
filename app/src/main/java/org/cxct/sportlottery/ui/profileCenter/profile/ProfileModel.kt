package org.cxct.sportlottery.ui.profileCenter.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.network.uploadImg.UploadImgResult
import org.cxct.sportlottery.network.user.iconUrl.IconUrlRequest
import org.cxct.sportlottery.network.user.iconUrl.IconUrlResult
import org.cxct.sportlottery.ui.base.BaseViewModel

class ProfileModel(
    private val androidContext: Context
) : BaseViewModel() {

    private val _uploadImgResult = MutableLiveData<UploadImgResult?>()
    private val _editIconUrlResult = MutableLiveData<IconUrlResult?>()

    val uploadImgResult: LiveData<UploadImgResult?>
        get() = _uploadImgResult
    val editIconUrlResult: LiveData<IconUrlResult?>
        get() = _editIconUrlResult

    //UploadImage API 在另一個伺服器，上傳成功後得到的 url，再透過 editIconUrl API 去更新，下次登入的 LoginData 裡面的 iconUrl 才會更新
    fun uploadImage(uploadImgRequest: UploadImgRequest) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.uploadImgService.uploadImg(uploadImgRequest.toParts())
            }

            //TODO simon test UserInfo API 串接後，review 如何去更新 iconUrl，更新每個頁面的 user 頭像
            if (result?.success == true)
                editIconUrl(IconUrlRequest(result.imgData?.path ?: ""))

            _uploadImgResult.postValue(result)
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