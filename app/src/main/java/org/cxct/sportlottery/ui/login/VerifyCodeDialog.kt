package org.cxct.sportlottery.ui.login

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.dialog_verify_code.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.index.validCode.ValidCodeResult
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.util.BitmapUtil
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.util.adjustEnableButton

/**
 * 顯示棋牌彈窗
 */
class VerifyCodeDialog(val callBack: (identity: String?, validCode: String) -> Unit) :
    BaseDialog<LoginViewModel>(LoginViewModel::class) {

    init {
        setStyle(R.style.CustomDialogStyle)
    }

    var onClick: (() -> Unit)? = null
    var onDismiss: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_verify_code, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eet_verification_code.checkRegisterListener {
            viewModel.checkValidCode(it).let {
                btn_sure.adjustEnableButton(it.isNullOrBlank())
            }
        }

        //不分手机上弹窗宽度会撑满，需重新设置下左右间距
        (view.layoutParams as MarginLayoutParams?)?.run {
            leftMargin = 15.dp
            rightMargin = 15.dp
        }

        initObserve()
        setupValidCode()
        initClick()
        eet_verification_code.requestFocus()
    }

    private fun initClick() {
        btn_sure.setOnClickListener {
            callBack.invoke(viewModel.validCodeResult.value?.validCodeData?.identity,
                eet_verification_code.text.toString())
            dismiss()
        }
        iv_close.setOnClickListener {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss?.invoke()
    }

    private fun initObserve() {
        viewModel.validateCodeMsg.observe(this) {
            et_verification_code.setError(
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
        eet_verification_code.apply {
            if (text.isNotBlank()) {
                text = null
            }
        }
    }

    private fun setupValidCode() {
        updateValidCode()
        ivReturn.setOnClickListener { updateValidCode() }
    }

    private fun updateUiWithResult(validCodeResult: ValidCodeResult?) {
        if (validCodeResult?.success == true) {
            val bitmap = BitmapUtil.stringToBitmap(validCodeResult.validCodeData?.img)
            Glide.with(this)
                .load(bitmap)
                .into(ivVerification)
        } else {
            ToastUtil.showToast(
                context,
                getString(R.string.get_valid_code_fail_point)
            )
        }
    }
}