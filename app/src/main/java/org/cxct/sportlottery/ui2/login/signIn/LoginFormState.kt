package org.cxct.sportlottery.ui2.login.signIn

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val accountError: String? = null,
    val passwordError: String? = null,
    val validCodeError: String? = null,
)