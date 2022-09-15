package org.cxct.sportlottery.ui.helpCenter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_help_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.feedback.FeedbackMainActivity
import org.cxct.sportlottery.util.JumpUtil

/**
 * @app_destination 帮助中心
 */
class HelpCenterFragment : BaseFragment<HelpCenterViewModel>(HelpCenterViewModel::class) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_help_center, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEvent()
    }

    private fun setupEvent() {
        linear_game_rule.setOnClickListener {
            JumpUtil.toInternalWeb(requireContext(), Constants.getGameRuleUrl(requireContext()), getString(R.string.game_rule))
        }
        linear_common_problem.setOnClickListener{
            JumpUtil.toInternalWeb(requireContext(),Constants.getFAQsUrl(requireContext()),getString(R.string.faqs))
        }
        linear_feedback.setOnClickListener {
            startActivity(Intent(activity, FeedbackMainActivity::class.java))
        }
    }
}