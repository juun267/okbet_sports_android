package org.cxct.sportlottery.ui.login.signUp.info

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.SingleEvent
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.net.user.UserRepository
import org.cxct.sportlottery.net.user.data.UserBasicInfoResponse
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bettingStation.AreaAll
import org.cxct.sportlottery.network.index.config.SalarySource
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.user.info.UserBasicInfoRequest
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseViewModel

class RegisterInfoViewModel(
    val androidContext: Application,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
) : BaseViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    //登录数据
    var loginResult: LoginResult? = null

    //生日
    var birthdayTimeInput = ""

    //真实姓名
    var realNameInput = ""

    //薪资来源
    var sourceInput = -1

    //省份
    var provinceInput = ""

    //城市
    var cityInput = ""

    //是否完成信息提交
    private var isFinishComplete = false

    //地区信息
    val areaAllList: LiveData<AreaAll>
        get() = _areaAllList
    private var _areaAllList = MutableLiveData<AreaAll>()

    //薪资来源
    val salaryList: LiveData<List<SalarySource>>
        get() = _salaryList
    private var _salaryList = MutableLiveData<List<SalarySource>>()

    //薪资来源string 列表
    val salaryStringList: ArrayList<String> = ArrayList()

    //提交监听
    val commitEvent = SingleEvent<Boolean>()

    //用户基础信息
    val userBasicInfoEvent = SingleEvent<UserBasicInfoResponse.UserBasicData>()


    /**
     * 获取省市数据
     */
    fun getAddressData() {
        launch {
            doNetwork(androidContext) {
                OneBoSportApi.bettingStationService.areaAll()
            }?.let {
                _areaAllList.postValue(it.areaAll)
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
                result.rows?.forEach { salary ->
                    salaryStringList.add(salary.name)
                }
                _salaryList.postValue(result.rows)
            }
        }
    }


    /**
     * 获取用户基本信息
     */
    fun getUserBasicInfo(){
        launch {
            val result=doNetwork(androidContext,false){ OneBoSportApi.indexService.getUserBasicInfo()}
            result?.let { data->

                data.t.birthday?.let {
                    birthdayTimeInput=it
                }
                data.t.city?.let {
                    cityInput=it
                }
                data.t.fullName?.let {
                    realNameInput=it
                }

                data.t.province?.let {
                    provinceInput=it
                }

                data.t.salarySource?.let {
                    sourceInput=it
                }
                userBasicInfoEvent.post(data.t)
            }
        }
    }


    fun getSalaryNameById():String{
        salaryList.value?.let {
            it.forEach {salary->
                if(sourceInput==salary.id){
                    return salary.name
                }
            }
        }
        return ""
    }
    /**
     * 未完善退出，注销登录信息
     */
    fun logout() {
        if (!isFinishComplete) {
            launch {
                loginRepository.logout()
            }
        }
    }

    /**
     * 恢复登录数据
     */
    fun restored() {
        loginResult?.let { result ->
            launch {
                loginRepository.setUpLoginData(result.loginData)
            }
        }
    }


    /**
     * 格式化省份 string list
     */
    fun getProvinceStringList(): ArrayList<String> {
        val provinceStringList = ArrayList<String>()
        _areaAllList.value?.let {
            it.provinces.forEach { province ->
                provinceStringList.add(province.name)
            }
        }
        return provinceStringList
    }

    /**
     * 格式化城市 string  list
     */
    fun getCityStringList(provinceList: ArrayList<String>): List<List<String>> {
        val cityList = ArrayList<ArrayList<String>>()
        _areaAllList.value?.let { all ->

            provinceList.forEach {
                val tempArray = arrayListOf<String>()
                all.provinces.forEach { province ->
                    if (province.name == it) {
                        all.cities.forEach { city ->
                            if (city.provinceId == province.id) {
                                tempArray.add(city.name)
                            }
                        }
                    }
                }
                cityList.add(tempArray)
            }
        }
        return cityList
    }


    /**
     * 提交完善信息
     */
    var commitMsg = ""
    fun commitUserBasicInfo() {
        val request = UserBasicInfoRequest(
            realNameInput,
            birthdayTimeInput,
            sourceInput,
            provinceInput,
            cityInput
        )

        launch {
            val commitResult =
                doNetwork(androidContext) { loginRepository.commitUserBasicInfo(request) }

            if (commitResult != null && commitResult.success) {
                isFinishComplete=true
                commitEvent.post(true)
            } else {
                isFinishComplete=false
//                commitMsg = "${commitResult?.msg}"
                commitMsg = androidContext.getString(R.string.unknown_error)
                commitEvent.post(false)


            }
        }
    }


    /**
     * 检查表单必选项
     */
    fun checkInput(): Boolean {
        return realNameInput.isNotEmpty()
                && birthdayTimeInput.isNotEmpty()
                && sourceInput > -1
                && provinceInput.isNotEmpty()
                && cityInput.isNotEmpty()
    }

    fun setCityData(provincePosition: Int, cityPosition: Int) {
        val provinceList = getProvinceStringList()
        val cityList = getCityStringList(provinceList)
        cityInput = cityList[provincePosition][cityPosition]
    }

    fun setProvinceData(provincePosition: Int) {
        val provinceList = getProvinceStringList()
        provinceInput = provinceList[provincePosition]
    }
}