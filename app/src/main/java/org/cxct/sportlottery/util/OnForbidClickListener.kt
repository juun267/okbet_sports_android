package org.cxct.sportlottery.util

import android.view.View

abstract class OnForbidClickListener : View.OnClickListener {


    companion object {
        private const val DELAYED_TIME: Long = 500
    }


    private var lastTime: Long = 0


    abstract fun forbidClick(view: View?)


    override fun onClick(v: View) {
        if (System.currentTimeMillis() - lastTime > DELAYED_TIME) {
            lastTime = System.currentTimeMillis()
            forbidClick(v)
        }
    }


}