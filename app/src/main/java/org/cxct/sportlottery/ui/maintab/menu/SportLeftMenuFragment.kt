package org.cxct.sportlottery.ui.maintab.menu

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.inVisible
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.FragmentSportLeftMenuBinding
import org.cxct.sportlottery.ui.base.BindingSocketFragment
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.menu.viewmodel.SportLeftMenuViewModel
import org.cxct.sportlottery.view.onClick
import org.cxct.sportlottery.view.setColors


class SportLeftMenuFragment:BindingSocketFragment<SportLeftMenuViewModel, FragmentSportLeftMenuBinding> (){
    private fun getMainTabActivity() = activity as MainTabActivity

    private val sportBettingFragment=LeftSportBetFragment()
    private val inPlayFragment by lazy { LeftInPlayFragment() }
    private val othersFragment by lazy { LeftOthersFragment() }

    override fun onInitView(view: View) =binding.run {
        //关闭按钮
        ivClose.onClick {
            close()
        }

        //Sport betting
        linearBetting.onClick {
            replaceTab(0)
        }
        //In play
        tvTabInPlay.onClick {
            replaceTab(1)
        }
        //others
        linearOthers.onClick {
            replaceTab(2)
        }
        tvLogin.onClick {
            LoginOKActivity.startRegist(requireContext())
            close()
        }



    }

    override fun onInitData() {
        super.onInitData()
       binding.linearBetting.performClick()
        reloadData()
    }

    fun reloadData(){
        initLoginData()
    }


    @SuppressLint("SetTextI18n")
    private fun initLoginData(){
        binding.apply {
            if(viewModel.isLogin()){
                tvUserName.visible()
                tvUserBalance.visible()
                tvLogin.gone()
            }else{
                tvLogin.visible()
                tvUserName.gone()
                tvUserBalance.gone()
            }

            viewModel.userInfo.value?.let {
                tvUserName.text=it.userName
            }
            viewModel.userMoney.value?.let {
                tvUserBalance.text="₱ $it"
            }
        }

    }

    private fun replaceTab(index:Int){
        clearTabStyle(index)
        val transaction=childFragmentManager.beginTransaction()
        when(index){
            0->{
                //Sport betting
                transaction.replace(R.id.frameContent,sportBettingFragment)
            }
            1->{
                //in-play
                transaction.replace(R.id.frameContent,inPlayFragment)
            }
            2->{
                //others
                transaction.replace(R.id.frameContent,othersFragment)
            }
        }
        transaction.commitAllowingStateLoss()
    }

    //退出
    private fun close() {
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