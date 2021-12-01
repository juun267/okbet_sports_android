package org.cxct.sportlottery.ui.login.signUp

/**
 * Data validation state of the register form.
 */
data class RegisterFormState(
    val inviteCodeError: String? = null,
    val memberAccountError: String? = null,
    val loginPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val fullNameError: String? = null,
    val fundPwdError: String? = null,
    val qqError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val weChatError: String? = null,
    val zaloError: String? = null,
    val facebookError: String? = null,
    val whatsAppError: String? = null,
    val telegramError: String? = null,
    val securityCodeError: String? = null,
    val validCodeError: String? = null,
    val checkAgreement: Boolean = false,
    val isDataValid: Boolean = true
)