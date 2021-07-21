package org.cxct.sportlottery.ui.game.betList

import android.annotation.SuppressLint
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBetListItemBinding
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.network.odds.list.OddState
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.bet.list.CHANGING_ITEM_BG_COLOR_DURATION
import org.cxct.sportlottery.ui.bet.list.INPLAY
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds
import timber.log.Timber

class BetListDiffAdapter(private val onItemClickListener: OnItemClickListener) : ListAdapter<DataItem, RecyclerView.ViewHolder>(BetListDiffCallBack()) {
    var focusPosition = -1


    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val mHandler by lazy { Handler() }

    private enum class ViewType { Bet, NoData }

    fun setupDataList(dataList: MutableList<BetInfoListData>) {
        val itemList = when {
            dataList.isEmpty() -> listOf(DataItem.NoData())
            else -> dataList.map { DataItem.BetInfoData(it) }
        }
        submitList(itemList)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.NoData -> ViewType.NoData.ordinal
            else -> ViewType.Bet.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.NoData.ordinal -> fromNoData(parent)
            else -> fromViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Timber.e("Dean, bind view holder itemCount = $itemCount , currentList = $currentList")
        val itemData = getItem(holder.adapterPosition)
        when (holder) {
            is ViewHolder -> holder.bind((itemData as DataItem.BetInfoData).betInfoListData, oddsType)
        }
    }

    private fun fromViewHolder(viewGroup: ViewGroup): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        val binding: ContentBetListItemBinding =
            ContentBetListItemBinding.inflate(layoutInflater, viewGroup, false)
        return ViewHolder(binding)
    }

    private fun fromNoData(viewGroup: ViewGroup): RecyclerView.ViewHolder {
        //TODO 目前缺少沒有投注單的圖片, 待更新後更換layout
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        val view = layoutInflater.inflate(R.layout.view_no_record, viewGroup, false)
        return NoDataViewHolder(view)
    }

    inner class ViewHolder(val binding: ContentBetListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        var inputError: Boolean = false

        @SuppressLint("ClickableViewAccessibility")
        fun bind(itemData: BetInfoListData, oddsType: OddsType) {
            binding.apply {
                matchOdd = itemData.matchOdd
                parlayOdd = itemData.parlayOdds
                betInfoDetail.apply {
                    when (itemData.matchType) {
                        MatchType.OUTRIGHT -> {
                            tvOddsSpread.visibility = View.GONE
                            tvMatch.visibility = View.GONE
                        }
                        else -> {
                            itemData.matchOdd.spread.apply {
                                tvOddsSpread.visibility = if (isEmpty()) {
                                    View.GONE
                                } else {
                                    tvOddsSpread.text = this
                                    View.VISIBLE
                                }
                            }
                            tvMatch.visibility = View.VISIBLE
                        }
                    }
                }
                itemData.parlayOdds?.let {
                    etBet.hint = String.format(root.context.getString(R.string.bet_info_list_hint), TextUtil.formatForBetHint(it.max))
                }

                betInfoDetail.tvOdds.text = TextUtil.formatForOdd(getOdds(itemData.matchOdd, oddsType))
                betInfoDetail.ivDelete.setOnClickListener {
                    Timber.e("Dean, delete click event")
                    onItemClickListener.onDeleteClick(itemData.matchOdd.oddsId, itemCount)
                }
                ivClearText.setOnClickListener { etBet.text.clear() }

                val strVerse = root.context.getString(R.string.verse_)
                val strMatch = "${itemData.matchOdd.homeName}${strVerse}${itemData.matchOdd.awayName}"

                betInfoDetail.tvMatch.text = strMatch

                betInfoDetail.tvName.text =
                    if (itemData.matchOdd.inplay == INPLAY) {
                        root.context.getString(
                            R.string.bet_info_in_play_score,
                            itemData.matchOdd.playCateName,
                            itemData.matchOdd.homeScore.toString(),
                            itemData.matchOdd.awayScore.toString()
                        )
                    } else itemData.matchOdd.playCateName

                etBet.setText(itemData.input)

                check(etBet.text.toString(), itemData.matchOdd, itemData.parlayOdds, itemData)

                /* check input focus */
                if (adapterPosition == focusPosition) {
                    etBet.requestFocus()
                    etBet.setSelection(binding.etBet.text.length)
                } else {
                    etBet.clearFocus()
                }

                /* set listener */
                val tw = object : TextWatcher {
                    override fun afterTextChanged(it: Editable?) {
                        itemData.parlayOdds?.let { pOdd ->
                            check(it.toString(), itemData.matchOdd, pOdd, itemData)
                        }
                        if (TextUtils.isEmpty(it)) {
                            itemData.input = ""
                        } else {
                            itemData.input = it.toString()
                        }
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                }

                val fc = View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus && adapterPosition != focusPosition)
                        binding.etBet.clearFocus()
                }

                etBet.apply {
                    onFocusChangeListener = fc
                    addTextChangedListener(tw)
                    tag = tw

                    setOnTouchListener { _, event ->
                        //若回傳true則不會觸發onTouchEvent,onClick
                        if (event.action == MotionEvent.ACTION_UP) {
                            focusPosition = adapterPosition
                            onItemClickListener.onShowKeyboard(binding.etBet, itemData.matchOdd)
                        }
                        false
                    }
                }

                tvCloseWarning.text = root.context.getString(
                    itemData.matchOdd.betAddError?.string ?: R.string.bet_info_list_game_closed
                )


                when (itemData.matchOdd.status) {
                    BetStatus.LOCKED.code, BetStatus.DEACTIVATED.code -> {
                        componentStatusByOdds(
                            betVisible = View.GONE,
                            warningVisible = View.VISIBLE,
                            betTextBg = R.drawable.bg_radius_4_button_unselected,
                            clickable = false,
                            moreTextBg = R.drawable.bg_radius_4_button_unselected,
                            moreTextColor = R.color.colorWhite,
                            moreClickable = false
                        )

                    }


                    BetStatus.ACTIVATED.code -> {
                        componentStatusByOdds(
                            betVisible = View.VISIBLE,
                            warningVisible = if (itemData.matchOdd.betAddError == null) View.GONE else View.VISIBLE,
                            betTextBg = R.drawable.bg_radius_4_button_orange_light,
                            clickable = true,
                            moreTextBg = R.drawable.bg_radius_4_button_colorwhite6,
                            moreTextColor = R.color.colorGray,
                            moreClickable = true
                        )
                        setChangeOdds(adapterPosition, itemData.matchOdd)
                    }
                }

//                executePendingBindings()

                //TODO 投注按鈕事件
                setupBetButton(itemData)
                //TODO 需先註冊或登入按鈕事件
                setupRegisterButton()
            }

        }

        /**
         * 投注金額變更、賠率變化，重新檢查金額、餘額及各按鈕是否需暫停功能
         * 影響功能:投注按鈕、投注金額輸入欄位、輸入欄位錯誤訊息
         */
        //TODO 更新上一層的投注按鈕及總金額
        private fun check(it: String, matchOdd: MatchOdd, parlayOdd: ParlayOdd?, itemData: BetInfoListData) {
            if (TextUtils.isEmpty(it)) {
                binding.etBet.setBackgroundResource(R.drawable.effect_select_bet_edit_text)
                itemData.betAmount = 0.000
                binding.tvErrorMessage.visibility = View.GONE
                (binding.clInput.layoutParams as LinearLayout.LayoutParams).bottomMargin = 11.dp
            } else {
                val quota = it.toDouble()
                itemData.betAmount = quota
                when {
                    quota > parlayOdd?.max ?: 0 -> {
                        inputError = true
                        binding.tvErrorMessage.text = binding.root.context.getString(R.string.bet_info_list_bigger_than_max_limit)
                        binding.etBet.setBackgroundResource(R.drawable.bg_radius_4_edittext_error)
                    }
                    quota < parlayOdd?.min ?: 0 -> {
                        inputError = true
                        binding.tvErrorMessage.text = binding.root.context.getString(R.string.bet_info_list_less_than_minimum_limit)
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

                binding.tvErrorMessage.visibility = if (inputError) View.VISIBLE else View.GONE
                (binding.clInput.layoutParams as LinearLayout.LayoutParams).bottomMargin = if (inputError) 0.dp else 11.dp
                binding.etBet.setTextColor(
                    if (inputError) ContextCompat.getColor(binding.root.context, R.color.colorRedDark) else ContextCompat.getColor(binding.root.context, R.color.colorBlackLight)
                )
            }

            onItemClickListener.refreshAmount()
        }

        //TODO review this method
        private fun componentStatusByOdds(
            betVisible: Int, warningVisible: Int,
            betTextBg: Int, clickable: Boolean,
            moreTextBg: Int, moreTextColor: Int, moreClickable: Boolean
        ) {
            binding.llBet.visibility = betVisible
            binding.tvCloseWarning.apply {
                visibility = warningVisible
            }

            //TODO 投注按鈕是否可點擊
            /*binding.betInfoAction.tv_bet.apply {
                background = ContextCompat.getDrawable(binding.root.context, betTextBg)
                isClickable = clickable
            }

            binding.betInfoAction.tv_add_more.apply {
                background = ContextCompat.getDrawable(binding.root.context, moreTextBg)
                setTextColor(ContextCompat.getColor(binding.root.context, moreTextColor))
                isClickable = moreClickable
            }*/

        }

        //TODO 投注按鈕事件
        private fun setupBetButton(data: BetInfoListData) {
            /*binding.betInfoAction.tv_bet.apply {
                visibility = if (isNeedRegister) View.INVISIBLE else View.VISIBLE

                setOnClickListener(object : OnForbidClickListener() {
                    override fun forbidClick(view: View?) {
                        if (data.matchOdd.status == BetStatus.LOCKED.code || data.matchOdd.status == BetStatus.DEACTIVATED.code) return
                        val stake = if (binding.etBet.text.toString().isEmpty()) {
                            0.0
                        } else {
                            binding.etBet.text.toString().toDouble()
                        }
                        onItemClickListener.onBetClick(data, stake)
                    }
                })
            }*/
        }

        //TODO 需先註冊或登入按鈕事件
        private fun setupRegisterButton() {
            /*binding.betInfoAction.tv_register.apply {
                visibility = if (isNeedRegister) View.VISIBLE else View.INVISIBLE
                setOnClickListener { onItemClickListener.onRegisterClick() }
            }*/
        }

        private fun setChangeOdds(position: Int, matchOdd: MatchOdd) {
            when (matchOdd.oddState) {
                OddState.LARGER.state, OddState.SMALLER.state -> {
                    binding.tvOddChange.visibility = View.VISIBLE

                    binding.betInfoDetail.tvOdds.apply {
                        setBackgroundColor(ContextCompat.getColor(context, R.color.colorRed))
                        setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                        text = TextUtil.formatForOdd(getOdds(matchOdd, oddsType))
                    }
                    onItemClickListener.saveOddsHasChanged(matchOdd)

                    //先清除前一次任務
                    matchOdd.runnable?.let { mHandler.removeCallbacks(it) }
                    val runnable = Runnable {
                        matchOdd.runnable = null
                        notifyItemChanged(position)
                    }

                    //三秒後 恢復 Odd 狀態
                    mHandler.postDelayed(runnable, CHANGING_ITEM_BG_COLOR_DURATION)
                    matchOdd.runnable = runnable
                }

                else -> {
                    //若有 賠率變更 任務還未執行完，就不刷新
                    if (matchOdd.runnable != null)
                        return

                    //TODO 輸入錯誤 投注按鈕反灰
                    when (inputError) {
                        /*true -> {
                            binding.betInfoAction.tv_bet.apply {
                                background = ContextCompat.getDrawable(tv_bet.context, R.drawable.bg_radius_4_button_unselected)
                                setTextColor(ContextCompat.getColor(tv_bet.context, R.color.colorWhite))
                                isClickable = false
                            }
                        }
                        false -> {
                            binding.betInfoAction.tv_bet.apply {
                                background = ContextCompat.getDrawable(tv_bet.context, R.drawable.bg_radius_4_button_orange_light)
                                setTextColor(ContextCompat.getColor(tv_bet.context, R.color.colorWhite))
                                isClickable = true
                            }
                        }*/
                    }

                    binding.tvOddChange.visibility = View.GONE

                    binding.betInfoDetail.tvOdds.apply {
                        setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                        setTextColor(ContextCompat.getColor(context, R.color.colorOrange))
                    }
                }
            }
        }
    }

    inner class NoDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface OnItemClickListener {
        fun onDeleteClick(oddsId: String, currentItemCount: Int)
        fun onShowKeyboard(editText: EditText, matchOdd: MatchOdd)
        fun saveOddsHasChanged(matchOdd: MatchOdd)
        fun refreshAmount()
    }
}

class BetListDiffCallBack : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.oddsId == newItem.oddsId
    }

}

sealed class DataItem {
    abstract var oddsId: String?

    data class BetInfoData(val betInfoListData: BetInfoListData, override var oddsId: String? = betInfoListData.matchOdd.oddsId) : DataItem()

    class NoData : DataItem() {
        override var oddsId: String? = null
    }
}