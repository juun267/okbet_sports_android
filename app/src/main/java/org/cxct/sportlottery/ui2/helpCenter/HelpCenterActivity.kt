package org.cxct.sportlottery.ui2.helpCenter

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_help_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.feedback.FeedbackMainActivity
import org.cxct.sportlottery.util.JumpUtil

class HelpCenterActivity : BaseSocketActivity<HelpCenterViewModel>(HelpCenterViewModel::class) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF,true)
        setContentView(R.layout.activity_help_center)
        setupEvent()
    }

    private fun setupEvent() {
        custom_tool_bar.setOnBackPressListener {
            finish()
        }

        val context = this@HelpCenterActivity

        linear_game_rule.setOnClickListener {
            JumpUtil.toInternalWeb(context, Constants.getGameRuleUrl(context), getString(R.string.game_rule))
        }
        linear_common_problem.setOnClickListener{
            JumpUtil.toInternalWeb(context,
                Constants.getFAQsUrl(context),getString(R.string.faqs))
        }
        linear_feedback.setOnClickListener {
            startActivity(Intent(context, FeedbackMainActivity::class.java))
        }
    }

}