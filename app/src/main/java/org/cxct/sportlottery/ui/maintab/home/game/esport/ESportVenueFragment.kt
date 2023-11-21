package org.cxct.sportlottery.ui.maintab.home.game.esport

import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.game.sport.SportVenueFragment
import org.cxct.sportlottery.ui.sport.SportTabViewModel

// 体育分类
class ESportVenueFragment: SportVenueFragment<SportTabViewModel, FragmentGamevenueBinding>() {

    private val esportTypeAdapter = ESportTypeAdapter()

    override fun RecyclerView.initGameList() {
        adapter = esportTypeAdapter

        esportTypeAdapter.setOnItemClickListener { _, _, position ->
            val selectItem = esportTypeAdapter.data[position]
            if (selectItem is ESportMatch){
                val pair = matchTabAdapter.data.firstOrNull { it.first == selectItem.name }
                val matchType = if (pair==null) MatchType.IN_PLAY else MatchType.getMatchTypeByStringId(pair.first)
                (activity as MainTabActivity).jumpToESport(matchType,GameType.ES)
            }
        }

        //实现左侧联动
        addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val firstItemPosition = rightManager.findFirstVisibleItemPosition()
                //这块判断dy！=0是防止左侧联动右侧影响
                if (firstItemPosition == -1 || dy == 0) {
                    return
                }

                when (val selectItem = esportTypeAdapter.data[firstItemPosition]) {
                    is ESportGroup -> matchTabAdapter.data.indexOfFirst { it.first == selectItem.name }
                    is ESportMatch -> matchTabAdapter.data.indexOfFirst { it.first == selectItem.name}
                    else -> null
                }?.let {
                    matchTabAdapter.setSelected(it)
                }
            }
        })
    }

    override fun onMenuResult(menuResult: SportMenuData) {
        val menu = menuResult.menu ?: return
        val datas = mutableListOf<Pair<Int, Sport>>()
        val categoryDatas = mutableListOf<Pair<Int, Item?>>()
        menu.inPlay?.let {
            datas.add(Pair(R.string.home_tab_in_play, it))
            categoryDatas.add(Pair(R.string.home_tab_in_play, it.filterESportItem()))
        }
        menuResult.atStart?.let {
            datas.add(Pair(R.string.home_tab_at_start, it))
            categoryDatas.add(Pair(R.string.home_tab_at_start, it.filterESportItem()))
        }
        menu.today?.let {
            datas.add(Pair(R.string.home_tab_today, it))
            categoryDatas.add(Pair(R.string.home_tab_today, it.filterESportItem()))
        }
        menuResult.in12hr?.let {
            datas.add(Pair(R.string.P228, it))
            categoryDatas.add(Pair(R.string.P228, it.filterESportItem()))
        }
        menuResult.in24hr?.let {
            datas.add(Pair(R.string.P229, it))
            categoryDatas.add(Pair(R.string.P229, it.filterESportItem()))
        }
        menu.early?.let {
            datas.add(Pair(R.string.home_tab_early, it))
            categoryDatas.add(Pair(R.string.home_tab_early, it.filterESportItem()))
        }
        menu.parlay?.let {
            datas.add(Pair(R.string.home_tab_parlay, it))
            categoryDatas.add(Pair(R.string.home_tab_parlay, it.filterESportItem()))
        }
        menu.outright?.let {
            datas.add(Pair(R.string.home_tab_outright, it))
            categoryDatas.add(Pair(R.string.home_tab_outright, it.filterESportItem()))
        }

        matchTabAdapter.setNewInstance(datas)
        esportTypeAdapter.setUp(categoryDatas)
    }

    override fun onTabClick(selectItem: Pair<Int, Sport>) {
        esportTypeAdapter.data.forEachIndexed { index, baseNode ->
            if (baseNode is ESportGroup && baseNode.name == selectItem.first){
                rightManager.scrollToPositionWithOffset(index, 0)
                return@forEachIndexed
            }
        }
    }


    private fun Sport.filterESportItem():Item?{
        return items.firstOrNull { it.code ==GameType.ES.key }
    }
}