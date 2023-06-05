package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import org.cxct.sportlottery.databinding.DialogVerificationTipBinding

/**
 * 身份证验证提示弹窗
 */
class VerificationTipDialog(context: Context) : Dialog(context) {

    private lateinit var binding: DialogVerificationTipBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.requestFeature(Window.FEATURE_NO_TITLE)
        binding = DialogVerificationTipBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        binding.btnSubmit.setOnClickListener {
            dismiss()
        }

    }

}