package org.cxct.sportlottery.ui.login.foget

import android.app.Activity
import android.content.Intent
import kotlinx.android.synthetic.main.activity_forget_ways.*
import org.cxct.sportlottery.common.extentions.bindFinish
import org.cxct.sportlottery.common.extentions.finishWithOK
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.databinding.ActivityForgetWaysBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.setServiceClick
import org.cxct.sportlottery.util.setupSummary

class ForgetWaysActivity:  BaseActivity<ForgetViewModel, ActivityForgetWaysBinding>() {

    override fun onInitView() {
        setStatusBarDarkFont()
        bindFinish(btn_back)
        setupSummary(binding.includeSubtitle.tvSummary)
        binding.includeSubtitle.tvSubTitle1.gone()
        binding.includeSubtitle.tvSubTitle2.gone()
        cl_live_chat.setServiceClick(supportFragmentManager)
        binding.bottomLiences.tvLicense.text = Constants.copyRightString
        btnPhoneWays.setOnClickListener { ForgetPasswordActivity.startByPhoneWays(this) }
        btnEmailWays.setOnClickListener { ForgetPasswordActivity.startByEmailWays(this) }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            finishWithOK()
        }
    }
}