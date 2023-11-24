package org.cxct.sportlottery.ui.maintab.home.hot

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.common.event.SportStatusEvent
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.FragmentHomeHotBinding
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.repository.ImageType
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.login.BindPhoneDialog
import org.cxct.sportlottery.ui.login.signUp.RegisterSuccessDialog
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui.maintab.games.OKLiveFragment
import org.cxct.sportlottery.ui.maintab.home.HomeFragment2
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.dialog.PopImageDialog
import org.cxct.sportlottery.view.dialog.ToGcashDialog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeHotFragment : BindingSocketFragment<MainHomeViewModel, FragmentHomeHotBinding>() {

     fun getMainTabActivity() = activity as MainTabActivity
     fun getHomeFragment() = parentFragment as HomeFragment2
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
        bottomView.bindServiceClick(childFragmentManager)
        EventBusUtil.targetLifecycle(this@HomeHotFragment)
        ToGcashDialog.showByLogin()
    }

    override fun onInitData() {
        //设置监听游戏试玩
        setTrialPlayGameDataObserve()

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
        if (binding.scrollView.scrollY != 0) {
            binding.scrollView.postDelayed({ backTop() }, 50)
        }
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

    private fun initObservable() {
        viewModel.gotConfig.observe(viewLifecycleOwner) { event ->
            viewModel.getSportMenuFilter()
            if (PopImageDialog.showHomeDialog) {
                PopImageDialog.showHomeDialog = false
                MultiLanguagesApplication.showPromotionPopupDialog(getMainTabActivity()){}
                if (PopImageDialog.checkImageTypeAvailable(ImageType.DIALOG_HOME.code)) {
                    requireContext().newInstanceFragment<PopImageDialog>(Bundle().apply {
                        putInt(PopImageDialog.IMAGE_TYPE, ImageType.DIALOG_HOME.code)
                    }).show(childFragmentManager, PopImageDialog::class.simpleName)
                }
            }
            if (viewModel.isLogin.value==true&&BindPhoneDialog.needShow()){
                BindPhoneDialog().show(parentFragmentManager,RegisterSuccessDialog::class.simpleName)
            }
            if (viewModel.isLogin.value==true&&RegisterSuccessDialog.needShow()){
                RegisterSuccessDialog{
                    getMainTabActivity().checkRechargeKYCVerify()
                }.show(parentFragmentManager,RegisterSuccessDialog::class.simpleName)
            }
        }
        setupSportStatusChange(this){
            if (it){
                binding.hotMatchView.setVisible()
                binding.hotEsportView.visible()
                receiver.addOddsChangeListener(this, mOddsChangeListener)
                refreshHotMatch()
            }else{
                binding.hotMatchView.gone()
                binding.hotEsportView.gone()
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
    private fun checkToCloseView(){
        context?.let {
            //关闭/显示 sports入口
//            binding.homeTopView.initSportEnterStatus()
            //关闭/显示   热门赛事
            binding.hotMatchView.setVisible()
            //判断当前fragment是否可见
            if(binding.hotMatchView.isVisible&&isVisibleToUser()){
                viewModel.getRecommend()
            }
            binding.hotEsportView.setVisible()
            //判断当前fragment是否可见
            if(binding.hotEsportView.isVisible&&isVisibleToUser()){
                viewModel.getRecommend(GameType.ES)
            }
        }
    }


}