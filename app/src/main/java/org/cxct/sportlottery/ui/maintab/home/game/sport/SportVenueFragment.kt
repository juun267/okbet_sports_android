package org.cxct.sportlottery.ui.maintab.home.game.sport

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.network.sport.Sport
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.game.GameVenueFragment
import org.cxct.sportlottery.ui.sport.SportTabViewModel
import org.cxct.sportlottery.util.LogUtil
import kotlin.reflect.jvm.internal.impl.incremental.components.Position

// 体育分类
class SportVenueFragment: GameVenueFragment<SportTabViewModel, FragmentGamevenueBinding>() {

    private val matchTabAdapter = MatchTableAdapter().apply {
        setOnItemClickListener{ _, _, position ->
            this.setSelected(position)
            val selectItem = data[position]
            sportTypeAdapter.data.forEachIndexed { index, baseNode ->
                if (baseNode is SportGroup && baseNode.name == selectItem.first){
                    rightManager.scrollToPositionWithOffset(index, 0)
                    return@setOnItemClickListener
                }
            }
        }
    }
    private val leftManager by lazy { LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false) }

    private val sportTypeAdapter = SportTypeAdapter()
    private val rightManager by lazy { LinearLayoutManager(requireContext(),RecyclerView.VERTICAL,false) }

    override fun onInitView(view: View) {
        super.onInitView(view)
        binding.rvcGameType.apply {
            layoutManager = leftManager
            adapter = matchTabAdapter
        }
        binding.rvcGameList.apply {
            layoutManager = rightManager
            adapter = sportTypeAdapter
            sportTypeAdapter.setOnItemClickListener{ _, _, position ->
                val selectItem = sportTypeAdapter.data[position]
                if (selectItem is Item){
                    val pair=matchTabAdapter.data.firstOrNull { it.second.items.contains(selectItem) }
                    val matchType = if (pair==null) MatchType.IN_PLAY else MatchType.getMatchTypeByStringId(pair.first)
                    val gameType = GameType.getGameType(selectItem.code)
                    LogUtil.d("jumpToTheSport="+matchType.postValue+","+gameType?.key)
                    (activity as MainTabActivity).jumpToTheSport(matchType,gameType)
                }
            }
            //实现左侧联动
            addOnScrollListener(object :RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstItemPosition = rightManager.findFirstVisibleItemPosition()
                    //这块判断dy！=0是防止左侧联动右侧影响
                    if (firstItemPosition != -1&&dy!=0) {
                        val selectItem = sportTypeAdapter.data[firstItemPosition]
                        val leftPosition = when{
                            selectItem is SportGroup->matchTabAdapter.data.indexOfFirst { selectItem.name == it.first }
                            selectItem is Item->matchTabAdapter.data.indexOfFirst { it.second.items.contains(selectItem)}
                            else ->null
                        }
                        leftPosition?.let {
                            matchTabAdapter.setSelected(it)
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
            menu.bkEnd?.let { datas.add(Pair(R.string.home_tab_end_score, it)) }
            menu.inPlay?.let { datas.add(Pair(R.string.home_tab_in_play, it)) }
            menu.today?.let { datas.add(Pair(R.string.home_tab_today, it)) }
            menu.early?.let { datas.add(Pair(R.string.home_tab_early, it)) }
            menu.parlay?.let { datas.add(Pair(R.string.home_tab_parlay, it)) }
            menu.outright?.let { datas.add(Pair(R.string.home_tab_outright, it)) }
            matchTabAdapter.setNewInstance(datas)
            sportTypeAdapter.setUp(datas)
        }
    }

}