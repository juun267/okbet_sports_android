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
    private val withdrawRepository: WithdrawRepository,
    favoriteRepository: MyFavoriteRepository
) : BaseBottomNavViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {

    enum class SecurityEnter(val code: Int){
        UPDATE_PW(0),//更新提款卡密碼
        SETTING_PW(1),//設定提現密碼
        COMPLETET_PROFILE_INFO(2),//完善個人資料
        SETTING_PROFILE_INFO(3),//設定個人資料
        BIND_BANK_CARD(4)//綁定銀行卡
    }

    val token = loginRepository.token

    val withdrawSystemOperation =
        withdrawRepository.withdrawSystemOperation
    val rechargeSystemOperation =
        withdrawRepository.rechargeSystemOperation
    val needToUpdateWithdrawPassword =
        withdrawRepository.needToUpdateWithdrawPassword //提款頁面是否需要更新提款密碼 true: 需要, false: 不需要
    val settingNeedToUpdateWithdrawPassword =
        withdrawRepository.settingNeedToUpdateWithdrawPassword //提款設置頁面是否需要更新提款密碼 true: 需要, false: 不需要
    val settingNeedToCompleteProfileInfo =
        withdrawRepository.settingNeedToCompleteProfileInfo //提款設置頁面是否需要完善個人資料 true: 需要, false: 不需要
    val needToCompleteProfileInfo =
        withdrawRepository.needToCompleteProfileInfo //提款頁面是否需要完善個人資料 true: 需要, false: 不需要
    val needToBindBankCard =
        withdrawRepository.needToBindBankCard //提款頁面是否需要新增銀行卡 -1 : 不需要新增, else : 以value作為string id 顯示彈窗提示
    val needToSendTwoFactor =
        withdrawRepository.showSecurityDialog //判斷是不是要進行手機驗證 true: 需要, false: 不需要

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

    //發送簡訊碼之後60s無法再發送
    val twoFactorResult: LiveData<BaseSecurityCodeResult?>
        get() = _twoFactorResult
    private val _twoFactorResult = MutableLiveData<BaseSecurityCodeResult?>()

    //錯誤提示
    val errorMessageDialog: LiveData<String?>
        get() = _errorMessageDialog
    private val _errorMessageDialog = MutableLiveData<String?>()

    //認證成功
    val twoFactorSuccess: LiveData<Boolean?>
        get() = _twoFactorSuccess
    private val _twoFactorSuccess = MutableLiveData<Boolean?>()

    //需要完善個人資訊(缺電話號碼) needPhoneNumber
    val showPhoneNumberMessageDialog = withdrawRepository.hasPhoneNumber

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

    //提款功能是否啟用
    fun checkWithdrawSystem() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                withdrawRepository.checkWithdrawSystem()
            }
        }
    }

    //充值功能是否啟用
    fun checkRechargeSystem() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                withdrawRepository.checkRechargeSystem()
            }
        }
    }

    //提款設置判斷權限
    fun settingCheckPermissions() {
        viewModelScope.launch {
            withdrawRepository.settingCheckPermissions()
        }
    }

    /**
     * 判斷個人資訊是否完整, 若不完整需要前往個人資訊頁面完善資料.
     * complete true: 個人資訊有缺漏, false: 個人資訊完整
     */
    fun checkProfileInfoComplete() {
        viewModelScope.launch {
            withdrawRepository.checkProfileInfoComplete()
        }
    }

    fun checkBankCardPermissions() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                withdrawRepository.checkBankCardPermissions()
            }
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


    //發送簡訊驗證碼
    fun sendTwoFactor() {
        viewModelScope.launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.withdrawService.sendTwoFactor()
            }
            _twoFactorResult.postValue(result)
        }
    }

    //双重验证校验
    fun validateTwoFactor(validateTwoFactorRequest: ValidateTwoFactorRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.withdrawService.validateTwoFactor(validateTwoFactorRequest)
            }?.let { result ->
                if(result.success){
                    _twoFactorSuccess.value = true
                    withdrawRepository.sendTwoFactor()
                }
                else
                    _errorMessageDialog.value = result.msg
            }
        }
    }

}