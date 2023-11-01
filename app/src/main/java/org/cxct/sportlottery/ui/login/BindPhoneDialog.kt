package org.cxct.sportlottery.ui.login

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.databinding.DialogBindphoneBinding
import org.cxct.sportlottery.repository.UserInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.profileCenter.modify.BindInfoViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.KeyboadrdHideUtil.Companion.hideSoftKeyboard
import org.cxct.sportlottery.view.checkRegisterListener

class BindPhoneDialog(): BaseDialog<BindInfoViewModel>(BindInfoViewModel::class) {

    companion object{
        fun needShow():Boolean{
            UserInfoRepository.userInfo.value?.let {
              return  (sConfigData?.firstPhoneGiveMoney?:0)>0 && it.phone.isNullOrEmpty()
            }
            return false
        }
    }

    init {
        setStyle(R.style.FullScreen)
    }
    lateinit var binding : DialogBindphoneBinding
    private var inputPhoneNoOrEmail: String="" // 输入的手机号或者邮箱，不为空即为输入的号码格式正确
    private var smsCode: String = "" // 短信或者邮箱验证码
    private var userName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DialogBindphoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserve()
    }
    private fun initView()=binding.run {
        tvAmount.text = "${sConfigData?.firstPhoneGiveMoney?:0}"
        tvName.text = getString(R.string.P235,"${sConfigData?.systemCurrencySign}${sConfigData?.firstPhoneGiveMoney?:0}")
        etPhone.checkRegisterListener(){
            inputPhoneNoOrEmail = it
            checkInputComplete()
        }
        etVerificationCode.checkRegisterListener(){
            smsCode = it
            checkInputComplete()
        }
        btnSubmit.setOnClickListener {
            toVerify()
        }
        btnSendSms.setOnClickListener {
            val errorMsg = when {
                inputPhoneNoOrEmail.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
                !VerifyConstUtil.verifyPhone(inputPhoneNoOrEmail) -> {
                    LocalUtils.getString(R.string.phone_no_error)
                }
                else -> null
            }
            if (errorMsg.isNullOrEmpty()){
                hideSoftKeyboard(requireActivity())
                VerifyCodeDialog().run {
                    callBack = { identity, validCode ->
                        sendCode(identity, validCode)
                        etVerificationCode.requestFocus()
                    }
                    show(this@BindPhoneDialog.childFragmentManager, null)
                }
            }else{
                ToastUtil.showToast(requireContext(),errorMsg)
            }
        }
        setOnClickListeners(binding.ivClose,binding.btnSkip){
            dismissAllowingStateLoss()
        }
    }
    private fun sendCode(identity: String?, validCode: String) = binding.btnSendSms.run {
        loading()
        setBtnEnable(false)
        viewModel.sendSMSOrEmailCode("$inputPhoneNoOrEmail", "$identity", validCode)
    }
    private fun codeCountDown() = binding.btnSendSms.run  {
        if (tag != null) {
            return@run
        }

        GlobalScope.launch(lifecycleScope.coroutineContext) {
            tag = this
            CountDownUtil.smsCountDown(
                this,
                { setBtnEnable(false) },
                { text = "${it}s" },
                { onCountDownEnd() }
            )
        }
    }

    private fun onCountDownEnd() = binding.btnSendSms.run  {
        tag = null
        onNewSMSStatus()
        setTextColor(Color.WHITE)
        setText(R.string.send)
    }
    private fun toVerify() {
        loading()
        hideSoftKeyboard(requireActivity())
        viewModel.verifyEmailOrPhoneCode("$inputPhoneNoOrEmail", "$smsCode")
    }
    private fun onNewSMSStatus()  {
        if (binding.btnSendSms.tag == null) {
            binding.btnSendSms.setBtnEnable(true)
        }
    }
    private fun initObserve() = viewModel.run {

        sendCodeResult.observe(viewLifecycleOwner) { smsResult-> // 发送验证码
            hideLoading()

            if (smsResult.succeeded()) {
                userName = smsResult.getData()?.userName
                ToastUtil.showToast(requireActivity(), smsResult.getData()?.msg)
                codeCountDown()
                return@observe
            }

            ToastUtil.showToast(requireActivity(), smsResult.msg)
            binding.btnSendSms.setBtnEnable(true)
            //做异常处理
//            if (smsResult?.code == 2765 || smsResult?.code == 2766) {
//                binding.inputForm.setError(smsResult.msg,false)
//            } else {
//                binding.etSmsValidCode.setError(smsResult?.msg,false)
//            }
        }

        verifyResult.observe(viewLifecycleOwner) { result-> // 验证短信验证码
            hideLoading()
            if (result.succeeded()){

                return@observe
            }

            ToastUtil.showToast(requireActivity(), result.msg)
//            if (result.code == 2765|| result.code == 2766) {
//                binding.inputForm.setError(result.msg,false)
//            } else {
//                binding.etSmsValidCode.setError(result.msg,false)
//            }
        }

        resetResult.observe(viewLifecycleOwner) {
            hideLoading()
            if (!it.succeeded()) {
                ToastUtil.showToast(requireActivity(), it.msg)
                return@observe
            }
            dismissAllowingStateLoss()
        }
    }
    private fun checkInputComplete(){
        val phoneErrorMsg = when {
            inputPhoneNoOrEmail.isBlank() -> LocalUtils.getString(R.string.error_input_empty)
            !VerifyConstUtil.verifyPhone(inputPhoneNoOrEmail) -> {
                LocalUtils.getString(R.string.phone_no_error)
            }
            else -> null
        }
        val codeErrorMsg = when {
            smsCode.isNullOrEmpty() -> LocalUtils.getString(R.string.error_input_empty)
            smsCode.length < 4 -> getString(R.string.sms_code_length_error)
            !VerifyConstUtil.verifySMSCode(smsCode, 4) -> LocalUtils.getString(R.string.error_verification_code_by_sms)
            else -> null
        }
        binding.btnSubmit.isEnabled = phoneErrorMsg.isNullOrEmpty()&&codeErrorMsg.isNullOrEmpty()
    }
}