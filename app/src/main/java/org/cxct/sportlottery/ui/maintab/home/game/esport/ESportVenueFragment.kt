package org.cxct.sportlottery.ui.maintab.home.game.esport

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.network.common.ESportType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.CategoryItem
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.game.GameVenueFragment
import org.cxct.sportlottery.ui.maintab.home.game.sport.MatchTableAdapter
import org.cxct.sportlottery.ui.maintab.home.game.sport.SportGroup
import org.cxct.sportlottery.ui.sport.SportTabViewModel
import org.cxct.sportlottery.ui.sport.esport.ESportTypeAdapter
import org.cxct.sportlottery.util.LogUtil

// 体育分类
class ESportVenueFragment: GameVenueFragment<SportTabViewModel, FragmentGamevenueBinding>() {

    private val esportMatchTabAdapter = ESportMatchTableAdapter().apply {
        setOnItemClickListener{ _, _, position ->
            this.setSelected(position)
            val selectItem = data[position]
            esportTypeAdapter.data.forEachIndexed { index, baseNode ->
                if (baseNode is ESportGroup && baseNode.name == selectItem.first){
                    rightManager.scrollToPositionWithOffset(index, 0)
                    return@setOnItemClickListener
                }
            }
        }
    }
    private val leftManager by lazy { LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false) }

    private val esportTypeAdapter = ESportTypeAdapter()
    private val rightManager by lazy { LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false) }

    override fun onInitView(view: View) {
        super.onInitView(view)
        binding.rvcGameType.apply {
            layoutManager = leftManager
            adapter = esportMatchTabAdapter
        }
        binding.rvcGameList.apply {
            layoutManager = rightManager
            adapter = esportTypeAdapter
            esportTypeAdapter.setOnItemClickListener{ _, _, position ->
                val selectItem = esportTypeAdapter.data[position]
                if (selectItem is ESportMatch){
                    val pair=esportMatchTabAdapter.data.firstOrNull { it.first == selectItem.name }
                    val matchType = if (pair==null) MatchType.IN_PLAY else MatchType.getMatchTypeByStringId(pair.first)
                    (activity as MainTabActivity).jumpToTheSport(matchType,GameType.ES)
                }
            }
            //实现左侧联动
            addOnScrollListener(object :RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstItemPosition = rightManager.findFirstVisibleItemPosition()
                    //这块判断dy！=0是防止左侧联动右侧影响
                    if (firstItemPosition != -1&&dy!=0) {
                        val selectItem = esportTypeAdapter.data[firstItemPosition]
                        val leftPosition = when{
                            selectItem is ESportGroup->esportMatchTabAdapter.data.indexOfFirst { it.first == selectItem.name }
                            selectItem is ESportMatch->esportMatchTabAdapter.data.indexOfFirst { it.first == selectItem.name}
                            else ->null
                        }
                        leftPosition?.let {
                            esportMatchTabAdapter.setSelected(it)
                        }
                    }
                }
            })
        }
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
            val menu = it.getData()?.menu ?: return@observe

            val datas = mutableListOf<Pair<Int, Sport>>()
            val categoryDatas = mutableListOf<Pair<Int, Item?>>()
            menu.inPlay?.let {
                datas.add(Pair(R.string.home_tab_in_play, it))
                categoryDatas.add(Pair(R.string.home_tab_in_play, it.filterESportItem()))
            }
            it.getData()?.atStart?.let {
                datas.add(Pair(R.string.home_tab_at_start, it))
                categoryDatas.add(Pair(R.string.home_tab_at_start, it.filterESportItem()))
            }
            menu.today?.let {
                datas.add(Pair(R.string.home_tab_today, it))
                categoryDatas.add(Pair(R.string.home_tab_today, it.filterESportItem()))
            }
            it.getData()?.in12hr?.let {
                datas.add(Pair(R.string.P228, it))
                categoryDatas.add(Pair(R.string.P228, it.filterESportItem()))
            }
            it.getData()?.in24hr?.let {
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
            esportMatchTabAdapter.setNewInstance(datas)
            esportTypeAdapter.setUp(categoryDatas)
        }
    }
    fun Sport.filterESportItem():Item?{
        return items.firstOrNull { it.code ==GameType.ES.key }
    }
}