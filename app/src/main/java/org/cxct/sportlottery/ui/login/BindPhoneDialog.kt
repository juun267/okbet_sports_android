package org.cxct.sportlottery.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.hideLoading
import org.cxct.sportlottery.common.extentions.loading
import org.cxct.sportlottery.common.extentions.setOnClickListeners
import org.cxct.sportlottery.databinding.DialogBindphoneBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.profileCenter.modify.BindInfoViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.KeyboadrdHideUtil.Companion.hideSoftKeyboard
import org.cxct.sportlottery.view.checkRegisterListener

class BindPhoneDialog: BaseDialog<BindInfoViewModel>(BindInfoViewModel::class) {

    companion object{
        private var instance: BindPhoneDialog?=null
        var afterLoginOrRegist =false //是否有登录注册的动作
        fun needShow():Boolean{
            if (instance!=null){
                  return false
            }else if((sConfigData?.firstPhoneGiveMoney?:0)==0){
                afterLoginOrRegist=false
                return false
            }
            return afterLoginOrRegist
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
                showCaptchaDialog(this@BindPhoneDialog.childFragmentManager){ identity, validCode ->
                        sendCode(identity, validCode)
                        etVerificationCode.requestFocus()
                }
            }else{
                ToastUtil.showToast(requireContext(),errorMsg)
            }
        }
        setOnClickListeners(binding.ivClose,binding.btnSkip){
            dismiss()
        }
    }
    private fun sendCode(identity: String?, validCode: String) = binding.btnSendSms.run {
        requireActivity().loading()
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
        setBtnEnable(true)
        setText(R.string.get_security_code)
    }
    private fun toVerify() {
        requireActivity().loading()
        hideSoftKeyboard(requireActivity())
        viewModel.resetEmailOrPhone("$inputPhoneNoOrEmail", "$smsCode")
    }
    private fun initObserve() = viewModel.run {

        sendCodeResult.observe(viewLifecycleOwner) { smsResult-> // 发送验证码
            requireActivity().hideLoading()

            if (smsResult.succeeded()) {
                userName = smsResult.getData()?.userName
                ToastUtil.showToast(requireActivity(), smsResult.getData()?.msg)
                codeCountDown()
                return@observe
            }
            ToastUtil.showToast(requireActivity(), smsResult.msg)
            binding.btnSendSms.setBtnEnable(true)
        }

        resetResult.observe(viewLifecycleOwner) {
            requireActivity().hideLoading()
            if (it.second.succeeded()) {
                dismiss()
                if (it.second.getData()?.firstPhoneGiveMoney==true){
                    ToastUtil.showToastInCenter(requireActivity(),getString(R.string.P237,"${sConfigData?.systemCurrencySign}${sConfigData?.firstPhoneGiveMoney?:0}"))
                }else{
                    ToastUtil.showToastInCenter(requireActivity(), it.second.getData()?.msg)
                }
            }else{
                ToastUtil.showToastInCenter(requireActivity(), it.second.msg)
            }
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

    override fun show(manager: FragmentManager) {
        super.show(manager)
        instance = this
        afterLoginOrRegist =false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        instance = null
    }

}