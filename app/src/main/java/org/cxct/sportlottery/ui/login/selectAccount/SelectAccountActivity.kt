package org.cxct.sportlottery.ui.login.selectAccount

import org.cxct.sportlottery.common.event.LoginSelectAccountEvent
import org.cxct.sportlottery.common.extentions.bindFinish
import org.cxct.sportlottery.databinding.ActivitySelectAccountBinding
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.setServiceClick

class SelectAccountActivity : BindingActivity<LoginViewModel, ActivitySelectAccountBinding>() {



    override fun onInitView() = binding.run {
        bindFinish(btnBack)
        clLiveChat.setServiceClick(supportFragmentManager)
        btnOkbet.setOnClickListener {
            finish()
            EventBusUtil.post(LoginSelectAccountEvent(false))
        }
        btnGlife.setOnClickListener {
            finish()
            EventBusUtil.post(LoginSelectAccountEvent(true))
        }
    }
}