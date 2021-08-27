package org.cxct.sportlottery.ui.game.betList

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_bet_info_item.*
import kotlinx.android.synthetic.main.content_bet_info_item.view.*
import kotlinx.android.synthetic.main.content_bet_info_item.view.et_bet
import kotlinx.android.synthetic.main.content_bet_info_item.view.tv_error_message
import kotlinx.android.synthetic.main.content_bet_info_item_quota_detail.view.*
import kotlinx.android.synthetic.main.content_bet_list_batch_control.view.*
import kotlinx.android.synthetic.main.item_bet_list_batch_control.view.*
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect.view.*
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect.view.et_clickable
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect.view.ll_winnable
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect.view.tv_winnable_amount
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.bet.list.INPLAY
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds

class BetListRefactorAdapter(private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private enum class ViewType { Bet, Parlay, ParlayFirst }

    var betList: MutableList<BetInfoListData>? = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder is BetInfoChangeViewHolder) {
            true -> holder.clearHandler()
        }
    }

    var parlayList: MutableList<ParlayOdd>? = mutableListOf()
        set(value) {
            //若無法組合串關時, 給予空物件用來紀錄”單注填充所有單注“的輸入金額
            field = if (value?.size == 0) {
                mutableListOf(
                    ParlayOdd(
                        max = -1,
                        min = -1,
                        num = -1,
                        odds = 0.0,
                        hkOdds = 0.0,
                        parlayType = ""
                    )
                )
            } else {
                value
            }
            notifyDataSetChanged()
        }

    var moreOptionCollapse = false

    var needScrollToBottom = false //用來紀錄是否為點擊更多選項需滾動至底部

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ViewType.Bet.ordinal -> BetInfoItemViewHolder(
                layoutInflater.inflate(
                    R.layout.content_bet_info_item,
                    parent,
                    false
                )
            )
            ViewType.ParlayFirst.ordinal -> BatchSingleViewHolder(
                layoutInflater.inflate(
                    R.layout.content_bet_list_batch_control,
                    parent,
                    false
                )
            )
            else -> BatchParlayConnectViewHolder(
                layoutInflater.inflate(
                    R.layout.item_bet_list_batch_control_connect,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BetInfoItemViewHolder -> {
                holder.bind(
                    betList?.getOrNull(position)!!,
                    oddsType,
                    itemCount,
                    onItemClickListener
                )
            }
            is BatchSingleViewHolder -> {
                holder.bind(
                    parlayList?.getOrNull(position - (betList?.size ?: 0)),
                    parlayList?.size ?: 0,
                    betList ?: mutableListOf(),
                    oddsType,
                    moreOptionCollapse,
                    onItemClickListener,
                    { notifyDataSetChanged() },
                    {
                        moreOptionCollapse = !moreOptionCollapse
                        needScrollToBottom = true
                        notifyDataSetChanged()
                    }
                )
            }
            is BatchParlayConnectViewHolder -> {
                holder.bind(
                    parlayList?.getOrNull(position - (betList?.size ?: 0)),
                    oddsType,
                    onItemClickListener
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val betSize = betList?.size ?: 0
        return when {
            position < betSize -> ViewType.Bet.ordinal
            position == betSize -> ViewType.ParlayFirst.ordinal
            else -> ViewType.Parlay.ordinal
        }
    }

    override fun getItemCount(): Int {
        val betListSize = betList?.size ?: 0
        val cannotParlay = betList?.find { it.matchOdd.status != BetStatus.ACTIVATED.code || it.pointMarked } != null
        val parlayListSize = when {
            betListSize < 2 -> 0
            cannotParlay || betListSize == 2 || !moreOptionCollapse -> 1
            else -> (parlayList?.size ?: 0)
        }
        return betListSize + parlayListSize
    }

    //使用HasStabledIds需複寫回傳的position, 若仍使用super.getItemId(position), 數據刷新會錯亂.
    //https://blog.csdn.net/karsonNet/article/details/80598435
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //單注
    class BetInfoItemViewHolder(itemView: View) : BetInfoChangeViewHolder(itemView) {
        fun bind(
            itemData: BetInfoListData,
            oddsType: OddsType,
            itemCount: Int,
            onItemClickListener: OnItemClickListener
        ) {
            itemView.apply {
                setupBetAmountInput(
                    itemData,
                    oddsType,
                    onItemClickListener
                )

                setupOddStatus(itemData)

                setupDeleteButton(itemData, itemCount, onItemClickListener)

                setupMaximumLimitView(itemData, onItemClickListener)
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setupBetAmountInput(
            itemData: BetInfoListData,
            oddsType: OddsType,
            onItemClickListener: OnItemClickListener
        ) {
            itemView.apply {
                et_bet.apply {
                    if (tag is TextWatcher) {
                        removeTextChangedListener(tag as TextWatcher)
                    }
                }
                onFocusChangeListener = null

                setupOddInfo(itemData, oddsType)
                setupMinimumLimitMessage(itemData)

                val tw: TextWatcher?
                tw = object : TextWatcher {
                    override fun afterTextChanged(it: Editable?) {
                        if (it.isNullOrEmpty()) {
                            itemData.betAmount = 0.000
                            itemData.input = ""

                            tv_check_maximum_limit.visibility = View.VISIBLE
                            ll_bet_quota_detail.visibility = View.GONE
                            ll_win_quota_detail.visibility = View.GONE
                            checkMinimumLimit(itemData)
                        } else {

                            //輸入時 直接顯示可贏額
                            tv_check_maximum_limit.visibility = View.GONE
                            ll_bet_quota_detail.visibility = View.GONE
                            ll_win_quota_detail.visibility = View.VISIBLE


                            val quota = it.toString().toDouble()
                            itemData.betAmount = quota
                            itemData.input = TextUtil.formatInputMoney(quota)
                            itemData.parlayOdds?.max?.let { max ->
                                if (quota > max) {
                                    et_bet.apply {
                                        setText(TextUtil.formatInputMoney(max))
                                        setSelection(text.length)
                                    }
                                    return@afterTextChanged
                                }
                            }

                            checkMinimumLimit(itemData, quota)
                            //比照以往計算
                            var win = quota * getOdds(itemData.matchOdd, oddsType)
                            if (oddsType == OddsType.EU) {
                                win -= quota
                            }
                            tv_win_quota.text = TextUtil.format(win)

                        }
                        onItemClickListener.refreshBetInfoTotal()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                }

                et_bet.keyListener = null

                et_bet.addTextChangedListener(tw)
                et_bet.tag = tw
                //TODO check on touch listener
                et_clickable.setOnClickListener {
                    et_bet.isFocusable = true
                    onItemClickListener.onShowKeyboard(et_bet, itemData.matchOdd)
                }
            }
        }

        private fun setupOddInfo(
            itemData: BetInfoListData,
            oddsType: OddsType
        ) {
            itemView.apply {
                v_point.visibility = if (itemData.pointMarked) View.VISIBLE else View.GONE
                if (itemData.matchType == MatchType.OUTRIGHT) itemData.matchOdd.spread = ""
                setupOddsContent(itemData.matchOdd, oddsType = oddsType, tv_odds_content)
                tv_match.text = if (itemData.matchType == MatchType.OUTRIGHT) itemData.outrightMatchInfo?.name
                else "${itemData.matchOdd.homeName}${context.getString(R.string.verse_)}${itemData.matchOdd.awayName}"

                tv_name.text = if (itemData.matchOdd.inplay == INPLAY) {
                    context.getString(
                        R.string.bet_info_in_play_score,
                        itemData.matchOdd.playCateName,
                        itemData.matchOdd.homeScore.toString(),
                        itemData.matchOdd.awayScore.toString()
                    )
                } else itemData.matchOdd.playCateName

                if (itemData.betAmount > 0) {
                    et_bet.setText(TextUtil.formatInputMoney(itemData.betAmount))
                    et_bet.setSelection(et_bet.text.length)
                    tv_check_maximum_limit.visibility = View.GONE
                    ll_bet_quota_detail.visibility = View.GONE
                    ll_win_quota_detail.visibility = View.VISIBLE
                    checkMinimumLimit(itemData)
                } else {
                    et_bet.setText("")
                    tv_check_maximum_limit.visibility = View.VISIBLE
                    ll_bet_quota_detail.visibility = View.GONE
                    ll_win_quota_detail.visibility = View.GONE
                    checkMinimumLimit(itemData)
                }
                et_bet.setText(if (itemData.betAmount > 0) TextUtil.formatInputMoney(itemData.betAmount) else "")
                et_bet.setSelection(et_bet.text.length)

                val quota = itemData.betAmount
                //比照以往計算
                var win = quota * getOdds(itemData.matchOdd, oddsType)
                if (oddsType == OddsType.EU) {
                    win -= quota
                }
                tv_win_quota.text = TextUtil.format(win)
            }
        }

        private fun setupMinimumLimitMessage(itemData: BetInfoListData) {
            itemView.apply {
                itemData.parlayOdds?.min?.let { min ->
                    tv_error_message.text = String.format(
                        context.getString(R.string.bet_info_list_minimum_limit_amount),
                        min,
                        context.getString(R.string.currency)
                    )
                }
            }
        }

        private fun checkMinimumLimit(itemData: BetInfoListData, betAmount: Double = itemData.betAmount) {
            itemView.apply {
                itemData.parlayOdds?.min?.let { min ->
                    tv_error_message.visibility = if (betAmount != 0.0 && betAmount < min) {
                        itemData.amountError = true
                        View.VISIBLE
                    } else {
                        itemData.amountError = false
                        View.GONE
                    }
                }
            }
        }

        private fun setupOddStatus(itemData: BetInfoListData) {
            itemView.apply {
                if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) {
                    cl_item_background.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
                    iv_bet_lock.visibility = View.GONE
                    et_bet.apply {
                        isEnabled = true
                        isFocusable = true
                        isFocusableInTouchMode = true
                    }
                    et_clickable.isEnabled = true //EditText的click事件
                    cl_quota_detail.visibility = View.VISIBLE
                } else {
                    cl_item_background.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite2))
                    iv_bet_lock.visibility = View.VISIBLE
                    et_bet.apply {
                        isEnabled = false
                        isFocusable = false
                        isFocusableInTouchMode = false
                    }
                    et_clickable.isEnabled = false //EditText的click事件
                    cl_quota_detail.visibility = View.GONE
                }
            }
        }

        private fun setupDeleteButton(
            itemData: BetInfoListData,
            itemCount: Int,
            onItemClickListener: OnItemClickListener
        ) {
            itemView.iv_close.setOnClickListener {
                onItemClickListener.onDeleteClick(itemData.matchOdd.oddsId, itemCount)
            }
        }

        private fun setupMaximumLimitView(itemData: BetInfoListData, onItemClickListener: OnItemClickListener) {
            itemView.apply {
                tv_bet_maximum_limit.text = TextUtil.formatBetQuota(itemData.parlayOdds?.max ?: 0)
                tv_check_maximum_limit.setOnClickListener {
                    it.visibility = View.GONE
                    ll_bet_quota_detail.visibility = View.VISIBLE
                }

                ll_bet_quota_detail.setOnClickListener {
                    et_bet.apply {
                        setText(itemData.parlayOdds?.max.toString())
                        isFocusable = true
                        setSelection(text.length)
                    }
                    onItemClickListener.onShowKeyboard(et_bet, itemData.matchOdd)
                }
            }
        }
    }

    //填充所有單注、串關第一項、展開更多
    class BatchSingleViewHolder(itemView: View) : BatchParlayViewHolder(itemView) {
        fun bind(
            itemData: ParlayOdd?,
            parlayListSize: Int,
            betList: MutableList<BetInfoListData>,
            oddsType: OddsType,
            moreOptionCollapse: Boolean,
            onItemClickListener: OnItemClickListener,
            notifyAllBet: () -> Unit,
            clickMoreOption: () -> Unit
        ) {
            itemView.apply {
                val hasOddStateClose =
                    betList.find { it.matchOdd.status != BetStatus.ACTIVATED.code || it.pointMarked } != null
                when {
                    hasOddStateClose || parlayListSize == 0 -> {
                        item_first_connect.visibility = View.GONE
                        ll_more_option.visibility = View.GONE

                        setupSingleItem(
                            betList,
                            itemData,
                            oddsType,
                            onItemClickListener,
                            notifyAllBet
                        )
                    }
                    parlayListSize == 1 -> {
                        item_first_connect.visibility = View.VISIBLE
                        ll_more_option.visibility = View.GONE

                        setupParlayItem(
                            itemData,
                            oddsType,
                            true,
                            onItemClickListener
                        )
                        setupSingleItem(
                            betList,
                            itemData,
                            oddsType,
                            onItemClickListener,
                            notifyAllBet
                        )
                    }
                    else -> {
                        item_first_connect.visibility = View.VISIBLE
                        ll_more_option.visibility = View.VISIBLE

                        setupParlayItem(
                            itemData,
                            oddsType,
                            true,
                            onItemClickListener
                        )
                        setupSingleItem(
                            betList,
                            itemData,
                            oddsType,
                            onItemClickListener,
                            notifyAllBet
                        )

                        setupClickMoreItem(itemView.ll_more_option, moreOptionCollapse, clickMoreOption)
                    }
                }
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setupSingleItem(
            betList: MutableList<BetInfoListData>,
            itemData: ParlayOdd?,
            oddsType: OddsType,
            onItemClickListener: OnItemClickListener,
            notifyAllBet: () -> Unit
        ) {
            itemView.item_first_single.apply {
                et_bet.apply {
                    if (tag is TextWatcher) {
                        removeTextChangedListener(tag as TextWatcher)
                    }
                    onFocusChangeListener = null
                }

                ll_winnable.visibility = View.INVISIBLE
                tv_single_count.text = betList.size.toString()

                val initValue =
                    if (!(itemData?.allSingleInput.isNullOrEmpty())) itemData?.allSingleInput else ""
                //init winnable amount
                tv_winnable_amount.text = TextUtil.formatMoney(
                    getAllSingleWinnableAmount(
                        if (initValue.isNullOrEmpty()) 0.0 else initValue.toDouble(),
                        oddsType,
                        betList
                    )
                )
                et_bet.apply {
                    //init bet amount value
                    setText(initValue)
                    et_bet.setSelection(et_bet.text.length)

                    /* set listener */
                    val tw: TextWatcher?
                    tw = object : TextWatcher {
                        override fun afterTextChanged(it: Editable?) {
                            val inputValue = if (it.isNullOrEmpty()) 0.0 else it.toString().toDouble()
                            itemData?.allSingleInput = if (it.isNullOrEmpty()) "" else it.toString()
                            val allWinnableAmount = getAllSingleWinnableAmount(inputValue, oddsType, betList)
                            when (allWinnableAmount > 0) {
                                true -> {
                                    itemView.apply {
                                        ll_winnable.visibility = View.VISIBLE
                                        tv_winnable_amount.text =
                                            TextUtil.formatMoney(allWinnableAmount)
                                    }

                                }
                                else -> {
                                    itemView.ll_winnable.visibility = View.INVISIBLE
                                }
                            }

                            betList.forEachIndexed { _, data ->
                                if (data.parlayOdds?.max == null || inputValue < (data.parlayOdds?.max ?: 0)) {
                                    data.betAmount = inputValue
                                } else {
                                    data.betAmount = (data.parlayOdds?.max ?: 0).toDouble()
                                }
                            }
                            notifyAllBet()
                            onItemClickListener.refreshBetInfoTotal()
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    }

                    keyListener = null
                    removeTextChangedListener(tw)
                    addTextChangedListener(tw)
                    tag = tw
                }
                et_container.setOnClickListener {
                    et_bet.isFocusable = true
                    et_bet.setSelection(et_bet.text.length)
                    onItemClickListener.onShowParlayKeyboard(et_bet, itemData)
                }
            }
        }

        /**
         * 填充所有單注後獲取總可贏額
         */
        private fun getAllSingleWinnableAmount(
            betAmount: Double,
            oddsType: OddsType,
            betList: MutableList<BetInfoListData>
        ): Double {
            var allWinnableAmount = 0.0
            betList.forEach {
                allWinnableAmount += getOdds(it.matchOdd, oddsType) * betAmount
            }
            return allWinnableAmount
        }

        private fun setupClickMoreItem(btnShowMore: View, moreOptionCollapse: Boolean, clickEvent: () -> Unit) {
            itemView.iv_arrow.setImageResource(if (moreOptionCollapse) R.drawable.ic_arrow_gray_top else R.drawable.ic_arrow_gray_down)
            btnShowMore.setOnClickListener {
                clickEvent()
            }
        }
    }

    //串關(除第一項)
    class BatchParlayConnectViewHolder(itemView: View) : BatchParlayViewHolder(itemView) {
        fun bind(
            itemData: ParlayOdd?,
            oddsType: OddsType,
            onItemClickListener: OnItemClickListener
        ) {
            setupParlayItem(
                itemData,
                oddsType,
                false,
                onItemClickListener
            )
        }
    }

    abstract class BatchParlayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected fun setupParlayItem(
            itemData: ParlayOdd?,
            oddsType: OddsType,
            firstItem: Boolean = false,
            onItemClickListener: OnItemClickListener
        ) {
            itemView.apply {
                ll_winnable.visibility = View.GONE
                ll_max_bet_amount.visibility = View.GONE

                itemData?.let { data ->
                    tv_parlay_type.text = TextUtil.replaceParlayByC(data.parlayType)

                    tv_parlay_odd.apply {
                        if (firstItem) {
                            visibility = View.VISIBLE

                            val itemOdd = TextUtil.formatForOdd(getOdds(data, oddsType))
                            text = itemOdd
                        } else
                            visibility = View.GONE
                    }

                    tv_symbol_odd.visibility = if (firstItem) View.VISIBLE else View.GONE

                    tv_com_count.text = data.num.toString()

                    setupBetAmountInput(data, oddsType, onItemClickListener)

                    setupMaximumLimitView(data, onItemClickListener)

                    setupParlayRuleButton(data, onItemClickListener)

                }
            }
        }

        private fun setupOddInfo(data: ParlayOdd, oddsType: OddsType) {
            itemView.apply {
                if (data.betAmount > 0) {
                    et_bet.setText(TextUtil.formatInputMoney(data.betAmount))
                    itemView.apply {
                        tv_check_maximum_limit.visibility = View.GONE
                        ll_bet_quota_detail.visibility = View.GONE
                        ll_win_quota_detail.visibility = View.VISIBLE
                        checkMinimumLimit(data)
                    }
                } else {
                    et_bet.setText("")
                    itemView.apply {
                        tv_check_maximum_limit.visibility = View.VISIBLE
                        ll_bet_quota_detail.visibility = View.GONE
                        ll_win_quota_detail.visibility = View.GONE
                        checkMinimumLimit(data)
                    }
                }

                et_bet.setSelection(et_bet.text.length)

                val quota = data.betAmount
                //比照以往計算
                var win = quota * getOdds(data, oddsType)
                if (oddsType == OddsType.EU) {
                    win -= quota
                }
                tv_win_quota.text = TextUtil.format(win)
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setupBetAmountInput(
            data: ParlayOdd,
            oddsType: OddsType,
            onItemClickListener: OnItemClickListener
        ) {
            itemView.apply {
                et_bet.apply {
                    if (tag is TextWatcher) {
                        removeTextChangedListener(tag as TextWatcher)
                    }

                    setupOddInfo(data, oddsType)
                    setupMinimumLimitMessage(data)

                    /* set listener */
                    val tw: TextWatcher?
                    tw = object : TextWatcher {
                        override fun afterTextChanged(it: Editable?) {
                            val inputValue = if (it.isNullOrEmpty()) 0.0 else it.toString().toDouble()
                            if (inputValue > data.max) {
                                val maxValue = TextUtil.formatInputMoney(data.max)
                                setText(maxValue)
                                setSelection(maxValue.length)
                                return
                            }

                            //查看最高限額
                            if (inputValue <= 0) {
                                data.betAmount = 0.000

                                itemView.apply {
                                    tv_check_maximum_limit.visibility = View.VISIBLE
                                    ll_bet_quota_detail.visibility = View.GONE
                                    ll_win_quota_detail.visibility = View.GONE
                                    checkMinimumLimit(data)
                                }
                            } else {

                                //輸入時 直接顯示可贏額
                                itemView.apply {
                                    tv_check_maximum_limit.visibility = View.GONE
                                    ll_bet_quota_detail.visibility = View.GONE
                                    ll_win_quota_detail.visibility = View.VISIBLE
                                }

                                val quota = it.toString().toDouble()
                                data.betAmount = inputValue
                                data.max.let { max ->
                                    if (quota > max) {
                                        et_bet.apply {
                                            setText(TextUtil.formatInputMoney(max))
                                            setSelection(text.length)
                                        }
                                        return@afterTextChanged
                                    }
                                }

                                checkMinimumLimit(data, quota)
                                //比照以往計算
                                var win = quota * getOdds(data, oddsType)
                                if (oddsType == OddsType.EU) {
                                    win -= quota
                                }
                                itemView.tv_win_quota.text = TextUtil.format(win)
                            }


                            data.betAmount = TextUtil.formatInputMoney(inputValue).toDouble()
                            onItemClickListener.refreshBetInfoTotal()
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    }

                    removeTextChangedListener(tw)
                    addTextChangedListener(tw)
                    tag = tw
                }
                et_clickable.setOnClickListener {
                    et_bet.isFocusable = true
                    onItemClickListener.onShowParlayKeyboard(et_bet, data)
                }
            }
        }

        private fun setupMinimumLimitMessage(itemData: ParlayOdd) {
            itemView.apply {
                tv_error_message.text = String.format(
                    context.getString(R.string.bet_info_list_minimum_limit_amount),
                    itemData.min,
                    context.getString(R.string.currency)
                )
            }
        }

        private fun checkMinimumLimit(itemData: ParlayOdd, betAmount: Double = itemData.betAmount) {
            itemView.apply {
                itemData.min.let { min ->
                    tv_error_message.visibility = if (betAmount != 0.0 && betAmount < min) {
                        itemData.amountError = true
                        View.VISIBLE
                    } else {
                        itemData.amountError = false
                        View.GONE
                    }
                }
            }
        }

        private fun setupMaximumLimitView(
            itemData: ParlayOdd,
            onItemClickListener: OnItemClickListener
        ) {
            itemView.apply {
                tv_bet_maximum_limit.text = TextUtil.formatBetQuota(itemData.max)
                tv_check_maximum_limit.setOnClickListener {
                    it.visibility = View.GONE
                    ll_bet_quota_detail.visibility = View.VISIBLE
                }

                ll_bet_quota_detail.setOnClickListener {
                    et_bet.apply {
                        setText(itemData.max.toString())
                        isFocusable = true
                        setSelection(text.length)
                    }
                    onItemClickListener.onShowParlayKeyboard(et_bet, itemData)
                }
            }
        }

        private fun setupParlayRuleButton(data: ParlayOdd, onItemClickListener: OnItemClickListener) {
            itemView.btn_rule.setOnClickListener {
                onItemClickListener.showParlayRule(data.parlayType, data.parlayRule ?: "")
            }
        }
    }

    interface OnItemClickListener {
        fun onDeleteClick(oddsId: String, currentItemCount: Int)
        fun onShowKeyboard(editText: EditText, matchOdd: MatchOdd)
        fun onShowParlayKeyboard(editText: EditText, parlayOdd: ParlayOdd?)
        fun onHideKeyBoard()
        fun saveOddsHasChanged(matchOdd: MatchOdd)
        fun refreshBetInfoTotal()
        fun showParlayRule(parlayType: String, parlayRule: String)
    }
}