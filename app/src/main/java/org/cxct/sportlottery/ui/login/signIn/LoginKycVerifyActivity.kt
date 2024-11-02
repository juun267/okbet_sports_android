package org.cxct.sportlottery.ui.login.signIn

import com.gyf.immersionbar.ImmersionBar
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.ActivityLoginKycVerifyBinding
import org.cxct.sportlottery.net.user.UserRepository
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityActivity
import org.cxct.sportlottery.util.KvUtils
import org.cxct.sportlottery.util.LogUtil

class LoginKycVerifyActivity : BaseActivity<LoginViewModel, ActivityLoginKycVerifyBinding>() {


    override fun pageName() = "登录注册用户状态临时冻结"

    override fun onInitView() = binding.run {
        ImmersionBar.with(this@LoginKycVerifyActivity)
            .statusBarDarkFont(true)
            .transparentStatusBar()
            .statusBarView(vTop.vStatusbar)
            .fitsSystemWindows(false)
            .init()
        binding.bottomLiences.tvLicense.text = Constants.copyRightString
        initEvent()
    }

    private fun initEvent() = binding.run {
        btnBack.setOnClickListener { finish() }
        btnLogin.setOnClickListener {
            finish()
            startActivity(VerifyIdentityActivity::class.java)
        }
    }

}