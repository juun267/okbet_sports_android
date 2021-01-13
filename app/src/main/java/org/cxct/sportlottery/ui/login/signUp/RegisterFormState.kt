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
    val validCodeError: String? = null,
    val checkAgreement: Boolean = false,
    val isDataValid: Boolean = false
)