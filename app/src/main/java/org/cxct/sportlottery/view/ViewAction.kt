package org.cxct.sportlottery.view

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.Log
import android.text.Editable
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cxct.sportlottery.ui.base.BaseViewModel

object ViewAction {
    const val emojiString="\uD83D\uDE00 \uD83D\uDE01 \uD83D\uDE06 \uD83D\uDE05 \uD83E\uDD23 \uD83D\uDE02 \uD83D\uDE42 \uD83D\uDE09 \uD83D\uDE0A \uD83D\uDE07 \uD83E\uDD70 \uD83D\uDE0D \uD83E\uDD29 \uD83D\uDE18 ☺️ \uD83E\uDD72 \uD83D\uDE1C \uD83D\uDE1D \uD83E\uDD11 \uD83E\uDD17 \uD83E\uDD2D \uD83E\uDEE3 \uD83E\uDD2B \uD83E\uDD14 \uD83E\uDEE1 \uD83E\uDD10 \uD83E\uDD28 \uD83D\uDE10 \uD83D\uDE11 \uD83D\uDE36 \uD83E\uDEE5 \uD83D\uDE36\u200D\uD83C\uDF2B️ \uD83D\uDE0F \uD83D\uDE12 \uD83D\uDE44 \uD83D\uDE2C \uD83D\uDE2E\u200D\uD83D\uDCA8 \uD83E\uDD25 \uD83E\uDEE8 \uD83D\uDE0C \uD83D\uDE14 \uD83D\uDE2A \uD83E\uDD24 \uD83D\uDE34 \uD83D\uDE37 \uD83E\uDD12 \uD83E\uDD15 \uD83E\uDD22 \uD83E\uDD2E \uD83E\uDD27 \uD83E\uDD75 \uD83E\uDD76 \uD83E\uDD74 \uD83D\uDE35 \uD83D\uDE35\u200D\uD83D\uDCAB \uD83E\uDD2F \uD83D\uDE0E \uD83D\uDE15 \uD83E\uDEE4 ☹️ \uD83D\uDE2F \uD83D\uDE32 \uD83D\uDE33 \uD83E\uDD7A \uD83E\uDD79 \uD83D\uDE28 \uD83D\uDE30 \uD83D\uDE22 \uD83D\uDE2D \uD83D\uDE31 \uD83D\uDE16 \uD83D\uDE13 \uD83D\uDE2B \uD83D\uDE24 \uD83D\uDE21 \uD83D\uDE20 \uD83D\uDE08 \uD83D\uDC80 ☠️ \uD83D\uDCA9 \uD83E\uDD21 \uD83D\uDC79 \uD83D\uDC7B \uD83D\uDC7D \uD83D\uDE3A \uD83D\uDE38 \uD83D\uDE39 \uD83D\uDE3B \uD83D\uDE3C \uD83D\uDE3D \uD83D\uDE40 \uD83D\uDE3F \uD83D\uDE3E \uD83D\uDE48 \uD83D\uDE49 \uD83D\uDE4A \uD83D\uDC8B \uD83D\uDCAF \uD83D\uDCA2 \uD83D\uDCA6 \uD83D\uDCA8 \uD83D\uDD73️ \uD83D\uDCA4 \uD83D\uDC4B \uD83D\uDD90️ \uD83E\uDEF1 \uD83E\uDEF2 \uD83D\uDC4C ✌️ \uD83D\uDC48 \uD83D\uDC49 \uD83D\uDC4D \uD83D\uDC4E \uD83D\uDC4A \uD83D\uDC4F \uD83E\uDEF6 \uD83E\uDD1D \uD83D\uDE4F \uD83D\uDC40 \uD83D\uDC94"
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
        this.layoutParams.height=-2
        this.requestLayout()
    }
    animator.start()
}

fun View.unExpand(height:Int){
    val animator=ObjectAnimator.ofInt(height,0)
    animator.duration = 200
    animator.addUpdateListener {
        this.layoutParams.height=0
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


fun View.isVisible():Boolean{
    return visibility==View.VISIBLE
}

fun EditText.insertEmoji(emoji:String){
    val index=selectionStart
    val text:Editable=text
    text.insert(index,emoji)
}



/**
 * @param tv         控件
 * @param startColor 开始颜色 Color.WHITE
 * @param endColor   结束颜色 Color.parseColor("#5A5A5A")
 * @param start      开始位置 0.4f
 * @param end        结束位置 0.9f
 */
fun TextView.setTextColorGradient( ) {
    val mLinearGradient = LinearGradient(0f, 0f, 0f,
        this.paint.textSize,
        Color.parseColor("#F2FFA0"),
        Color.parseColor("#FFC300"),
        Shader.TileMode.CLAMP)
    this.paint.shader = mLinearGradient
}