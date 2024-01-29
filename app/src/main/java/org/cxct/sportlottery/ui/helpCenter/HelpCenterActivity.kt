package org.cxct.sportlottery.ui.helpCenter

import android.content.Intent
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityHelpCenterBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.feedback.FeedbackMainActivity
import org.cxct.sportlottery.util.JumpUtil

class HelpCenterActivity : BaseActivity<HelpCenterViewModel, ActivityHelpCenterBinding>() {


    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        setupEvent()
    }

    private fun setupEvent()=binding.run {
        customToolBar.setOnBackPressListener {
            finish()
        }

        val context = this@HelpCenterActivity

        linearGameRule.setOnClickListener {
            JumpUtil.toInternalWeb(context, Constants.getGameRuleUrl(context), getString(R.string.game_rule))
        }
        linearCommonProblem.setOnClickListener{
            JumpUtil.toInternalWeb(context,
                Constants.getFAQsUrl(context),getString(R.string.faqs))
        }
        linearFeedback.setOnClickListener {
            startActivity(Intent(context, FeedbackMainActivity::class.java))
        }
    }



}