package org.cxct.sportlottery.ui.login.signIn

import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.view_status_bar.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityLoginVerifyBinding
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.util.drawable.DrawableCreatorUtils
import org.cxct.sportlottery.util.setBtnEnable
import org.cxct.sportlottery.view.checkRegisterListener

class LoginVerifyActivity: BindingActivity<LoginViewModel, ActivityLoginVerifyBinding>() {

    override fun onInitView() = binding.run {
        ImmersionBar.with(this@LoginVerifyActivity)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .statusBarView(v_statusbar)
            .fitsSystemWindows(false)
            .init()
        btnBack.setOnClickListener { finish() }
        llInput.background = DrawableCreatorUtils.getCommonBackgroundStyle(15, R.color.white, R.color.color_DDE8FF)
        btnLogin.setBtnEnable(false)
        edtCode.checkRegisterListener(::onInputCode)

    }

    private fun onInputCode(code: String) {
        binding.btnLogin.setBtnEnable(code.length == 4)
    }
}