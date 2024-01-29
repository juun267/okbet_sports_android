package org.cxct.sportlottery.view.dialog

import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogRedenvelopeFailBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel

class RedEnvelopeFailDialog : BaseDialog<BaseViewModel,DialogRedenvelopeFailBinding>() {
    init {
        setStyle(R.style.FullScreen)
    }
    companion object {
        const val ERROR_DESC = "ERROR_DESC"

        @JvmStatic
        fun newInstance(errorDesc: String? = "") = RedEnvelopeFailDialog().apply {
            arguments = Bundle().apply {
                putString(ERROR_DESC, errorDesc)
            }
        }
    }

    override fun onInitView()=binding.run {
        startAnimation()
        btnOk.setOnClickListener {
            dismiss()
        }
        val errorDesc = arguments?.getString(ERROR_DESC).orEmpty()
        if (errorDesc.isNotEmpty()) tvErrorDesc.text = errorDesc
    }

    fun startAnimation() {
        RotateAnimation(-30f,
            30f,
            Animation.RELATIVE_TO_SELF,
            0.65f,
            Animation.RELATIVE_TO_SELF,
            0.65f)
            .apply {
                repeatCount = Animation.INFINITE
                repeatMode = Animation.REVERSE
                duration = 2000
            }.let {
                binding.ivBuble1.startAnimation(it)
                binding.ivBuble2.startAnimation(it)
                binding.ivBuble3.startAnimation(it)
                binding.ivBuble4.startAnimation(it)
            }
    }
}