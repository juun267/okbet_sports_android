package org.cxct.sportlottery.ui.maintab.menu

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.SportStatusEvent
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.inVisible
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentSportLeftMenuBinding
import org.cxct.sportlottery.repository.showCurrencySign
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.menu.viewmodel.SportLeftMenuViewModel
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.startLogin
import org.cxct.sportlottery.view.onClick
import org.cxct.sportlottery.view.rumWithSlowRequest
import org.cxct.sportlottery.view.setColors
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class SportLeftMenuFragment:BindingSocketFragment<SportLeftMenuViewModel, FragmentSportLeftMenuBinding> (){
    private fun getMainTabActivity() = activity as MainTabActivity

    //Betting sport
    private val sportBettingFragment=LeftSportBetFragment()
    //滚球
    private val inPlayFragment by lazy { LeftInPlayFragment() }
    //其他
    private val othersFragment by lazy { LeftOthersFragment() }

    @SuppressLint("SetTextI18n")
    override fun onInitView(view: View) =binding.run {
        //关闭按钮
        ivClose.onClick {
            close()
        }

        ivHome.onClick {
            getMainTabActivity().backMainHome()
            close()
        }

        //click Sport betting
        linearBetting.onClick {
            replaceTab(0)
        }
        //click 滚球
        tvTabInPlay.onClick {
            replaceTab(1)
        }
        //click 其他
        linearOthers.onClick {
            replaceTab(2)
        }
        //登录注册
        tvLogin.onClick {
            requireActivity().startLogin()
            close()
        }

        ivTopCover.onClick {

        }
        tvLogin.text="${getString(R.string.btn_login)}/${getString(R.string.btn_register)}"
    }

    override fun onInitData() {
        super.onInitData()
        //默认选中sport betting
       binding.linearBetting.performClick()
        reloadData()

        EventBusUtil.targetLifecycle(this)


    }

    fun reloadData(){
        if (activity == null) {
            return
        }

        //初始化顶部登录状态
        initLoginData()
        //刷新订单数量
        if(sportBettingFragment.isVisible){
            if(viewModel.isLogin()){
                rumWithSlowRequest(viewModel){
                    sportBettingFragment.viewModel.getBetRecordCount()
                }
            }
        }
        //刷新滚球列表
        if(inPlayFragment.isVisible){
            inPlayFragment.viewModel.getInPlayList()
        }
        //刷新热门赛事
        if(sportBettingFragment.isVisible){
            sportBettingFragment.viewModel.getRecommend()
        }
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSportStatusChange(event: SportStatusEvent) {
        close()
    }


    @SuppressLint("SetTextI18n")
    private fun initLoginData(){

        binding.apply {
            //已登录
            if(viewModel.isLogin()){
                tvUserName.visible()
                tvUserBalance.visible()
                tvLogin.gone()
                //用户名
                if(viewModel.userInfo.value?.nickName.isNullOrEmpty()){
                    tvUserName.text="${viewModel.userInfo.value?.userName} "
                }else{
                    tvUserName.text="${viewModel.userInfo.value?.nickName} "
                }

                //余额
                tvUserBalance.text="$showCurrencySign ${TextUtil.format(viewModel.userMoney.value?:0)}"
                ivUserCover.load(viewModel.userInfo.value?.iconUrl, R.drawable.ic_person_avatar)
            }else{
                //未登录
                tvLogin.visible()
                tvUserName.gone()
                tvUserBalance.gone()
            }
        }
    }

    /**
     * 切换tab
     */
    private fun replaceTab(index:Int){
        clearTabStyle(index)
        val transaction=childFragmentManager.beginTransaction()
        when(index){
            0->{
                //Sport betting
                transaction.replace(R.id.frameContent,sportBettingFragment)
            }
            1->{
                //滚球
                transaction.replace(R.id.frameContent,inPlayFragment)
            }
            2->{
                //其他
                transaction.replace(R.id.frameContent,othersFragment)
            }
        }
        transaction.commitAllowingStateLoss()
    }

    //退出
    fun close() {
        getMainTabActivity().closeDrawerLayout()
    }


    private fun clearTabStyle(index:Int){
        binding.tvTabBetting.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        binding.tvTabInPlay.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        binding.tvTabOthers.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        binding.ivTabBetting.inVisible()
        binding.ivTabInPlay.inVisible()
        binding.ivTabOthers.inVisible()
        binding.tvTabBetting.setColors(R.color.color_6D7693)
        binding.tvTabInPlay.setColors(R.color.color_6D7693)
        binding.tvTabOthers.setColors(R.color.color_6D7693)
        selectTabStyle(index)
    }

    private fun selectTabStyle(index:Int){
        when(index){
            0->{
                binding.tvTabBetting.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                binding.tvTabBetting.setColors(R.color.color_025BE8)
                binding.ivTabBetting.visible()
            }
            1->{
                binding.tvTabInPlay.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                binding.tvTabInPlay.setColors(R.color.color_025BE8)
                binding.ivTabInPlay.visible()
            }
            2->{
                binding.tvTabOthers.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                binding.tvTabOthers.setColors(R.color.color_025BE8)
                binding.ivTabOthers.visible()
            }
        }
    }
}