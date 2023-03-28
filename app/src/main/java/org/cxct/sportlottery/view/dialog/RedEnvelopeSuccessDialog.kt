package org.cxct.sportlottery.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import kotlinx.android.synthetic.main.dialog_redenvelope_success.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.TextUtil

class RedEnvelopeSuccessDialog : BaseDialog<BaseViewModel>(BaseViewModel::class) {
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_redenvelope_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val operatingAnim = AnimationUtils.loadAnimation(
            activity, R.anim.red_envelope_rotate_clockwise
        )
        val lin = LinearInterpolator()
        operatingAnim.interpolator = lin
        iv_shinne.startAnimation(operatingAnim)
        val amount = arguments?.getString(AMOUNT) ?: "0"
        tv_amount.text = BuildConfig.SYSTEM_CURREMCY_SIGN + " " + TextUtil.format(amount)
        btn_ok.setOnClickListener {
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
                iv_buble_1.startAnimation(it)
                iv_buble_2.startAnimation(it)
                iv_buble_3.startAnimation(it)
                iv_buble_4.startAnimation(it)
            }
    }


}