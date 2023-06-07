package org.cxct.sportlottery.ui.maintab.menu.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.MyFavoriteRepository
import org.cxct.sportlottery.repository.SportMenuRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.maintab.entity.NodeBean
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel

class SportLeftMenuViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
    private val sportMenuRepository: SportMenuRepository, )
    : MainHomeViewModel(androidContext, userInfoRepository, loginRepository, betInfoRepository, infoCenterRepository, favoriteRepository,sportMenuRepository) {




        fun isLogin():Boolean{
            return loginRepository.isLogined()
        }


    fun getHandicapData():ArrayList<NodeBean>{
        val dataList= arrayListOf<NodeBean>()
        dataList.add(NodeBean("European Handicap",true))
        dataList.add(NodeBean("European Handicap"))
        dataList.add(NodeBean("European Handicap"))
        return dataList
    }


    /**
     * 获取盘口配置
     */
    fun getHandicapConfig():ArrayList<NodeBean>{
        val handicapConfig=sConfigData?.handicapShow?.split(",")?.filter { it.isNotEmpty() }
        val handicapList= arrayListOf<NodeBean>()
        handicapConfig?.forEach {
            val tempNode=NodeBean(it)
            handicapList.add(tempNode)
        }
        return handicapList
    }
}