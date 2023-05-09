package org.cxct.sportlottery.ui2.login.foget

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_forget_ways.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.bindFinish
import org.cxct.sportlottery.common.extentions.finishWithOK
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.setServiceClick

class ForgetWaysActivity: BaseActivity<ForgetViewModel>(ForgetViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarDarkFont()
        setContentView(R.layout.activity_forget_ways)
        initView()
    }

    private fun initView() {
        bindFinish(btn_back)
        cl_live_chat.setServiceClick(supportFragmentManager)
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