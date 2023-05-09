package org.cxct.sportlottery.ui2.aboutMe

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_about_me.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui2.helpCenter.HelpCenterViewModel
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.setVisibilityByMarketSwitch

/**
 * @app_destination 关于我们
 */
class AboutMeActivity : BaseSocketActivity<HelpCenterViewModel>(HelpCenterViewModel::class) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        setContentView(R.layout.activity_about_me)
        setupEvent()
    }


    private fun setupEvent() {
        custom_tool_bar.setOnBackPressListener { finish() }
        val context = this@AboutMeActivity
        linear_about_us.setOnClickListener {
            JumpUtil.toInternalWeb(context,
                Constants.getAboutUsUrl(context),getString(R.string.about_us))
        }
        linear_responsible.setOnClickListener {
            JumpUtil.toInternalWeb(context,
                Constants.getDutyRuleUrl(context),
                getString(R.string.responsible))
        }
        linear_terms.setVisibilityByMarketSwitch()
        linear_terms.setOnClickListener {
            JumpUtil.toInternalWeb(context,
                Constants.getAgreementRuleUrl(context),
                getString(R.string.terms_conditions))
        }
        linear_privacy.setOnClickListener {
            JumpUtil.toInternalWeb(context,
                Constants.getPrivacyRuleUrl(context),getString(R.string.privacy_policy))
        }
    }
}