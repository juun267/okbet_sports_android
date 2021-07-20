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
 * @description 共用多筆注單
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

        when{
            matchOdd.spreadState == SpreadState.SAME.state && matchOdd.oddState == MatchOdd.OddState.SAME.state -> {
                matchOdd.runnable?.let {
                    return
                }?:run { setupMergeString(matchOdd, oddsType, textView, isSpreadChanged = false, isOddsChanged = false) }
            }

            matchOdd.spreadState != SpreadState.SAME.state && matchOdd.oddState == MatchOdd.OddState.SAME.state -> {
                setupMergeString(matchOdd, oddsType, textView, isSpreadChanged = true, isOddsChanged = false)
                resetRunnable(matchOdd, oddsType, textView)
            }

            matchOdd.spreadState == SpreadState.SAME.state && matchOdd.oddState != MatchOdd.OddState.SAME.state -> {
                setupMergeString(matchOdd, oddsType, textView, isSpreadChanged = false, isOddsChanged = true)
                resetRunnable(matchOdd, oddsType, textView)
            }

            matchOdd.spreadState != SpreadState.SAME.state && matchOdd.oddState != MatchOdd.OddState.SAME.state -> {
                setupMergeString(matchOdd, oddsType, textView, isSpreadChanged = true, isOddsChanged = true)
                resetRunnable(matchOdd, oddsType, textView)
            }
        }
    }


    private fun setupMergeString(matchOdd: MatchOdd, oddsType: OddsType, textView: TextView?, isSpreadChanged: Boolean, isOddsChanged: Boolean) {
        if (textView == null) return

        setupPlayNameSpannableString(matchOdd)

        setupSpreadSpannableString(textView.context, matchOdd, isSpreadChanged)

        setupOddsSpannableString(textView.context, matchOdd, isOddsChanged, oddsType)

        mergeString(textView)
    }


    private fun setupPlayNameSpannableString(matchOdd: MatchOdd) {
        playNameSpan = SpannableString(matchOdd.playName)
        playNameSpan.setSpan(StyleSpan(Typeface.BOLD), 0, matchOdd.playName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }


    private fun setupSpreadSpannableString(context: Context, matchOdd: MatchOdd, isChanged: Boolean) {
        val textColor = ContextCompat.getColor(context, if (isChanged) R.color.colorWhite else R.color.colorRedDark)
        val backgroundColor = ContextCompat.getColor(context, if (isChanged) R.color.colorRed else R.color.transparent)

        val spreadEnd = matchOdd.spread.length + 1
        spreadSpan = SpannableString(" ${matchOdd.spread}")
        spreadSpan.setSpan(ForegroundColorSpan(textColor), 1, spreadEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spreadSpan.setSpan(StyleSpan(Typeface.BOLD), 1, spreadEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spreadSpan.setSpan(BackgroundColorSpan(backgroundColor), 1, spreadEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }


    private fun setupOddsSpannableString(context: Context, matchOdd: MatchOdd, isChanged: Boolean, oddsType: OddsType) {
        val textColor = ContextCompat.getColor(context, if (isChanged) R.color.colorWhite else R.color.colorBlackLight)
        val backgroundColor = ContextCompat.getColor(context, if (isChanged) R.color.colorRed else R.color.transparent)

        val odds = TextUtil.formatForOdd(getOdds(matchOdd, oddsType))
        val oddsEnd = odds.length
        oddsSpan = SpannableString(odds)
        oddsSpan.setSpan(ForegroundColorSpan(textColor), 0, oddsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        oddsSpan.setSpan(StyleSpan(Typeface.BOLD), 0, oddsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        oddsSpan.setSpan(BackgroundColorSpan(backgroundColor), 0, oddsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }


    private fun mergeString(textView: TextView) {
        val oddContentBuilder = SpannableStringBuilder()

        oddContentBuilder.append(playNameSpan)
        oddContentBuilder.append(spreadSpan)
        oddContentBuilder.append(" ＠ ")
        oddContentBuilder.append(oddsSpan)

        textView.text = oddContentBuilder
    }


    private fun highLightRunnable(matchOdd: MatchOdd, oddsType: OddsType, textView: TextView?): Runnable {
        return Runnable {
            setupMergeString(matchOdd, oddsType, textView, isSpreadChanged = false, isOddsChanged = false)
            matchOdd.oddState = OddState.SAME.state
            matchOdd.spreadState = SpreadState.SAME.state
            matchOdd.runnable = null
        }
    }


    private fun resetRunnable(matchOdd: MatchOdd, oddsType: OddsType, textView: TextView?){
        matchOdd.runnable?.let {
            mHandler.removeCallbacks(it)
        }
        val runnable = highLightRunnable(matchOdd, oddsType, textView)
        matchOdd.runnable = runnable
        mHandler.postDelayed(runnable, HIGH_LIGHT_TIME)
    }


    fun clearHandler(){
        mHandler.removeCallbacksAndMessages(null)
    }


}