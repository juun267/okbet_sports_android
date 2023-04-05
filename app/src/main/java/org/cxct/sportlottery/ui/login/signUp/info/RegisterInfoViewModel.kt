package org.cxct.sportlottery.ui.login.signUp.info

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bettingStation.AreaAll
import org.cxct.sportlottery.network.index.config.SalarySource
import org.cxct.sportlottery.network.index.login.LoginData
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
    var loginData:LoginData?=null

    //生日
    var birthdayTimeInput = ""

    //真实姓名
    var realNameInput = ""

    //薪资来源
    var sourceInput = 0

    //省份
    var provinceInput = ""

    //城市
    var cityInput=""

    //是否完成信息提交
    var isFinishComplete=false

    //地区信息
    val areaAllList: LiveData<AreaAll>
        get() = _areaAllList
    private var _areaAllList = MutableLiveData<AreaAll>()

    //薪资来源
    val salaryList: LiveData<List<SalarySource>>
        get() = _salaryList
    private var _salaryList = MutableLiveData<List<SalarySource>>()
    val salaryStringList:ArrayList<String> = ArrayList()


    /**
     * 获取省市数据
     */
    fun getAddressData() {
        onNet {
            val result = doNetwork(androidContext) {
                OneBoSportApi.bettingStationService.areaAll()
            }

            onMain {
                result?.let {
                    _areaAllList.postValue(result.areaAll)
                }
            }

        }
    }

    /**
     * 获取收入来源选项
     */
    fun getUserSalaryList(){
        viewModelScope.launch {
            doNetwork(androidContext){
                OneBoSportApi.indexService.getUserSalaryList()
            }?.let {result->
                result.rows?.forEach {salary->
                    salaryStringList.add(salary.name)
                }
                _salaryList.postValue(result.rows)
            }
        }
    }

    /**
     * 获取配置，提取薪资来源配置
     */
//    fun getConfig(){
//        onNet {
//            doNetwork(androidContext){
//                OneBoSportApi.indexService.getConfig()
//            }?.let {result->
//                result.configData?.salarySource?.forEach {salary->
//                    salaryStringList.add(salary.name)
//                }
//                onMain {
//                    _salaryList.postValue(result.configData?.salarySource)
//                }
//            }
//        }
//    }

    /**
     * 未完善退出，注销登录信息
     */
    fun logOut(){
        if(!isFinishComplete){
            onMain {
                loginRepository.logout()
            }
        }
    }



    /**
     * 获取省份 list
     */

    fun getProvinceStringList():ArrayList<String> {
        val provinceStringList = ArrayList<String>()
        _areaAllList.value?.let {
            it.provinces.forEach { province ->
                provinceStringList.add(province.name)
            }
        }
        return provinceStringList
    }

    /**
     * 获取城市 list
     */

    fun getCityStringList(provinceList:ArrayList<String>):List<List<String>> {
        val cityList = ArrayList<ArrayList<String>>()
        _areaAllList.value?.let {all->

            provinceList.forEach {
                val tempArray= arrayListOf<String>()
                all.provinces.forEach { province ->
                    if(province.name==it){
                        all.cities.forEach {city->
                            if(city.provinceId==province.id){
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
    fun commitUserBasicInfo(){
        val request=UserBasicInfoRequest(realNameInput,birthdayTimeInput,sourceInput,provinceInput,cityInput)
        viewModelScope.launch {
            val commitResult=doNetwork(androidContext){ loginRepository.commitUserBasicInfo(request)}
            commitResult?.let {

            }
        }
    }

}