package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogRedeemBinding

/**
 * 常用提示對話框
 * 預設按鈕: 取消 / 確定
 */
class RedeemDialog(context: Context) : Dialog(context) {

    private lateinit var binding: DialogRedeemBinding
    private var content: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.requestFeature(Window.FEATURE_NO_TITLE)
        binding = DialogRedeemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        binding.btnPositive.setOnClickListener {
            dismiss()
        }
        if (title == "Congratulations" && content.contains("₱")) {
            var sps = SpannableString(content)
            sps.setSpan(
                ForegroundColorSpan(context.resources.getColor(R.color.color_025BE8)),
                content.indexOf("₱"),
                content.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.tvMessage.text = sps
        } else {
            binding.tvMessage.text = content
        }
        binding.tvDialogRedeemTitle.text = title
    }

    fun setContentMsg(msg: String) {
        content = msg
    }

    fun setTitle(tit: String) {
        title = tit
    }

}