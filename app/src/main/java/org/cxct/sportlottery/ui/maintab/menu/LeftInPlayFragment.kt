package org.cxct.sportlottery.ui.maintab.menu

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.databinding.FragmentLeftInplayBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.maintab.menu.adapter.RecyclerInPlayAdapter
import org.cxct.sportlottery.util.EventBusUtil

class LeftInPlayFragment:BindingSocketFragment<MainViewModel,FragmentLeftInplayBinding>() {
    private val inPlayAdapter= RecyclerInPlayAdapter()
    override fun onInitView(view: View) =binding.run {
        recyclerInPlay.layoutManager=GridLayoutManager(requireContext(),2)
        recyclerInPlay.adapter=inPlayAdapter

        //点击类型，跳转到体育首页
        inPlayAdapter.setOnItemClickListener{_,_,position->
            val gameType = GameType.getGameType(inPlayAdapter.getItem(position).code)
            EventBusUtil.post(MenuEvent(false))
            (activity as MainTabActivity).jumpToTheSport(
                MatchType.IN_PLAY,gameType ?: GameType.FT
            )
        }
    }


    /**
     * 请求滚球类型列表
     */
    fun getInPlayData(){
        viewModel.getInPlayList()
        viewModel.inplayList.observe(viewLifecycleOwner) { sportList ->
            sportList?.let {
                inPlayAdapter.setList(sportList)
            }
        }
    }

    override fun onInitData() {
        super.onInitData()
        getInPlayData()
    }
}