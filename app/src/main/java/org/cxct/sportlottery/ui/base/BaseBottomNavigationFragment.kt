package org.cxct.sportlottery.ui.base

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.view_bottom_navigation.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.game.ServiceDialog
import org.cxct.sportlottery.util.JumpUtil
import timber.log.Timber
import kotlin.reflect.KClass

abstract class BaseBottomNavigationFragment<T : BaseSocketViewModel>(clazz: KClass<T>) :
    BaseSocketFragment<T>(clazz), Animation.AnimationListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        receiver.dataSourceChange.observe(viewLifecycleOwner) {
            dataSourceChangeEven?.let {
                showErrorPromptDialog(
                    title = getString(R.string.prompt),
                    message = getString(R.string.message_source_change),
                    hasCancel = false
                ) { it.invoke() }
            }
        }
    }

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
        ll_language.setOnClickListener {
//            (activity as GameActivity).showSwitchLanguageFragment()
            //ChangeLanguageDialog().show(parentFragmentManager, null)
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

        txv_affiliate.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getAffiliateUrl(requireContext()),
                resources.getString(R.string.btm_navigation_affiliate)
            )
        }

    }

    var afterAnimateListener: AfterAnimateListener? = null

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (enter) {
            if (nextAnim == 0) {
                try {
                    afterAnimateListener?.onAfterAnimate()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                super.onCreateAnimation(transit, enter, nextAnim)
            } else {
                AnimationUtils.loadAnimation(activity, nextAnim).apply {
                    setAnimationListener(this@BaseBottomNavigationFragment)
                }
            }
        } else {
            super.onCreateAnimation(transit, enter, nextAnim)
        }
    }

    override fun onAnimationStart(animation: Animation?) {

    }

    override fun onAnimationEnd(animation: Animation?) {
        try {
            afterAnimateListener?.onAfterAnimate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onAnimationRepeat(animation: Animation?) {}

    class AfterAnimateListener(private val onAfterAnimate: () -> Unit) {
        fun onAfterAnimate() = onAfterAnimate.invoke()
    }

    fun clickMenu() {
        when (activity) {
            is BaseBottomNavActivity<*> -> {
                (activity as BaseBottomNavActivity<*>).clickMenuEvent()
            }
            else -> Timber.i("$this 尚未實作關閉菜單方法")
        }
    }

    private var dataSourceChangeEven: (() -> Unit)? = null

    /**
     * 设置有新赛事数据监听回调。
     *  重点!!!
     *  页面加载完成后再调用该方法就行设置回调，
     *  不然由于LiveData粘性事件的原因，在页面初始化的时候就有可能弹窗
     */
    fun setDataSourceChangeEvent(dataSourceChangeEven: () -> Unit) {
        this.dataSourceChangeEven = dataSourceChangeEven
    }

}