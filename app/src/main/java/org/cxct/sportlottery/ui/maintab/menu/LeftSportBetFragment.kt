package org.cxct.sportlottery.ui.maintab.menu

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.VerifiedType
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.event.SelectMatchEvent
import org.cxct.sportlottery.common.extentions.collectWith
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.loading.Gloading
import org.cxct.sportlottery.databinding.FragmentLeftSportBetBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.repository.ConfigRepository
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.betRecord.BetRecordActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.menu.adapter.RecyclerInPlayAdapter
import org.cxct.sportlottery.ui.maintab.menu.adapter.RecyclerLeftMatchesAdapter
import org.cxct.sportlottery.ui.maintab.menu.viewmodel.SportLeftMenuViewModel
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.ui.profileCenter.invite.InviteActivity
import org.cxct.sportlottery.ui.profileCenter.pointshop.PointShopActivity
import org.cxct.sportlottery.ui.profileCenter.taskCenter.TaskCenterActivity
import org.cxct.sportlottery.ui.promotion.PromotionListActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.rumWithSlowRequest
import org.cxct.sportlottery.view.updateLastRequestTime
import org.greenrobot.eventbus.EventBus

class LeftSportBetFragment:BaseSocketFragment<SportLeftMenuViewModel,FragmentLeftSportBetBinding>() {

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
        viewModel.userInfo.observe(this) {
            bindVerifyStatus(userInfo = it)
        }
        viewModel.betCountEvent.observe(this){
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
        ConfigRepository.config.observe(this) {
            binding.menuInvite.isVisible = StaticData?.inviteUserOpened()
            binding.menuTaskCenter.isVisible = StaticData.taskCenterOpened()
            binding.menuPointShop.isVisible = StaticData.pointShopOpened()
        }
        viewModel.taskRedDotEvent.collectWith(lifecycleScope){
            binding.menuTaskCenter.ivDot().isVisible = it
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
            startActivity(PromotionListActivity::class.java)
        }.apply {
            setVisibilityByMarketSwitch()
        }
        menuTaskCenter.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_taskcenter_sel, R.drawable.ic_left_menu_taskcenter_nor),
            R.string.A025
        ){
            close()
            startActivity(TaskCenterActivity::class.java)
        }.apply {
            setSummaryStatus(true,R.string.A049, ContextCompat.getColor(requireContext(),R.color.color_A7B2C4))
            isVisible = StaticData.taskCenterOpened()
            ivDot().isVisible = LoginRepository.isLogined()
        }
        menuPointShop.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_gift, R.drawable.ic_left_menu_gift),
            R.string.A051
        ){
            startActivity(PointShopActivity::class.java)
        }.apply {
            setSummaryTag(R.drawable.ic_point_tag_new)
            isVisible = StaticData.pointShopOpened()
        }
        menuInvite.setItem(
            requireContext().getIconSelector(R.drawable.ic_left_menu_invite_sel, R.drawable.ic_left_menu_invite_nor),
            R.string.P462
        ){
            loginedRun(requireContext(),true){
                close()
                startActivity(InviteActivity::class.java)
            }
        }.apply {
            isVisible = StaticData.inviteUserOpened()
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
            R.string.N909
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
            requireActivity().jumpToKYC()
        }.showBottomLine(false)
    }
    private fun bindVerifyStatus(userInfo: UserInfo?) = binding.run {
        if (getMarketSwitch()) {
            menuVerify.setVisibilityByMarketSwitch()
            return@run
        }
        menuVerify.isVisible = sConfigData?.realNameWithdrawVerified.isStatusOpen()
                || sConfigData?.realNameRechargeVerified.isStatusOpen() || !getMarketSwitch()

        VerifiedType.getVerifiedType(userInfo).let{
            setVerify(enable = true, clickAble = true,
                text = it.nameResId,
                statusColor = ContextCompat.getColor(requireContext(),it.colorResId))
        }
    }

    private inline fun setVerify(enable: Boolean, clickAble: Boolean, text: Int, statusColor: Int) = binding.run  {
        menuVerify.isEnabled = enable
        menuVerify.isClickable = clickAble
        menuVerify.setSummaryStatus(enable, text, statusColor)
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
        }
    }
    fun close() {
        getMainTabActivity().closeDrawerLayout()
    }
}