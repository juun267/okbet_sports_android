package org.cxct.sportlottery.ui.maintab.menu.viewmodel

import android.app.Application
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.HandicapType
import org.cxct.sportlottery.repository.InfoCenterRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.MyFavoriteRepository
import org.cxct.sportlottery.repository.SportMenuRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.maintab.entity.NodeBean
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LanguageManager.makeUseLanguage

class SportLeftMenuViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository,
    private val sportMenuRepository: SportMenuRepository,
) : MainHomeViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository,
    sportMenuRepository
) {


    fun isLogin(): Boolean {
        return loginRepository.isLogined()
    }


    fun getHandicapData(): ArrayList<NodeBean> {
        val dataList = arrayListOf<NodeBean>()
        dataList.add(NodeBean("European Handicap", "",true))
        dataList.add(NodeBean("European Handicap"))
        dataList.add(NodeBean("European Handicap"))
        return dataList
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
            if(it.key==LanguageManager.getSelectLanguageName()){
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
}