package org.cxct.sportlottery.ui.maintab.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.maintab.menu.adapter.RecyclerInPlayAdapter
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.EventBusUtil

class LeftInPlayFragment: BaseFragment<MainViewModel>() {
    private val inPlayAdapter= RecyclerInPlayAdapter()
    private val loadingHolder by lazy { Gloading.wrapView(recyclerInPlay) }
    private val recyclerInPlay: RecyclerView by lazy { RecyclerView(requireContext()) }

    override fun createRootView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return loadingHolder.wrapper
    }


    override fun onBindView(view: View) {
        if (recyclerInPlay.layoutManager == null) {
            recyclerInPlay.setPadding(12.dp, 0, 2.dp, 0)
            recyclerInPlay.layoutManager = GridLayoutManager(requireContext(),2)
            recyclerInPlay.adapter = inPlayAdapter

            //点击类型，跳转到体育首页
            inPlayAdapter.setOnItemClickListener{_,_,position->
                val gameType = GameType.getGameType(inPlayAdapter.getItem(position).code)
                EventBusUtil.post(MenuEvent(false))
                (activity as MainTabActivity).jumpToTheSport(
                    MatchType.IN_PLAY,gameType ?: GameType.FT
                )
            }
        }

        getInPlayData()
    }

    /**
     * 请求滚球类型列表
     */
    private fun getInPlayData(){
        viewModel.getInPlayList()
        loadingHolder.showLoading()
        viewModel.inplayList.observe(viewLifecycleOwner) { sportList ->
            if (sportList.isNullOrEmpty()) {
                loadingHolder.showEmpty()
            } else {
                loadingHolder.showLoadSuccess()
                inPlayAdapter.setList(sportList)
            }
        }
    }

}