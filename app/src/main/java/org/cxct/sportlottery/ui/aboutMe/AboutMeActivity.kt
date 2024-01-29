package org.cxct.sportlottery.ui.aboutMe

import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityAboutMeBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.helpCenter.HelpCenterViewModel
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.setVisibilityByMarketSwitch

/**
 * @app_destination 关于我们
 */
class AboutMeActivity : BaseActivity<HelpCenterViewModel, ActivityAboutMeBinding>() {


    override fun onInitView() {
        setStatusbar(R.color.color_FFFFFF,true)
        setupEvent()
    }

    private fun setupEvent()=binding.run {
        customToolBar.setOnBackPressListener { finish() }
        val context = this@AboutMeActivity
        linearAboutUs.setOnClickListener {
            JumpUtil.toInternalWeb(context,
                Constants.getAboutUsUrl(context),getString(R.string.about_us))
        }
        linearResponsible.setOnClickListener {
            JumpUtil.toInternalWeb(context,
                Constants.getDutyRuleUrl(context),
                getString(R.string.responsible))
        }
        linearTerms.setVisibilityByMarketSwitch()
        linearTerms.setOnClickListener {
            JumpUtil.toInternalWeb(context,
                Constants.getAgreementRuleUrl(context),
                getString(R.string.terms_conditions))
        }
        linearPrivacy.setOnClickListener {
            JumpUtil.toInternalWeb(context,
                Constants.getPrivacyRuleUrl(context),getString(R.string.privacy_policy))
        }
    }
}