package org.cxct.sportlottery.ui.money.recharge.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogDepistHintBinding
import org.cxct.sportlottery.ui.base.BaseDialogFragment
import org.cxct.sportlottery.util.DisplayUtil
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.shape.ShapeDrawable

class DepositHintDialog : BaseDialogFragment() {

    interface ConfirmListener {
        fun onContinue()
    }

    companion object {

        fun show(fragment: Fragment) {
            if (fragment is ConfirmListener) {
                DepositHintDialog().show(fragment.childFragmentManager, DepositHintDialog::javaClass.name)
            }
        }

    }

    private lateinit var binding: DialogDepistHintBinding

    init {
        setStyle(STYLE_NO_TITLE, R.style.CustomDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogDepistHintBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        view.background = ShapeDrawable().setSolidColor(Color.WHITE).setRadius(12.dp.toFloat())
        val cxt = view.context
        tvContinue.background = ShapeDrawable().setSolidColor(cxt.getColor(R.color.color_025BE8)).setRadius(12.dp.toFloat())
        ivClose.setOnClickListener { dismiss() }
        tvContinue.setOnClickListener {
            dismiss()
            val parent = parentFragment
            if (parent is ConfirmListener) {
                parent.onContinue()
                return@setOnClickListener
            }

            val act = activity
            if (act is ConfirmListener) {
                act.onContinue()
            }
        }

        setLayoutParams()
    }

    private fun setLayoutParams() {
        dialog?.window?.let { window->
            val lp = window.attributes
            lp.width = DisplayUtil.screenWith - 60.dp
            lp.height = 208.dp
            window.attributes = lp
        }
    }
}