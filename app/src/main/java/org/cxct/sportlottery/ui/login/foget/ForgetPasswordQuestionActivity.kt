package org.cxct.sportlottery.ui.login.foget

import android.app.Activity
import android.content.Context
import android.content.Intent
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.ActivityForgetPasswordQuestionBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.login.VerifyCallback
import org.cxct.sportlottery.ui.login.foget.reset.ResetPasswordActivity
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.setBtnEnable
import org.cxct.sportlottery.util.setServiceClick
import org.cxct.sportlottery.util.showCaptchaDialog
import org.cxct.sportlottery.view.checkRegisterListener
import splitties.activities.start

/**
 * @app_destination 通过手机号或者邮箱重置登录密码
 */
class ForgetPasswordQuestionActivity: BaseActivity<ForgetViewModel, ActivityForgetPasswordQuestionBinding>(ForgetViewModel::class)
 ,VerifyCallback {

    companion object{
        fun start(context: Context, userName: String, question: String){
            context.start<ForgetPasswordQuestionActivity> {
                putExtra("userName",userName)
                putExtra("question",question)
            }
        }
    }

    private val userName by lazy { intent.getStringExtra("userName")!! }
    private val question by lazy { intent.getStringExtra("question")!! }
    private var answer: String? = null

    override fun onInitView() {
        setStatusBarDarkFont()
        initView()
        initObserve()
    }

    private fun initView() = binding.run {
        bindFinish(btnBack)
        clLiveChat.setServiceClick(supportFragmentManager)
        bottomLiences.tvLicense.text = Constants.copyRightString
        etUsername.isEnabled = false
        etQuestion.isEnabled = false
        eetUsername.setText(userName)
        etUsername.setError(null,false)
        eetQuestion.setText(question)
        etQuestion.setError(null,false)
        btnPut.setBtnEnable(false)
        btnPut.setOnClickListener { next() }
        eetAnswer.checkRegisterListener {
            answer = it
            setNextBtnStatus()
        }

    }


    private fun setNextBtnStatus()  {
        val enable = !userName.isNullOrEmpty() && !binding.eetQuestion.text.isNullOrEmpty() && !binding.eetAnswer.text.isNullOrEmpty()
        binding.btnPut.setBtnEnable(enable)
    }


    private fun next() {
        hideSoftKeyboard()
        if (!answer.isNullOrEmpty()){
            showCaptchaDialog()
        }
    }

    private fun initObserve(){
        viewModel.checkSafeQuestionEvent.observe(this){
            hideLoading()
            if (it.succeeded()){
                finish()
                ResetPasswordActivity.start(
                    this@ForgetPasswordQuestionActivity,
                    userName,
                    null,
                    it.getData()?.securityCode,
                    3
                )
            }else{
                ToastUtil.showToast(this@ForgetPasswordQuestionActivity,it.msg)
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            finishWithOK()
        }
    }
    override fun onVerifySucceed(identity: String, validCode: String, tag: String?) {
        if (userName.isNullOrEmpty() || answer.isNullOrEmpty()) return
        loading()
        viewModel.checkSafeQuest(userName!!, answer!!,identity, validCode)
    }

}