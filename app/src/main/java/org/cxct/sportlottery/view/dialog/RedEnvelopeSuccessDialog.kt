package org.cxct.sportlottery.view.dialog

import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogRedenvelopeSuccessBinding
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.TextUtil

class RedEnvelopeSuccessDialog : BaseDialog<BaseViewModel,DialogRedenvelopeSuccessBinding>() {
    init {
        setStyle(R.style.FullScreen)
    }

    companion object {
        const val AMOUNT = "amount"

        @JvmStatic
        fun newInstance(amount: String?) = RedEnvelopeSuccessDialog().apply {
            arguments = Bundle().apply {
                putString(AMOUNT, amount)
            }
        }
    }

    override fun onInitView()=binding.run {
        val operatingAnim = AnimationUtils.loadAnimation(
            activity, R.anim.red_envelope_rotate_clockwise
        )
        val lin = LinearInterpolator()
        operatingAnim.interpolator = lin
        ivShinne.startAnimation(operatingAnim)
        val amount = arguments?.getString(AMOUNT) ?: "0"
        tvAmount.text = BuildConfig.SYSTEM_CURREMCY_SIGN + " " + TextUtil.format(amount)
        btnOk.setOnClickListener {
            dismiss()
        }
        startAnimation()
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