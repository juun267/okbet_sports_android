package org.cxct.sportlottery.ui.login.signUp

import android.content.Context
import android.text.Spanned
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.index.ValidCodeRequest
import org.cxct.sportlottery.network.index.ValidCodeResult
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.FileUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.VerifyConstUtil

class RegisterViewModel(private val loginRepository: LoginRepository) : BaseViewModel() {

    val registerFormState: LiveData<RegisterFormState>
        get() = _registerFormState
    val validCodeResult: LiveData<ValidCodeResult?>
        get() = _validCodeResult

    private val _registerFormState = MutableLiveData<RegisterFormState>()
    private val _validCodeResult = MutableLiveData<ValidCodeResult?>()

    fun registerDataChanged(context: Context, memberAccount: String?, loginPassword: String?, confirmPassword: String?, fullName: String?, validCode: String?, checkAgreement: Boolean) {
        val memberAccountError = checkMemberAccount(context, memberAccount)
        val loginPasswordError = checkLoginPassword(context, loginPassword)
        val confirmPasswordError = checkConfirmPassword(context, loginPassword, confirmPassword)
        val fullNameError = checkFullName(context, fullName)
        val validCodeError = checkValidCode(context, validCode)
        val isDataValid = memberAccountError == null && loginPasswordError == null && confirmPasswordError == null &&
                (sConfigData?.enableFullName != FLAG_OPEN || fullNameError == null) &&
                (sConfigData?.enableValidCode != FLAG_OPEN || validCodeError == null)

        _registerFormState.value = RegisterFormState(
            memberAccountError = memberAccountError,
            loginPasswordError = loginPasswordError,
            confirmPasswordError = confirmPasswordError,
            fullNameError = fullNameError,
            validCodeError = validCodeError,
            checkAgreement = checkAgreement,
            isDataValid = isDataValid
        )
    }

    fun getAgreementContent(context: Context): Spanned {
        //TODO 添加多國語系 開戶協議 檔案路徑 mapping
        val path = when (LanguageManager.getSelectLanguage(context)) {
            LanguageManager.Language.ZH -> "agreement/register_agreement_zh.html"
            LanguageManager.Language.ZHT -> "agreement/register_agreement_zh.html"
            LanguageManager.Language.EN -> "agreement/register_agreement_zh.html"
            LanguageManager.Language.VI -> "agreement/register_agreement_zh.html"
        }

        val assetManager = context.assets
        val htmlString = FileUtil.readStringFromAssetManager(assetManager, path) ?: ""
        return HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    private fun checkMemberAccount(context: Context, account: String?): String? {
        return when {
            account.isNullOrBlank() -> context.getString(R.string.error_account_empty)
            account.length !in 4..16 -> context.getString(R.string.error_member_account)
            VerifyConstUtil.verifyAccount(account) -> context.getString(R.string.error_input_format)
            else -> null
        }
    }

    private fun checkLoginPassword(context: Context, password: String?): String? {
        return when {
            password.isNullOrBlank() -> context.getString(R.string.error_password_empty)
            password.length !in 6..20 -> context.getString(R.string.error_register_password)
            VerifyConstUtil.verifyPwd(password) -> context.getString(R.string.error_input_format)
            else -> null
        }
    }

    private fun checkConfirmPassword(context: Context, password: String?, confirmPassword: String?): String? {
        return when {
            password != confirmPassword -> context.getString(R.string.error_confirm_password)
            else -> null
        }
    }

    private fun checkFullName(context: Context, fullName: String?): String? {
        return when {
            VerifyConstUtil.verifyFullName(fullName ?: "") -> context.getString(R.string.error_input_format)
            else -> null
        }
    }

    private fun checkValidCode(context: Context, validCode: String?): String? {
        return when {
            validCode.isNullOrBlank() -> context.getString(R.string.hint_verification_code)
            else -> null
        }
    }

    fun getValidCode(identity: String?) {
        viewModelScope.launch {
            val result = doNetwork {
                OneBoSportApi.indexService.getValidCode(ValidCodeRequest(identity))
            }
            _validCodeResult.postValue(result)
        }
    }
}