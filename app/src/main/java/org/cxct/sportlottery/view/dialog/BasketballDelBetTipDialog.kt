package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import com.bumptech.glide.Glide
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBasketballDeleteBetTipBinding

/**
 * 常用提示對話框
 * 預設按鈕: 取消 / 確定
 */
class BasketballDelBetTipDialog(context: Context) : Dialog(context) {


    private var mPositiveClickListener: OnPositiveListener? = null
    private var mNegativeClickListener: OnNegativeListener? = null
    private lateinit var binding: DialogBasketballDeleteBetTipBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.requestFeature(Window.FEATURE_NO_TITLE)
        binding = DialogBasketballDeleteBetTipBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        binding.btnPositive.setOnClickListener {
            dismiss()
        }

        binding.btnNegative.setOnClickListener {
            mNegativeClickListener?.negativeClick(binding.cbOkIknow.isChecked)
        }
        Glide.with(context).load(R.drawable.basketball_delete_bet_tip).into(binding.ivGifBasket)

    }

    fun setPositiveClickListener(positiveClickListener: OnPositiveListener) {
        mPositiveClickListener = positiveClickListener
    }

    fun setNegativeClickListener(negativeClickListener: OnNegativeListener) {
        mNegativeClickListener = negativeClickListener
    }

    interface OnPositiveListener {
        fun positiveClick(isCheck: Boolean)
    }

    interface OnNegativeListener {
        fun negativeClick(isCheck: Boolean)
    }


}