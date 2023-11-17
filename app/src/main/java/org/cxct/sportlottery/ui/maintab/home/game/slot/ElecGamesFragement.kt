package org.cxct.sportlottery.ui.maintab.home.game.slot

import android.view.View
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.common.extentions.onConfirm
import org.cxct.sportlottery.databinding.FragmentGamevenueBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.net.games.data.OKGamesCategory
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.ui.maintab.home.game.GameVenueFragment
import org.cxct.sportlottery.util.enterThirdGame
import org.cxct.sportlottery.util.loginedRun
import org.cxct.sportlottery.view.transform.TransformInDialog

open class ElecGamesFragement<M, VB>: GameVenueFragment<OKGamesViewModel, FragmentGamevenueBinding>() {

    private val tabAdapter = ElecTabAdapter()
    private val gameAdapter2 = ElecGame2Adapter()
    val rightManager by lazy { GridLayoutManager(requireContext(),2) }

    override fun onInitView(view: View) {
        super.onInitView(view)
        binding.etSearch.onConfirm {
            (requireActivity() as MainTabActivity).apply {
                jumpToOKGames()
                binding.etSearch.postDelayed(500){
                    (getCurrentFragment() as? OKGamesFragment)?.search(it)
                }
            }
        }
        binding.rvcGameType.adapter = tabAdapter
        tabAdapter.setOnItemClickListener{ _, _, position ->
            tabAdapter.setSelected(position)
            val selectItem = tabAdapter.data[position]
            gameAdapter2.findFirmPosition(selectItem.id)?.let { rightManager.scrollToPositionWithOffset(it, 0) }
        }

        binding.rvcGameList.layoutManager = rightManager
        binding.rvcGameList.adapter = gameAdapter2
        gameAdapter2.setOnItemClickListener{ _, _, position ->
            val okGameBean = gameAdapter2.getItem(position)
            if (okGameBean !is OKGameBean) {
                return@setOnItemClickListener
            }
            if (okGameBean.isShowMore){
                (activity as MainTabActivity).jumpToOKGames()
                return@setOnItemClickListener
            }

            if (LoginRepository.isLogined()) {
                viewModel.homeOkGamesEnterThirdGame(okGameBean, this@ElecGamesFragement)
                viewModel.homeOkGameAddRecentPlay(okGameBean)
            } else {
                //请求试玩路线
                loading()
                viewModel.requestEnterThirdGameNoLogin(okGameBean)
            }
        }

        //实现左侧联动
        binding.rvcGameList.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val firstItemPosition = rightManager.findFirstVisibleItemPosition()
                //这块判断dy！=0是防止左侧联动右侧影响
                if (firstItemPosition == -1 || dy == 0) {
                    return
                }

                val item = gameAdapter2.getItem(firstItemPosition)
                val firmId = if (item is OKGamesCategory) {
                    item.id
                } else {
                    (item as OKGameBean).categoryId
                }
                val leftPosition = tabAdapter.data.indexOfFirst { firmId == it.id }
                if (leftPosition >= 0) {
                    tabAdapter.setSelected(leftPosition)
                }
            }
        })
    }

    override fun onBindViewStatus(view: View) {
        initObserver()
    }

    override fun onInitData() {
        loading()
        viewModel.getOKGamesHall()
    }

    private fun initObserver() {
        viewModel.gameHall.observe(viewLifecycleOwner) {

            val categoryList = it.categoryList?.toMutableList()
            if (categoryList.isNullOrEmpty()) {
                hideLoading()
                return@observe
            }
            
            gameAdapter2.setupData(categoryList, it.firmList)
            tabAdapter.setNewInstance(categoryList)
            hideLoading()
        }

        viewModel.enterThirdGameResult.observe(viewLifecycleOwner) {
            if (isVisibleToUser()) enterThirdGame(it.second, it.first)
        }

        viewModel.gameBalanceResult.observe(viewLifecycleOwner) {
            val event = it.getContentIfNotHandled() ?: return@observe
            TransformInDialog(event.first, event.second, event.third) { enterResult ->
                enterThirdGame(enterResult, event.first)
            }.show(childFragmentManager, null)
        }
    }
}