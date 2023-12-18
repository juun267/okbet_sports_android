package org.cxct.sportlottery.ui.maintab.menu

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentLeftSportBetBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.betRecord.BetRecordActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.menu.adapter.RecyclerInPlayAdapter
import org.cxct.sportlottery.ui.maintab.menu.adapter.RecyclerLeftMatchesAdapter
import org.cxct.sportlottery.ui.maintab.menu.viewmodel.SportLeftMenuViewModel
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.GridItemDecoration
import org.cxct.sportlottery.util.LogUtil
import org.cxct.sportlottery.util.loginedRun
import org.cxct.sportlottery.view.isVisible
import org.cxct.sportlottery.view.updateLastRequestTime

class LeftSportBetFragment:BindingSocketFragment<SportLeftMenuViewModel,FragmentLeftSportBetBinding>() {

    private val inPlayAdapter= RecyclerInPlayAdapter()
    //热门赛事 adapter
    private val hotMatchAdapter= RecyclerLeftMatchesAdapter()

    private val loadingHolder by lazy { Gloading.wrapView(binding.content) }

    override fun onInitView(view: View) =binding.run{
        initInplayList()
        initHotMatch()
        initObservable()
        //投注详情
        constrainBetRecord.setOnClickListener {
            loginedRun(it.context) {
                startActivity(BetRecordActivity::class.java)
                constrainBetRecord.postDelayed({ (parentFragment as SportLeftMenuFragment?)?.close() },500)
            }
        }
    }

    override fun onInitData() {
        super.onInitData()
        loadingHolder.showLoading()
        getInPlayData()
        getRecommendLeagueData()

        if(viewModel.isLogin()){
            updateLastRequestTime()
            viewModel.getBetRecordCount()
        }
    }
    private fun initObservable() {
        viewModel.betCountEvent.observe(this){
            binding.tvRecordNumber.text="${viewModel.totalCount}"
        }
        viewModel.recommendLeagueEvent.observe(this){
            loadingHolder.showLoadSuccess()
            hotMatchAdapter.setList(it)
            binding.tvRecommendLeague.isVisible = it.isNullOrEmpty()
        }
        viewModel.inplayList.observe(this) {
            inPlayAdapter.setList(it)
            binding.tvInplay.isVisible = it.isNullOrEmpty()
        }
    }
    private fun initInplayList()=binding.rvInPlay.run{
        layoutManager = GridLayoutManager(requireContext(),2)
        addItemDecoration(GridSpacingItemDecoration(2,10.dp,false))
        adapter = inPlayAdapter

        //点击类型，跳转到体育首页
        inPlayAdapter.setOnItemClickListener{_,_,position->
            val gameType = GameType.getGameType(inPlayAdapter.getItem(position).code)
            EventBusUtil.post(MenuEvent(false))
            (activity as MainTabActivity).jumpToTheSport(
                MatchType.IN_PLAY,gameType ?: GameType.FT
            )
        }
    }
    private fun initHotMatch()=binding.rvRecommendLeague.run{
        layoutManager=LinearLayoutManager(requireContext())
        adapter=hotMatchAdapter
        hotMatchAdapter.setOnItemClickListener{_,_,position->
//            //item点击进入详情
//            SportDetailActivity.startActivity(requireContext(),
//                matchInfo = hotMatchAdapter.data[position].matchInfo!!,
//                matchType = MatchType.IN_PLAY)
        }
    }

    /**
     * 请求滚球类型列表
     */
    private fun getInPlayData(){
        viewModel.getInPlayList()
    }
    /**
     * 获取热门赛事数据
     */
    private fun getRecommendLeagueData(){
        viewModel.getRecommendLeague()
    }
}