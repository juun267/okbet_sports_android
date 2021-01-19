package org.cxct.sportlottery.ui.profileCenter.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.network.uploadImg.UploadImgResult
import org.cxct.sportlottery.ui.base.BaseViewModel

class ProfileModel(
    private val androidContext: Context
) : BaseViewModel() {

    private val _uploadImgResult = MutableLiveData<UploadImgResult?>()

    val uploadImgResult: LiveData<UploadImgResult?>
        get() = _uploadImgResult


    fun uploadImage(uploadImgRequest: UploadImgRequest) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.uploadImgService.uploadImg(uploadImgRequest.toParts())
            }

            _uploadImgResult.postValue(result)
        }
    }

}