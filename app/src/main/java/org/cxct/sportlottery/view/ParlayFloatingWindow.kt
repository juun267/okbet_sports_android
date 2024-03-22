package org.cxct.sportlottery.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import me.jessyan.autosize.utils.ScreenUtils
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.BetBarLayout2Binding
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.ui.betList.BetListFragment
import org.cxct.sportlottery.util.DisplayUtil.dp
import timber.log.Timber
import kotlin.math.abs


/**
 * 可以跟随手指移动的窗口
 */
class ParlayFloatingWindow @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    init {
//        setBackgroundResource(R.drawable.bg_rect_ffffff)
        initView()
    }

    lateinit var binding: BetBarLayout2Binding

    private fun initView() {
        binding = BetBarLayout2Binding.inflate(LayoutInflater.from(context), this, true)
    }


    private var mLastX = 0
    private var mLastY = 0

    lateinit var onViewClick: () -> Unit

    private var mLastDownX = 0
    private var mLastDownY = 0


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var x = event.rawX.toInt()
        //控制左边缘
        if (x <= width / 2) {
            x = width / 2
        }
        //控制右边缘
        if (x >= ScreenUtils.getScreenSize(context)[0] - width / 2) {
            x = ScreenUtils.getScreenSize(context)[0] - width / 2
        }

        var y = event.rawY.toInt()
        //控制下边缘
        if (y >= ScreenUtils.getScreenSize(context)[1] - 40.dp) {
            y = ScreenUtils.getScreenSize(context)[1] - 40.dp
        }
        //控制上边缘
        if (y <= ScreenUtils.getStatusBarHeight() + 20.dp) {
            y = ScreenUtils.getStatusBarHeight() + 20.dp
        }

        Timber.d("x:$x y:$y")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mLastDownX = event.rawX.toInt()
                mLastDownY = event.rawY.toInt()
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX: Int = x - mLastX
                val deltaY: Int = y - mLastY
                val translationX = translationX + deltaX
                val translationY = translationY + deltaY
//                Timber.d("translationX:$translationX translationY:$translationY")
                setTranslationX(translationX)
                setTranslationY(translationY)
            }

            MotionEvent.ACTION_UP -> {
                val upRawX = event.rawX.toInt()
                val upRawY = event.rawY.toInt()
                val deltaX: Int = upRawX - mLastDownX
                val deltaY: Int = upRawY - mLastDownY
                Timber.d("deltaX:$deltaX deltaY:$deltaY")
                if (abs(deltaX) < 20 && abs(deltaY) < 20) {
                    if (this::onViewClick.isInitialized) {
                        onViewClick()
                        return true
                    }
                }
                mLastDownX = upRawX
                mLastDownY = upRawY
            }
        }
        mLastX = x
        mLastY = y
        return true
    }

    fun setBetText(string: String) {
        binding.tvBetList.apply {
            text = string
            background = ContextCompat.getDrawable(context,R.drawable.bg_circle_yellow)
        }
    }
    fun updateCount(count: String){
        val cannotParlay =
            BetInfoRepository.currentBetType == BetListFragment.PARLAY
            && BetInfoRepository.betInfoList.value?.peekContent()?.any { it.pointMarked }==true
            && (BetInfoRepository.betIDList.value?.peekContent()?.size?:0>1 || (BetInfoRepository.betIDList.value?.peekContent()?.size ==1 && BetInfoRepository.betInfoList.value?.peekContent()?.firstOrNull()?.matchType== MatchType.OUTRIGHT))
        if (cannotParlay){
            binding.tvBetListCount.apply {
                text = ""
                background = ContextCompat.getDrawable(context,R.drawable.bg_parlay_circle_error)
            }
        }else{
            binding.tvBetListCount.apply {
                text = count
                background = ContextCompat.getDrawable(context,R.drawable.bg_circle_yellow)
            }
        }
    }
   fun showRedCount(count: String){
       binding.tvBetListCount.apply {
           text = count
           background = ContextCompat.getDrawable(context,R.drawable.bg_circle_red)
       }
   }
}
