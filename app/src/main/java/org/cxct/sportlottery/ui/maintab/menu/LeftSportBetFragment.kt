package org.cxct.sportlottery.ui.maintab.menu

import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.event.SelectMatchEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.show
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentLeftSportBetBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.betRecord.BetRecordActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.menu.adapter.RecyclerInPlayAdapter
import org.cxct.sportlottery.ui.maintab.menu.adapter.RecyclerLeftMatchesAdapter
import org.cxct.sportlottery.ui.maintab.menu.viewmodel.SportLeftMenuViewModel
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.sport.detail.SportDetailActivity
import org.cxct.sportlottery.ui.sport.filter.LeagueSelectActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.isVisible
import org.cxct.sportlottery.view.rumWithSlowRequest
import org.cxct.sportlottery.view.updateLastRequestTime
import org.greenrobot.eventbus.EventBus

class LeftSportBetFragment:BindingSocketFragment<SportLeftMenuViewModel,FragmentLeftSportBetBinding>() {

    private inline fun getMainTabActivity() = activity as MainTabActivity

    private val inPlayAdapter= RecyclerInPlayAdapter()
    //热门赛事 adapter
    private val hotMatchAdapter= RecyclerLeftMatchesAdapter()

    private val loadingHolder by lazy { Gloading.wrapView(binding.content) }

    override fun onInitView(view: View) =binding.run{
        initInplayList()
        initHotMatch()
        initMenuItems()
        //投注详情
        constrainBetRecord.setOnClickListener {
            loginedRun(it.context) {
                startActivity(BetRecordActivity::class.java)
                constrainBetRecord.postDelayed({ (parentFragment as SportLeftMenuFragment?)?.close() },500)
            }
        }
    }

    override fun onBindViewStatus(view: View) {
        super.onBindViewStatus(view)
        initObservable()
        loadingHolder.showLoading()
        getBetRecordCount()
        getInPlayData()
        getRecommendLeagueData()
    }

    private fun initObservable() {
        viewModel.betCountEvent.observe(this){
            binding.constrainBetRecord.isVisible = viewModel.totalCount>0
            binding.tvRecordNumber.text="${viewModel.totalCount}"
        }
        viewModel.recommendLeague.observe(this){
            loadingHolder.showLoadSuccess()
            hotMatchAdapter.setList(it)
            binding.tvRecommendLeague.isVisible = !it.isNullOrEmpty()
        }
        viewModel.inplayList.observe(this) {
            inPlayAdapter.setList(it)
            binding.tvInplay.isVisible = !it.isNullOrEmpty()
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
//            //item点击进入体育大厅早盘下，指定的联赛
            val itemData = hotMatchAdapter.data[position]
            close()
            getMainTabActivity().jumpToTheSport(MatchType.EARLY, GameType.getGameType(itemData.gameType))
            postDelayed(500){
               EventBus.getDefault().post(SelectMatchEvent(leagueIds = arrayListOf(itemData.id), matchIds = arrayListOf()))
            }
        }
    }
    private fun initMenuItems() = binding.run {
        menuPromo.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_promo_sel, R.drawable.ic_left_menu_promo_nor),
            R.string.B005
        ){
            close()
            menuPromo.bindPromoClick {}
        }.apply {
            setVisibilityByMarketSwitch()
        }
        menuAffiliate.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_affiliate_sel, R.drawable.ic_left_menu_affiliate_nor),
            R.string.B015
        ){
            close()
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAffiliateUrl(binding.root.context),
                resources.getString(R.string.btm_navigation_affiliate)
            )
        }.apply {
            setVisibilityByMarketSwitch()
        }

        menuNews.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_news_sel, R.drawable.ic_left_menu_news_nor),
            R.string.B015
        ){
            close()
            getMainTabActivity().jumpToNews()
        }

        menuSupport.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_custom_sel, R.drawable.ic_left_menu_custom_nor),
            R.string.LT050
        ).setServiceClick(parentFragmentManager) { close() }

        menuVerify.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_verify_sel, R.drawable.ic_left_menu_verify_nor),
            R.string.N914
        ){
            close()
            loginedRun(requireContext()) { startActivity(VerifyIdentityActivity::class.java) }
        }.showBottomLine(false)
    }
    /**
     * 请求滚球类型列表
     */
    fun getInPlayData(){
        viewModel.getInPlayList()
    }
    /**
     * 获取热门赛事数据
     */
    fun getRecommendLeagueData(){
        viewModel.getRecommendLeague()
    }
    fun getBetRecordCount(){
        if(viewModel.isLogin()){
            rumWithSlowRequest(viewModel){
                updateLastRequestTime()
                viewModel.getBetRecordCount()
            }
        }else{
            binding.constrainBetRecord.gone()
        }
    }
    fun close() {
        getMainTabActivity().closeDrawerLayout()
    }
}