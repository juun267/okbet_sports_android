package org.cxct.sportlottery.ui.common

import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.*
import androidx.viewbinding.ViewBinding
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityWebBinding
import org.cxct.sportlottery.network.bettingStation.BettingStation
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.util.JumpUtil

/**
 * Create by Simon Chang
 */
open class WebActivity<VM : BaseViewModel, VB : ViewBinding> : BindingActivity<MainViewModel,ActivityWebBinding>() {
    companion object {
        const val KEY_URL = "key-url"
        const val KEY_TITLE = "key-title"
        const val KEY_TOOLBAR_VISIBILITY = "key-toolbar-visibility"
        const val KEY_BACK_EVENT = "key-back-event"
        const val FIRM_CODE = "firm-code" // 厂商id
        const val GAME_CATEGORY_CODE = "game-category-code" //OK_GAMES、OK_LIVE、OK_BINGO、OK_SPORT
        const val BET_STATION = "betstation"
        const val TAG = "tag"

        var currentTag = ""
        const val TAG_403 = "tag403"
    }

    private val mTitle: String by lazy { intent?.getStringExtra(KEY_TITLE) ?: "" }
    private val mUrl: String by lazy { intent?.getStringExtra(KEY_URL) ?: "about:blank" }
    private val mToolbarVisibility: Boolean by lazy { intent?.getBooleanExtra(KEY_TOOLBAR_VISIBILITY, true) ?: true }
    private val mBackEvent: Boolean by lazy { intent?.getBooleanExtra(KEY_BACK_EVENT, true) ?: true }
    private val bettingStation: BettingStation? by lazy { intent?.getSerializableExtra(BET_STATION) as? BettingStation }
    private val tag: String by lazy { intent?.getStringExtra(TAG) ?: "" }
    private val webActivityImp by lazy { WebActivityImp(this, this::overrideUrlLoading) }
    override fun onInitView()=binding.run {
        currentTag = tag
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        if (!mToolbarVisibility) customToolBar.gone() else initToolBar()
        webActivityImp.setCookie(mUrl)
        webActivityImp.setupWebView(okWebView)
        okWebView.loadUrl(mUrl)
    }

    private fun initToolBar()=binding.run {
        customToolBar.setOnBackPressListener {
            onBackPressed()
        }
        customToolBar.titleText = mTitle
        bettingStation?.let {
            addBetStationInfo()
        }
    }

     private fun overrideUrlLoading(view: WebView, url: String): Boolean {
        if (!url.startsWith("http")) {
            try {
                val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                i.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(i)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return true
        }
        view.loadUrl(url)
        return true
    }
    override fun onBackPressed() {
        if (binding.okWebView.canGoBack()) {
            binding.okWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun addBetStationInfo()=binding.run {
        linBetstation.root.visible()
        bettingStation?.let {
            with(linBetstation) {
                tvAddress.text = it.addr
                tvMobile.text = it.telephone
                var startTime = if (it.officeStartTime.isNotBlank()) it.officeStartTime else "00:00"
                var endTime = if (it.officeEndTime.isNotBlank()) it.officeEndTime else "00:00"
                tvTime.text = startTime + "-" + endTime
                tvAppointmentTime.text = it.appointmentTime
                linAppointmentTime.visibility =
                    if (it.appointmentTime.isNullOrBlank()) View.GONE else View.VISIBLE
                tvMobile.setOnClickListener {
                    tvMobile.text.toString().let {
                        if (it.isNotBlank()) {
                            runWithCatch {
                                val intent = Intent();
                                intent.action = Intent.ACTION_DIAL
                                intent.data = Uri.parse("tel:" + it)
                                startActivity(intent)
                            }
                        }
                    }
                }
                tvAddress.setOnClickListener {
                    var url =
                        "https://maps.google.com/?q=@" + bettingStation!!.lat + "," + bettingStation!!.lat
                    JumpUtil.toExternalWeb(this@WebActivity, url)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runWithCatch { binding.okWebView.destroy() }
    }

}