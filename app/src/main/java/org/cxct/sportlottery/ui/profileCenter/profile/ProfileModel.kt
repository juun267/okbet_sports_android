package org.cxct.sportlottery.ui.profileCenter.profile

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bettingStation.AreaAllResult
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

    //薪资来源string 列表
    val salaryStringList: ArrayList<DialogBottomDataEntity> = ArrayList()

    //地区信息
    //国籍 列表
    val nationalityList: ArrayList<DialogBottomDataEntity> = ArrayList()

    //省 列表
    val provincesList: ArrayList<DialogBottomDataEntity> = ArrayList()

    //市区 列表
    val cityList: ArrayList<DialogBottomDataEntity> = ArrayList()

    //省 列表
    val provincesPList: ArrayList<DialogBottomDataEntity> = ArrayList()

    //市区 列表
    val cityPList: ArrayList<DialogBottomDataEntity> = ArrayList()

    //职业 列表
    val workList: ArrayList<DialogBottomDataEntity> = ArrayList()

    //性别：男,女,其他
    val genderList = arrayOf(DialogBottomDataEntity())

    private val _userDetail = MutableLiveData<UserInfoDetailsEntity>()
    val userDetail: LiveData<UserInfoDetailsEntity> //使用者餘額
        get() = _userDetail

    var areaData: AreaAllResult? = null

    fun uploadImage(uploadImgRequest: UploadImgRequest) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                avatarRepository.uploadImage(uploadImgRequest)
            }
        }
    }

    fun getSalaryName(id: Int,default: String): String {
        var sdf = salaryStringList.find { it.id == id }
        return if (sdf == null || sdf.name.isEmpty()) {
            default
        } else {
            salaryStringList.find { it.id == id }!!.name
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
        if (!salaryStringList.isEmpty()) {

            launch {
                //用户信息详情查询
                doNetwork(androidContext) {
                    OneBoSportApi.userService.userQueryUserInfoDetails()
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
                OneBoSportApi.userService.getAreaUniversal()
            }?.let {
                areaData = it
                var dbde = it.areaAll.countries.find { it.name.contains("PHILIPPINES") }
                dbde?.let {
                    nationalityList.add(DialogBottomDataEntity(dbde.nationality, id = dbde.id))
                }
                it.areaAll.countries.remove(dbde)
                var sortList = it.areaAll.countries.sortedBy { it.nationality.substring(0, 1) }
                sortList.forEach {
                    nationalityList.add(DialogBottomDataEntity(it.nationality, id = it.id))
                }
                it.areaAll.provinces.forEach {
                    provincesList.add(DialogBottomDataEntity(it.name, id = it.id))
                    provincesPList.add(DialogBottomDataEntity(it.name, id = it.id))
                }
            }

            //获取所有工作性质列表
            doNetwork(androidContext) {
                OneBoSportApi.userService.getWorksQueryAll()
            }?.let {
                it.let {
                    it.rows.forEach { rowsItem ->
                        workList.add(DialogBottomDataEntity(rowsItem))
                    }
                }
            }

            //用户信息详情查询
            doNetwork(androidContext) {
                OneBoSportApi.userService.userQueryUserInfoDetails()
            }?.let {
                _userDetail.postValue(it)
                if (areaData == null) {
                    return@launch
                }

                it.t.province?.let { itarea ->
                    var provincesId = areaData!!.areaAll.provinces.find { it.name == itarea }?.id
                    if (provincesId != null) {
                        updateCityData(provincesId)
                    }
                }
                it.t.permanentProvince?.let { itarea ->
                    var provincesId = areaData!!.areaAll.provinces.find { it.name == itarea }?.id
                    if (provincesId != null) {
                        updateCityPData(provincesId)
                    }
                }

            }
        }
    }

    fun updateCityData(id: Int) {
        if (areaData == null) {
            return
        }
        cityList.clear()
        areaData!!.areaAll.cities.filter { it.provinceId == id }.forEach {
            cityList.add(DialogBottomDataEntity(it.name, id = it.id))
        }
    }

    fun updateCityPData(id: Int) {
        if (areaData == null) {
            return
        }
        cityPList.clear()
        areaData!!.areaAll.cities.filter { it.provinceId == id }.forEach {
            cityPList.add(DialogBottomDataEntity(it.name, id = it.id))
        }
    }

    /**
     * 完善用户信息
     */
    fun userCompleteUserDetails(uide: Uide) {
        launch {
            doNetwork(androidContext) {
                OneBoSportApi.userService.userCompleteUserDetails(uide)
            }?.let {
                it.let {
                }
            }
        }
    }
}