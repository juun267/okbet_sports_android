package org.cxct.sportlottery.ui.login

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import com.bumptech.glide.Glide
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.databinding.DialogVerifyCodeBinding
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.util.BitmapUtil
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.setBtnEnable
import org.cxct.sportlottery.view.checkSMSCode

/**
 * 顯示棋牌彈窗
 */
class VerifyCodeDialog(val callBack: (identity: String, validCode: String) -> Unit): BaseDialog<LoginViewModel>(LoginViewModel::class) {

    init {
        setStyle(R.style.CustomDialogStyle)
    }
    lateinit var binding: DialogVerifyCodeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DialogVerifyCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            eetVerificationCode.requestFocus()
            eetVerificationCode.checkSMSCode(etVerificationCode) { btnSure.setBtnEnable(!it.isEmptyStr()) }
        }


        //不分手机上弹窗宽度会撑满，需重新设置下左右间距
        (view.layoutParams as MarginLayoutParams?)?.run {
            leftMargin = 15.dp
            rightMargin = 15.dp
        }

        initObserve()
        setupValidCode()
        initClick()
    }

    private fun initClick() =binding.run{
        btnSure.setOnClickListener {
            viewModel.validCodeResult.value?.validCodeData?.identity?.let { it1 ->
                callBack?.invoke(it1, eetVerificationCode.text.toString())
            }
            dismiss()
        }
        ivClose.setOnClickListener {
            dismiss()
        }
    }
    private fun initObserve() {
        viewModel.validateCodeMsg.observe(this) {
            binding.etVerificationCode.setError(
                it.first,
                false
            )
        }
        viewModel.validCodeResult.observe(this) {
            updateUiWithResult(it)
        }
    }

    private fun updateValidCode() {
        val data = viewModel.validCodeResult.value?.validCodeData
        viewModel.getValidCode(data?.identity)
        binding.eetVerificationCode.apply {
            if (text.isNotBlank()) {
                text = null
            }
        }
    }

    private fun setupValidCode() {
        updateValidCode()
        binding.ivReturn.setOnClickListener { updateValidCode() }
    }

    private fun updateUiWithResult(validCodeResult: ValidCodeResult?) {
        if (validCodeResult?.success == true) {
            val bitmap = BitmapUtil.stringToBitmap(validCodeResult.validCodeData?.img)
            Glide.with(this)
                .load(bitmap)
                .into(binding.ivVerification)
        } else {
            ToastUtil.showToast(
                context,
                getString(R.string.get_valid_code_fail_point)
            )
        }
    }
}