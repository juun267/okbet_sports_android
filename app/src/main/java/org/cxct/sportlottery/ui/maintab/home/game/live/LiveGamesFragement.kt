package org.cxct.sportlottery.ui.maintab.home.game.live

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKLiveViewModel
import org.cxct.sportlottery.ui.maintab.home.game.GameVenueFragment
import org.cxct.sportlottery.ui.maintab.home.game.slot.ElecGameAdapter
import org.cxct.sportlottery.ui.maintab.home.game.slot.ElecTabAdapter
import org.cxct.sportlottery.util.enterThirdGame
import org.cxct.sportlottery.util.loginedRun
import org.cxct.sportlottery.view.transform.TransformInDialog

class LiveGamesFragement: GameVenueFragment<OKLiveViewModel, FragmentGamevenueBinding>() {

    private val tabAdapter = ElecTabAdapter()
    private val gameAdapter = ElecGameAdapter()
    val rightManager by lazy { GridLayoutManager(requireContext(),2) }

    override fun onInitView(view: View) {
        super.onInitView(view)
        binding.rvcGameType.adapter = tabAdapter
        tabAdapter.setOnItemClickListener{ _, _, position ->
            tabAdapter.setSelected(position)
            val selectItem = tabAdapter.data[position]
            gameAdapter.data.forEachIndexed { index, pair ->
                if (pair.first == selectItem.id){
                    (binding.rvcGameList.layoutManager as GridLayoutManager).scrollToPositionWithOffset(index, 0)
                    return@setOnItemClickListener
                }
            }
        }
        binding.rvcGameList.apply {
            layoutManager = rightManager
            adapter = gameAdapter
            gameAdapter.setOnItemClickListener{ _, _, position ->
                val okGameBean = gameAdapter.data[position].second
                if (okGameBean.isShowMore){
                    (activity as MainTabActivity).jumpToOkLive()
                    return@setOnItemClickListener
                }
                if (okGameBean.isShowBlank){
                    return@setOnItemClickListener
                }
                if(LoginRepository.isLogined()){
                    loginedRun(requireContext()) {
                        okGameBean.let {okGameBean->
                            viewModel.homeOkGamesEnterThirdGame(okGameBean, this@LiveGamesFragement)
                            viewModel.homeOkGameAddRecentPlay(okGameBean)
                        }
                    }
                }else{
                    //请求试玩路线
                    loading()
                    viewModel.requestEnterThirdGameNoLogin(okGameBean)
                }
            }
            //实现左侧联动
            addOnScrollListener(object :RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstItemPosition = rightManager.findFirstVisibleItemPosition()
                    //这块判断dy！=0是防止左侧联动右侧影响
                    if (firstItemPosition != -1&&dy!=0) {
                        val selectItem = gameAdapter.data[firstItemPosition]
                        val leftPosition = tabAdapter.data.indexOfFirst { selectItem.first == it.id }
                        tabAdapter.setSelected(leftPosition)
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
        viewModel.getOKLiveHall()
    }

    private fun initObserver() {
        viewModel.gameHall.observe(viewLifecycleOwner) {
            hideLoading()
            tabAdapter.setNewInstance(it?.categoryList?.toMutableList())
            val itemList = mutableListOf<Pair<Int,OKGameBean>>()
            it.categoryList?.forEach { category->
                val maxSizeSubList =
                    (if (category.gameList?.size?:0>8) category.gameList?.subList(0,8) else category.gameList)?.toMutableList()?: mutableListOf()
                if (maxSizeSubList.size==8){
                    maxSizeSubList.last().isShowMore = true
                }else if(maxSizeSubList.size%2==1){
                    val empty = maxSizeSubList.last().copy()
                    empty.isShowBlank = true
                    maxSizeSubList.add(empty)
                }
                maxSizeSubList.forEach { bean->
                    itemList.add(Pair(category.id,bean))
                }
            }
            it.firmList?.forEach { it.img }
            gameAdapter.firmList = it.firmList
            gameAdapter.setNewInstance(itemList)
            }
        viewModel.enterThirdGameResult.observe(viewLifecycleOwner) {
            if (isVisibleToUser()) enterThirdGame(it.second, it.first)
        }
        viewModel.gameBalanceResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { event ->
                TransformInDialog(event.first, event.second, event.third) { enterResult ->
                    enterThirdGame(enterResult, event.first)
                }.show(childFragmentManager, null)
            }
        }
        }
}