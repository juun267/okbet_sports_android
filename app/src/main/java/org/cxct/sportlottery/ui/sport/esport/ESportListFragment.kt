package org.cxct.sportlottery.ui.sport.esport

import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.sport.list.SportListFragment2
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.cxct.sportlottery.ui.sport.list.adapter.SportLeagueAdapter2
import org.cxct.sportlottery.util.loginedRun
import java.util.ArrayList

class ESportListFragment:SportListFragment2<SportListViewModel, FragmentSportList2Binding>() {

    override var gameType = GameType.ES.key
    override val sportLeagueAdapter2 by lazy {
        SportLeagueAdapter2(matchType,
            this,
            esportTheme = true,
            onNodeExpand = { resubscribeChannel(200) },
            onOddClick = this,
            onFavorite = { matchId ->
                loginedRun(context()) { viewModel.pinFavorite(FavoriteType.MATCH, matchId) }
            })
    }
    override fun onInitView(view: View) {
        super.onInitView(view)
        binding.sportTypeList.setBackgroundResource(R.drawable.bg_esport_game)
        binding.linOpt.setBackgroundResource(R.drawable.bg_white_alpha70_radius_8_top)
        binding.gameList.setBackgroundResource(R.color.color_FFFFFF)
    }
    override fun reload(matchType: MatchType, gameType: String?) {
        super.reload(matchType, GameType.ES.key)
    }
    override fun load(
        item: Item,
        selectLeagueIdList: ArrayList<String>,
        selectMatchIdList: ArrayList<String>
    ) {
        super.load(item, selectLeagueIdList, selectMatchIdList)
    }

}