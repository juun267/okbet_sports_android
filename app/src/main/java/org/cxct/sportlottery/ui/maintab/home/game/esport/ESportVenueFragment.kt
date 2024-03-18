package org.cxct.sportlottery.ui.maintab.home.game.esport

import androidx.annotation.StringRes
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
                val matchType = if (pair == null) MatchType.IN_PLAY else MatchType.getMatchTypeByStringId(pair.first)
                (activity as MainTabActivity).jumpToESport(matchType, selectItem.item.code)
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
        if (lastMenuData == menuResult) {
            return
        }
        val menu = menuResult.menu ?: return
        val datas = mutableListOf<Pair<Int, Sport>>()
        val categoryDatas = mutableListOf<Pair<Int, Item>>()

        menu.inPlay?.let { assembleData(R.string.home_tab_in_play, it, datas, categoryDatas) }
        menuResult.atStart?.let { assembleData(R.string.home_tab_at_start, it, datas, categoryDatas) }
        menu.today?.let { assembleData(R.string.home_tab_today, it, datas, categoryDatas) }
//        menuResult.in12hr?.let { assembleData(R.string.P228, it, datas, categoryDatas) }
//        menuResult.in24hr?.let { assembleData(R.string.P229, it, datas, categoryDatas) }
        menu.early?.let { assembleData(R.string.home_tab_early, it, datas, categoryDatas) }
        menu.parlay?.let { assembleData(R.string.home_tab_parlay, it, datas, categoryDatas) }
        menu.outright?.let { assembleData(R.string.home_tab_outright, it, datas, categoryDatas) }

        matchTabAdapter.setNewInstance(datas)
        esportTypeAdapter.setUp(categoryDatas)
    }


    private fun assembleData(@StringRes name: Int,
                             sport: Sport,
                             tabs: MutableList<Pair<Int, Sport>>,
                             categoryDatas: MutableList<Pair<Int, Item>>) {

        val eSport = sport.items.firstOrNull { it.code == GameType.ES.key }
        if (eSport == null || eSport.num <= 0) {
            return
        }
        sport.num = eSport.num
        tabs.add(Pair(name, sport))
        categoryDatas.add(Pair(name, eSport))
    }

    override fun onTabClick(selectItem: Pair<Int, Sport>) {
        esportTypeAdapter.data.forEachIndexed { index, baseNode ->
            if (baseNode is ESportGroup && baseNode.name == selectItem.first){
                rightManager.scrollToPositionWithOffset(index, 0)
                return@forEachIndexed
            }
        }
    }
}