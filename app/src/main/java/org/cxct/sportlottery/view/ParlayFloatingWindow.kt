package org.cxct.sportlottery.view


import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.BetBarLayout2Binding
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.ui.betList.BetListFragment
import org.cxct.sportlottery.view.floatingbtn.SuckEdgeTouch

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
        setOnTouchListener(SuckEdgeTouch())
        setOnClickListener { onViewClick.invoke() }
    }

    lateinit var onViewClick: () -> Unit

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
