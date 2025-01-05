package org.cxct.sportlottery.ui.maintab.home.hot


import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.common.event.SportStatusEvent
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.FragmentHomeHotBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.repository.ImageType
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.service.dispatcher.DataResourceChange
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.login.BindPhoneDialog
import org.cxct.sportlottery.ui.login.signUp.RegisterSuccessDialog
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.ui.maintab.home.HomeFragment
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.dialog.*
import org.cxct.sportlottery.view.dialog.queue.DialogQueueManager
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeHotFragment : BaseSocketFragment<MainHomeViewModel, FragmentHomeHotBinding>() {

    private val  PRIORITY_SYSTEM_NOTICE = 90
    private val PRIORITY_DIALOG_HOME = 100
    private val PRIORITY_BIND_PHONE = 200
    private val PRIORITY_REGISTER_SUCCESS = 360
    private val PRIORITY_AGE_VERIFY = 400
    private val PRIORITY_FIRST_DEPOSIT = 350
    private val PRIORITY_KYC_REMINDER = 320

    private val recommendMiniGameHelper by lazy {
        RecommendMiniGameHelper(context(), ::enterGame) {
            (binding.scrollView.getChildAt(0) as ViewGroup).addView(it, 0)
        }
    }

    private fun enterGame(okGameBean: OKGameBean) {
        getMainTabActivity().enterThirdGame(okGameBean, null)
    }
    fun getMainTabActivity() = activity as MainTabActivity
    private fun getHomeFragment() = parentFragment as HomeFragment
    private val mOddsChangeListener by lazy {
        ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            binding.hotMatchView.updateOddChange(oddsChangeEvent)
        }
    }
    override fun onInitView(view: View) = binding.run {
        applyHalloweenStyle()
        scrollView.setupBackTop(ivBackTop, 180.dp) {
            if (hotMatchView.isVisible) {
                hotMatchView.resubscribe()
            }
        }
        ToGcashDialog.showByLogin()
        ToMayaDialog.showByLogin()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSportStatusChange(event: SportStatusEvent) {
        checkToCloseView()
    }

    private fun backTop() {
        if (binding.scrollView.scrollY != 0) {
            binding.scrollView.scrollTo(0, 0)
        }
    }

    override fun onBindViewStatus(view: View) = binding.run {
        EventBusUtil.targetLifecycle(this@HomeHotFragment)
        if (binding.scrollView.scrollY != 0) {
            binding.scrollView.postDelayed({ backTop() }, 50)
        }
        recommendMiniGameHelper.bindLifeEvent(this@HomeHotFragment)
        bottomView.bindServiceClick(childFragmentManager)
        recentView.setup(this@HomeHotFragment)
        hotMatchView.onCreate(viewModel.publicityRecommend, viewModel.oddsType,this@HomeHotFragment)
        newGamesView.setUp(this@HomeHotFragment)
        hotGameView.setUp(this@HomeHotFragment)
        providerView.setup(this@HomeHotFragment){
            when(it.gameEntryTypeEnum){
                GameEntryType.OKGAMES->{
                    getMainTabActivity().jumpToOKGames()
                    providerView.postDelayed(500){
                        (getMainTabActivity().getCurrentFragment() as? OKGamesFragment)?.showByProvider(it)
                    }
                }
                GameEntryType.OKLIVE->{
                    getMainTabActivity().jumpToOKGames()
                    providerView.postDelayed(500){
                        (getMainTabActivity().getCurrentFragment() as? OKGamesFragment)?.showByProvider(it)
                    }
                }
                GameEntryType.OKSPORT->{
                    setupOKPlay { okPlayBean ->
                        if (okPlayBean==null){
                            getMainTabActivity().showPromptDialog(message = getString(R.string.shaba_no_open)){}
                        }else{
                            getMainTabActivity()?.enterThirdGame(okPlayBean, "首页-点击厂商直跳")
                        }
                    }
                }
                GameEntryType.OKMINIS->{
                    getHomeFragment().jumpToPerya()
                }
            }
        }
        promotionView.setup(this@HomeHotFragment)
        newsView.setup(this@HomeHotFragment)
        winsRankView.setUp(this@HomeHotFragment, { viewModel.getBetRecord() }, { viewModel.getWinRecord() })
        initObservable()
        viewModel.getSystemNotice()
        OKGamesViewModel.loadMiniGameList(lifecycleScope)
    }


    override fun onResume() {
        super.onResume()
        //返回页面时，刷新体育相关view状态
        checkToCloseView()
        if (getMainTabActivity().getCurrentPosition() == 0 && getHomeFragment().getCurrentFragment() == this) {
            receiver.addOddsChangeListener(this, mOddsChangeListener)
            refreshHotMatch()
            binding.providerView.loadData()
            binding.recentView.loadData();
        }
        getFirstDepositDetail()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        backTop()
        if (hidden) {
            //隐藏时取消赛事监听
            unSubscribeChannelHallAll()
            recommendMiniGameHelper.pausePlay()
        } else {
            receiver.addOddsChangeListener(this, mOddsChangeListener)
            refreshHotMatch()
            //返回页面时，刷新体育相关view状态
            checkToCloseView()
            binding.providerView.loadData()
            recommendMiniGameHelper.resumePlay()
            binding.recentView.setup(this@HomeHotFragment)
        }

    }

    private val dialogQueueManager by lazy { DialogQueueManager(this) }
    private var isShowed = false
    private var announcementsShowed = false
    private fun showDialogs() {
        if (isShowed) {
            return
        }
        //如果首充活动还没返回数据就等到结果出来再一起限时
        if (viewModel.gotConfig.value?.peekContent()==null) return

        isShowed = true
        val fmProvider = ::getParentFragmentManager
        PopImageDialog.buildImageDialog(PRIORITY_DIALOG_HOME, ImageType.DIALOG_HOME, fmProvider)?.let {
            dialogQueueManager.enqueue(it)
        }

        if (viewModel.isLogin.value == true){
            BindPhoneDialog.bindBindPhoneDialog(PRIORITY_BIND_PHONE, fmProvider)?.let {
                dialogQueueManager.enqueue(it)
            }

            RegisterSuccessDialog.buildRegisterSuccessDialog(PRIORITY_REGISTER_SUCCESS, fmProvider) {
                getMainTabActivity().jumpToDeposit("首冲弹窗引导")
            }?.let {
                dialogQueueManager.enqueue(it)
            }
        }

        AgeVerifyDialog.buildAgeVerifyDialog(PRIORITY_AGE_VERIFY, fmProvider)?.let {
            dialogQueueManager.enqueue(it)
        }
        RemindKYCDialog.buildDialog(PRIORITY_KYC_REMINDER, fmProvider)?.let {
            dialogQueueManager.enqueue(it)
        }
        showAnnouncementsDialog()

        dialogQueueManager.showNext()
    }

    private fun showAnnouncementsDialog() {
        if (announcementsShowed) {
            return
        }
        viewModel.systemNotice.value?.let { notice->
            AnnouncementsDialog.buildAnnouncementsDialog(notice, PRIORITY_SYSTEM_NOTICE, ::getParentFragmentManager)?.let { dialog ->
                announcementsShowed = true
                if (isShowed&&!dialogQueueManager.isShowing()) {
                    //config已经组建好弹窗顺序，并且当前没有最新弹窗就直接显示弹窗
                    dialogQueueManager.showNext()
                }else{
                    dialogQueueManager.enqueue(dialog)
                }
            }
        }
    }

    private var firstDeposit = false
    private var firstDepositReward = false
    fun showFirstDepositDetail(fm: FragmentManager, showNow: Boolean=false){
        viewModel.firstDepositDetailEvent.value?.let {
            when (it.userStatus) {
                //限时首充和首充活动
                in 0..1 -> {
                    if (showNow || !firstDeposit) {
                        HomeFirstDepositDialog.buildDialog(PRIORITY_FIRST_DEPOSIT, { fm }, it)?.let {
                            firstDeposit = true
                            dialogQueueManager.enqueue(it)
                        }
                    }
                }
                //次日活动弹窗
                in 2..5 -> {
                    if (showNow || !firstDepositReward) {
                        FirstDepositRewardDialog.buildDialog(PRIORITY_FIRST_DEPOSIT, { fm }, it)?.let {
                            firstDepositReward = true
                            dialogQueueManager.enqueue(it)
                        }
                    }
                }
                else -> {

                }
            }
            if (showNow){
                dialogQueueManager.showNext()
            }
        }
    }

    private fun initObservable() {
        DataResourceChange.observe(viewLifecycleOwner) { loadRecommend() }
        viewModel.gotConfig.observe(viewLifecycleOwner) { event ->
            showDialogs()
        }

        setupSportStatusChange(this){
            if (it){
                binding.hotMatchView.setVisible()
                receiver.addOddsChangeListener(this, mOddsChangeListener)
                refreshHotMatch()
            }else{
                binding.hotMatchView.gone()
            }
        }

        viewModel.systemNotice.observe(viewLifecycleOwner) {
            if (isShowed) {
                showAnnouncementsDialog()
            }
        }
        viewModel.showFirstDepositDetail.observe(this){ show->
            viewModel.firstDepositDetailEvent.value?.let {
                getHomeFragment().showFirstDepositFloatBtn(it)
                if (show){
                    showFirstDepositDetail(parentFragmentManager)
                }
            }
        }
    }

    //hot match
    private fun refreshHotMatch() {
        binding.hotMatchView.onResume(this@HomeHotFragment)
        loadRecommend()
    }

    private fun loadRecommend() {
        viewModel.getRecommend()
        viewModel.getRecommend(GameType.ES)
    }

    /**
     * 检查体育服务状态
     */
    private fun checkToCloseView() {
        if (context == null) {
            return
        }

        //关闭/显示 sports入口
//      binding.homeTopView.initSportEnterStatus()
        //关闭/显示   热门赛事
        binding.hotMatchView.setVisible()
        if(binding.hotMatchView.isVisible && isVisibleToUser()) { //判断当前fragment是否可见
            viewModel.getRecommend()
        }

    }

    override fun onVisibleExceptFirst() {
        super.onVisibleExceptFirst()
        binding.winsRankView.startLoopCall()
    }
    override fun onInvisible() {
        super.onInvisible()
        binding.winsRankView.stopLoopCall()
    }
    fun getFirstDepositDetail(){
        viewModel.getFirstDepositDetail()
    }

    private fun applyHalloweenStyle() = binding.run {
        if (!isHalloweenStyle()) {
            return@run
        }

        val parent = scrollView.getChildAt(0) as ViewGroup
        val index = parent.indexOfChild(recentView)
        val frameLayout = FrameLayout(context())
        val vBg = View(context())
        vBg.setBackgroundResource(R.drawable.img_home_newgame_bg_h)
        frameLayout.addView(vBg, FrameLayout.LayoutParams(-1, 52.dp))
        val linearLayout = LinearLayout(context())
        linearLayout.setPadding(0, 10.dp, 0, 0)
        linearLayout.orientation = LinearLayout.VERTICAL
        parent.removeView(recentView)
        linearLayout.addView(recentView)
        parent.removeView(newGamesView)
        linearLayout.addView(newGamesView)
        frameLayout.addView(linearLayout)
        parent.addView(frameLayout, index, ViewGroup.LayoutParams(-1, -2))

        recentView.applyHalloweenStyle()
        bottomView.applyHalloweenStyle()
        newGamesView.applyHalloweenStyle()
        hotGameView.applyHalloweenStyle()
        hotMatchView.applyHalloweenStyle()
        providerView.applyHalloweenStyle()
        newsView.applyHalloweenStyle()
        winsRankView.applyHalloweenStyle()
    }

}