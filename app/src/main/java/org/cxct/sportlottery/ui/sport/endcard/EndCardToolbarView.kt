package org.cxct.sportlottery.ui.sport.endcard

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import org.cxct.sportlottery.common.extentions.doOnResume
import org.cxct.sportlottery.databinding.ViewEndcardToolbarBinding
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.ui.login.signIn.LoginOKActivity
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.refreshMoneyLoading
import org.cxct.sportlottery.util.startLogin
import org.cxct.sportlottery.view.dialog.ToGcashDialog
import splitties.systemservices.layoutInflater

class EndCardToolbarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : LinearLayout(context, attrs, defStyle)  {

    val binding by lazy { ViewEndcardToolbarBinding.inflate(layoutInflater,this,true) }
    private lateinit var viewModel: BaseSocketViewModel
    private lateinit var lifecycleOwner: LifecycleOwner
    fun attach(
        lifecycleOwner: LifecycleOwner,
        clickLogo: ()->Unit,
        viewModel: BaseSocketViewModel,
    ) {
        this.lifecycleOwner = lifecycleOwner
        this.viewModel = viewModel
        binding.ivLogo.setOnClickListener { clickLogo.invoke() }
        initView()
        initObservable()
        lifecycleOwner.doOnResume(-1) { onRefreshMoney() }
    }

    private fun initView()=binding.run {
        ivRefresh.setOnClickListener { onRefreshMoney() }
        ivDeposit.setOnClickListener { ToGcashDialog.showByClick { viewModel.checkRechargeKYCVerify() } }
        btnLogin.setOnClickListener { context.startLogin() }
        btnRegister.setOnClickListener { LoginOKActivity.startRegist(context) }
        setupLogin()
    }
    private fun initObservable()=viewModel.run {
        if (LoginRepository.isLogined()) {
            isLogin.observe(lifecycleOwner) { setupLogin() }
            userMoney.observe(lifecycleOwner) {
                it?.let { bindMoneyText(it) }
            }
        }
    }

    private fun onRefreshMoney(){
        binding.ivRefresh.refreshMoneyLoading()
        viewModel.getMoneyAndTransferOut()
    }
    private fun setupLogin()=binding.run {
       if(LoginRepository.isLogined()){
           llBalance.isVisible = true
           ivDeposit.isVisible = true
           btnLogin.isVisible = false
           btnRegister.isVisible = false
           bindMoneyText(LoginRepository.userMoney())
       }else{
           llBalance.isVisible = false
           ivDeposit.isVisible = false
           btnLogin.isVisible = true
           btnRegister.isVisible = true
       }
    }
    private fun bindMoneyText(money: Double) {
        binding.tvBalance.text = "${TextUtil.format(money)}"
    }
}