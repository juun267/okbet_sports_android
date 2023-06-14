package org.cxct.sportlottery.ui.profileCenter.profile

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.network.user.iconUrl.IconUrlResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.Event
import org.cxct.sportlottery.util.LogUtil

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
    //薪资来源string 列表
    val salaryStringList: ArrayList<DialogBottomDataEntity> = ArrayList()

    //地区信息
    //国籍 列表
    val nationalityList: ArrayList<DialogBottomDataEntity> = ArrayList()

    //省 列表
    val provincesList: ArrayList<DialogBottomDataEntity> = ArrayList()

    //市区 列表
    val cityList: ArrayList<DialogBottomDataEntity> = ArrayList()

    //职业 列表
    val workList: ArrayList<DialogBottomDataEntity> = ArrayList()

    private val _userDetail = MutableLiveData<UserInfoDetailsEntity>()
    val userDetail: LiveData<UserInfoDetailsEntity> //使用者餘額
        get() = _userDetail

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
        LogUtil.d("queryUserInfoDetails onresume")
        viewModelScope.launch {
            runWithCatch { userInfoRepository.getUserInfo() }
        }
        if (!salaryStringList.isEmpty()) {

            launch {
                //用户信息详情查询
                doNetwork(androidContext) {
                    OneBoSportApi.bettingStationService.userQueryUserInfoDetails()
                }?.let {
                    it.let {
                        _userDetail.postValue(it)
                    }
                }
            }
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
                        salaryStringList.add(
                            DialogBottomDataEntity(
                                salary.name,
                                id = salary.id
                            )
                        )
                    }
                }
            }

            //获取省市数据
            doNetwork(androidContext) {
                OneBoSportApi.bettingStationService.getAreaUniversal()
            }?.let {
                it.let {
                    it.areaAll.countries.forEach {
                        nationalityList.add(DialogBottomDataEntity(it.name, id = it.id))
                    }
                    it.areaAll.cities.forEach {
                        cityList.add(DialogBottomDataEntity(it.name, id = it.id))
                    }
                    it.areaAll.provinces.forEach {
                        provincesList.add(DialogBottomDataEntity(it.name, id = it.id))
                    }
                }
            }

            //获取所有工作性质列表
            doNetwork(androidContext) {
                OneBoSportApi.bettingStationService.getWorksQueryAll()
            }?.let {
                it.let {
                    it.rows.forEach { rowsItem ->
                        workList.add(DialogBottomDataEntity(rowsItem))
                    }
                }
            }

            //用户信息详情查询
            doNetwork(androidContext) {
                OneBoSportApi.bettingStationService.userQueryUserInfoDetails()
            }?.let {
                it.let {
                    _userDetail.postValue(it)
                }
            }
        }
    }

    /**
     * 完善用户信息
     */
    fun userCompleteUserDetails(uide: Uide) {
        launch {
            doNetwork(androidContext) {
                OneBoSportApi.bettingStationService.userCompleteUserDetails(uide)
            }?.let {
                it.let {
                }
            }
        }
    }
}