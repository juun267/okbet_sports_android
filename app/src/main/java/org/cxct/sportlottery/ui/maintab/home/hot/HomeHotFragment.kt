package org.cxct.sportlottery.ui.maintab.home.hot

import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.common.event.SportStatusEvent
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.FragmentHomeHotBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.repository.ImageType
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.login.BindPhoneDialog
import org.cxct.sportlottery.ui.login.signUp.RegisterSuccessDialog
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui.maintab.games.OKLiveFragment
import org.cxct.sportlottery.ui.maintab.home.HomeFragment
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.dialog.AgeVerifyDialog
import org.cxct.sportlottery.view.dialog.AnnouncementsDialog
import org.cxct.sportlottery.view.dialog.PopImageDialog
import org.cxct.sportlottery.view.dialog.ToGcashDialog
import org.cxct.sportlottery.view.dialog.queue.DialogQueueManager
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeHotFragment : BaseSocketFragment<MainHomeViewModel, FragmentHomeHotBinding>() {

    private val PRIORITY_SYSTEM_NOTICE = 90
    private val PRIORITY_DIALOG_HOME = 100
    private val PRIORITY_BIND_PHONE = 200
    private val PRIORITY_REGISTER_SUCCESS = 300
    private val PRIORITY_AGE_VERIFY = 400

     fun getMainTabActivity() = activity as MainTabActivity
     private fun getHomeFragment() = parentFragment as HomeFragment
     private val mOddsChangeListener by lazy {
        ServiceBroadcastReceiver.OddsChangeListener { oddsChangeEvent ->
            binding.hotMatchView.updateOddChange(oddsChangeEvent)
            binding.hotEsportView.updateOddChange(oddsChangeEvent)
        }
    }
    override fun onInitView(view: View) = binding.run {
        scrollView.setupBackTop(ivBackTop, 180.dp) {
            if (hotMatchView.isVisible) {
                hotMatchView.resubscribe()
            }
            if (hotEsportView.isVisible) {
                hotEsportView.resubscribe()
            }
        }

        ToGcashDialog.showByLogin()
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
        binding.miniGameView.startPlay("")
        binding.miniGameView.bindLifeCycle(this@HomeHotFragment)
        bottomView.bindServiceClick(childFragmentManager)
        recentView.setup(this@HomeHotFragment)
        hotMatchView.onCreate(viewModel.publicityRecommend, viewModel.oddsType,this@HomeHotFragment)
        okGamesView.setUp(this@HomeHotFragment)
        hotEsportView.onCreate(viewModel.hotESportMatch, viewModel.oddsType,this@HomeHotFragment)
        okLiveView.setUp(this@HomeHotFragment)
        providerView.setup(this@HomeHotFragment){
            if (it.gameEntryTypeEnum==GameEntryType.OKGAMES){
                getMainTabActivity().jumpToOKGames()
                providerView.postDelayed(500){
                    (getMainTabActivity().getCurrentFragment() as? OKGamesFragment)?.showByProvider(it)
                }
            }else{
                getMainTabActivity().jumpToOkLive()
                providerView.postDelayed(500){
                    (getMainTabActivity().getCurrentFragment() as? OKLiveFragment)?.showByProvider(it)
                }
            }

        }
        promotionView.setup(this@HomeHotFragment)
        newsView.setup(this@HomeHotFragment)
        winsRankView.setUp(this@HomeHotFragment, { viewModel.getBetRecord() }, { viewModel.getWinRecord() })
        bettingStationView.setup(this@HomeHotFragment)
        initObservable()

        viewModel.getSystemNotice()
    }


    override fun onResume() {
        super.onResume()
        //返回页面时，刷新体育相关view状态
        checkToCloseView()
        if (getMainTabActivity().getCurrentPosition() == 0 && getHomeFragment().getCurrentFragment() == this) {
            receiver.addOddsChangeListener(this, mOddsChangeListener)
            refreshHotMatch()
            binding.providerView.loadData()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        backTop()
        if (hidden) {
            //隐藏时取消赛事监听
            unSubscribeChannelHallAll()
        } else {
            receiver.addOddsChangeListener(this, mOddsChangeListener)
            refreshHotMatch()
            //返回页面时，刷新体育相关view状态
            checkToCloseView()
            binding.providerView.loadData()
        }

    }

    private val dialogQueueManager by lazy { DialogQueueManager(this) }
    private var isShowed = false
    private var announcementsShowed = false
    private fun showDialogs() {
        if (isShowed) {
            return
        }

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
                getMainTabActivity().checkRechargeKYCVerify()
            }?.let {
                dialogQueueManager.enqueue(it)
            }

        }

        AgeVerifyDialog.buildAgeVerifyDialog(PRIORITY_AGE_VERIFY, fmProvider)?.let {
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
                dialogQueueManager.enqueue(dialog)
            }
        }
    }

    private fun initObservable() {
        viewModel.gotConfig.observe(viewLifecycleOwner) { event ->
            showDialogs()
        }

        setupSportStatusChange(this){
            if (it){
                binding.hotMatchView.setVisible()
                binding.hotEsportView.setVisible()
                receiver.addOddsChangeListener(this, mOddsChangeListener)
                refreshHotMatch()
            }else{
                binding.hotMatchView.gone()
                binding.hotEsportView.gone()
            }
        }

        viewModel.systemNotice.observe(viewLifecycleOwner) {
            if (isShowed) {
                showAnnouncementsDialog()
            }
        }
    }

    //hot match
    private fun refreshHotMatch() {
        binding.hotMatchView.onResume(this@HomeHotFragment)
        binding.hotEsportView.onResume(this@HomeHotFragment)
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

        binding.hotEsportView.setVisible()
        if(binding.hotEsportView.isVisible && isVisibleToUser()) { //判断当前fragment是否可见
            viewModel.getRecommend(GameType.ES)
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

}