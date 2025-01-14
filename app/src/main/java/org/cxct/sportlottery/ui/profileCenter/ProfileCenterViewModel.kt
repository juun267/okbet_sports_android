package org.cxct.sportlottery.ui.profileCenter

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.safeApi
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.user.UserRepository
import org.cxct.sportlottery.net.user.data.KYCVerifyConfig
import org.cxct.sportlottery.net.user.data.LiveKycVerifyResult
import org.cxct.sportlottery.net.user.data.OCRInfo
import org.cxct.sportlottery.net.user.data.VerifyConfig
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.uploadImg.*
import org.cxct.sportlottery.network.user.iconUrl.IconUrlResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.Uide
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.SingleLiveEvent
import java.io.File

class ProfileCenterViewModel(
    androidContext: Application
) : BaseSocketViewModel(
    androidContext
) {
    val token = LoginRepository.token

    val editIconUrlResult: LiveData<Event<IconUrlResult?>> = AvatarRepository.editIconUrlResult

    val docUrlResult: LiveData<Event<UploadImgResult>>
        get() = _docUrlResult
    private val _docUrlResult = MutableLiveData<Event<UploadImgResult>>()

    val photoUrlResult: LiveData<Event<UploadImgResult>>
        get() = _photoUrlResult
    private val _photoUrlResult = MutableLiveData<Event<UploadImgResult>>()

    val uploadVerifyPhotoResult: LiveData<Event<UploadVerifyPhotoResult?>>
        get() = _uploadVerifyPhotoResult
    private val _uploadVerifyPhotoResult = MutableLiveData<Event<UploadVerifyPhotoResult?>>()

    val userVerifiedType: LiveData<Event<Int?>>
        get() = _userVerifiedType
    private val _userVerifiedType = MutableLiveData<Event<Int?>>()

    val verifyConfig = SingleLiveEvent<ApiResult<VerifyConfig>>()
    val imgUpdated = SingleLiveEvent<Pair<File, ImgData?>>()
    val uploadReview = SingleLiveEvent<ApiResult<String>>()
    val ocrResult = SingleLiveEvent<Triple<Boolean, String, OCRInfo?>>()
    val kycResult = SingleLiveEvent<ApiResult<String>>()
    val reVerifyResult = SingleLiveEvent<ApiResult<String?>>()
    val kycVerifyConfigResult = SingleLiveEvent<ApiResult<KYCVerifyConfig>>()
    val taskRedDotEvent get() = TaskCenterRepository.taskRedDotEvent
    val licenseResult = SingleLiveEvent<ApiResult<String>>()
    val liveKycVerifyResult = SingleLiveEvent<ApiResult<LiveKycVerifyResult>>()

    fun getUserInfo() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                UserInfoRepository.getUserInfo()
            }
        }
    }


    fun uploadImage(uploadImgRequest: UploadImgRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                AvatarRepository.uploadImage(uploadImgRequest)
            }
        }
    }
    fun getUserVerified() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                UserInfoRepository.getUserInfo()
            }?.let { result ->
                if (result.success) {
                    _userVerifiedType.postValue(Event(result.userInfoData?.verified))
                }
            }
        }
    }


    fun getVerifyConfig() {
        callApi({ UserRepository.getVerifyConfig() }) { verifyConfig.value = it}
    }


    fun uploadImage(imgeFile: File) {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.uploadImgService.uploadImg(
                    UploadVerifyDocRequest(
                        userInfo.value?.userId.toString(),
                        imgeFile
                    ).toPars()
                )
            }

            imgUpdated.value = Pair(imgeFile, result?.imgData)
        }


    }

    fun updateReverifyInfo(selfImgUrl: String?, proofImgUrl: String?, backOfID: String?) {
        callApi({ UserRepository.uploadReviewPhoto(selfImgUrl, proofImgUrl, backOfID) }) { uploadReview .value = it }
    }

    fun startOCR(photo: File, idType: Int, id: Int) = viewModelScope.launch {
        val uploadImgRequest = UploadVerifyDocRequest(userInfo.value?.userId.toString(), photo).toPars()
        val uploadResult = kotlin.runCatching { OneBoSportApi.uploadImgService.uploadImg(uploadImgRequest) }.getOrNull()
        val url = uploadResult?.body()?.imgData?.path
        if (url.isEmptyStr()) {
            ocrResult.postValue(Triple(false, "", null))
            return@launch
        }

        if (idType != 1) {
            ocrResult.postValue(Triple(false, url!!, null))
            return@launch
        }
        //选择KYC识别厂商
        val ocrResponse = if (sConfigData?.kycsupplierSelectionValue==1){
             safeApi { UserRepository.getOCRInfoByHuawei(id, photo,url!!) }
        }else{
             safeApi { UserRepository.getOCRInfo(id, url!!) }
        }
        ocrResult.postValue(Triple(ocrResponse.succeeded(), url!!, ocrResponse.getData()))
    }


    fun putKYCInfo(idType: Int, idNumber: String?, idImageUrl: String,
                   firstName: String, middleName: String, lastName: String, birthday: String, uide: Uide
    ) {
        callApi({ UserRepository.uploadKYCInfo(idType, idNumber, idImageUrl, firstName, middleName, lastName, birthday, uide) }) {
            kycResult.value = it
        }
    }

    fun reVerify() {
        callApi({ UserRepository.reVerify() }) {
            reVerifyResult.postValue(it)
        }
    }

    fun getKycNeedInformation() {
        callApi({ UserRepository.getKycNeedInformation() }) {
            kycVerifyConfigResult.value = it
        }
    }
    fun getLivenessLicense(pageName: String) {
        callApi({ UserRepository.getLicense(pageName) }) {
            licenseResult.postValue(it)
        }
    }
    fun liveKycVerify(verifyId: String) {
        callApi({ UserRepository.liveKycVerify(verifyId) }) {
            liveKycVerifyResult.postValue(it)
        }
    }

}