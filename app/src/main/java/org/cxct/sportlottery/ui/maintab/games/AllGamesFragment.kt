package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_about_us.linear_about_us
import kotlinx.android.synthetic.main.fragment_about_us.linear_privacy
import kotlinx.android.synthetic.main.fragment_about_us.linear_responsible
import kotlinx.android.synthetic.main.fragment_about_us.linear_terms
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setOnClickListener
import org.cxct.sportlottery.common.extentions.startActivity
import org.cxct.sportlottery.databinding.FragmentAllOkgamesBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.setVisibilityByMarketSwitch

// OkGames所有分类
class AllGamesFragment : BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    private lateinit var binding: FragmentAllOkgamesBinding

    private fun okGamesFragment() = parentFragment as OKGamesFragment
    override fun createRootView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllOkgamesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onBindView(view: View) {


        onBindPart5View()
    }



    private fun onBindPart5View() {
        val include5 = binding.include5
        val tvPrivacyPolicy = include5.tvPrivacyPolicy
        val tvTermConditions = include5.tvTermConditions
        val tvResponsibleGaming = include5.tvResponsibleGaming
        val tvLiveChat = include5.tvLiveChat
        val tvContactUs = include5.tvContactUs
        val tvFaqs = include5.tvFaqs
        setUnderline(
            tvPrivacyPolicy,
            tvTermConditions,
            tvResponsibleGaming,
            tvLiveChat,
            tvContactUs,
            tvFaqs
        )
        setOnClickListener(
            tvPrivacyPolicy,
            tvTermConditions,
            tvResponsibleGaming,
            tvLiveChat,
            tvContactUs,
            tvFaqs
        ) {
            when (it.id) {
                R.id.tvPrivacyPolicy -> {
                    JumpUtil.toInternalWeb(requireContext(),
                        Constants.getPrivacyRuleUrl(requireContext()),getString(R.string.privacy_policy))
                }

                R.id.tvTermConditions -> {
                    JumpUtil.toInternalWeb(requireContext(),
                        Constants.getAgreementRuleUrl(requireContext()),
                        getString(R.string.terms_conditions))
                }

                R.id.tvResponsibleGaming -> {

                }

                R.id.tvLiveChat -> {

                }

                R.id.tvContactUs -> {

                }

                R.id.tvFaqs -> {
                    JumpUtil.toInternalWeb(requireContext(),Constants.getFAQsUrl(requireContext()),getString(R.string.faqs))
                }
            }
        }

    }

    private fun setUnderline(vararg view:TextView){
        view.forEach {
            it.text = Html.fromHtml("<u>"+it.text+"</u>")
        }
    }
}