package org.cxct.sportlottery.ui.profileCenter.modify

import android.content.Context
import android.content.Intent
import android.view.View
import kotlinx.android.synthetic.main.activity_forget_ways.*
import org.cxct.sportlottery.extentions.isEmptyStr
import org.cxct.sportlottery.ui.login.foget2.ForgetWaysActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyType
import org.cxct.sportlottery.util.setBtnEnable

class VerificationWaysActivity: ForgetWaysActivity() {

    companion object {

        fun start(context: Context, modifyType: @ModifyType Int, phone: String?, email: String?) {
            val intent = Intent(context, VerificationWaysActivity::class.java)
            intent.putExtra("phone", phone)
            intent.putExtra("email", email)
            intent.putExtra("MODIFY_INFO", modifyType)
            context.startActivity(intent)
        }
    }

    private val phone by lazy { intent.getStringExtra("phone") }
    private val email by lazy { intent.getStringExtra("email") }
    private val modifyType by lazy { intent.getIntExtra("MODIFY_INFO", ModifyType.PhoneNumber) }
    private val requestVerification = 100

    override fun initWaysClick() {
        tv_title_forget_password.text = "选择验证方式"
        btnPhoneWays.setBtnEnable(!phone.isEmptyStr())
        btnEmailWays.setBtnEnable(!email.isEmptyStr())
        btnPhoneWays.setOnClickListener {
            VerificationBindInfoActivity.start(this@VerificationWaysActivity, modifyType, requestVerification, phone, null)
        }
        btnEmailWays.setOnClickListener {
            VerificationBindInfoActivity.start(this@VerificationWaysActivity, modifyType, requestVerification, null, email)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (RESULT_OK != resultCode) {
            return
        }

        if (requestCode == requestVerification) {
            return
        }
    }
}