package org.cxct.sportlottery.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import kotlinx.android.synthetic.main.dialog_redenvelope_fail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel

class RedEnvelopeFailDialog : BaseDialog<BaseViewModel>(BaseViewModel::class) {
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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.dialog_redenvelope_fail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startAnimation()
        btn_ok.setOnClickListener {
            dismiss()
        }
        val errorDesc = arguments?.getString(ERROR_DESC).orEmpty()
        if (errorDesc.isNotEmpty()) tv_error_desc.text = errorDesc
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