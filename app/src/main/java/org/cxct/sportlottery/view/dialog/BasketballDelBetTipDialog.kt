package org.cxct.sportlottery.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.dialog_basketball_delete_bet_tip.*
import kotlinx.android.synthetic.main.dialog_custom_alert.btn_negative
import kotlinx.android.synthetic.main.dialog_custom_alert.btn_positive
import org.cxct.sportlottery.R

/**
 * 常用提示對話框
 * 預設按鈕: 取消 / 確定
 */
class BasketballDelBetTipDialog(context: Context) : Dialog(context) {

    private var mPositiveClickListener: OnPositiveListener? = null
    private var mNegativeClickListener: View.OnClickListener = View.OnClickListener { dismiss() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.requestFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_basketball_delete_bet_tip)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        btn_positive.setOnClickListener {
            mPositiveClickListener?.positiveClick(cb_ok_iknow.isChecked)
            dismiss()
        }
        btn_negative.setOnClickListener(mNegativeClickListener)
        context?.let {
            Glide.with(it).load(R.drawable.basketball_delete_bet_tip).into(iv_gif_basket)
        }
    }

    fun setPositiveClickListener(positiveClickListener: OnPositiveListener) {
        mPositiveClickListener = positiveClickListener
    }

    fun setNegativeClickListener(negativeClickListener: View.OnClickListener) {
        mNegativeClickListener = negativeClickListener
    }

    interface OnPositiveListener {
        fun positiveClick(isCheck: Boolean)
    }

    //建造者类
    class Builder(context: Context) {
        var mPerson = BasketballDelBetTipDialog(context)  //实例化人物类

        fun setPositiveListener(positiveClickListener: OnPositiveListener): Builder {//设置名字
            mPerson.mPositiveClickListener = positiveClickListener
            return this
        }

        fun setNegativeClickListener(negativeClickListener: View.OnClickListener): Builder { //设置职位
            mPerson.mNegativeClickListener = negativeClickListener
            return this
        }

        fun create(): BasketballDelBetTipDialog {//创建Computer类的实例化
            return mPerson
        }
    }


}