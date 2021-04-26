package org.cxct.sportlottery.ui.bet.list

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_bet_info_item_action.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBetInfoItemSingleBinding
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.OnForbidClickListener
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds
import java.lang.Exception

const val NOT_INPLAY: Int = 0
const val INPLAY: Int = 1
const val CHANGING_ITEM_BG_COLOR_DURATION: Long = 3000

@SuppressLint("SetTextI18n", "ClickableViewAccessibility")
class BetInfoListAdapter(private val context: Context, private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<BetInfoListAdapter.ViewHolder>() {


    var focusPosition = -1


    var betInfoList: MutableList<BetInfoListData> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    var updatedBetInfoList: MutableList<Odd> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isNeedRegister: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    private fun updateItemDataFromSocket(matchOdd: MatchOdd, updatedBetInfoList: MutableList<Odd>) {
        for (newItem in updatedBetInfoList) {
            //null check後還是會crash 先以不crash為主
            try {
                newItem.id.let {
                    if (it == matchOdd.oddsId) {
                        matchOdd.oddState = getOddState(getOdds(matchOdd, oddsType), newItem)
                        newItem.odds?.let { odds -> matchOdd.odds = odds }
                        newItem.hkOdds?.let { hkOdds -> matchOdd.hkOdds = hkOdds }
                        newItem.status?.let { status -> matchOdd.status = status }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun getOddState(oldItemOdd: Double, it: Odd): Int {
        val odds = when (oddsType) {
            OddsType.EU -> it.odds
            OddsType.HK -> it.hkOdds
        }
        val newOdd = odds ?: 0.0
        return when {
            newOdd == oldItemOdd -> OddState.SAME.state
            newOdd > oldItemOdd -> OddState.LARGER.state
            newOdd < oldItemOdd -> OddState.SMALLER.state
            else -> OddState.SAME.state
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ContentBetInfoItemSingleBinding = ContentBetInfoItemSingleBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return betInfoList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        updateItemDataFromSocket(betInfoList[position].matchOdd, updatedBetInfoList)
        holder.bind(betInfoList[position], position)
    }


    inner class ViewHolder(private val binding: ContentBetInfoItemSingleBinding) : RecyclerView.ViewHolder(binding.root) {

        var inputError: Boolean = false

        private fun check(it: String, matchOdd: MatchOdd, parlayOdd: ParlayOdd) {

            if (TextUtils.isEmpty(it)) {
                binding.etBet.setBackgroundResource(R.drawable.effect_select_bet_edit_text)
                binding.betInfoAction.tv_bet_quota.text = "0.000"
                binding.betInfoAction.tv_win_quota.text = "0.000"
                binding.tvErrorMessage.visibility = View.GONE
                (binding.clInput.layoutParams as LinearLayout.LayoutParams).bottomMargin = 11.dp
                binding.betInfoAction.tv_bet.apply {
                    isClickable = true
                    background = ContextCompat.getDrawable(binding.root.context, R.drawable.bg_radius_4_button_orangelight)
                    setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorWhite))
                }
            } else {
                val quota = it.toLong()
                when {
                    quota > parlayOdd.max -> {
                        inputError = true
                        binding.tvErrorMessage.text =
                            String.format(binding.root.context.getString(R.string.bet_info_list_bigger_than_max_limit), TextUtil.formatMoneyNoDecimal(parlayOdd.max))
                        binding.etBet.setBackgroundResource(R.drawable.bg_radius_4_edittext_error)
                    }
                    quota < parlayOdd.min -> {
                        inputError = true
                        binding.tvErrorMessage.text = String.format(binding.root.context.getString(R.string.bet_info_list_less_than_minimum_limit), parlayOdd.min.toString())
                        binding.etBet.setBackgroundResource(R.drawable.bg_radius_4_edittext_error)
                    }
                    else -> {
                        inputError = false
                        binding.etBet.setBackgroundResource(R.drawable.effect_select_bet_edit_text)
                    }
                }
                var win = it.toDouble() * getOdds(matchOdd, oddsType)

                if (oddsType == OddsType.EU) {
                    win -= quota
                }
                binding.betInfoAction.tv_bet_quota.text = TextUtil.format(quota)
                binding.betInfoAction.tv_win_quota.text = TextUtil.format(win)

                binding.tvErrorMessage.visibility = if (inputError) View.VISIBLE else View.GONE
                (binding.clInput.layoutParams as LinearLayout.LayoutParams).bottomMargin = if (inputError) 0.dp else 11.dp
                binding.etBet.setTextColor(
                    if (inputError) ContextCompat.getColor(binding.root.context, R.color.colorRedDark) else ContextCompat.getColor(binding.root.context, R.color.main_dark)
                )
                binding.betInfoAction.tv_bet.apply {
                    isClickable = if (inputError) {
                        background = ContextCompat.getDrawable(binding.root.context, R.drawable.bg_radius_4_button_unselected)
                        setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorWhite))
                        false
                    } else {
                        background = ContextCompat.getDrawable(binding.root.context, R.drawable.bg_radius_4_button_orangelight)
                        setTextColor(ContextCompat.getColor(binding.root.context, R.color.colorWhite))
                        true
                    }
                }
            }
        }

        private fun componentStatusByOdds(
            betVisible: Int, warningVisible: Int, warningString: Int,
            betTextBg: Int, betTextColor: Int, clickable: Boolean,
            moreTextBg: Int, moreTextColor: Int, moreClickable: Boolean
        ) {
            binding.llBet.visibility = betVisible
            binding.tvCloseWarning.apply {
                visibility = warningVisible
                text = context.getString(warningString)
            }
            binding.betInfoAction.tv_bet.apply {
                background = ContextCompat.getDrawable(binding.root.context, betTextBg)
                setTextColor(ContextCompat.getColor(binding.root.context, betTextColor))
                isClickable = clickable
            }

            binding.betInfoAction.tv_add_more.apply {
                background = ContextCompat.getDrawable(binding.root.context, moreTextBg)
                setTextColor(ContextCompat.getColor(binding.root.context, moreTextColor))
                isClickable = moreClickable
            }

        }


        fun bind(mBetInfoList: BetInfoListData, position: Int) {

            /* fix focus */
            if (binding.etBet.tag is TextWatcher) {
                binding.etBet.removeTextChangedListener(binding.etBet.tag as TextWatcher)
            }
            binding.etBet.onFocusChangeListener = null


            val matchOdd = mBetInfoList.matchOdd
            val parlayOdd = mBetInfoList.parlayOdds
            binding.matchOdd = matchOdd
            binding.parlayOdd = parlayOdd
            binding.betInfoDetail.apply {
                when (mBetInfoList.matchType) {
                    MatchType.OUTRIGHT -> {
                        tvOddsSpread.visibility = View.GONE
                        tvMatch.visibility = View.GONE
                    }
                    else -> {
                        matchOdd.spread.apply {
                            if (isEmpty()) {
                                tvOddsSpread.visibility = View.GONE
                            } else {
                                tvOddsSpread.visibility = View.VISIBLE
                                tvOddsSpread.text = this
                            }
                        }
                        tvMatch.visibility = View.VISIBLE
                    }
                }
            }

            binding.etBet.hint = String.format(binding.root.context.getString(R.string.bet_info_list_hint), TextUtil.formatForBetHint(parlayOdd.max))
            binding.betInfoDetail.tvOdds.text = TextUtil.formatForOdd(getOdds(matchOdd, oddsType))
            binding.betInfoDetail.ivDelete.setOnClickListener { onItemClickListener.onDeleteClick(position) }
            binding.betInfoAction.tv_add_more.setOnClickListener { onItemClickListener.onAddMoreClick(mBetInfoList) }
            binding.ivClearText.setOnClickListener { binding.etBet.text.clear() }

            val strVerse = context.getString(R.string.verse_lower)
            val strMatch = "${matchOdd.homeName}${strVerse}${matchOdd.awayName}"

            binding.betInfoDetail.tvMatch.text = strMatch

            binding.betInfoDetail.tvName.text =
                if (matchOdd.inplay == INPLAY) {
                    context.getString(
                        R.string.bet_info_in_play_score,
                        matchOdd.playCateName,
                        matchOdd.homeScore.toString(),
                        matchOdd.awayScore.toString()
                    )
                } else matchOdd.playCateName

            binding.etBet.setText(mBetInfoList.input)
            check(binding.etBet.text.toString(), matchOdd, parlayOdd)

            /* check input focus */
            if (position == focusPosition) {
                binding.etBet.requestFocus()
                binding.etBet.setSelection(binding.etBet.text.length)
            }

            /* set listener */
            val tw = object : TextWatcher {
                override fun afterTextChanged(it: Editable?) {
                    check(it.toString(), matchOdd, parlayOdd)
                    if (TextUtils.isEmpty(it)) {
                        betInfoList[position].input = ""
                    } else {
                        betInfoList[position].input = it.toString()
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

            val fc = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    focusPosition = position
                    binding.etBet.requestFocus()
                } else {
                    binding.etBet.clearFocus()
                }
            }

            binding.etBet.onFocusChangeListener = fc
            binding.etBet.addTextChangedListener(tw)
            binding.etBet.tag = tw

            binding.etBet.setOnTouchListener { v, event ->
                onItemClickListener.onShowKeyboard(binding.etBet, matchOdd)
                false
            }


            when (matchOdd.status) {
                BetStatus.LOCKED.code, BetStatus.DEACTIVATED.code -> {
                    componentStatusByOdds(
                        betVisible = View.GONE,
                        warningVisible = View.VISIBLE,
                        warningString = R.string.bet_info_list_game_closed,
                        betTextBg = R.drawable.bg_radius_4_button_unselected,
                        betTextColor = R.color.colorWhite,
                        clickable = false,
                        moreTextBg = R.drawable.bg_radius_4_button_unselected,
                        moreTextColor = R.color.colorWhite,
                        moreClickable = false
                    )

                    binding.betInfoAction.tv_bet.apply {
                        background = ContextCompat.getDrawable(context, R.drawable.bg_radius_4_button_unselected)
                        setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                        isClickable = false
                        text = context.getString(R.string.bet_info_list_bet)
                    }

                }

                BetStatus.ACTIVATED.code -> {
                    componentStatusByOdds(
                        betVisible = View.VISIBLE,
                        warningVisible = View.GONE,
                        warningString = R.string.bet_info_list_bet,
                        betTextBg = R.drawable.bg_radius_4_button_orangelight,
                        betTextColor = R.color.colorWhite,
                        clickable = true,
                        moreTextBg = R.drawable.bg_radius_4_button_colorwhite6,
                        moreTextColor = R.color.colorGray,
                        moreClickable = true
                    )

                    binding.betInfoAction.tv_bet.apply {
                        background = ContextCompat.getDrawable(context, R.drawable.bg_radius_4_button_orangelight)
                        setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                        isClickable = true
                        text = context.getString(
                            if(mBetInfoList.oddsHasChanged)R.string.bet_info_list_odds_change else R.string.bet_info_list_bet
                        )
                    }

                    setChangeOdds(
                        binding.betInfoAction.tv_bet,
                        binding.betInfoDetail.tvOdds,
                        binding.tvCloseWarning,
                        matchOdd,
                        inputError
                    )
                }
            }
            binding.executePendingBindings()


            setupBetButton()
            setupRegisterButton()
        }

        private fun setupRegisterButton() {
            binding.betInfoAction.tv_register.apply {
                visibility = if (isNeedRegister) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }

                setOnClickListener {
                    onItemClickListener.onRegisterClick()
                }
            }
        }

        private fun setupBetButton() {
            binding.betInfoAction.tv_bet.apply {
                visibility = if (isNeedRegister) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }

                setOnClickListener(object : OnForbidClickListener() {
                    override fun forbidClick(view: View?) {
                        val stake = if (TextUtils.isEmpty(binding.etBet.text.toString())) {
                            0.0
                        } else {
                            binding.etBet.text.toString().toDouble()
                        }
                        onItemClickListener.onBetClick(betInfoList[position], stake)
                    }
                })
            }
        }
    }


    fun modify(list: MutableList<BetInfoListData>, position: Int) {
        if (betInfoList.size == 0) {
            betInfoList.addAll(list)
            notifyDataSetChanged()
        } else if (list.size < betInfoList.size) {
            with(betInfoList) {
                notifyItemRemoved(position)
                notifyItemRangeChanged(0, betInfoList.size)
                clear()
                addAll(list)
            }
        }
    }


    interface OnItemClickListener {
        fun onDeleteClick(position: Int)
        fun onBetClick(betInfoListData: BetInfoListData, stake: Double)
        fun onAddMoreClick(betInfoList: BetInfoListData)
        fun onRegisterClick()
        fun onShowKeyboard(editText: EditText, matchOdd: MatchOdd)
        fun saveOddsHasChanged(matchOdd: MatchOdd)
    }


    private fun setChangeOdds(tv_bet: TextView, tv_odds: TextView, tv_close_warning: TextView, matchOdd: MatchOdd, error: Boolean) {
        when (matchOdd.oddState) {
            OddState.LARGER.state -> {
                changeColorByOdds(tv_bet, tv_close_warning)
                tv_odds.apply {
                    setBackgroundColor(ContextCompat.getColor(tv_odds.context, R.color.orangeRed))
                    setTextColor(ContextCompat.getColor(tv_odds.context, R.color.colorWhite))
                    text = TextUtil.formatForOdd(getOdds(matchOdd, oddsType))
                }
                onItemClickListener.saveOddsHasChanged(matchOdd)
            }

            OddState.SMALLER.state -> {
                changeColorByOdds(tv_bet, tv_close_warning)
                tv_odds.apply {
                    setBackgroundColor(ContextCompat.getColor(tv_odds.context, R.color.orangeRed))
                    setTextColor(ContextCompat.getColor(tv_odds.context, R.color.colorWhite))
                    text = TextUtil.formatForOdd(getOdds(matchOdd, oddsType))
                }
                onItemClickListener.saveOddsHasChanged(matchOdd)
            }
        }

        Handler().postDelayed({
            when (error) {
                true -> {
                    tv_bet.apply {
                        background = ContextCompat.getDrawable(tv_bet.context, R.drawable.bg_radius_4_button_unselected)
                        setTextColor(ContextCompat.getColor(tv_bet.context, R.color.colorWhite))
                        isClickable = false
                    }
                }
                false -> {
                    tv_bet.apply {
                        background = ContextCompat.getDrawable(tv_bet.context, R.drawable.bg_radius_4_button_orangelight)
                        setTextColor(ContextCompat.getColor(tv_bet.context, R.color.colorWhite))
                        isClickable = true
                    }
                }
            }
            tv_close_warning.visibility = View.GONE
            tv_odds.apply {
                setBackgroundColor(ContextCompat.getColor(tv_odds.context, R.color.transparent))
                setTextColor(ContextCompat.getColor(tv_odds.context, R.color.colorOrange))
            }
        }, CHANGING_ITEM_BG_COLOR_DURATION)

    }


    private fun changeColorByOdds(tv_bet: TextView, tv_close_warning: TextView) {
        tv_bet.text = context.getText(R.string.bet_info_list_odds_change)
        tv_close_warning.apply {
            visibility = View.VISIBLE
            text = context.getString(R.string.bet_info_list_game_odds_changed)
        }
    }


}