package org.cxct.sportlottery.ui.maintab.home.game.sport

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.network.sport.SportMenuData
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.game.GameVenueFragment
import org.cxct.sportlottery.ui.sport.SportTabViewModel

// 体育分类
open class SportVenueFragment<VM : BaseViewModel, VB>: GameVenueFragment<SportTabViewModel, FragmentGamevenueBinding>() {

    protected val matchTabAdapter = MatchTableAdapter(::onTabClick)
    private val leftManager by lazy { LinearLayoutManager(requireContext() ,RecyclerView.VERTICAL,false) }
    protected val rightManager by lazy { LinearLayoutManager(requireContext(), RecyclerView.VERTICAL,false) }

    override fun onInitView(view: View) {
        super.onInitView(view)
        binding.rvcGameType.layoutManager = leftManager
        binding.rvcGameType.adapter = matchTabAdapter
        binding.rvcGameList.layoutManager = rightManager
        binding.rvcGameList.initGameList()
    }

    override fun onBindViewStatus(view: View) {
        initObserver()
    }

    override fun onInitData() {
        loading()
        viewModel.getSportMenuData()
    }

    private fun initObserver() {
        viewModel.sportMenuResult.observe(viewLifecycleOwner) {
            hideLoading()
            it.getData()?.let { onMenuResult(it) }
        }
    }

    protected open fun RecyclerView.initGameList() {
        val sportTypeAdapter = SportTypeAdapter()
        binding.rvcGameList.adapter = sportTypeAdapter
        sportTypeAdapter.setOnItemClickListener { _, _, position ->
            val selectItem = sportTypeAdapter.data[position]
            if (selectItem is Item){
                val pair=matchTabAdapter.data.firstOrNull { it.second.items.contains(selectItem) }
                val matchType = if (pair==null) MatchType.IN_PLAY else MatchType.getMatchTypeByStringId(pair.first)
                val gameType = GameType.getGameType(selectItem.code)
                (activity as MainTabActivity).jumpToTheSport(matchType,gameType)
            }
        }

        //实现左侧联动
        addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val firstItemPosition = rightManager.findFirstVisibleItemPosition()
                //这块判断dy！=0是防止左侧联动右侧影响
                if (firstItemPosition == -1 && dy == 0) {
                    return
                }

                 when (val selectItem = sportTypeAdapter.data[firstItemPosition]) {
                    is SportGroup -> matchTabAdapter.data.indexOfFirst { selectItem.name == it.first }
                    is Item -> matchTabAdapter.data.indexOfFirst { it.second.items.contains(selectItem)}
                    else -> null
                }?.let {
                    matchTabAdapter.setSelected(it)
                }
            }
        })
    }

    protected open fun onMenuResult(menuResult: SportMenuData) {
        val menu = menuResult.menu ?: return
        val datas = mutableListOf<Pair<Int, Sport>>()
        menu.inPlay?.let { datas.add(Pair(R.string.home_tab_in_play, it)) }
        menuResult.atStart?.let { datas.add(Pair(R.string.home_tab_at_start, it)) }
        menu.today?.let { datas.add(Pair(R.string.home_tab_today, it)) }
        menuResult.in12hr?.let { datas.add(Pair(R.string.P228, it)) }
        menuResult.in24hr?.let { datas.add(Pair(R.string.P229, it)) }
        menu.early?.let { datas.add(Pair(R.string.home_tab_early, it)) }
        menu.parlay?.let { datas.add(Pair(R.string.home_tab_parlay, it)) }
        menu.outright?.let { datas.add(Pair(R.string.home_tab_outright, it)) }
        matchTabAdapter.setNewInstance(datas)
        (binding.rvcGameList.adapter as SportTypeAdapter).setUp(datas)
    }

    protected open fun onTabClick(selectItem: Pair<Int, Sport>) {
        (binding.rvcGameList.adapter as SportTypeAdapter).data.forEachIndexed { index, baseNode ->
            if (baseNode is SportGroup && baseNode.name == selectItem.first){
                rightManager.scrollToPositionWithOffset(index, 0)
                return@forEachIndexed
            }
        }
    }

}