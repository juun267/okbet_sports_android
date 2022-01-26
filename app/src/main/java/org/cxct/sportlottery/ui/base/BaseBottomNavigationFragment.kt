package org.cxct.sportlottery.ui.base

import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.view_bottom_navigation.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.menu.ChangeLanguageDialog
import org.cxct.sportlottery.util.JumpUtil
import kotlin.reflect.KClass

abstract class BaseBottomNavigationFragment<T : BaseSocketViewModel>(clazz: KClass<T>) :
    BaseSocketFragment<T>(clazz) {

    fun initBottomNavigation() {
        //底部hint提示
        txv_btm_hint.text = String.format(
            getString(R.string.btm_navigation_hint), context?.getString(
                R.string.app_name
            )
        )
        txv_copyright.text = String.format(
            getString(R.string.btm_navigation_copyright), context?.getString(
                R.string.app_name
            )
        )

        //WinBet平台顯示邏輯
        when (BuildConfig.CHANNEL_NAME) {
            "spwb" -> {
                img_pagcor_mark.isVisible = false
                txv_btm_hint.isVisible = false
            }
            else -> {
                img_pagcor_mark.isVisible = true
                txv_btm_hint.isVisible = true
            }
        }

        //關於我們
        txv_about.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAboutUsUrl(requireContext()),
                getString(R.string.about_us)
            )
        }

        //博彩責任
        txv_gaming.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getDutyRuleUrl(requireContext()),
                getString(R.string.responsible)
            )
        }

        //規則與條款
        txv_terms.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAgreementRuleUrl(requireContext()),
                getString(R.string.terms_conditions)
            )
        }

        //語言切換
        img_flag.setOnClickListener {
            ChangeLanguageDialog().show(parentFragmentManager, null)
        }

        txv_language.setOnClickListener {
            ChangeLanguageDialog().show(parentFragmentManager, null)
        }

        //隱私權條款
        txv_policy.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getPrivacyRuleUrl(requireContext()),
                getString(R.string.privacy_policy)
            )
        }

        //常見問題
        txv_faq.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getFAQsUrl(requireContext()),
                getString(R.string.faqs)
            )
        }

        //在線客服 (取代原有的客服懸浮按鈕)
        if (sConfigData?.customerServiceUrl.isNullOrBlank() && sConfigData?.customerServiceUrl2.isNullOrBlank()) {
            txv_chat.setOnClickListener {
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
        }

        //聯繫我們
        txv_contact.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getContactUrl(requireContext()),
                getString(R.string.contact)
            )
        }

        //菲律賓憑證 (要外開)
        img_pagcor_mark.setOnClickListener {
            JumpUtil.toExternalWeb(requireContext(), "https://pagcor.ph/regulatory/index.php")
        }

    }

}