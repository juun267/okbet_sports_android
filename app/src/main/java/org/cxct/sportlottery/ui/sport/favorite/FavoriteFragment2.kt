package org.cxct.sportlottery.ui.sport.favorite

import android.util.Log
import android.view.View
import com.google.gson.Gson
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.sport.list.SportListFragment2
import org.cxct.sportlottery.ui.sport.list.SportListViewModel

class FavoriteFragment2: SportListFragment2<SportListViewModel, FragmentSportList2Binding>() {

    override var matchType = MatchType.MY_EVENT
    private var currentFavoriteList = listOf<Item>()

    override fun onInitView(view: View) {
        super.onInitView(view)
        binding.ivFilter.gone()
    }

    override fun onInitData() {
        updateSportType(currentFavoriteList)
    }

    override fun load(item: Item) {
        sportLeagueAdapter2.setNewInstance(item.leagueOddsList.toMutableList())
        setMatchInfo(item.name, item.num.toString())
    }

    override fun observeSportList() {

    }

    fun setFavoriteData(favoriteLeagues: List<Item>) {
        if(currentFavoriteList == favoriteLeagues) {
            return
        }
        currentFavoriteList = favoriteLeagues
        if (isAdded) {
            updateSportType(favoriteLeagues)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gameTypeAdapter.setNewInstance(null)
    }
}