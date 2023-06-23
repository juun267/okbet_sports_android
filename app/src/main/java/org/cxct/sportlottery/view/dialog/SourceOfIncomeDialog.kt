package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import org.cxct.sportlottery.databinding.DialogSourceOfIncomeBinding

/**
 * 常用提示對話框
 * 預設按鈕: 取消 / 確定
 */
class SourceOfIncomeDialog(context: Context) : Dialog(context) {

    private var mPositiveClickListener: OnPositiveListener? = null
    private lateinit var binding: DialogSourceOfIncomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.requestFeature(Window.FEATURE_NO_TITLE)
        binding = DialogSourceOfIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        binding.btnPositive.setOnClickListener {
            dismiss()
        }

        binding.btnNegative.setOnClickListener {
            mPositiveClickListener?.positiveClick(binding.etSourceOfIncome.text.toString())
            dismiss()
        }

    }

    fun setPositiveClickListener(positiveClickListener: OnPositiveListener) {
        mPositiveClickListener = positiveClickListener
    }

    interface OnPositiveListener {
        fun positiveClick(str: String)
    }


}