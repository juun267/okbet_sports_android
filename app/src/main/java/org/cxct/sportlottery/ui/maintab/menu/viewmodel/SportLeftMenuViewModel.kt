@file:Suppress("DEPRECATION")

package org.cxct.sportlottery.ui.maintab.menu.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.net.sport.SportRepository
import org.cxct.sportlottery.net.sport.data.RecommendLeague
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.bet.list.BetListRequest
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.user.odds.OddsChangeOptionRequest
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.HandicapType
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.FavoriteRepository
import org.cxct.sportlottery.repository.SportMenuRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.betRecord.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.maintab.entity.NodeBean
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.LanguageManager.makeUseLanguage

class SportLeftMenuViewModel(
    androidContext: Application
) : MainHomeViewModel(
    androidContext
) {


    val betCountEvent=SingleLiveEvent<Long>()
    var totalCount=0

    val inplayList = SingleLiveEvent<List<Item>>()

    val recommendLeague = SingleLiveEvent<List<RecommendLeague>>()

    val sportCountEvent=SingleLiveEvent<Int>()

    fun isLogin(): Boolean {
        return LoginRepository.isLogined()
    }


    /**
     * 获取未截单数量
     */
    fun getBetRecordCount() {
        val betListRequest = BetListRequest(
            championOnly = 0,
            statusList = listOf(0,1), //全部注單，(0:待成立, 1:未結算)
            page = 1,
            gameType = "",
            pageSize = AccountHistoryViewModel.PAGE_SIZE
        )
        viewModelScope.launch {
            val result= doNetwork(androidContext) {
                OneBoSportApi.betService.getBetList(betListRequest)
            } ?: return@launch

            result.total?.let {
                totalCount=it
                betCountEvent.postValue(System.currentTimeMillis())
            }
        }
    }


    /**
     * 获取盘口配置
     */
    fun getHandicapConfig(): ArrayList<NodeBean> {
        val handicapConfig = sConfigData?.handicapShow?.split(",")?.filter { it.isNotEmpty() }
        //已配置的盘口
        var handicapType: OddsType?=MultiLanguagesApplication.mInstance.mOddsType.value
        if(handicapType==null){
            handicapType=OddsType.EU
        }
        //初始化盘口列表
        val handicapList = arrayListOf<NodeBean>()
        handicapConfig?.forEach {
            //格式化盘口名称
            val tempNode = NodeBean(formatHandicapName(it),it)
            //如果是已配置的盘口
            if(handicapType.code==it){
                //设置选中
                tempNode.select=true
            }
            handicapList.add(tempNode)
        }
        return handicapList
    }


    /**
     * 更换盘口
     */
    fun changeHandicap(codeName:String){
        when (codeName) {
            HandicapType.EU.name -> {
                saveOddsType(OddsType.EU)
            }
            HandicapType.HK.name -> {
                saveOddsType(OddsType.HK)
            }
            HandicapType.MY.name -> {
                saveOddsType(OddsType.MYS)
            }
            HandicapType.ID.name -> {
                saveOddsType(OddsType.IDN)
            }
        }
    }


    /**
     * 格式化盘口名称
     */
    private fun formatHandicapName(handicapCode:String):String{
        var formatName = ""
        when (handicapCode) {
            HandicapType.EU.name -> {
                formatName = androidContext.getString(R.string.odd_type_eu)
            }
            HandicapType.HK.name -> {
                formatName = androidContext.getString(R.string.odd_type_hk)
            }
            HandicapType.MY.name -> {
                formatName = androidContext.getString(R.string.odd_type_mys)
            }
            HandicapType.ID.name -> {
                formatName = androidContext.getString(R.string.odd_type_idn)
            }
        }
        return formatName
    }


    /**
     * 获取语言配置
     */
    fun getLanguageConfig():ArrayList<NodeBean>{
        val languageList=makeUseLanguage()
        val langNodeList = arrayListOf<NodeBean>()
        languageList.forEach {
            val langNode=formatLanguageData(it)
            if(it.key== LanguageManager.getSelectLanguage(androidContext).key){
                langNode.select=true
            }
            langNodeList.add(langNode)
        }
        return langNodeList
    }


    /**
     * 更换语言配置
     */
    fun changeLanguage(language:LanguageManager.Language){
        if (LanguageManager.getSelectLanguageName() != language.key) {
            LanguageManager.saveSelectLanguage(androidContext, language)
        }
    }


    private fun formatLanguageData(item: LanguageManager.Language):NodeBean{
        when (item) {
            LanguageManager.Language.ZH -> {
                return NodeBean(androidContext.getString(R.string.language_cn),item,false,R.drawable.ic_flag_cn)
            }
            LanguageManager.Language.VI -> {
                return NodeBean(androidContext.getString(R.string.language_vi),item,false,R.drawable.ic_flag_vi)
            }
            LanguageManager.Language.TH -> {
                return NodeBean(androidContext.getString(R.string.language_th),item,false,R.drawable.ic_flag_th)
            }
            LanguageManager.Language.PHI ->{
                return NodeBean(androidContext.getString(R.string.language_phi),item,false,R.drawable.ic_flag_phi)
            }
            else -> {
                return NodeBean(androidContext.getString(R.string.language_en),item,false,R.drawable.ic_flag_en)
            }
        }
    }


    /**
     * 获取投注方式
     */
    fun getBettingRulesData():ArrayList<NodeBean>{
        val bettingNodeList = arrayListOf<NodeBean>()
        val userInfo = MultiLanguagesApplication.getInstance()?.userInfo()

        bettingNodeList
            .add(NodeBean(LocalUtils.getString(R.string.accept_any_change_in_odds), OddsModeUtil.accept_any_odds))
        bettingNodeList
            .add(NodeBean(LocalUtils.getString(R.string.accept_better_change_in_odds), OddsModeUtil.accept_better_odds))
        bettingNodeList
            .add(NodeBean(LocalUtils.getString(R.string.accept_never_change_in_odds), OddsModeUtil.never_accept_odds_change))

        //旧的投注方式
        when (userInfo?.oddsChangeOption ?: 0) {
            OddsModeUtil.accept_any_odds -> bettingNodeList[0].select=true
            OddsModeUtil.accept_better_odds -> bettingNodeList[1].select=true
            OddsModeUtil.never_accept_odds_change -> bettingNodeList[2].select=true
        }

        return bettingNodeList
    }

    /**
     * 更改投注方式
     */
    fun updateOddsChangeOption(option: Int) {
        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.userService.oddsChangeOption(
                    OddsChangeOptionRequest(option)
                )
            }?.let {
                //更新到用户设置
                UserInfoRepository.updateOddsChangeOption(option)
            }
        }
    }

    fun getInPlayList() {
        viewModelScope.launch {
            doNetwork(androidContext) {
                SportMenuRepository.getSportMenu(
                    TimeUtil.getNowTimeStamp().toString(),
                    TimeUtil.getTodayStartTimeStamp().toString()
                )
            }?.sportMenuData?.let { sportMenuList ->
                inplayList.postValue(sportMenuList.menu.inPlay.items)
            }
        }
    }
    fun getRecommendLeague() {
        callApi({SportRepository.getRecommendLeague()}){
            recommendLeague.postValue(it.getData())
        }
    }

    /**
     * 滚球+早盘+冠军
     */
    fun getSportCount() {
        callApi({SportRepository.getSportMenu(true)}){
            it.getData()?.menu?.let {
                sportCountEvent.postValue(it.inPlay.num+it.early.num+it.outright.num)
            }
        }
    }

}