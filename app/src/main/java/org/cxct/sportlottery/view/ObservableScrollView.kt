package org.cxct.sportlottery.view

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView
import org.cxct.sportlottery.view.ObservableScrollView.OnScrollStatusListener

class ObservableScrollView : NestedScrollView {
    private var onScrollStatusListener: OnScrollStatusListener? = null

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr) {
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (onScrollStatusListener != null) {
            onScrollStatusListener!!.onScrolling()
            mHandler.removeCallbacksAndMessages(null)
            mHandler.sendEmptyMessageDelayed(0x01, 200)
        }
    }

    fun setOnScrollStatusListener(onScrollStatusListener: OnScrollStatusListener?) {
        this.onScrollStatusListener = onScrollStatusListener
    }

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                0x01 -> if (onScrollStatusListener != null) {
                    onScrollStatusListener!!.onScrollStop()
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandler.removeCallbacksAndMessages(null)
    }

    interface OnScrollStatusListener {
        fun onScrollStop()
        fun onScrolling()
    }
}