package org.cxct.sportlottery.ui.login.signUp.info

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.SingleEvent
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.net.user.data.UserBasicInfoResponse
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bettingStation.AreaAll
import org.cxct.sportlottery.network.index.config.SalarySource
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.network.user.info.UserBasicInfoRequest
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.VerifyConstUtil

class RegisterInfoViewModel(
    androidContext: Application
) : BaseSocketViewModel(androidContext) {

    //登录数据
    var loginResult: LoginResult? = null

    //生日
    var birthdayTimeInput = ""

    //手机号
    var phoneNumberInput = ""
    var phoneEnable = false

    //邮箱
    var emailInput=""
    var emailEnable=false

    //薪资来源
    var sourceInput = -1

    //省份
    var provinceInput = ""

    //城市
    var cityInput = ""

    // 名
    var firstName = ""

    // 中间名
    var middleName = ""
    var noMiddleName = false

    // 姓
    var lastName = ""

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
                OneBoSportApi.bettingStationService.getAreaUniversal()
            }?.let {
                it.let {
                    _areaAllList.postValue(it.areaAll)
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
                        salaryStringList.add(salary.name)
                    }
                    _salaryList.postValue(it)
                }
            }
        }
    }


    /**
     * 获取用户基本信息
     */
    var filledBirthday=false
    var filledPhone=false
    var filledEmail=false
    var filledProvince=false
    var filledCity=false
    var filledSalary=false
    fun getUserBasicInfo(){
        launch {
            val result=doNetwork(androidContext){ OneBoSportApi.indexService.getUserBasicInfo()}
            result?.let { data->


                data.t.birthday?.let {
                    birthdayTimeInput=it
                    if(it.isNotEmpty()){
//                        filledBirthday=true
                    }
                }
                data.t.city?.let {
                    cityInput=it
                    if(it.isNotEmpty()){
//                        filledCity=true
                    }
                }

                data.t.province?.let {
                    provinceInput=it
                    if(it.isNotEmpty()){
//                        filledProvince=true
                    }
                }

                data.t.salarySource?.let {
                    sourceInput=it
                    if(it>-1){
//                        filledSalary=true
                    }
                }
                data.t.phone?.let {
                    phoneNumberInput=it
                    if(it.isNotEmpty()){
                        filledPhone = VerifyConstUtil.verifyPhone(it)
                    }
                }

                data.t.email?.let {
                    emailInput=it
                    if(it.isNotEmpty()){
                        filledEmail=VerifyConstUtil.verifyMail(it)
                    }
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
//    fun getCityStringList(provinceList: ArrayList<String>): List<List<String>> {
//        val cityList = ArrayList<ArrayList<String>>()
//        _areaAllList.value?.let { all ->
//
//            provinceList.forEach {
//                val tempArray = arrayListOf<String>()
//                all.provinces.forEach { province ->
//                    if (province.name == it) {
//                        all.cities.forEach { city ->
//                            if (city.provinceId == province.id) {
//                                tempArray.add(city.name)
//                            }
//                        }
//                    }
//                }
//                cityList.add(tempArray)
//            }
//        }
//        return cityList
//    }

    fun getCityStringListByProvince():ArrayList<String>{
        val cityList=ArrayList<String>()
        _areaAllList.value?.let { all ->
            all.provinces.forEach {province->
                if(province.name==provinceInput){
                    all.cities.forEach {city->
                        if(city.provinceId==province.id){
                            cityList.add(city.name)
                        }
                    }
                    return cityList
                }
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
            "$firstName $middleName $lastName",
            firstName,
            middleName,
            lastName,
            birthdayTimeInput,
            sourceInput,
            provinceInput,
            cityInput,
            phoneNumberInput,
            emailInput
        )

        launch {
            val commitResult =
                doNetwork(androidContext) { LoginRepository.commitUserBasicInfo(request) }

            if (commitResult != null && commitResult.success) {
                isFinishComplete=true
                commitEvent.post(true)
                GlobalScope.launch { runWithCatch { UserInfoRepository.getUserInfo() } }
            } else {
                isFinishComplete=false
                commitMsg = "${commitResult?.msg}"
//                commitMsg = androidContext.getString(R.string.unknown_error)
                commitEvent.post(false)
            }
        }
    }


    /**
     * 检查表单必选项
     */
    fun checkInput(): Boolean {
        return birthdayTimeInput.isNotEmpty()
                && sourceInput > -1
                && phoneEnable
                && emailEnable
                && provinceInput.isNotEmpty()
                && cityInput.isNotEmpty()

    }

    fun setCityData( cityPosition: Int) {
        val cityList =getCityStringListByProvince()
        if(cityList.size==0||cityPosition>cityList.size-1){
            cityInput = ""
        }else{
            cityInput = cityList[cityPosition]
        }

    }

    fun setProvinceData(provincePosition: Int) {
        val provinceList = getProvinceStringList()
        if(provincePosition>provinceList.size-1){
            return
        }
        provinceInput = provinceList[provincePosition]
    }



    /**
     * 手机号/邮箱/用户名
     */
    fun checkPhone(phoneNumber: String): String ?{
        var msg:String?=null
        if(VerifyConstUtil.verifyPhone(phoneNumber) ){
            phoneEnable=true
        }else{
            msg=LocalUtils.getString(R.string.N177)
        }
        return msg
    }

    fun checkEmail(email: String): String? {
        var msg:String?=null
        if(VerifyConstUtil.verifyMail(email)){
            emailEnable=true
        }else{
            emailEnable=false
            msg=LocalUtils.getString(R.string.N889)
        }
        return msg
    }
}