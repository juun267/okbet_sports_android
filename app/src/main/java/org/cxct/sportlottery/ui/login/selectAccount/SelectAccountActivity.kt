package org.cxct.sportlottery.ui.login.selectAccount

import android.text.Html
import android.widget.TextView
import androidx.core.view.isVisible
import org.cxct.sportlottery.common.event.ForgetPwdSelectAccountEvent
import org.cxct.sportlottery.common.event.LoginSelectAccountEvent
import org.cxct.sportlottery.common.extentions.bindFinish
import org.cxct.sportlottery.databinding.ActivitySelectAccountBinding
import org.cxct.sportlottery.network.index.login.LoginResult
import org.cxct.sportlottery.repository.ImageType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.getMarketSwitch
import org.cxct.sportlottery.util.setServiceClick

class SelectAccountActivity : BindingActivity<LoginViewModel, ActivitySelectAccountBinding>() {

    companion object{
        const val TYPE_LOGIN = 1
        const val TYPE_FORGET = 2
        const val TYPE_SELECT = "typeSelect"
    }
    val type by lazy { intent.getIntExtra(TYPE_SELECT,TYPE_LOGIN) }

    override fun onInitView() = binding.run {
        setStatusBarDarkFont()
        bindFinish(btnBack)
        clLiveChat.setServiceClick(supportFragmentManager)
        btnOkbet.setOnClickListener {
            finish()
            when(type){
                TYPE_LOGIN->EventBusUtil.post(LoginSelectAccountEvent(false))
                TYPE_FORGET->EventBusUtil.post(ForgetPwdSelectAccountEvent(false))
            }
        }
        btnGlife.setOnClickListener {
            finish()
            when(type){
                TYPE_LOGIN->EventBusUtil.post(LoginSelectAccountEvent(true))
                TYPE_FORGET->EventBusUtil.post(ForgetPwdSelectAccountEvent(true))
            }
        }
    }
}