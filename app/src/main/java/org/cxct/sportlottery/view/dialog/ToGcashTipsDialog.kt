package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import org.cxct.sportlottery.databinding.DialogToGcashTipsBinding
import org.cxct.sportlottery.util.KvUtils
import org.cxct.sportlottery.util.KvUtils.GLIFE_LOGIN_TIP_FLAG

/**
 * glife用户登录提示
 */
class ToGcashTipsDialog(context: Context) : Dialog(context) {

    private lateinit var binding: DialogToGcashTipsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.requestFeature(Window.FEATURE_NO_TITLE)
        binding = DialogToGcashTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        binding.btnGlifeTips.setOnClickListener {
            KvUtils.put(GLIFE_LOGIN_TIP_FLAG, binding.cbNoReminderTips.isChecked)
            dismiss()
        }

    }

}