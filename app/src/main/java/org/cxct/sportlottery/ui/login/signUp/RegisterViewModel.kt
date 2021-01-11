package org.cxct.sportlottery.ui.login.signUp

import android.content.Context
import android.text.Spanned
import androidx.core.text.HtmlCompat
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.FileUtil
import org.cxct.sportlottery.util.LanguageManager

class RegisterViewModel(private val loginRepository: LoginRepository) : BaseViewModel() {

    fun getAgreementContent(context: Context): Spanned {
        //TODO 添加多國語系 開戶協議 檔案路徑 mapping
        val path = when (LanguageManager.getSelectLanguage(context)) {
            LanguageManager.Language.ZH -> "agreement/register_agreement_zh.html"
            LanguageManager.Language.ZHT -> "agreement/register_agreement_zh.html"
            LanguageManager.Language.EN -> "agreement/register_agreement_zh.html"
            LanguageManager.Language.VI -> "agreement/register_agreement_zh.html"
        }

        val assetManager = context.assets
        val htmlString =  FileUtil.readStringFromAssetManager(assetManager, path) ?: ""
        return HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }
}