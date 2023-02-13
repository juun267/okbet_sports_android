package org.cxct.sportlottery.ui.login.foget2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_forget_ways.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.extentions.bindFinish
import org.cxct.sportlottery.extentions.finishWithOK
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.foget.ForgetViewModel
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
        btnPhoneWays.setOnClickListener { ForgetPasswordActivity2.startByPhoneWays(this) }
        btnEmailWays.setOnClickListener { ForgetPasswordActivity2.startByEmailWays(this) }
        cl_live_chat.setServiceClick(supportFragmentManager)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("For Test", "=====>>> ${this@ForgetWaysActivity::class.java.simpleName} onActivityResult $resultCode")
        if (resultCode == Activity.RESULT_OK) {
            finishWithOK()
        }
    }

}