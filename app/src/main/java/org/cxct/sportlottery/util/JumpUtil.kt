package org.cxct.sportlottery.util

import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.common.WebActivity
import org.cxct.sportlottery.ui.thirdGame.ThirdGameActivity
import timber.log.Timber

object JumpUtil {

    fun toInternalWeb(context: Context, href: String?, title: String?, toolbarVisibility: Boolean = true, backEvent: Boolean = true) {
        context.startActivity(
            Intent(context, WebActivity::class.java)
                .putExtra(WebActivity.KEY_URL, Constants.appendMode(href))
                .putExtra(WebActivity.KEY_TITLE, title)
                .putExtra(WebActivity.KEY_TOOLBAR_VISIBILITY, toolbarVisibility)
                .putExtra(WebActivity.KEY_BACK_EVENT, backEvent)
        )
    }

    /**
     * 開啟外部瀏覽器
     *
     * @param context:
     * @param dnbUrl:  要跳轉的 url
     */
    fun toExternalWeb(context: Context, dnbUrl: String?) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(dnbUrl))
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.showToastInCenter(context, context.getString(R.string.error_url_fail))
        }
    }

    //跳轉第三方遊戲網頁
    fun toThirdGameWeb(context: Context, href: String) {
        try {
            Timber.i("跳转到链接:$href")
            if (URLUtil.isValidUrl(href)) {
                context.startActivity(
                    Intent(context, ThirdGameActivity::class.java).putExtra(WebActivity.KEY_URL, href)
                )
            } else {
                throw Exception(href) //20191022 記錄問題：當網址無效時，代表他回傳的 url 是錯誤訊息
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.showToastInCenter(context, context.getString(R.string.error_url_fail))
        }
    }

    fun openEmail(context: Context, extraEmail: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = ClipDescription.MIMETYPE_TEXT_PLAIN
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(extraEmail))
            context.startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.showToastInCenter(context, context.getString(R.string.error_url_fail))
        }
    }
}