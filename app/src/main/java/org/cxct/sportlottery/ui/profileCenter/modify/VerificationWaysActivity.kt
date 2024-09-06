package org.cxct.sportlottery.ui.profileCenter.modify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.bindFinish
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.databinding.ActivityForgetWaysBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.foget.ForgetViewModel
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyType
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.setBtnEnable
import org.cxct.sportlottery.util.setServiceClick

class VerificationWaysActivity: BaseActivity<ForgetViewModel,ActivityForgetWaysBinding>()  {

    override fun pageName() = "手机号验证页面"

    companion object {

        fun start(context: Context, @ModifyType modifyType: Int, phone: String?, email: String?) {
            val intent = Intent(context, VerificationWaysActivity::class.java)
            intent.putExtra("phone", phone)
            intent.putExtra("email", email)
            intent.putExtra("MODIFY_INFO", modifyType)
            context.startActivity(intent)
        }
    }

    private val userPhone by lazy { intent.getStringExtra("phone") }
    private val userEemail by lazy { intent.getStringExtra("email") }
    private val modifyType by lazy { intent.getIntExtra("MODIFY_INFO", ModifyType.PhoneNumber) }
    private val requestVerification = 100
    private fun isModifyPhoneNo() = ModifyType.PhoneNumber == modifyType

    override fun onInitView() {
        setStatusBarDarkFont()
        initView()
    }

    private fun initView() =binding.run{
        bindFinish(btnBack)
        clLiveChat.setServiceClick(supportFragmentManager)
        setTitle()
        btnPhoneWays.setText(R.string.verify_phone_no)
        btnEmailWays.setText(R.string.verify_email)
        btnPhoneWays.setBtnEnable(!userPhone.isEmptyStr())
        btnEmailWays.setBtnEnable(!userEemail.isEmptyStr())
        btnPhoneWays.setOnClickListener { toVerify(userPhone, null) }
        btnEmailWays.setOnClickListener {toVerify(null, userEemail) }

        val padding = 38.dp
        setBtnPadding(btnEmailWays, padding)
        setBtnPadding(btnPhoneWays, padding)
    }

    private fun setBtnPadding(btn: View, padding: Int) {
        btn.setPadding(btn.paddingLeft, btn.paddingTop, padding, btn.paddingBottom)
    }

    private inline fun setTitle() {
        val title = if (isModifyPhoneNo()) {
            if (userPhone.isEmptyStr()) {
                R.string.set_phone_no
            } else {
                R.string.edit_phone_no
            }
        } else {
            if (userEemail.isEmptyStr()) {
                R.string.set_email
            } else {
                R.string.edit_email
            }
        }
        binding.tvTitleForgetPassword.setText(title)
    }

    private fun toVerify(phone: String?, email: String?) {
        val oldInfo = if(isModifyPhoneNo()) userPhone else userEemail
        ModifyBindInfoActivity.start(this, modifyType, requestVerification, phone, email, oldInfo)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (RESULT_OK == resultCode) {
            finish()
            return
        }
    }
}