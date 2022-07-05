package org.cxct.sportlottery.ui.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.view_bottom_navigation.view.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.game.Page
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.menu.ChangeLanguageDialog
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.setVisibilityByCreditSystem

class BottomNavigationView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RelativeLayout(context, attrs, defStyle) {

    private var mNowPage: Page? = null

    init {
        addView(LayoutInflater.from(context).inflate(R.layout.view_bottom_navigation, this, false))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initBottomNavigation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    fun initBottomNavigation() {
        //底部hint提示
        txv_btm_hint.text = String.format(
            resources.getString(R.string.btm_navigation_hint), context?.getString(
                R.string.app_name
            )
        )
        txv_copyright.text = String.format(
            resources.getString(R.string.btm_navigation_copyright), context?.getString(
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
                context,
                Constants.getAboutUsUrl(context),
                resources.getString(R.string.about_us)
            )
        }

        //博彩責任
        txv_gaming.setOnClickListener {
            JumpUtil.toInternalWeb(
                context,
                Constants.getDutyRuleUrl(context),
                resources.getString(R.string.responsible)
            )
        }

        //規則與條款
        txv_terms.setOnClickListener {
            JumpUtil.toInternalWeb(
                context,
                Constants.getAgreementRuleUrl(context),
                resources.getString(R.string.terms_conditions)
            )
        }

        //語言切換
        img_flag.setOnClickListener {
            goSwitchLanguagePage()
        }

        //代理加盟
        txv_affiliate.setOnClickListener {
            JumpUtil.toInternalWeb(
                context,
                Constants.getAffiliateUrl(context),
                resources.getString(R.string.btm_navigation_affiliate)
            )
        }

        txv_language.setOnClickListener {
            goSwitchLanguagePage()
        }

        //隱私權條款
        txv_policy.setOnClickListener {
            JumpUtil.toInternalWeb(
                context,
                Constants.getPrivacyRuleUrl(context),
                resources.getString(R.string.privacy_policy)
            )
        }

        //常見問題
        txv_faq.setVisibilityByCreditSystem()
        txv_faq.setOnClickListener {
            JumpUtil.toInternalWeb(
                context,
                Constants.getFAQsUrl(context),
                resources.getString(R.string.faqs)
            )
        }

        //在線客服 (取代原有的客服懸浮按鈕)
        txv_chat.setVisibilityByCreditSystem()
        txv_chat.setOnClickListener {
            val serviceUrl = sConfigData?.customerServiceUrl
            val serviceUrl2 = sConfigData?.customerServiceUrl2
            when {
                !serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    (context as AppCompatActivity).supportFragmentManager?.let { it1 ->
                        ServiceDialog().show(
                            it1,
                            null
                        )
                    }
                }
                serviceUrl.isNullOrBlank() && !serviceUrl2.isNullOrBlank() -> {
                    (context as Activity).let { it1 -> JumpUtil.toExternalWeb(it1, serviceUrl2) }
                }
                !serviceUrl.isNullOrBlank() && serviceUrl2.isNullOrBlank() -> {
                    (context as Activity).let { it1 -> JumpUtil.toExternalWeb(it1, serviceUrl) }
                }
            }
        }

        //聯繫我們
        txv_contact.setOnClickListener {
            JumpUtil.toInternalWeb(
                context,
                Constants.getContactUrl(context),
                resources.getString(R.string.contact)
            )
        }

        //菲律賓憑證 (要外開)
        img_pagcor_mark.setOnClickListener {
            JumpUtil.toExternalWeb(context, "https://pagcor.ph/regulatory/index.php")
        }

        //信用盤開啟，要隱藏底部Pagcor文案與logo
        ll_pagcor_container.setVisibilityByCreditSystem()
    }

    private fun goSwitchLanguagePage() {
        val intent = Intent(context, SwitchLanguageActivity::class.java).apply {
            putExtra(SwitchLanguageActivity.FROM_ACTIVITY, mNowPage ?: Page.GAME)
        }
        context?.startActivity(intent)
    }

    fun setTopBackground(background: Drawable) {
        bg_top.background = background
    }

    fun setBottomBackground(background: Drawable) {
        bg_bottom.background = background
    }

    fun setNowPage(page: Page) {
        mNowPage = page
    }
}