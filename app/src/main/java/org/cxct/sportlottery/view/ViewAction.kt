package org.cxct.sportlottery.view

import android.animation.ObjectAnimator
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cxct.sportlottery.ui.base.BaseViewModel
import java.util.concurrent.locks.ReentrantLock

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
            return@setOnClickListener
        }
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
            if (newState ==RecyclerView.SCROLL_STATE_IDLE ){
                if( lastVisibleItem + 1 ==adapter?.itemCount){
                    block()
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            lastVisibleItem =manager.findLastVisibleItemPosition()
        }
    })
}

//上次请求时间
private var lastRequestTime = 0L
fun updateLastRequestTime(){
    lastRequestTime=System.currentTimeMillis()
}
fun rumWithSlowRequest(viewModel:BaseViewModel, delayTime:Int=2200,  block : () -> Unit){
    //当前时间
    val currentTime=System.currentTimeMillis()
    //间隔时间
    val diffTime = currentTime - lastRequestTime
    //请求前  更新时间
    lastRequestTime=currentTime+diffTime
    viewModel.viewModelScope.launch {
        //间隔少于阈值
        if(diffTime<delayTime){
            //挂起
            delay(delayTime-diffTime)
        }
        //执行请求方法
        block()
        //请求后 更新时间
        lastRequestTime=System.currentTimeMillis()
    }
}

