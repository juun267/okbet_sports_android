package org.cxct.sportlottery.ui.statistics

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_statistics.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.util.LanguageManager


const val KEY_MATCH_ID = "matchId"

class StatisticsActivity : BaseActivity<StatisticsViewModel>(StatisticsViewModel::class) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_statistics)

        setupToolbar()

        setupWebView(intent.extras?.getString(KEY_MATCH_ID))
    }

    private fun setupToolbar() {
        setSupportActionBar(statistics_toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        statistics_close.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(matchId: String?) {
        statistics_web.apply {
            settings.javaScriptEnabled = true

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    hideLoading()

                }
            }

            sConfigData?.analysisUrl?.replace(
                "{lang}",
                LanguageManager.getSelectLanguage(context).key
            )?.replace("{eventId}", matchId ?: "")?.let {
                loadUrl(
                    it
                ).run {
                    loading()
                }
            }
        }
    }

    override fun finish() {
        super.finish()

        overridePendingTransition(R.anim.pop_top_to_bottom_enter, R.anim.pop_bottom_to_top_exit)
    }
}