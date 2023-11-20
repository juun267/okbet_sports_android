package org.cxct.sportlottery.ui.sport

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.callApi
import org.cxct.sportlottery.net.ApiResult
import org.cxct.sportlottery.net.sport.SportRepository
import org.cxct.sportlottery.network.common.ESportType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.CategoryItem
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.maintab.worldcup.FIBAUtil
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.cxct.sportlottery.util.SingleLiveEvent
import org.cxct.sportlottery.util.TimeUtil

class SportTabViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    myFavoriteRepository: MyFavoriteRepository,
    sportMenuRepository: SportMenuRepository,
) : SportListViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    myFavoriteRepository,
    sportMenuRepository
) {

    val sportMenuResult: LiveData<ApiResult<SportMenuData>>
        get() = _sportMenuResult
    private val _sportMenuResult = MutableLiveData<ApiResult<SportMenuData>>()

    private var lastMenuTag = 0L
    private var menuLoading = false
    fun getSportMenuData() {
        if (menuLoading || System.currentTimeMillis() - lastMenuTag < 10_000) {
            return
        }
        menuLoading = true
        callApi({
            SportRepository.getSportMenu(
                TimeUtil.getNowTimeStamp().toString(),
                TimeUtil.getTodayStartTimeStamp().toString())
        }) {
            if (it.succeeded()) {
                it.getData()?.sortSport()
                it.getData()?.makeEsportCategoryItem()
                _sportMenuResult.postValue(it)     // 更新大廳上方球種數量、各MatchType下球種和數量
            }
            menuLoading = false
            lastMenuTag = System.currentTimeMillis()
        }
    }

    private fun SportMenuData.sortSport(): SportMenuData {
        this.menu.inPlay.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.today.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.early.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.cs.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.parlay.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.outright.items.sortedBy { sport ->
            sport.sortNum
        }
        this.atStart.items.sortedBy { sport ->
            sport.sortNum
        }
        this.menu.eps?.items?.sortedBy { sport ->
            sport.sortNum
        }

        return this
    }
    private fun SportMenuData.makeEsportCategoryItem(): SportMenuData {
        this.menu.inPlay.buildCategoryList()
        this.atStart.buildCategoryList()
        this.menu.today.buildCategoryList()
        this.in12hr.buildCategoryList()
        this.in24hr.buildCategoryList()
        this.menu.early.buildCategoryList()
        this.menu.parlay.buildCategoryList()
        this.menu.outright.buildCategoryList()
        return this
    }
    private fun Sport.buildCategoryList(){
        items.firstOrNull { it.code == GameType.ES.key }?.buildCategoryList()
    }
    /**
     * 电竞others需要前端自己拼装数据
     */
    private fun Item.buildCategoryList(){
        var otherCodeArr = mutableListOf<String>()
        var otherNum = 0
        val newCategoryList = mutableListOf<CategoryItem>()
        this.categoryList?.forEach{
            when(it.code){
                ESportType.DOTA.key,
                ESportType.LOL.key,
                ESportType.CS.key,
                ESportType.KOG.key,
                ESportType.LOLWR.key,
                ESportType.VLR.key,
                ESportType.ML.key,
                ESportType.COD.key,
                ESportType.PUBG.key,
                ESportType.APL.key,
                ->{
                    it.categoryCodeList = mutableListOf(it.code)
                    newCategoryList.add(it)
                }
                else-> {
                    otherCodeArr.add(it.code)
                    otherNum += it.num
                }
            }
        }
        if (otherNum>0){
            val othersCategoryItem = CategoryItem(
                id = ESportType.OTHERS.key,
                code = ESportType.OTHERS.key,
                name = androidContext.getString(R.string.other),
                num = otherNum,
                sort = 999,
            ).apply {
                categoryCodeList = otherCodeArr.toList()
            }
            newCategoryList.add(othersCategoryItem)
        }
        this.categoryList = newCategoryList
    }
}