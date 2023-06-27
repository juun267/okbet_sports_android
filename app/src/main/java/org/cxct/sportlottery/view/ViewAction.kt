package org.cxct.sportlottery.view

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.util.Log
import android.view.TextureView
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.util.DisplayUtil.dp
import java.util.Date

object ViewAction {

}


private var lastClickTime = 0L

/**
 * 防连续点击click
 */
fun View.onClick(time:Int=200,block: () -> Unit) {
    this.setOnClickListener {
        val systemTime =System.currentTimeMillis()
        if (systemTime - lastClickTime < time) {
            Log.e("dachang","systemTime ${systemTime }   lastClickTime${lastClickTime}  time${time}")
            return@setOnClickListener
        }
        Log.e("dachang","onClick")
        lastClickTime=systemTime
        block()
    }
}


fun TextView.setColors(colorResource:Int){
    setTextColor(ContextCompat.getColor(this.context,colorResource))
}

fun View.expand(height:Int){
    val animator=ObjectAnimator.ofInt(height)
    animator.duration = 200
    animator.addUpdateListener {
        this.layoutParams.height=animator.animatedValue as Int
        this.requestLayout()
    }
    animator.start()
}

fun View.unExpand(height:Int){
    val animator=ObjectAnimator.ofInt(height,0)
    animator.duration = 200
    animator.addUpdateListener {
        this.layoutParams.height=animator.animatedValue as Int
        this.requestLayout()
    }
    animator.start()
}


fun RecyclerView.loadMore(block: () -> Unit){
    var lastVisibleItem=0
    val manager=layoutManager as LinearLayoutManager
    addOnScrollListener(object:RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState ==RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 ==adapter?.itemCount){
                block()
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            lastVisibleItem =manager.findLastVisibleItemPosition()
        }
    })
}

