package org.cxct.sportlottery.ui.betList.holder

import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddState
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.enums.SpreadState
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate.Companion.needShowSpread
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds

abstract class BetInfoChangeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    companion object{
       private const val HIGH_LIGHT_TIME: Long = 3000
    }

    private val mHandler: Handler by lazy { Handler() }


    private lateinit var playNameSpan: SpannableString
    private lateinit var spreadSpan: SpannableString
    private lateinit var oddsSpan: SpannableString
    private lateinit var oddsTypeSpan: SpannableString
    private var extInfo: SpannableString? = null


    fun setupOddsContent(betInfoData: BetInfoListData, oddsType: OddsType, textView: TextView?) {
        if (textView == null) return

        val matchType = betInfoData.matchType
        val matchOdd = betInfoData.matchOdd

        when{
            matchOdd.spreadState == SpreadState.SAME && matchOdd.oddState == OddState.SAME.state -> {
                matchOdd.runnable?.let {
                    return
                }?:run { setupMergeString(matchType, matchOdd, oddsType, textView, isSpreadChanged = false, isOddsChanged = false) }
            }

            matchOdd.spreadState != SpreadState.SAME && matchOdd.oddState == OddState.SAME.state -> {
                setupMergeString(betInfoData.matchType,matchOdd, oddsType, textView, isSpreadChanged = true, isOddsChanged = false)
                resetRunnable(matchType, matchOdd, oddsType, textView)
            }

            matchOdd.spreadState == SpreadState.SAME && matchOdd.oddState != OddState.SAME.state -> {
                setupMergeString(betInfoData.matchType,matchOdd, oddsType, textView, isSpreadChanged = false, isOddsChanged = true)
                resetRunnable(matchType, matchOdd, oddsType, textView)
            }

            matchOdd.spreadState != SpreadState.SAME && matchOdd.oddState != OddState.SAME.state -> {
                setupMergeString(betInfoData.matchType,matchOdd, oddsType, textView, isSpreadChanged = true, isOddsChanged = true)
                resetRunnable(matchType, matchOdd, oddsType, textView)
            }
        }
    }


    private fun setupMergeString(matchType: MatchType?, matchOdd: MatchOdd, oddsType: OddsType, textView: TextView?, isSpreadChanged: Boolean, isOddsChanged: Boolean) {
        if (textView == null) return

        setupPlayNameSpannableString(matchOdd)

        setupExtInfoSpannableString(matchType, matchOdd)

        setupSpreadSpannableString(textView.context, matchType, matchOdd, isSpreadChanged)

        setupOddsSpannableString(textView.context, matchOdd, isOddsChanged, oddsType)

        //setupOddsTypeSpannableString(textView.context, oddsType)


        mergeString(textView)
    }


    private fun setupPlayNameSpannableString(matchOdd: MatchOdd) {
        playNameSpan = SpannableString(matchOdd.playName)
        playNameSpan.setSpan(StyleSpan(Typeface.BOLD), 0, matchOdd.playName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun setupExtInfoSpannableString(matchType: MatchType?, matchOdd: MatchOdd) {
        if(!matchOdd.extInfo.isNullOrEmpty() && matchType != MatchType.EPS){
            matchOdd.extInfo?.let {
                extInfo = SpannableString(matchOdd.extInfo + " ")
                extInfo?.setSpan(StyleSpan(Typeface.BOLD), 0, it.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private fun setupSpreadSpannableString(context: Context, matchType: MatchType?, matchOdd: MatchOdd, isChanged: Boolean) {
        val textColor = ContextCompat.getColor(context,
            if (isChanged) R.color.color_191919_FCFCFC else R.color.color_F75452_E23434)
        val backgroundColor = ContextCompat.getColor(context,
            if (isChanged) R.color.color_FF9143_cb7c2e else R.color.transparent_black_0)

        if (matchOdd.spread.isEmpty() || !needShowSpread(matchOdd.playCode) || matchType == MatchType.OUTRIGHT
        ) {
            spreadSpan = SpannableString("")
        }else {
            val spreadEnd = matchOdd.spread.length + 1
            spreadSpan = SpannableString(" ${matchOdd.spread}")
            spreadSpan.setSpan(ForegroundColorSpan(textColor), 1, spreadEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spreadSpan.setSpan(StyleSpan(Typeface.BOLD), 1, spreadEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spreadSpan.setSpan(BackgroundColorSpan(backgroundColor), 1, spreadEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }


    private fun setupOddsSpannableString(context: Context, matchOdd: MatchOdd, isChanged: Boolean, oddsType: OddsType) {
        val textColor = ContextCompat.getColor(context, if (isChanged) R.color.color_191919_FCFCFC else R.color.color_BBBBBB_333333)
        val backgroundColor = ContextCompat.getColor(context, if (isChanged) R.color.color_FF9143_cb7c2e else R.color.transparent_black_0)

        val odds =
            if (matchOdd.status == BetStatus.ACTIVATED.code) TextUtil.formatForOdd(getOdds(matchOdd, oddsType)) else "–"
        val oddsEnd = odds.length
        oddsSpan = SpannableString(odds)
        oddsSpan.setSpan(ForegroundColorSpan(textColor), 0, oddsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        oddsSpan.setSpan(StyleSpan(Typeface.BOLD), 0, oddsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        oddsSpan.setSpan(BackgroundColorSpan(backgroundColor), 0, oddsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun setupOddsTypeSpannableString(context: Context, oddsType: OddsType) {
        val textColor = ContextCompat.getColor(context,  R.color.color_909090_666666)
        val backgroundColor = ContextCompat.getColor(context,  R.color.transparent_black_0)

        val oddsType = " ("+context.getString(oddsType.res)+")"
        val oddsEnd = oddsType.length
        oddsTypeSpan = SpannableString(oddsType)
        oddsTypeSpan.setSpan(ForegroundColorSpan(textColor), 0, oddsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        oddsTypeSpan.setSpan(BackgroundColorSpan(backgroundColor), 0, oddsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }


    private fun mergeString(textView: TextView) {
        val oddContentBuilder = SpannableStringBuilder()

        if(!extInfo.isNullOrEmpty()){
            oddContentBuilder.append(extInfo)
        }
        oddContentBuilder.append(playNameSpan)
        oddContentBuilder.append(spreadSpan)
        oddContentBuilder.append(" ＠ ")
        oddContentBuilder.append(oddsSpan)
        //oddContentBuilder.append(oddsTypeSpan)

        textView.text = oddContentBuilder
    }


    private fun highLightRunnable(matchType: MatchType?, matchOdd: MatchOdd, oddsType: OddsType, textView: TextView?): Runnable {
        return Runnable {
            setupMergeString(matchType, matchOdd, oddsType, textView, isSpreadChanged = false, isOddsChanged = false)
            matchOdd.oddState = OddState.SAME.state
            matchOdd.spreadState = SpreadState.SAME
            matchOdd.runnable = null
        }
    }


    private fun resetRunnable(matchType: MatchType?, matchOdd: MatchOdd, oddsType: OddsType, textView: TextView?){
        matchOdd.runnable?.let {
            mHandler.removeCallbacks(it)
        }
        val runnable = highLightRunnable(matchType, matchOdd, oddsType, textView)
        matchOdd.runnable = runnable
        mHandler.postDelayed(runnable, HIGH_LIGHT_TIME)
    }


    fun clearHandler(){
        mHandler.removeCallbacksAndMessages(null)
    }


}