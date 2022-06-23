package org.cxct.sportlottery.ui.profileCenter

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.BaseSecurityCodeResult
import org.cxct.sportlottery.network.credential.CredentialCompleteRequest
import org.cxct.sportlottery.network.credential.CredentialCompleteResult
import org.cxct.sportlottery.network.credential.CredentialInitialRequest
import org.cxct.sportlottery.network.credential.CredentialResult
import org.cxct.sportlottery.network.uploadImg.*
import org.cxct.sportlottery.network.user.iconUrl.IconUrlResult
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.Event
import java.io.File

class ProfileCenterViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    private val avatarRepository: AvatarRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository
) : BaseBottomNavViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {
    val token = loginRepository.token

    val editIconUrlResult: LiveData<Event<IconUrlResult?>> = avatarRepository.editIconUrlResult

    val docUrlResult: LiveData<Event<UploadImgResult>>
        get() = _docUrlResult
    private val _docUrlResult = MutableLiveData<Event<UploadImgResult>>()

    val photoUrlResult: LiveData<Event<UploadImgResult>>
        get() = _photoUrlResult
    private val _photoUrlResult = MutableLiveData<Event<UploadImgResult>>()

    val uploadVerifyPhotoResult: LiveData<Event<UploadVerifyPhotoResult?>>
        get() = _uploadVerifyPhotoResult
    private val _uploadVerifyPhotoResult = MutableLiveData<Event<UploadVerifyPhotoResult?>>()

    val credentialInitialResult: LiveData<Event<CredentialResult?>>
        get() = _credentialInitialResult
    private val _credentialInitialResult = MutableLiveData<Event<CredentialResult?>>()

    val credentialCompleteResult: LiveData<Event<CredentialCompleteResult?>>
        get() = _credentialCompleteResult
    private val _credentialCompleteResult = MutableLiveData<Event<CredentialCompleteResult?>>()

    fun getUserInfo() {
        viewModelScope.launch {
            userInfoRepository.getUserInfo()
        }
    }

    fun getCredentialInitial(metaInfo: String, docType: String) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.credentialService.getCredentialInitial(
                    CredentialInitialRequest(
                        metaInfo,
                        docType
                    )
                )
            }?.let { result ->
                _credentialInitialResult.postValue(Event(result))
            }
        }
    }

    fun getCredentialCompleteResult(transactionId: String?) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.credentialService.getCredentialComplete(
                    CredentialCompleteRequest(transactionId)
                )
            }?.let { result ->
                _credentialCompleteResult.postValue(Event(result))
            }
        }
    }

    //提款設置判斷權限
    fun settingCheckPermissions() {
        viewModelScope.launch {
            withdrawRepository.settingCheckPermissions()
        }
    }

    fun uploadImage(uploadImgRequest: UploadImgRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                avatarRepository.uploadImage(uploadImgRequest)
            }
        }
    }

    fun uploadVerifyPhoto(docFile: File, photoFile: File) {
        viewModelScope.launch {
            val docResponse = doNetwork(androidContext) {
                OneBoSportApi.uploadImgService.uploadImg(
                    UploadVerifyDocRequest(
                        userInfo.value?.userId.toString(),
                        docFile
                    ).toPars()
                )
            }
            when {
                docResponse == null -> _docUrlResult.postValue(
                    Event(
                        UploadImgResult(
                            -1,
                            androidContext.getString(R.string.unknown_error),
                            false,
                            null
                        )
                    )
                )
                docResponse.success -> {
                    _docUrlResult.postValue(Event(docResponse))

                    val photoResponse = doNetwork(androidContext) {
                        OneBoSportApi.uploadImgService.uploadImg(
                            UploadVerifyDocRequest(
                                userInfo.value?.userId.toString(),
                                photoFile
                            ).toPars()
                        )
                    }
                    when {
                        photoResponse == null -> _photoUrlResult.postValue(
                            Event(
                                UploadImgResult(
                                    -1,
                                    androidContext.getString(R.string.unknown_error),
                                    false,
                                    null
                                )
                            )
                        )
                        photoResponse.success -> {
                            _photoUrlResult.postValue(Event(photoResponse))
                            uploadIdentityDoc(docResponse.imgData?.path, photoResponse.imgData?.path)
                        }
                        else -> {
                            val error =
                                UploadImgResult(photoResponse.code, photoResponse.msg, photoResponse.success, null)
                            _photoUrlResult.postValue(Event(error))
                        }
                    }
                }
                else -> {
                    val error = UploadImgResult(
                        docResponse.code, docResponse.msg, docResponse.success, null
                    )
                    _docUrlResult.postValue(Event(error))
                }
            }
        }
    }

    fun uploadIdentityDoc(docPath: String? = null, photoPath: String? = null) {
        val docPathUrl = docPath ?: _docUrlResult.value?.peekContent()?.imgData?.path
        val photoPathUrl = photoPath ?: _photoUrlResult.value?.peekContent()?.imgData?.path
        when {
            docPathUrl == null -> {
                _uploadVerifyPhotoResult.postValue(
                    Event(
                        UploadVerifyPhotoResult(
                            -1,
                            androidContext.getString(R.string.upload_fail),
                            false,
                            null
                        )
                    )
                )
            }
            photoPathUrl == null -> {
                UploadVerifyPhotoResult(
                    -1,
                    androidContext.getString(R.string.upload_fail),
                    false,
                    null
                )
            }
            else -> {
                viewModelScope.launch {
                    val verifyPhotoResponse = doNetwork(androidContext) {
                        OneBoSportApi.uploadImgService.uploadVerifyPhoto(
                            UploadVerifyPhotoRequest(
                                docPathUrl,
                                photoPathUrl
                            )
                        )
                    }
                    _uploadVerifyPhotoResult.postValue(Event(verifyPhotoResponse))
                }
            }
        }
    }
}