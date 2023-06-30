package org.cxct.sportlottery.ui.sport.favorite

import android.view.View
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.toStringS
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.sport.list.SportListFragment2
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import java.util.ArrayList

class FavoriteFragment2: SportListFragment2<SportListViewModel, FragmentSportList2Binding>() {

    override var matchType = MatchType.MY_EVENT
    private var currentFavoriteList = listOf<Item>()
    private var haveData = false
    override fun observeSportList() { }

    override fun onInitView(view: View) {
        super.onInitView(view)
        binding.ivFilter.gone()
    }

    override fun onInitData() {
        if (haveData) {
            updateSportType(currentFavoriteList)
        } else {
            showLoading()
        }
    }

    override fun load(item: Item, selectMatchIdList: ArrayList<String>) {
        setSportDataList(item.leagueOddsList?.toMutableList())
        setMatchInfo(item.name, item.leagueOddsList?.size.toStringS("0"))
    }

    fun setFavoriteData(favoriteLeagues: List<Item>) {
        currentFavoriteList = favoriteLeagues
        if (!haveData && isAdded) {
            dismissLoading()
            updateSportType(favoriteLeagues)
        }
        haveData = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        haveData = false
        currentFavoriteList = listOf()
        gameTypeAdapter.setNewInstance(null)
        setMatchInfo("", "")
    }
}