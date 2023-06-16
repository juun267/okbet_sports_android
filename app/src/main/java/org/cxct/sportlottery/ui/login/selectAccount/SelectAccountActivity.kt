package org.cxct.sportlottery.ui.login.selectAccount

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.bindFinish
import org.cxct.sportlottery.common.extentions.finishWithOK
import org.cxct.sportlottery.databinding.ActivitySelectAccountBinding
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.util.setServiceClick

class SelectAccountActivity: BindingActivity<LoginViewModel,ActivitySelectAccountBinding>() {


    override fun onInitView() = binding.run{
        bindFinish(btnBack)
        clLiveChat.setServiceClick(supportFragmentManager)
        btnOkbet.setOnClickListener {

        }
        btnGlife.setOnClickListener {
           
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            finishWithOK()
        }
    }

}