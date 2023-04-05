package org.cxct.sportlottery.ui.login.signUp.info

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bettingStation.AreaAll
import org.cxct.sportlottery.network.index.config.SalarySource
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseViewModel

class RegisterInfoViewModel(
    val androidContext: Application,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
) : BaseViewModel(loginRepository, betInfoRepository, infoCenterRepository) {

    //生日
    var birthdayTimeInput = 0L

    //真实姓名
    var realNameInput = ""

    //薪资来源
    var sourceInput = ""

    //地址
    var addressInput = ""

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
     * 获取配置，提取薪资来源配置
     */
    fun getConfig(){
        onNet {
            doNetwork(androidContext){
                OneBoSportApi.indexService.getConfig()
            }?.let {
                it.configData?.salarySource?.forEach {salary->
                    salaryStringList.add(salary.name)
                }
                _salaryList.postValue(it.configData?.salarySource)
            }
        }
    }

    /**
     * 未完成信息完善，注销登录信息
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


}