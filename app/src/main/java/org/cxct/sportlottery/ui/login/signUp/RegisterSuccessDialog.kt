package org.cxct.sportlottery.ui.login.signUp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spanned
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_custom_alert.*
import kotlinx.android.synthetic.main.dialog_register_success.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.util.setTitleLetterSpacing

class RegisterSuccessDialog(context: Context) : DialogFragment() {


    private var mContext: Context = context
    private var mNegativeClickListener: View.OnClickListener = View.OnClickListener { dismiss() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return inflater.inflate(R.layout.dialog_register_success, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }
    val timer= object: CountDownTimer(5000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            var diff = millisUntilFinished
            val secondsInMilli: Long = 1000
            val elapsedSeconds = diff / secondsInMilli

            tvCount?.let {
                it.text = "($elapsedSeconds)"
            }
        }

        override fun onFinish() {
            dismiss()
            startActivity(Intent(mContext, GamePublicityActivity::class.java))
        }
    }
    private fun initView() {

        timer.start()
        tvVisitFirst.setOnClickListener {
            dismiss()
            startActivity(Intent(mContext, GamePublicityActivity::class.java))
        }
        tvJump.text = getString(R.string.register_jump,getString(R.string.app_name))
        btnRecharge.setOnClickListener (mNegativeClickListener)
        btnRecharge.setTitleLetterSpacing()
    }
    fun setNegativeClickListener(negativeClickListener: View.OnClickListener) {
        mNegativeClickListener = negativeClickListener
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }
}