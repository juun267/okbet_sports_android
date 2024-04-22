package org.cxct.sportlottery.ui.profileCenter.vip

import android.app.Application
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.common.extentions.toast
import org.cxct.sportlottery.net.user.UserRepository
import org.cxct.sportlottery.net.user.data.UserVip
import org.cxct.sportlottery.net.user.data.VipDetail
import org.cxct.sportlottery.net.user.data.VipRedenpApplyResult
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.SingleLiveEvent

class VipViewModel(
    androidContext: Application
) : BaseViewModel(
    androidContext
) {
    val userVipEvent = SingleLiveEvent<UserVip>()
    val vipDetailEvent = SingleLiveEvent<VipDetail?>()
    val vipRewardEvent = SingleLiveEvent<String>()
    val applyResultEvent = SingleLiveEvent<VipRedenpApplyResult?>()

    fun getUserVip(){
        callApi({UserRepository.getUserVip()}){
            if (it.succeeded()){
                it.getData()?.let {
                    userVipEvent.postValue(it)
                }
            }else{
                toast(it.msg)
            }
        }
    }
    fun getVipDetail() {
        callApi({ UserRepository.getVipDetail() }) {
            if (it.succeeded()) {
                vipDetailEvent.postValue(it?.getData())
            } else {
                toast(it.msg)
            }
        }
    }
    fun vipReward(activityId: Int, rewardType: Int, levelV2Id: Int){
        callApi({UserRepository.vipReward(activityId,rewardType,levelV2Id)}){
            if (it.succeeded()){
                vipRewardEvent.postValue(it?.getData())
            }else{
                toast(it.msg)
            }
        }
    }
    fun vipRedenpApply(levelV2Id: Int){
        callApi({UserRepository.vipRedenpApply(levelV2Id)}){
            if (it.succeeded()){
                applyResultEvent.postValue(it?.getData())
            }else{
                toast(it.msg)
            }
        }
    }
}