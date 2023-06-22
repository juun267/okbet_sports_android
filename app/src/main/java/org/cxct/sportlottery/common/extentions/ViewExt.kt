package org.cxct.sportlottery.common.extentions

import android.animation.*
import android.text.InputFilter
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListenerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import org.cxct.sportlottery.util.BreatheInterpolator
import org.cxct.sportlottery.util.ScreenUtil
import java.util.regex.Pattern

/**
 * 关于View的一些扩展函数
 */
inline fun View.show() {
    this.visibility = View.VISIBLE
}

inline fun View.hide() {
    this.visibility = View.GONE
}

inline fun View.visible() {
    this.visibility = View.VISIBLE
}

inline fun View.gone() {
    this.visibility = View.GONE
}

inline fun View.inVisible() {
    this.visibility = View.INVISIBLE
}

inline fun setViewVisible(vararg views: View) {
    views.forEach { it.visibility = View.VISIBLE }
}

fun setOnClickListeners(vararg view: View?, onClick: (View) -> Unit) {
    view.forEach {
        it?.setOnClickListener(onClick)
    }
}

inline fun setViewGone(vararg views: View) {
    views.forEach { it.visibility = View.GONE }
}

inline fun setViewInvisible(vararg views: View) {
    views.forEach { it.visibility = View.INVISIBLE }
}

inline fun View.getColor(@ColorRes id: Int): Int {
    return ContextCompat.getColor(context, id)
}

//私有扩展属性，允许2次点击的间隔时间
private var <T : View> T.delayTime: Long
    get() = getTag(0x7FFF0001) as? Long ?: 0
    set(value) {
        setTag(0x7FFF0001, value)
    }

//私有扩展属性，记录点击时的时间戳
private var <T : View> T.lastClickTime: Long
    get() = getTag(0x7FFF0002) as? Long ?: 0
    set(value) {
        setTag(0x7FFF0002, value)
    }

//私有扩展方法，判断能否触发点击事件
private fun <T : View> T.canClick(): Boolean {
    var flag = false
    var now = System.currentTimeMillis()
    if (now - this.lastClickTime >= this.delayTime) {
        flag = true
        this.lastClickTime = now
    }
    return flag
}

//扩展点击事件，默认 300ms 内不能触发 2 次点击
fun <T : View> T.clickWithDuration(time: Long = 300, block: (T) -> Unit) {
    delayTime = time
    setOnClickListener {
        if (canClick()) {
            block(this)
        }
    }
}

//顶部偏移状态栏高度
inline fun View.fitsSystemStatus() {

    val statuHeight = ScreenUtil.getStatusBarHeight(context)
    if (layoutParams?.height ?: 0 > 0) {
        layoutParams.height = layoutParams.height + statuHeight
    }
    setPadding(paddingLeft, paddingTop + statuHeight, paddingRight, paddingBottom)
}

inline fun RecyclerView.setLinearLayoutManager(
    @RecyclerView.Orientation orientation: Int = RecyclerView.VERTICAL,
    reverseLayout: Boolean = false): LinearLayoutManager {
    return LinearLayoutManager(context, orientation, reverseLayout).apply { layoutManager = this }
}

/**
 * 扩展方法 控件添加闪烁动画 可选传值
 * @duration 闪烁一次的时间
 * @repeatCount 闪烁几次
 * @startAlpha 开始的透明度 默认为全透明
 * @endAlpha 结束的透明度 默人为完全显示
 */
fun View.flashAnimation(
    duration: Long = 1000,
    repeatCount: Int = ValueAnimator.INFINITE,
    startAlpha: Float = 0f,
    endAlpha: Float = 1f
): ObjectAnimator {
    this.clearAnimation()

    val alphaAnimator: ObjectAnimator = ObjectAnimator.ofFloat(this, "alpha", startAlpha, endAlpha)
    alphaAnimator.duration = duration
    alphaAnimator.interpolator = BreatheInterpolator()
    alphaAnimator.repeatCount = repeatCount
    alphaAnimator.addListener(object : AnimatorListenerAdapter() {

        override fun onAnimationEnd(animation: Animator) {
            this@flashAnimation.alpha = 1f
        }

    })

    alphaAnimator.start()
    return alphaAnimator
}

fun View.alpahAnimation(
    duration: Long,
    startAlpha: Float,
    endAlpha: Float,
    onEnd: (() -> Unit)? = null
): ObjectAnimator {
    this.clearAnimation()

    val alphaAnimator: ObjectAnimator = ObjectAnimator.ofFloat(this, "alpha", startAlpha, endAlpha)
    alphaAnimator.duration = duration
    onEnd?.let {
        alphaAnimator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator) {
                it.invoke()
            }

        })
    }

    alphaAnimator.start()
    return alphaAnimator
}



fun View.translationXAnimation(x: Float, endCall: (() -> Unit)? = null, duration: Long = 200) {
    val anim = ViewCompat.animate(this)
        .setDuration(duration)
        .setInterpolator(DecelerateInterpolator())
        .translationX(x)

    endCall?.let {
        anim.setListener(object : ViewPropertyAnimatorListenerAdapter() {
            override fun onAnimationEnd(view: View) {
                it.invoke()
            }
        })
    }

    anim.start()
}

fun <T> BaseQuickAdapter<T, *>.showLoading(@LayoutRes layoutId: Int) {
    if (data.isNotEmpty()) {
        setNewInstance(null)
    }
    this.setEmptyView(layoutId)
}

fun <T> BaseQuickAdapter<T, *>.showEmpty(@LayoutRes layoutId: Int) {
    this.setEmptyView(layoutId)
}

fun View.rotationAnimation(rotation: Float, duration: Long = 200) {
    ViewCompat.animate(this)
        .setDuration(duration)
//        .setInterpolator(DecelerateInterpolator())
        .rotation(rotation)
        .start()
}

fun View.animDuang(scale: Float, duration: Long = 500) {
    isEnabled = false
    val animatorSet = AnimatorSet()
    val scaleX = ObjectAnimator.ofFloat(this,"scaleX", 1f, scale, 1f)
    val scaleY = ObjectAnimator.ofFloat(this,"scaleY", 1f, scale, 1f)
    animatorSet.play(scaleX).with(scaleY)
    animatorSet.interpolator = BounceInterpolator()
    animatorSet.duration = duration
    animatorSet.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            isEnabled = true
        }
    })
    animatorSet.start()
}


fun EditText.filterSpecialCharacters() {
    val spaceFilter = InputFilter { source, _, _, _, _, _ ->
        if (source == " ") {
            ""
        } else {
            null
        }
    }

    val specialFilter = InputFilter { source, _, _, _, _, _ ->
        val speChat = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“'。，、？]"
        val pattern = Pattern.compile(speChat)
        val matcher = pattern.matcher(source.toString())
        if (matcher.find()) "" else null
    }

    filters = arrayOf(spaceFilter, specialFilter)
}


fun EditText.onConfirm(block: (String) -> Unit) {
    imeOptions = EditorInfo.IME_ACTION_DONE
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
            block.invoke(text.toString())
            return@setOnEditorActionListener true
        }

        return@setOnEditorActionListener false
    }
}