package org.cxct.sportlottery.ui.statistics


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.dialog_bottom_sheet_webview.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBottomSheetWebviewBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomSheetFragment
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.MetricsUtil


/**
 * @author kevin
 * @create 2022/3/7
 * @description
 */
@SuppressLint("SetJavaScriptEnabled")
class StatisticsDialog : BaseBottomSheetFragment<StatisticsViewModel>(StatisticsViewModel::class) {


    companion object {
        const val MATCH_ID = "match_id"

        @JvmStatic
        fun newInstance(matchId: String?) = StatisticsDialog().apply {
            arguments = Bundle().apply {
                putString(MATCH_ID, matchId)
            }
        }
    }


    private lateinit var vBinding: DialogBottomSheetWebviewBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        vBinding = DialogBottomSheetWebviewBinding.inflate(inflater, container, false)
        return vBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTitle()
        setupListener()
        setupContentHeight()
        setupWebView()
    }


    private fun setupTitle() {
        vBinding.sheetTvTitle.text = context?.getString(R.string.statistics_title)
    }


    private fun setupListener() {
        vBinding.ivClose.setOnClickListener {
            dismiss()
        }
    }


    private fun setupContentHeight() {
        (ll_content.layoutParams as LinearLayout.LayoutParams).setMargins(0, 50.dp, 0, 0)
    }


    private fun setupWebView() {
        dialog?.findViewById<View>(R.id.design_bottom_sheet)?.let {
            it.setBackgroundResource(android.R.color.transparent)
            it.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            BottomSheetBehavior.from<View>(it).apply {
                //扣除tool bar及status bar的高度
                peekHeight = resources.displayMetrics.heightPixels
                isHideable = false
            }
        }

        vBinding.bottomSheetWebView.apply {
            settings.javaScriptEnabled = true

            webViewClient = WebViewClient()

            sConfigData?.analysisUrl?.replace("{lang}", LanguageManager.getSelectLanguage(context).key)
                ?.replace("{eventId}", arguments?.getString(MATCH_ID) ?: "")?.let {
                    loadUrl(it)
                }
        }
    }


}