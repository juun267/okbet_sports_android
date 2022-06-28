package org.cxct.sportlottery.ui.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.dialog_age_verify.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ScreenUtil
import org.cxct.sportlottery.util.setWebViewCommonBackgroundColor

class AgeVerifyDialog(
    val activity: FragmentActivity,
    private val onAgeVerifyCallBack: OnAgeVerifyCallBack
) : AlertDialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setLayout(
            ScreenUtil.getScreenWidth(context) - 40.dp,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(Gravity.CENTER)
        setContentView(R.layout.dialog_age_verify)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(false) //設置無法點擊外部關閉
        setCancelable(false) //設置無法點擊 Back 關閉
        initView()
        initWebView()
    }

    private fun initView() {
        btn_exit.setOnClickListener {
            onAgeVerifyCallBack.onExit()
            dismiss()
        }

        btn_confirm.setOnClickListener {
            if (!cb_agree_statement.isChecked) return@setOnClickListener
            onAgeVerifyCallBack.onConfirm()
            dismiss()
        }

        cb_agree_statement.setOnCheckedChangeListener { _, isChecked ->
            btn_confirm.apply {
                setBackgroundResource(
                    if (isChecked) R.drawable.bg_radius_4_button_1053af else R.drawable.bg_rectangle_4dp_gray_dark
                )
                setTextColor(
                    ContextCompat.getColor(
                        activity,
                        if (isChecked) R.color.color_FCFCFC else R.color.color_e5e5e5_666666
                    )
                )
            }
        }
        cb_agree_statement.isChecked = false

        tv_statement_link.setOnClickListener {
            wv_statement.visibility = View.VISIBLE
            iv_close.visibility = View.VISIBLE
        }

        iv_close.setOnClickListener {
            wv_statement.visibility = View.GONE
            iv_close.visibility = View.GONE
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        wv_statement.setWebViewCommonBackgroundColor()
        val settings: WebSettings = wv_statement.settings
        settings.javaScriptEnabled = true
        settings.blockNetworkImage = false
        settings.domStorageEnabled = true //对H5支持
        settings.useWideViewPort = true //将图片调整到适合webview的大小
        settings.loadWithOverviewMode = true // 缩放至屏幕的大小
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.defaultTextEncodingName = "utf-8"
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.databaseEnabled = false
        settings.setAppCacheEnabled(false)
        settings.setSupportMultipleWindows(true) //20191120 記錄問題： target=_black 允許跳轉新窗口處理
        wv_statement.loadUrl(Constants.appendMode(Constants.getAgreementRuleUrl(context)) ?: "")
    }

    interface OnAgeVerifyCallBack {
        fun onConfirm()
        fun onExit()
    }
}
