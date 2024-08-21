package org.cxct.sportlottery.ui.maintab.home

import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentHomeBinding
import org.cxct.sportlottery.net.games.OKGamesRepository
import org.cxct.sportlottery.net.money.data.FirstDepositDetail
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.network.message.Row
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.repository.StaticData.Companion.okGameOpened
import org.cxct.sportlottery.repository.StaticData.Companion.okLiveOpened
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.bean.XBannerImage
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.firstdeposit.SevenDaysSignInActivity
import org.cxct.sportlottery.ui.maintab.home.game.esport.ESportVenueFragment
import org.cxct.sportlottery.ui.maintab.home.game.live.LiveGamesFragment
import org.cxct.sportlottery.ui.maintab.home.game.slot.ElectGamesFragment
import org.cxct.sportlottery.ui.maintab.home.game.sport.SportVenueFragment
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.ui.maintab.home.view.HomeMenuAdapter
import org.cxct.sportlottery.ui.maintab.publicity.MarqueeAdapter
import org.cxct.sportlottery.ui.news.NewsActivity
import org.cxct.sportlottery.ui.promotion.PromotionListActivity
import org.cxct.sportlottery.ui.sport.endcard.EndCardActivity
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.floatingbtn.SuckEdgeTouch
import timber.log.Timber

class HomeFragment : BaseFragment<MainHomeViewModel,FragmentHomeBinding>() {

    fun getCurrentFragment() = fragmentHelper2.currentFragment()
    private fun getMainTabActivity() = activity as MainTabActivity
    private val fragmentHelper2: FragmentHelper2 by lazy { FragmentHelper2(childFragmentManager, R.id.flContent) }
    private lateinit var hotFragment: HomeHotFragment

    private val homeMenuAdapter = HomeMenuAdapter { item->
        val fragmentClass = item.content
        if (fragmentClass == null) {
            if (item.name == R.string.promo) {
                startActivity(PromotionListActivity::class.java)
            } else if (item.name == R.string.LT050_1) {
                serviceEvent(context(), childFragmentManager)
            } else if (item.name == R.string.P333) {
                startActivity(EndCardActivity::class.java)
            }

            return@HomeMenuAdapter false
        }
        if ((fragmentClass == SportVenueFragment::class.java || fragmentClass == ESportVenueFragment::class.java)
            && getMainTabActivity().checkSportMaintain(true)) {
            return@HomeMenuAdapter false
        }
        if (fragmentClass == ElectGamesFragment::class.java && !okGameOpened()) {
            return@HomeMenuAdapter false
        }
        if (fragmentClass == LiveGamesFragment::class.java && !okLiveOpened()) {
            return@HomeMenuAdapter false
        }
        fragmentHelper2.show(fragmentClass) { fragment, _ ->

        }

        return@HomeMenuAdapter true
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            binding.appBarLayout.expand(false)
        }else{
            homeMenuAdapter.selectedRecommend()
        }
        fragmentHelper2.currentFragment()?.let {
            if (it.isAdded) {
                it.onHiddenChanged(hidden)
            }
        }
    }

    override fun onInitView(view: View) {
        initToolBar()
        binding.rvMarquee.bindLifecycler(this)
        initMenu()
        initIndicate()
        binding.ivService.setOnTouchListener(SuckEdgeTouch())
        binding.ivService.setServiceClick(childFragmentManager)
        viewModel.getHallOkSport()
        initRewardItems()
    }

    private fun initRewardItems() {
        binding.fbtnFirstDeposit.setOnTouchListener(SuckEdgeTouch())
        binding.fbtnFirstDeposit.setOnClickListener {
            hotFragment.showFirstDepositDetail(childFragmentManager,true)
        }
        binding.fbtnFirstDeposit.setOnTouchListener(SuckEdgeTouch())
        binding.fbtnFirstDeposit.setOnClickListener {
            hotFragment.showFirstDepositDetail(childFragmentManager,true)
        }
    }

    override fun onBindViewStatus(view: View) {
        initObservable()
        viewModel.getConfigData()
        viewModel.getAnnouncement()
    }

    private fun initObservable() {
        ConfigRepository.onNewConfig(this) {
            setUpBanner()
            viewModel.getActivityImageListH5()
            homeMenuAdapter.reload()
            binding.rvMenu.scrollToPosition(homeMenuAdapter.initiallyPosition)
            homeMenuAdapter.checkMaintain()
            binding.hIndicator.isVisible = homeMenuAdapter.pageCount() > 1
        }
        //新版宣傳頁
        viewModel.messageListResult.observe(viewLifecycleOwner) {

            val messageListResult = it.getContentIfNotHandled() ?: return@observe
            val sortMsgList =
                messageListResult.rows?.sortedWith(compareByDescending<Row> { it.sort }.thenByDescending { it.addTime })
            val titleList: MutableList<String> = mutableListOf()
            sortMsgList?.forEach { data ->
                if (data.type.toInt() == 1) {
                    titleList.add(data.title + " - " + data.message)
                }
            }
            setupAnnouncement(titleList)
        }
        setupSportStatusChange(this){
            homeMenuAdapter.notifyDataSetChanged()
            homeMenuAdapter.checkMaintain()
        }
        OKGamesRepository.okPlayEvent.observe(this){
            (fragmentHelper2.currentFragment() as? SportVenueFragment<*,*>)?.setOKPlay()
        }

    }

    private fun initToolBar() = binding.homeToolbar.run {
        setMenuClick { getMainTabActivity().showMainLeftMenu(fragmentHelper2.currentFragment()?.javaClass as Class<BaseFragment<*,*>>? ) }
        attach(this@HomeFragment)
        tvUserMoney.setOnClickListener {
            EventBusUtil.post(MenuEvent(true,Gravity.RIGHT))
            getMainTabActivity().showMainRightMenu()
        }
    }

    private fun setUpBanner() {
        val lang = LanguageManager.getSelectLanguage(context).key
        var imageList = sConfigData?.imageList?.filter {
            it.imageType == ImageType.BANNER_HOME && it.lang == lang && !it.imageName1.isNullOrEmpty() && (!getMarketSwitch() && !it.isHidden)
        }?.sortedWith(compareByDescending<ImageData> { it.imageSort }.thenByDescending { it.createdAt })
        val loopEnable = (imageList?.size ?: 0) > 1
        if (imageList.isNullOrEmpty()) {
            return
        }
        var xbanner = binding.topBanner
        xbanner.setHandLoop(loopEnable)
        xbanner.setAutoPlayAble(loopEnable)
        xbanner.setOnItemClickListener { banner, model, view, position ->
            val jumpUrl = (model as XBannerImage).jumpUrl
            if (jumpUrl.isNullOrEmpty()) {
                return@setOnItemClickListener
            }
            if (jumpUrl!!.contains("sweepstakes")) {
                JumpUtil.toLottery(requireContext(), Constants.getLotteryH5Url(requireContext(), LoginRepository.token))
            } else {
                JumpUtil.toInternalWeb(requireContext(), jumpUrl, "")
            }
        }
        xbanner.loadImage { _, model, view, _ ->
            (view as ImageView).load((model as XBannerImage).imgUrl, R.drawable.img_banner01)
        }
        val host = sConfigData?.resServerHost
        val images = imageList.map {
            Timber.d("host:$host url1:${host + it.imageName1}")
            XBannerImage(it.imageText1 + "", host + it.imageName1, it.appUrl)
        }
        //opt1 ->ImageType = 5,为活动轮播图
        //opt2 ->后台有配置
        //满足以上两点 -> 显示活动轮播图r
        if (images.isNotEmpty()) {
            xbanner.visible()
        }
        xbanner.setBannerData(images.toMutableList())
    }

    private fun setupAnnouncement(titleList: List<String>) = binding.linAnnouncement.run {
        if (titleList.isEmpty()) {
            visibility = View.GONE
            return@run
        }

        visibility = View.VISIBLE
        var marqueeAdapter = MarqueeAdapter()
        setOnClickListener {
            startActivity(Intent(requireActivity(), NewsActivity::class.java))
        }
        val rvMarquee = binding.rvMarquee
        rvMarquee.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvMarquee.adapter = marqueeAdapter
        marqueeAdapter.setData(titleList.toMutableList())
        if (titleList.isNotEmpty()) {
            rvMarquee.startAuto(false) //啟動跑馬燈
        } else {
            rvMarquee.stopAuto(true) //停止跑馬燈
        }
    }

    private fun initMenu() = binding.rvMenu.run {
        layoutManager = LinearLayoutManager(requireContext(),RecyclerView.HORIZONTAL,false)
        adapter = homeMenuAdapter
        binding.hIndicator.isVisible = homeMenuAdapter.pageCount() > 1
        fragmentHelper2.show(HomeHotFragment::class.java) { frament, _ ->
            hotFragment = frament
        }
        binding.rvMenu.scrollToPosition(homeMenuAdapter.initiallyPosition)
        PagerSnapHelper().attachToRecyclerView(this)
    }

    private fun initIndicate(){
        binding.hIndicator.ratio = 0.5f
        binding.rvMenu.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val lm = binding.rvMenu.layoutManager as LinearLayoutManager
                    binding.hIndicator.progress = lm.findFirstVisibleItemPosition().toFloat() % 2
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.getConfigData()
    }
    fun showFirstDepositFloatBtn(firstDepositDetail: FirstDepositDetail){

        when (firstDepositDetail?.userStatus) {
            in 0..1 -> {
                cancelCountTimer()
                binding.ivFirstDeposit.setImageResource(R.drawable.ic_float_firstcharge)
                if (firstDepositDetail?.userStatus==0){
                    binding.tvFirstDeposit.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_clock_white, 0,0, 0)
                    val countSecond = (firstDepositDetail.expireTime - System.currentTimeMillis())/1000
                    if (countSecond > 0){
                        startCount(countSecond.toInt(), firstDepositDetail)
                    }
                }else{
                    binding.tvFirstDeposit.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_peso_stroke_white, 0,0, 0)
                    binding.tvFirstDeposit.text = "${firstDepositDetail?.getCurrentDepositConfig()?.limit}"
                }
                binding.fbtnFirstDeposit.isVisible = true
            }
            in 2..5 -> {
                binding.ivFirstDeposit.setImageResource(R.drawable.ic_float_cashback)
                binding.tvFirstDeposit.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_peso_stroke_white, 0,0, 0)
                binding.tvFirstDeposit.text = TextUtil.formatMoney2(firstDepositDetail?.rewardAmount ?: 0)
                fbtnFirstDeposit.isVisible = true
            }
            else -> {
                fbtnFirstDeposit.isVisible = false
            }
        }
        if (firstDepositDetail?.isSign==1){
            binding.ivSevenDaysSignIn.setImageResource(R.drawable.ic_float_dailyrewards)
            binding.tvSevenDaysSignIn.text = TextUtil.formatMoney2(firstDepositDetail?.signReward ?: 0)
            binding.fbtnSevenDaysSignIn.isVisible = true
            binding.fbtnSevenDaysSignIn.setOnTouchListener(SuckEdgeTouch())
            binding.fbtnSevenDaysSignIn.setOnClickListener {
                startActivity(SevenDaysSignInActivity::class.java)
            }
        }else{
            fbtnSevenDaysSignIn.isVisible = false
        }
    }

    private var timerJob: Job? = null
    private fun cancelCountTimer() {
        timerJob?.cancel()
        timerJob = null
    }
    private fun startCount(totalSecond: Int, firstDepositDetail: FirstDepositDetail) {
        cancelCountTimer()
        timerJob = GlobalScope.launch(lifecycleScope.coroutineContext) {
            CountDownUtil.countDown(
                this,
                totalSecond,
                { binding.tvFirstDeposit.text = TimeUtil.showCountDownHMS(totalSecond.toLong() * 1000) },
                { binding.tvFirstDeposit.text = TimeUtil.showCountDownHMS(it.toLong() * 1000) },
                { onComplete->
                    timerJob = null
                    if (onComplete) {
                        binding.tvFirstDeposit.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_peso_stroke_white, 0,0, 0)
                        binding.tvFirstDeposit.text = "${firstDepositDetail?.getCurrentDepositConfig()?.limit}"
                        (getCurrentFragment() as? HomeHotFragment)?.getFirstDepositDetail()
                    }
                }
            )
        }
    }
}