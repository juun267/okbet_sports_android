package org.cxct.sportlottery.ui.game.betList

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_bet_info_item.view.*
import kotlinx.android.synthetic.main.content_bet_info_item.view.et_bet
import kotlinx.android.synthetic.main.content_bet_info_item_quota_detail.view.*
import kotlinx.android.synthetic.main.content_bet_list_batch_control.view.*
import kotlinx.android.synthetic.main.item_bet_list_batch_control.view.*
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect.view.*
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect.view.ll_winnable
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect.view.tv_winnable_amount
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.bet.list.INPLAY
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds

class BetListRefactorAdapter(private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        var allSingleInput = ""
    }

    private enum class ViewType { Bet, Parlay, ParlayFirst }

    var betList: MutableList<BetInfoListData>? = mutableListOf()
        set(value) {
            field = value
            updateDataList()
        }
    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            updateDataList()
        }

    private fun updateDataList() {
        notifyDataSetChanged()
        //TODO review ui錯位
        /*if ((betList?.size ?: 0) > 0) {
            if ((betList ?: mutableListOf()).all { it.matchOdd.refreshData }) {
                Timber.e("Dean, notify all")
                betList?.forEach {
                    it.matchOdd.refreshData = false
                }
                notifyDataSetChanged()
            } else {
                betList?.forEachIndexed { index, betInfoListData ->
                    if (betInfoListData.matchOdd.refreshData) {
                        Timber.e("Dean, index = $index notify")
                        betInfoListData.matchOdd.refreshData = false
                        notifyItemChanged(index)
                    }
                }
            }
        }*/
    }

    var parlayList: MutableList<ParlayOdd>? = mutableListOf()

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

    private var focusPosition = -1 //紀錄當前鍵盤作用於哪個EditText

    private fun setFocusPosition(focusPosition: Int) {
        this.focusPosition = focusPosition
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is BetInfoItemViewHolder -> {
                holder.bind(
                    betList?.getOrNull(position)!!,
                    oddsType,
                    position,
                    focusPosition,
                    itemCount,
                    onItemClickListener
                ) { setFocusPosition(it) }
            }
            is BatchSingleViewHolder -> {
                holder.bind(
                    parlayList?.getOrNull(position - (betList?.size ?: 0)),
                    parlayList?.size ?: 0,
                    betList ?: mutableListOf(),
                    oddsType,
                    position,
                    focusPosition,
                    onItemClickListener,
                    { notifyDataSetChanged() },
                    { setFocusPosition(it) },
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
                    position,
                    focusPosition,
                    onItemClickListener
                ) { setFocusPosition(it) }
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

    //單注
    class BetInfoItemViewHolder(itemView: View) : BetInfoChangeViewHolder(itemView) {
        fun bind(
            itemData: BetInfoListData,
            oddsType: OddsType,
            position: Int,
            focusPosition: Int,
            itemCount: Int,
            onItemClickListener: OnItemClickListener,
            setValue: (focusPosition: Int) -> Unit
        ) {
            itemView.apply {
                setupBetAmountInput(itemData, oddsType, position, focusPosition, onItemClickListener, setValue)

                setupOddStatus(itemData)

                setupDeleteButton(itemData, itemCount, onItemClickListener)

                setupMaximumLimitView(itemData, onItemClickListener)
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setupBetAmountInput(
            itemData: BetInfoListData,
            oddsType: OddsType,
            position: Int,
            focusPosition: Int,
            onItemClickListener: OnItemClickListener,
            setValueFun: (focusPosition: Int) -> Unit
        ) {
            itemView.apply {
                et_bet.apply {
                    if (tag is TextWatcher) {
                        removeTextChangedListener(tag as TextWatcher)
                    }
                }
                onFocusChangeListener = null

                setupOddInfo(itemData, oddsType, position, focusPosition)

                val tw: TextWatcher?
                tw = object : TextWatcher {
                    override fun afterTextChanged(it: Editable?) {
                        if (it.isNullOrEmpty()) {
                            itemData.betAmount = 0.000
                            itemData.input = ""

                            tv_check_maximum_limit.visibility = View.VISIBLE
                            ll_bet_quota_detail.visibility = View.GONE
                            ll_win_quota_detail.visibility = View.GONE
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

                            //比照以往計算
                            var win = quota * getOdds(itemData.matchOdd, oddsType)
                            if (oddsType == OddsType.EU) {
                                win -= quota
                            }
                            tv_win_quota.text = TextUtil.format(win)

                        }
                        onItemClickListener.refreshAmount()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                }

                val fc = View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus && position != focusPosition)
                        et_bet.clearFocus()
                }

                et_bet.onFocusChangeListener = fc
                et_bet.addTextChangedListener(tw)
                et_bet.tag = tw
                et_bet.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        setValueFun.invoke(position)
                        onItemClickListener.onShowKeyboard(et_bet, itemData.matchOdd)
                    }
                    false
                }

            }
        }

        private fun setupOddInfo(
            itemData: BetInfoListData,
            oddsType: OddsType,
            position: Int,
            focusPosition: Int,
        ) {
            itemView.apply {
                v_point.visibility = if (itemData.pointMarked) View.VISIBLE else View.GONE
                setupOddsContent(itemData.matchOdd, oddsType = oddsType, tv_odds_content)
                tv_match.text =
                    "${itemData.matchOdd.homeName}${context.getString(R.string.verse_)}${itemData.matchOdd.awayName}"
                tv_name.text = if (itemData.matchOdd.inplay == INPLAY) {
                    context.getString(
                        R.string.bet_info_in_play_score,
                        itemData.matchOdd.playCateName,
                        itemData.matchOdd.homeScore.toString(),
                        itemData.matchOdd.awayScore.toString()
                    )
                } else itemData.matchOdd.playCateName

                et_bet.setText(if (itemData.betAmount > 0) TextUtil.formatInputMoney(itemData.betAmount) else "")
                et_bet.setSelection(et_bet.text.length)

                /* check input focus */
                if (position == focusPosition) {
                    et_bet.requestFocus()
                    et_bet.setSelection(et_bet.text.length)
                } else {
                    et_bet.clearFocus()
                }
            }
        }

        private fun setupOddStatus(itemData: BetInfoListData) {
            itemView.apply {
                if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) {
                    cl_item_background.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
                    iv_bet_lock.visibility = View.GONE
                    et_bet.apply {
                        isFocusable = true
                        isFocusableInTouchMode = true
                    }
                    cl_quota_detail.visibility = View.VISIBLE
                } else {
                    cl_item_background.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite2))
                    iv_bet_lock.visibility = View.VISIBLE
                    et_bet.apply {
                        isFocusable = false
                        isFocusableInTouchMode = false
                    }
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
                tv_bet_maximum_limit.text = (itemData.parlayOdds?.max ?: 0).toString()
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
            itemData: ParlayOdd?, parlayListSize: Int, betList: MutableList<BetInfoListData>, oddsType: OddsType,
            position: Int,
            focusPosition: Int,
            onItemClickListener: OnItemClickListener, notifyAllBet: () -> Unit,
            setFocusPosition: (position: Int) -> Unit,
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
                            position,
                            focusPosition,
                            onItemClickListener,
                            notifyAllBet,
                            setFocusPosition
                        )
                    }
                    parlayListSize == 1 -> {
                        item_first_connect.visibility = View.VISIBLE
                        ll_more_option.visibility = View.GONE

                        setupParlayItem(
                            itemData,
                            oddsType,
                            position,
                            focusPosition,
                            onItemClickListener,
                            setFocusPosition
                        )
                        setupSingleItem(
                            betList,
                            itemData,
                            oddsType,
                            position,
                            focusPosition,
                            onItemClickListener,
                            notifyAllBet,
                            setFocusPosition
                        )
                    }
                    else -> {
                        item_first_connect.visibility = View.VISIBLE
                        ll_more_option.visibility = View.VISIBLE

                        setupParlayItem(
                            itemData,
                            oddsType,
                            position,
                            focusPosition,
                            onItemClickListener,
                            setFocusPosition
                        )
                        setupSingleItem(
                            betList,
                            itemData,
                            oddsType,
                            position,
                            focusPosition,
                            onItemClickListener,
                            notifyAllBet,
                            setFocusPosition
                        )

                        setupClickMoreItem(itemView.ll_more_option, clickMoreOption)
                    }
                }
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setupSingleItem(
            betList: MutableList<BetInfoListData>,
            itemData: ParlayOdd?,
            oddsType: OddsType,
            position: Int,
            focusPosition: Int,
            onItemClickListener: OnItemClickListener,
            notifyAllBet: () -> Unit,
            setFocusPosition: (position: Int) -> Unit
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

                et_bet.apply {
                    val initValue =
                        if (!(itemData?.allSingleInput.isNullOrEmpty())) itemData?.allSingleInput else allSingleInput
                    setText(initValue)
                    /* check input focus */
                    if (position == focusPosition) {
                        requestFocus()
                        setSelection(text.length)
                    } else {
                        clearFocus()
                    }

                    /* set listener */
                    val tw: TextWatcher?
                    tw = object : TextWatcher {
                        override fun afterTextChanged(it: Editable?) {
                            val inputValue = if (it.isNullOrEmpty()) 0.0 else it.toString().toDouble()
                            itemData?.allSingleInput = if (it.isNullOrEmpty()) "" else it.toString()
                            allSingleInput = if (it.isNullOrEmpty()) "" else it.toString()
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
                            onItemClickListener.refreshAmount()
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    }

                    val fc = View.OnFocusChangeListener { _, hasFocus ->
                        if (hasFocus && adapterPosition != focusPosition)
                            et_bet.clearFocus()
                    }

                    onFocusChangeListener = fc
                    removeTextChangedListener(tw)
                    addTextChangedListener(tw)
                    tag = tw

                    setOnTouchListener { _, event ->
                        //若回傳true則不會觸發onTouchEvent,onClick
                        if (event.action == MotionEvent.ACTION_UP) {
                            setFocusPosition(position)
                            onItemClickListener.onShowParlayKeyboard(this, itemData)
                        }
                        false
                    }
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

        private fun setupClickMoreItem(btnShowMore: View, clickEvent: () -> Unit) {
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
            position: Int,
            focusPosition: Int,
            onItemClickListener: OnItemClickListener,
            setFocusPosition: (position: Int) -> Unit
        ) {
            setupParlayItem(
                itemData,
                oddsType,
                position,
                focusPosition,
                onItemClickListener,
                setFocusPosition
            )
        }
    }

    abstract class BatchParlayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected fun setupParlayItem(
            itemData: ParlayOdd?,
            oddsType: OddsType,
            position: Int,
            focusPosition: Int,
            onItemClickListener: OnItemClickListener,
            setFocusPosition: (position: Int) -> Unit
        ) {
            itemView.apply {
                ll_winnable.visibility = View.GONE
                ll_max_bet_amount.visibility = View.GONE

                itemData?.let { data ->
                    tv_parlay_type.text = TextUtil.replaceParlayByC(data.parlayType)

                    val itemOdd = TextUtil.formatForOdd(getOdds(data, oddsType))
                    tv_parlay_odd.text = itemOdd
                    tv_com_count.text = data.num.toString()

                    setupBetAmountInput(data, position, focusPosition, onItemClickListener, setFocusPosition)

                    setupMaximumLimitView(data, onItemClickListener)

                    setupParlayRuleButton(data, onItemClickListener)

                }
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setupBetAmountInput(
            data: ParlayOdd,
            position: Int,
            focusPosition: Int,
            onItemClickListener: OnItemClickListener,
            setFocusPosition: (position: Int) -> Unit
        ) {
            itemView.apply {
                et_bet.apply {
                    if (tag is TextWatcher) {
                        removeTextChangedListener(tag as TextWatcher)
                    }
                    onFocusChangeListener = null
                    setText(data.betAmount.let {
                        if (it > 0) TextUtil.formatInputMoney(it) else ""
                    })
                    setSelection(text.length)
                    /* check input focus */
                    if (position == focusPosition) {
                        requestFocus()
                        setSelection(text.length)
                    } else {
                        clearFocus()
                    }
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

                            data.betAmount = TextUtil.formatInputMoney(inputValue).toDouble()
                            onItemClickListener.refreshAmount()
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    }

                    val fc = View.OnFocusChangeListener { _, hasFocus ->
                        if (hasFocus && adapterPosition != focusPosition)
                            et_bet.clearFocus()
                    }

                    onFocusChangeListener = fc
                    removeTextChangedListener(tw)
                    addTextChangedListener(tw)
                    tag = tw

                    setOnTouchListener { _, event ->
                        //若回傳true則不會觸發onTouchEvent,onClick
                        if (event.action == MotionEvent.ACTION_UP) {
                            setFocusPosition(position)
                            onItemClickListener.onShowParlayKeyboard(this, data)
                        }
                        false
                    }
                }
            }
        }

        private fun setupMaximumLimitView(
            itemData: ParlayOdd,
            onItemClickListener: OnItemClickListener
        ) {
            itemView.apply {
                tv_bet_maximum_limit.text = itemData.max.toString()
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
        fun refreshAmount()
        fun showParlayRule(parlayType: String, parlayRule: String)
    }
}