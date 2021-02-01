package org.cxct.sportlottery.util

import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.net.Uri
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.common.ServiceSelectDialog
import org.cxct.sportlottery.ui.common.WebActivity

object JumpUtil {

    //跳轉線上客服 //當只有一個線路直接跳轉，當兩個都有跳 dialog 讓 user 選擇
    fun toOnlineService(context: Context) {
        val zxkfUrl = sConfigData?.customerServiceUrl ?: ""
        val zxkfUrl2 = sConfigData?.customerServiceUrl2 ?: ""
        when {
            zxkfUrl.isNotEmpty() && zxkfUrl2.isEmpty() -> toExternalWeb(context, zxkfUrl)
            zxkfUrl.isEmpty() && zxkfUrl2.isNotEmpty() -> toExternalWeb(context, zxkfUrl2)
            zxkfUrl.isNotEmpty() && zxkfUrl2.isNotEmpty() -> ServiceSelectDialog(context).show()
            else -> ToastUtil.showToastInCenter(context, context.getString(R.string.error_url_fail))
        }
    }

    fun toInternalWeb(context: Context, href: String?, title: String?) {
        context.startActivity(Intent(context, WebActivity::class.java).putExtra(WebActivity.KEY_URL, href).putExtra(WebActivity.KEY_TITLE, title))
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