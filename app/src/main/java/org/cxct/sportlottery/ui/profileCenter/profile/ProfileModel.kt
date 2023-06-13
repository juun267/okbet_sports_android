package org.cxct.sportlottery.ui.profileCenter.profile

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.config.SalarySource
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.network.user.iconUrl.IconUrlResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.Event

class ProfileModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
    private val avatarRepository: AvatarRepository,
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {
    val editIconUrlResult: LiveData<Event<IconUrlResult?>> = avatarRepository.editIconUrlResult
    //薪资来源
    val salaryList: LiveData<List<SalarySource>>
        get() = _salaryList
    private var _salaryList = MutableLiveData<List<SalarySource>>()
    //薪资来源string 列表
    val salaryStringList: ArrayList<String> = ArrayList()
    fun uploadImage(uploadImgRequest: UploadImgRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                avatarRepository.uploadImage(uploadImgRequest)
            }
        }
    }

    //確認是否需要驗證手機
    fun checkNeedToShowSecurityDialog() {
        viewModelScope.launch {
            withdrawRepository.checkNeedToShowSecurityDialog()
        }
    }

    fun getUserInfo() {
        viewModelScope.launch {
            runWithCatch { userInfoRepository.getUserInfo() }
        }
    }

    /**
     * 获取收入来源选项
     */
    fun getUserSalaryList() {
        launch {
            val result = doNetwork(androidContext) {
                OneBoSportApi.indexService.getUserSalaryList()
            }
            result?.let {
                salaryStringList.clear()
                result.rows?.let {
                    result.rows.forEach { salary ->
                        salaryStringList.add(salary.name)
                    }
                    _salaryList.postValue(it)
                }
            }
        }
    }
}