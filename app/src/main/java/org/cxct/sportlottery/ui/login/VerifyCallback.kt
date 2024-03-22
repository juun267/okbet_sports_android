package org.cxct.sportlottery.ui.login

interface VerifyCallback {
    fun onVerifySucceed(ticket: String, randstr: String, tag: String?)
}