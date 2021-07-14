package org.cxct.sportlottery.ui.bet.list


import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.SpreadState
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds


/**
 * @author Kevin
 * @create 2021/7/14
 * @description
 */
object OddSpannableString {


    //多筆注單回調
    interface StateChangeListener {
        fun refreshMatchState(matchOdd: MatchOdd)
    }


    var stateChangeListener: StateChangeListener? = null


    private const val HIGH_LIGHT_TIME: Long = 3000


    private val mHandler: Handler by lazy { Handler() }


    private lateinit var playNameSpan: SpannableString
    private lateinit var spreadSpan: SpannableString
    private lateinit var oddsSpan: SpannableString


    fun setupOddsContent(matchOdd: MatchOdd, oddsType: OddsType, textView: TextView?) {
        if (textView == null) return
        setupPlayNameSpannableString(matchOdd)

        setupSpreadSpannableString(textView.context, matchOdd)

        setupOddsSpannableString(textView.context, matchOdd, oddsType)

        mergeOddContent(textView)

        if (matchOdd.spreadState != SpreadState.SAME.state || matchOdd.oddState != MatchOdd.OddState.SAME.state) {
            matchOdd.runnable?.let {
                mHandler.removeCallbacks(it)
            }
            val runnable = highLightRunnable(matchOdd, oddsType, textView)
            matchOdd.runnable = runnable
            mHandler.postDelayed(runnable, HIGH_LIGHT_TIME)
        }
    }


    private fun setupPlayNameSpannableString(matchOdd: MatchOdd) {
        playNameSpan = SpannableString(matchOdd.playName)
        playNameSpan.setSpan(StyleSpan(Typeface.BOLD), 0, matchOdd.playName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }


    private fun setupSpreadSpannableString(context: Context, matchOdd: MatchOdd) {
        val isChanged = matchOdd.spreadState != SpreadState.SAME.state

        val textColor = ContextCompat.getColor(context, if (isChanged) R.color.colorWhite else R.color.colorRedDark)
        val backgroundColor = ContextCompat.getColor(context, if (isChanged) R.color.colorRed else R.color.colorWhite)

        val spreadEnd = matchOdd.spread.length + 1
        spreadSpan = SpannableString(" ${matchOdd.spread}")
        spreadSpan.setSpan(ForegroundColorSpan(textColor), 1, spreadEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spreadSpan.setSpan(StyleSpan(Typeface.BOLD), 1, spreadEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spreadSpan.setSpan(BackgroundColorSpan(backgroundColor), 1, spreadEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }


    private fun setupOddsSpannableString(context: Context, matchOdd: MatchOdd, oddsType: OddsType) {
        val isChanged = matchOdd.oddState != MatchOdd.OddState.SAME.state

        val textColor = ContextCompat.getColor(context, if (isChanged) R.color.colorWhite else R.color.colorBlackLight)
        val backgroundColor = ContextCompat.getColor(context, if (isChanged) R.color.colorRed else R.color.colorWhite)

        val oddsEnd = TextUtil.formatForOdd(getOdds(matchOdd, oddsType)).length
        oddsSpan = SpannableString(TextUtil.formatForOdd(getOdds(matchOdd, oddsType)))
        oddsSpan.setSpan(ForegroundColorSpan(textColor), 0, oddsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        oddsSpan.setSpan(StyleSpan(Typeface.BOLD), 0, oddsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        oddsSpan.setSpan(BackgroundColorSpan(backgroundColor), 0, oddsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }


    private fun mergeOddContent(textView: TextView) {
        val oddContentBuilder = SpannableStringBuilder()

        oddContentBuilder.append(playNameSpan)
        oddContentBuilder.append(spreadSpan)
        oddContentBuilder.append(" ＠ ")
        oddContentBuilder.append(oddsSpan)

        textView.text = oddContentBuilder
    }


    private fun highLightRunnable(matchOdd: MatchOdd, oddsType: OddsType, textView: TextView?): Runnable {
        return Runnable {
            matchOdd.oddState = OddState.SAME.state
            matchOdd.spreadState = SpreadState.SAME.state
            matchOdd.runnable = null
            setupOddsContent(matchOdd, oddsType, textView)
        }
    }


}