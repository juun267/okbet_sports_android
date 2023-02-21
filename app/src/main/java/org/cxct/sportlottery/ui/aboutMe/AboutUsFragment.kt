package org.cxct.sportlottery.ui.aboutMe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_about_us.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.helpCenter.HelpCenterViewModel
import org.cxct.sportlottery.util.JumpUtil

/**
 * @app_destination 关于我们
 */
class AboutUsFragment : BaseFragment<HelpCenterViewModel>(HelpCenterViewModel::class) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about_us, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEvent()
    }

    private fun setupEvent() {
        linear_about_us.setOnClickListener {
            JumpUtil.toInternalWeb(requireContext(),Constants.getAboutUsUrl(requireContext()),getString(R.string.about_us))
        }
        linear_responsible.setOnClickListener {
            JumpUtil.toInternalWeb(requireContext(), Constants.getDutyRuleUrl(requireContext()), getString(R.string.responsible))
        }
        linear_terms.setOnClickListener{
            JumpUtil.toInternalWeb(requireContext(),Constants.getAgreementRuleUrl(requireContext()),getString(R.string.terms_conditions))
        }
        linear_privacy.setOnClickListener {
            JumpUtil.toInternalWeb(requireContext(),Constants.getPrivacyRuleUrl(requireContext()),getString(R.string.privacy_policy))
        }
    }
}