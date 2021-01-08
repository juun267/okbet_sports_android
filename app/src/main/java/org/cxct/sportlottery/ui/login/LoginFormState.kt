package org.cxct.sportlottery.ui.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val accountError: String? = null,
    val passwordError: String? = null,
    val isDataValid: Boolean = false
)