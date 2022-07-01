package org.cxct.sportlottery.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import kotlinx.android.synthetic.main.dialog_redenvelope_success.*
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.base.BaseViewModel
import org.cxct.sportlottery.util.TextUtil

class RedEnvelopeFailDialog : BaseDialog<BaseViewModel>(BaseViewModel::class) {
    init {
        setStyle(R.style.CustomDialogStyle)
    }
    companion object {
        @JvmStatic
        fun newInstance() = RedEnvelopeFailDialog()
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
        val operatingAnim = AnimationUtils.loadAnimation(
            activity, R.anim.red_envelope_rotate
        )
        val lin = LinearInterpolator()
        operatingAnim.interpolator = lin
        iv_shinne.startAnimation(operatingAnim)
        iv_close.setOnClickListener {
            dismiss()
        }
    }
}