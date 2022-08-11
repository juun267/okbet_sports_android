package org.cxct.sportlottery.ui.maintab

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.fragment_main_left.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity
import org.cxct.sportlottery.ui.infoCenter.InfoCenterActivity
import org.cxct.sportlottery.ui.menu.ChangeOddsTypeDialog
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.setVisibilityByCreditSystem

class MainLeftFragment : BaseFragment<BaseViewModel>(BaseViewModel::class) {
    companion object {
        fun newInstance(): MainLeftFragment {
            val args = Bundle()
            val fragment = MainLeftFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_left, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lin_message.setOnClickListener {
            startActivity(
                Intent(requireContext(), InfoCenterActivity::class.java)
                    .putExtra(InfoCenterActivity.KEY_READ_PAGE, InfoCenterActivity.YET_READ)
            )
        }
        lin_odds_type.setOnClickListener {
            ChangeOddsTypeDialog().show(requireActivity().supportFragmentManager, null)
        }
        lin_betting_setting.setOnClickListener {

        }
        lin_language.setOnClickListener {
            val intent = Intent(context, SwitchLanguageActivity::class.java).apply {
                putExtra(SwitchLanguageActivity.FROM_ACTIVITY, false)
            }
            context?.startActivity(intent)
        }
        cb_appearance.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                MultiLanguagesApplication.saveNightMode(true)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                tv_appearance.text =
                    getString(R.string.appearance) + ": " + getString(R.string.night_mode)
            } else {
                MultiLanguagesApplication.saveNightMode(false)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                tv_appearance.text =
                    getString(R.string.appearance) + ": " + getString(R.string.day_mode)
            }
        }
        //在線客服 (取代原有的客服懸浮按鈕)
        lin_customer.setOnClickListener {
            val serviceUrl = sConfigData?.customerServiceUrl
            val serviceUrl2 = sConfigData?.customerServiceUrl2
            when {
                !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    activity?.supportFragmentManager?.let { it1 ->
                        ServiceDialog().show(
                            it1,
                            null
                        )
                    }
                }
                serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    activity?.let { it1 -> JumpUtil.toExternalWeb(it1, serviceUrl2) }
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    activity?.let { it1 -> JumpUtil.toExternalWeb(it1, serviceUrl) }
                }
            }
        }
        //常見問題
        lin_question.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getFAQsUrl(requireContext()),
                getString(R.string.faqs)
            )
        }
        //代理加盟
        tv_affiliate.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAffiliateUrl(requireContext()),
                resources.getString(R.string.btm_navigation_affiliate)
            )
        }
        //聯繫我們
        tv_contact.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getContactUrl(requireContext()),
                getString(R.string.contact)
            )
        }
        //關於我們
        tv_about_us.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAboutUsUrl(requireContext()),
                getString(R.string.about_us)
            )
        }

        //博彩責任
        tv_responsible.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getDutyRuleUrl(requireContext()),
                getString(R.string.responsible)
            )
        }

        //規則與條款
        tv_terms.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAgreementRuleUrl(requireContext()),
                getString(R.string.terms_conditions)
            )
        }
        //隱私權條款
        tv_privacy.setVisibilityByCreditSystem()
        tv_privacy.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getPrivacyRuleUrl(requireContext()),
                resources.getString(R.string.privacy_policy)
            )
        }

        //語言切換
        lin_language.setOnClickListener {
            (activity as GameActivity).showSwitchLanguageFragment()
            //ChangeLanguageDialog().show(parentFragmentManager, null)
        }

        //隱私權條款
        tv_privacy.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getPrivacyRuleUrl(requireContext()),
                getString(R.string.privacy_policy)
            )
        }

    }

    override fun onResume() {
        super.onResume()
        if (MultiLanguagesApplication.isNightMode) {
            tv_appearance.text =
                getString(R.string.appearance) + ": " + getString(R.string.night_mode)
        } else {
            tv_appearance.text =
                getString(R.string.appearance) + ": " + getString(R.string.day_mode)
        }
        tv_language.text = LanguageManager.getLanguageStringResource(requireContext())
        iv_language.setImageResource(LanguageManager.getLanguageFlag(requireContext()))
    }
}