package org.cxct.sportlottery.ui.game.betList

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_bet_info_item_quota_detail_v2.view.*
import kotlinx.android.synthetic.main.content_bet_info_item_v2.view.*
import kotlinx.android.synthetic.main.content_bet_info_item_v2.view.et_bet
import kotlinx.android.synthetic.main.content_bet_info_item_v2.view.et_clickable
import kotlinx.android.synthetic.main.content_bet_info_item_v2.view.iv_bet_lock
import kotlinx.android.synthetic.main.content_bet_info_item_v2.view.layoutKeyBoard
import kotlinx.android.synthetic.main.content_bet_info_item_v2.view.tv_error_message
import kotlinx.android.synthetic.main.content_bet_list_batch_control.view.*
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect_v2.view.*
import kotlinx.android.synthetic.main.item_bet_list_batch_control_v2.view.*
import kotlinx.android.synthetic.main.item_bet_list_batch_control_v2.view.ll_winnable
import kotlinx.android.synthetic.main.item_bet_list_batch_control_v2.view.tv_winnable_amount
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.transactionStatus.ParlayType
import org.cxct.sportlottery.ui.transactionStatus.ParlayType.Companion.getParlayRuleStringRes
import org.cxct.sportlottery.ui.transactionStatus.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.getNameMap
import kotlin.math.min

class BetListRefactorAdapter(private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private enum class ViewType { Bet, Parlay, ParlayFirst, Warn }
    enum class BetViewType { SINGLE, PARLAY, NULL }

    var betList: MutableList<BetInfoListData>? = mutableListOf()
        set(value) {
            field = value
            //判斷是否有注單封盤

            hasBetClosed =
                value?.find { it.matchOdd.status != BetStatus.ACTIVATED.code || it.pointMarked } != null

            hasBetClosedForSingle =
                value?.find { it.matchOdd.status != BetStatus.ACTIVATED.code } != null

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

    var mSelectedPosition: Int = -1

    var mBetView = BetViewType.NULL

    var onSelectedPositionListener: OnSelectedPositionListener =
        object : OnSelectedPositionListener {
            override fun onSelectChange(position: Int, betViewType: BetViewType) {
                if (mSelectedPosition != position || mBetView != betViewType) {
                    mSelectedPosition = position
                    mBetView = betViewType
                    notifyDataSetChanged()
                }
            }
        }

    var userMoney: Double = 0.0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var hasBetClosed: Boolean = false
    var hasBetClosedForSingle: Boolean = false

    var hasParlayList: Boolean = false

    var parlayList: MutableList<ParlayOdd>? = mutableListOf()
        set(value) {
            //若無法組合串關時, 給予空物件用來紀錄”單注填充所有單注“的輸入金額
            field = value
            notifyDataSetChanged()
        }

    var moreOptionCollapse = false

    var needScrollToBottom = false //用來紀錄是否為點擊更多選項需滾動至底部

    var isCantParlayWarn = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ViewType.Bet.ordinal -> BetInfoItemViewHolder(
                layoutInflater.inflate(
                    R.layout.content_bet_info_item_v2,
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
            ViewType.Warn.ordinal -> CantParlayWarnViewHolder(
                layoutInflater.inflate(
                    R.layout.content_cant_parlay_warn,
                    parent,
                    false
                )
            )
            else -> BatchParlayConnectViewHolder(
                layoutInflater.inflate(
                    R.layout.item_bet_list_batch_control_connect_v2,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var currentOddsType = oddsType
        betList?.getOrNull(position)?.apply {
            if (matchOdd.isOnlyEUType
                || matchOdd.odds == matchOdd.malayOdds
                || matchType == MatchType.OUTRIGHT
                || matchType == MatchType.OTHER_OUTRIGHT
            ) {
                currentOddsType = OddsType.EU
            }
        }

        when (holder) {
            is BetInfoItemViewHolder -> {
                holder.bind(
                    betList?.getOrNull(position)!!,
                    currentOddsType,
                    itemCount,
                    onItemClickListener,
                    betList?.size ?: 0,
                    mSelectedPosition,
                    onSelectedPositionListener,
                    position,
                    userMoney
                )
            }
            is BatchSingleViewHolder -> {
                holder.bind(
                    parlayList?.getOrNull(position - (betList?.size ?: 0)),
                    parlayList?.size ?: 0,
                    betList ?: mutableListOf(),
                    currentOddsType,
                    hasBetClosed,
                    hasParlayList,
                    moreOptionCollapse,
                    onItemClickListener,
                    { notifyDataSetChanged() },
                    {
                        moreOptionCollapse = !moreOptionCollapse
                        needScrollToBottom = true
                        notifyDataSetChanged()
                    },
                    mSelectedPosition,
                    mBetView,
                    onSelectedPositionListener,
                    position,
                    hasBetClosedForSingle,
                    userMoney
                )
            }
            is BatchParlayConnectViewHolder -> {
                holder.bind(
                    parlayList?.getOrNull(position - (betList?.size ?: 0)),
                    currentOddsType,
                    hasBetClosed,
                    onItemClickListener,
                    mSelectedPosition,
                    mBetView,
                    onSelectedPositionListener,
                    position,
                    userMoney
                )
            }
            is CantParlayWarnViewHolder -> {
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val betSize = betList?.size ?: 0
        return when {
            isCantParlayWarn && position == (itemCount - 1) -> {
                ViewType.Warn.ordinal
            }
            position < betSize -> ViewType.Bet.ordinal
            position == betSize -> ViewType.ParlayFirst.ordinal
            else -> ViewType.Parlay.ordinal
        }
    }

    override fun getItemCount(): Int {
        var size = getListSize()
        if (isCantParlayWarn) {
            size++
        }
        return size
    }

    //使用HasStabledIds需複寫回傳的position, 若仍使用super.getItemId(position), 數據刷新會錯亂.
    //https://blog.csdn.net/karsonNet/article/details/80598435
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun showCantParlayWarn() {
        isCantParlayWarn = true
        notifyDataSetChanged()
    }

    fun hideCantParlayWarn() {
        isCantParlayWarn = false
        notifyDataSetChanged()
    }

    fun closeAllKeyboard() {
        //betList?.forEachIndexed { index, betInfoListData -> notifyItemChanged(index) }
        for (i in 0 until getListSize()) {
            notifyItemChanged(i)
        }
    }

    private fun getListSize(): Int {
        val betListSize = betList?.size ?: 0
        val parlayListSize = when {
            betListSize < 2 -> 0
            betListSize == 2 || !moreOptionCollapse -> 1
            else -> (parlayList?.size ?: 0)
        }
        return betListSize + parlayListSize
    }

    //單注
    class BetInfoItemViewHolder(itemView: View) : BetInfoChangeViewHolder(itemView) {
        private var parlayMaxBet: Long = 0
        private var inputMaxMoney: Long = 0
        var mUserMoney: Double = 0.0
        fun bind(
            itemData: BetInfoListData,
            currentOddsType: OddsType,
            itemCount: Int,
            onItemClickListener: OnItemClickListener,
            betListSize: Int,
            mSelectedPosition: Int,
            onSelectedPositionListener: OnSelectedPositionListener,
            position: Int,
            userMoney: Double
        ) {

            //設置輸入投注上限額
            setupInputMaxMoney(itemData, userMoney)

            itemView.apply {
                setupBetAmountInput(
                    itemData,
                    currentOddsType,
                    onItemClickListener,
                    betListSize,
                    mSelectedPosition,
                    onSelectedPositionListener,
                    position,
                    userMoney
                )

                setupOddStatus(itemData)

                setupDeleteButton(itemData, itemCount, onItemClickListener)

                setupMaximumLimitView(itemData, onItemClickListener, position)

//                bottom_view.visibility = if(position == betListSize -1) View.GONE else View.VISIBLE
            }
        }

        private fun setupInputMaxMoney(itemData: BetInfoListData, userMoney: Double) {
            mUserMoney = userMoney
            parlayMaxBet = itemData.parlayOdds?.max?.toLong() ?: 0
            inputMaxMoney = if (parlayMaxBet > 0) {
                min(parlayMaxBet, userMoney.toLong())
            } else {
                userMoney.toLong()
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setupBetAmountInput(
            itemData: BetInfoListData,
            currentOddsType: OddsType,
            onItemClickListener: OnItemClickListener,
            betListSize: Int,
            mSelectedPosition: Int,
            onSelectedPositionListener: OnSelectedPositionListener,
            position: Int,
            userMoney: Double
        ) {
            itemView.apply {
                et_bet.apply {
                    if (tag is TextWatcher) {
                        removeTextChangedListener(tag as TextWatcher)
                    }
                    filters = arrayOf(MoneyInputFilter())
                }
                onFocusChangeListener = null

                setupOddInfo(itemData, currentOddsType, betListSize, onItemClickListener)
                setupMinimumLimitMessage(itemData)
                onItemClickListener.refreshBetInfoTotal()

                val parlayMaxBet = itemData.parlayOdds?.max?.toLong() ?: 0
                val inputMaxMoney = if (parlayMaxBet > 0) {
                    min(parlayMaxBet, userMoney.toLong())
                } else {
                    userMoney.toLong()
                }

                if (et_bet.isFocusable) {
                    layoutKeyBoard?.setMaxBetMoney(inputMaxMoney)
                }

                val tw: TextWatcher?
                tw = object : TextWatcher {
                    override fun afterTextChanged(it: Editable?) {
                        if (it.isNullOrEmpty()) {
                            itemData.betAmount = 0.000
                            itemData.inputBetAmountStr = ""
                            itemData.input = ""

                            tv_check_maximum_limit.visibility = View.GONE
                            ll_bet_quota_detail.visibility = View.GONE
                            ll_win_quota_detail.visibility = View.VISIBLE
                            checkMinimumLimit(itemData)
                        } else {

                            //輸入時 直接顯示可贏額
                            tv_check_maximum_limit.visibility = View.GONE
                            ll_bet_quota_detail.visibility = View.GONE
                            ll_win_quota_detail.visibility = View.VISIBLE


                            val quota = it.toString().toDouble()
                            itemData.betAmount = quota
                            itemData.inputBetAmountStr = it.toString()
                            itemData.input = TextUtil.formatInputMoney(quota)
                            inputMaxMoney.let { max ->
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
//                            var win = quota * getOdds(itemData.matchOdd, oddsType)
//                            if (oddsType == OddsType.EU) {
//                                win -= quota
//                            }

                            var realAmount = itemData.betAmount
                            var win = 0.0
                            when (currentOddsType) {
                                OddsType.MYS -> {
                                    if (getOdds(itemData.matchOdd, currentOddsType) < 0) {
                                        realAmount = itemData.betAmount * Math.abs(
                                            getOdds(
                                                itemData.matchOdd,
                                                currentOddsType
                                            )
                                        )
                                        tvRealAmount.text = ArithUtil.toMoneyFormat(realAmount)
                                        win = itemData.betAmount
                                    } else {
                                        win = itemData.betAmount * getOdds(
                                            itemData.matchOdd,
                                            currentOddsType
                                        )
                                        tvRealAmount.text =
                                            ArithUtil.toMoneyFormat(itemData.betAmount)
                                    }

                                }
                                OddsType.IDN -> {
                                    if (getOdds(itemData.matchOdd, currentOddsType) < 0) {
                                        realAmount = itemData.betAmount * Math.abs(
                                            getOdds(
                                                itemData.matchOdd,
                                                currentOddsType
                                            )
                                        )
                                        tvRealAmount.text = ArithUtil.toMoneyFormat(realAmount)
                                        win = itemData.betAmount
                                    } else {
                                        win = itemData.betAmount * getOdds(
                                            itemData.matchOdd,
                                            currentOddsType
                                        )
                                        tvRealAmount.text =
                                            ArithUtil.toMoneyFormat(itemData.betAmount)
                                    }
                                }
                                OddsType.EU -> {
                                    win = itemData.betAmount * (getOdds(
                                        itemData.matchOdd,
                                        currentOddsType
                                    ) - 1)
                                    tvRealAmount.text = ArithUtil.toMoneyFormat(itemData.betAmount)

                                }
                                else -> {
                                    win = itemData.betAmount * getOdds(
                                        itemData.matchOdd,
                                        currentOddsType
                                    )
                                    tvRealAmount.text = ArithUtil.toMoneyFormat(itemData.betAmount)
                                }
                            }

                            itemData.realAmount = realAmount
                            tv_win_quota.text = TextUtil.format(win)
                        }
                        onItemClickListener.refreshBetInfoTotal()
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }
                }

                //et_bet.keyListener = null

                et_bet.addTextChangedListener(tw)
                et_bet.tag = tw
                //TODO check on touch listener
                et_bet.isSelected = mSelectedPosition == bindingAdapterPosition

                et_bet.setOnTouchListener { view, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) {
                            et_bet.isFocusable = true
                            layoutKeyBoard.showKeyboard(
                                et_bet,
                                position,
                                inputMaxMoney,
                                itemData.parlayOdds?.min?.toLong() ?: 0
                            )
                            onSelectedPositionListener.onSelectChange(
                                bindingAdapterPosition,
                                BetViewType.SINGLE
                            )
                        }
                    }
                    false
                }

                et_bet.setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus) layoutKeyBoard?.hideKeyboard()
                }

//                et_clickable.setOnClickListener {
//                    et_bet.isFocusable = true
//                    //onItemClickListener.onShowKeyboard(et_bet, itemData.matchOdd, position, itemData.parlayOdds?.max?.toLong() ?: 0)
//                    layoutKeyBoard.showKeyboard(
//                        et_bet,
//                        position,
//                        itemData.parlayOdds?.max?.toLong() ?: 0,
//                        itemData.parlayOdds?.min?.toLong() ?: 0
//                    )
//                    onSelectedPositionListener.onSelectChange(
//                        bindingAdapterPosition,
//                        BetViewType.SINGLE
//                    )
//                }

                cl_item_background.setOnClickListener {
                    onItemClickListener.onHideKeyBoard()
                    clearFocus()
                }
            }
        }

        var oldOdds = ""
        var handler = Handler()

        private fun setupOddInfo(
            itemData: BetInfoListData,
            currentOddsType: OddsType,
            betListSize: Int,
            onItemClickListener: OnItemClickListener
        ) {
            itemView.apply {
                v_point.visibility =
                    if (itemData.pointMarked && betListSize > 1) View.VISIBLE else View.GONE

//                if (itemData.matchOdd.odds == itemData.matchOdd.malayOdds
//                    || itemData.matchType == MatchType.OUTRIGHT
//                    || itemData.matchType == MatchType.OTHER_OUTRIGHT
//                ) {
//                    currentOddsType = OddsType.EU
//                }

                //setupOddsContent(itemData, oddsType = currentOddsType, tv_odds_content)
                if (itemData.matchOdd.status == BetStatus.ACTIVATED.code && oldOdds != "" && oldOdds != TextUtil.formatForOdd(getOdds(itemData.matchOdd, currentOddsType))) {
                    tv_odd_content_changed.visibility = if (handler != null) View.VISIBLE else View.GONE
                    handler?.postDelayed({
                        tv_odd_content_changed?.visibility = View.GONE
                    }, 3000)
                    tv_odd_content_changed.text = context.getString(
                        R.string.bet_info_odd_content_changed2,
                        oldOdds,
                        TextUtil.formatForOdd(getOdds(itemData.matchOdd, currentOddsType))
                    )
                }
                var spread = ""
                spread = if (itemData.matchOdd.spread.isEmpty() || !PlayCate.needShowSpread(itemData.matchOdd.playCode) || itemData.matchType == MatchType.OUTRIGHT
                ) {
                    ""
                } else {
                    itemData.matchOdd.spread
                }
                tv_odds_content.text = itemData.matchOdd.playName
                if (itemData.matchOdd.status == BetStatus.ACTIVATED.code && oldOdds != TextUtil.formatForOdd(getOdds(itemData.matchOdd, currentOddsType))) {
                    oldOdds = TextUtil.formatForOdd(getOdds(itemData.matchOdd, currentOddsType))
                }
                tvOdds.text = if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) "@" + TextUtil.formatForOdd(getOdds(itemData.matchOdd, currentOddsType)) else "–"
                if(itemData.matchOdd.extInfo != null){
                    tvContent.text = itemData.matchOdd.extInfo + spread
                }else{
                    tvContent.text = spread
                }
                btnRecharge.setOnClickListener {
                    onItemClickListener.onRechargeClick()
                }
                //隊伍名稱
                tv_match.text = when {
                    itemData.matchType == MatchType.OUTRIGHT -> itemData.outrightMatchInfo?.name
                    itemData.matchOdd.awayName?.length?.let {
                        itemData.matchOdd.homeName?.length?.plus(
                            it
                        )
                    } ?: 0 > 21 -> "${itemData.matchOdd.homeName}${context.getString(R.string.verse_)}\n${itemData.matchOdd.awayName}"
                    else -> "${itemData.matchOdd.homeName}${context.getString(R.string.verse_)}${itemData.matchOdd.awayName}"
                }

                //玩法名稱 目前詳細玩法裡面是沒有給betPlayCateNameMap，所以顯示邏輯沿用舊版
                val nameOneLine = { inputStr: String ->
                    inputStr.replace("\n", "-")
                }

                val inPlay = System.currentTimeMillis() > itemData.matchOdd.startTime ?: 0
                when {
                    itemData.betPlayCateNameMap.isNullOrEmpty() -> {
                        tv_name.text = when (inPlay && itemData.matchType != MatchType.OUTRIGHT && itemData.matchOdd.gameType == GameType.FT.key) {
                            true -> {
                                context.getString(
                                    R.string.bet_info_in_play_score,
                                    itemData.matchOdd.playCateName,
                                    itemData.matchOdd.homeScore.toString(),
                                    itemData.matchOdd.awayScore.toString()
                                )
                            }
                            else -> itemData.matchOdd.playCateName
                        }
                    }

                    else -> {
                        tv_name.text = when (inPlay && itemData.matchType != MatchType.OUTRIGHT && itemData.matchOdd.gameType == GameType.FT.key) {
                            true -> {
                                context.getString(
                                    R.string.bet_info_in_play_score,
                                    itemData.betPlayCateNameMap?.getNameMap(itemData.matchOdd.gameType, itemData.matchOdd.playCode)
                                        ?.get(LanguageManager.getSelectLanguage(context).key) ?: "",
                                    itemData.matchOdd.homeScore.toString(),
                                    itemData.matchOdd.awayScore.toString()
                                )
                            }
                            else -> nameOneLine(
                                itemData.betPlayCateNameMap?.getNameMap(itemData.matchOdd.gameType, itemData.matchOdd.playCode)
                                    ?.get(LanguageManager.getSelectLanguage(context).key) ?: ""
                            )
                        }
                    }
                }

                if (itemData.betAmount > 0) {
                    et_bet.setText(itemData.inputBetAmountStr)
                    et_bet.setSelection(et_bet.text.length)
                    tv_check_maximum_limit.visibility = View.GONE
                    ll_bet_quota_detail.visibility = View.GONE
                    ll_win_quota_detail.visibility = View.VISIBLE
                    checkMinimumLimit(itemData)
                } else {
                    et_bet.setText("")
                    tv_check_maximum_limit.visibility = View.GONE
                    ll_bet_quota_detail.visibility = View.GONE
                    ll_win_quota_detail.visibility = View.VISIBLE
                    checkMinimumLimit(itemData)
                }
                et_bet.setSelection(et_bet.text.length)

                val quota = itemData.betAmount
                //比照以往計算
//                            var win = quota * getOdds(itemData.matchOdd, oddsType)
//                            if (oddsType == OddsType.EU) {
//                                win -= quota
//                            }

                var realAmount = itemData.betAmount
                var win = 0.0
                when (currentOddsType) {
                    OddsType.MYS -> {
                        if (getOdds(itemData.matchOdd, currentOddsType) < 0) {
                            realAmount = itemData.betAmount * Math.abs(
                                getOdds(
                                    itemData.matchOdd,
                                    currentOddsType
                                )
                            )
                            tvRealAmount.text = ArithUtil.toMoneyFormat(realAmount)
                            win = itemData.betAmount
                        } else {
                            win = itemData.betAmount * getOdds(itemData.matchOdd, currentOddsType)
                            tvRealAmount.text = ArithUtil.toMoneyFormat(itemData.betAmount)
                        }

                    }
                    OddsType.IDN -> {
                        if (getOdds(itemData.matchOdd, currentOddsType) < 0) {
                            realAmount = itemData.betAmount * Math.abs(
                                getOdds(
                                    itemData.matchOdd,
                                    currentOddsType
                                )
                            )
                            tvRealAmount.text = ArithUtil.toMoneyFormat(realAmount)
                            win = itemData.betAmount
                        } else {
                            win = itemData.betAmount * getOdds(itemData.matchOdd, currentOddsType)
                            tvRealAmount.text = ArithUtil.toMoneyFormat(itemData.betAmount)
                        }
                    }
                    OddsType.EU -> {
                        win = itemData.betAmount * (getOdds(itemData.matchOdd, currentOddsType) - 1)
                        tvRealAmount.text = ArithUtil.toMoneyFormat(itemData.betAmount)

                    }
                    else -> {
                        win = itemData.betAmount * getOdds(itemData.matchOdd, currentOddsType)
                        tvRealAmount.text = ArithUtil.toMoneyFormat(itemData.betAmount)
                    }
                }

                itemData.realAmount = realAmount
                tv_win_quota.text = TextUtil.format(win)
            }
        }

        private fun setupMinimumLimitMessage(itemData: BetInfoListData) {
            itemView.apply {
                itemData.parlayOdds?.min?.let { min ->
                    tv_error_message.text = String.format(
                        context.getString(R.string.bet_info_list_minimum_limit_amount),
                        min,
                        sConfigData?.systemCurrency
                    )
                }
            }
        }

        private fun checkMinimumLimit(
            itemData: BetInfoListData,
            betAmount: Double = itemData.betAmount
        ) {
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
                var inPlay = System.currentTimeMillis() > itemData.matchOdd.startTime ?: 0
                if (itemData.matchOdd.startTime == null)
                    inPlay = false
                if (inPlay) {
                    tvInGame.visibility = View.VISIBLE
                } else {
                    tvInGame.visibility = View.GONE
                }
                tvLeagueName.text = itemData.matchOdd.leagueName
                ivSportLogo.setImageResource(GameType.getGameTypeIcon(GameType.getGameType(itemData.matchOdd.gameType)!!))

                if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) {
                    cl_item_background.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.colorWhite
                        )
                    )
                    iv_bet_lock.visibility = View.GONE
                    viewGrey.visibility = View.VISIBLE
                    et_bet.apply {
                        isEnabled = true
                        isFocusable = true
                        isFocusableInTouchMode = true
//                        hint = getLimitHint(
//                            context,
//                            itemData.parlayOdds?.min ?: 0,
//                            itemData.parlayOdds?.max ?: 0
//                        )
                    }
                    et_clickable.isEnabled = true //EditText的click事件
                    cl_quota_detail.visibility = View.VISIBLE
                } else {
                    cl_item_background.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.colorWhite2
                        )
                    )
                    iv_bet_lock.visibility = View.VISIBLE
                    viewGrey.visibility = View.INVISIBLE
                    et_bet.apply {
                        isEnabled = false
                        isFocusable = false
                        isFocusableInTouchMode = false
                    }
                    et_clickable.isEnabled = false //EditText的click事件
                    cl_quota_detail.visibility = View.VISIBLE
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

        private fun setupMaximumLimitView(
            itemData: BetInfoListData,
            onItemClickListener: OnItemClickListener,
            position: Int
        ) {
            itemView.apply {
                tv_bet_maximum_limit.text = TextUtil.formatBetQuota(itemData.parlayOdds?.max ?: 0)
                tv_check_maximum_limit.setOnClickListener {
                    it.visibility = View.GONE
                    ll_bet_quota_detail.visibility = View.GONE
                }

                ll_bet_quota_detail.setOnClickListener {
                    et_bet.apply {
                        setText(itemData.parlayOdds?.max.toString())
                        isFocusable = true
                        setSelection(text.length)
                    }
                    onItemClickListener.onShowKeyboard(
                        et_bet,
                        itemData.matchOdd,
                        position,
                        itemData.parlayOdds?.max?.toLong() ?: 0
                    )
                }
            }
        }

        private fun getLimitHint(context: Context, min: Int, max: Int): String {
            return String.format(
                "${context.getString(R.string.edt_hint_deposit_money_new)}",
                TextUtil.formatBetQuota(min),
                TextUtil.formatBetQuota(max)
            )
        }
    }

    //填充所有單注、串關第一項、展開更多
    class BatchSingleViewHolder(itemView: View) : BatchParlayViewHolder(itemView) {
        private var mUserMoney: Double = 0.0
        fun bind(
            itemData: ParlayOdd?,
            parlayListSize: Int,
            betList: MutableList<BetInfoListData>,
            currentOddsType: OddsType,
            hasBetClosed: Boolean,
            hasParlayList: Boolean,
            moreOptionCollapse: Boolean,
            onItemClickListener: OnItemClickListener,
            notifyAllBet: () -> Unit,
            clickMoreOption: () -> Unit,
            mSelectedPosition: Int,
            mBetView: BetViewType,
            onSelectedPositionListener: OnSelectedPositionListener,
            position: Int,
            hasBetClosedForSingle: Boolean,
            userMoney: Double
        ) {
            mUserMoney = userMoney

            itemView.apply {
                when (parlayListSize) {
                    1 -> {
                        ll_more_option.visibility = View.GONE
                        if (hasParlayList) {
                            item_first_connect.visibility = View.VISIBLE
                            itemData.let {
                                it?.max =
                                    if (GameConfigManager.maxParlayBetMoney?.toLong() ?: 0 > itemData?.max?.toLong() ?: 0) itemData?.max
                                        ?: 0 else GameConfigManager.maxParlayBetMoney ?: 0
                            }
                            setupParlayItem(
                                itemData,
                                OddsType.EU,
                                hasBetClosed,
                                true,
                                onItemClickListener,
                                mSelectedPosition,
                                mBetView,
                                onSelectedPositionListener,
                                position,
                                userMoney
                            )
                        } else {
                            item_first_connect.visibility = View.GONE
                        }

                        setupSingleItem(
                            betList,
                            itemData,
                            currentOddsType,
                            onItemClickListener,
                            notifyAllBet,
                            mSelectedPosition,
                            mBetView,
                            onSelectedPositionListener,
                            position,
                            hasBetClosedForSingle
                        )
                    }
                    else -> {
                        item_first_connect.visibility = View.VISIBLE
                        ll_more_option.visibility = View.VISIBLE
                        itemData.let {
                            it?.max =
                                if (GameConfigManager.maxParlayBetMoney?.toLong() ?: 0 > itemData?.max?.toLong() ?: 0) itemData?.max
                                    ?: 0 else GameConfigManager.maxParlayBetMoney
                                    ?: 0
                        }
                        setupParlayItem(
                            itemData,
                            OddsType.EU,
                            hasBetClosed,
                            true,
                            onItemClickListener,
                            mSelectedPosition,
                            mBetView,
                            onSelectedPositionListener,
                            position,
                            userMoney
                        )
                        setupSingleItem(
                            betList,
                            itemData,
                            currentOddsType,
                            onItemClickListener,
                            notifyAllBet,
                            mSelectedPosition,
                            mBetView,
                            onSelectedPositionListener,
                            position,
                            hasBetClosedForSingle
                        )

                        setupClickMoreItem(
                            itemView.ll_more_option,
                            moreOptionCollapse,
                            clickMoreOption
                        )
                    }
                }
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setupSingleItem(
            betList: MutableList<BetInfoListData>,
            itemData: ParlayOdd?,
            currentOddsType: OddsType,
            onItemClickListener: OnItemClickListener,
            notifyAllBet: () -> Unit,
            mSelectedPosition: Int,
            mBetView: BetViewType,
            onSelectedPositionListener: OnSelectedPositionListener,
            position: Int,
            hasBetClosed: Boolean,
        ) {
            itemView.item_first_single.apply {

                et_bet_single.apply {
                    if (tag is TextWatcher) {
                        removeTextChangedListener(tag as TextWatcher)
                    }
                    onFocusChangeListener = null
                    filters = arrayOf(MoneyInputFilter())
                }

                if (et_bet_single.text.isNullOrEmpty())
                    ll_winnable.visibility = View.VISIBLE
                else
                    ll_winnable.visibility = View.VISIBLE

                tv_single_count.text = betList.size.toString()

                val initValue =
                    if (!(itemData?.allSingleInput.isNullOrEmpty())) itemData?.allSingleInput else ""
                //init winnable amount
                tv_winnable_amount.text = TextUtil.formatMoney(
                    getAllSingleWinnableAmount(
                        if (initValue.isNullOrEmpty()) 0.0 else initValue.toDouble(),
                        currentOddsType,
                        betList
                    )
                )
                et_bet_single.apply {
                    //init bet amount value
                    val maxAmount = getMaxOrMinAmount(isGetMax = true, betList)
                    val minAmount = getMaxOrMinAmount(isGetMax = false, betList)
                    //hint = getAllSingleMinMaxHint(context, maxAmount, minAmount)

                    setText(initValue)
                    et_bet_single.setSelection(et_bet_single.text.length)

                    /* set listener */
                    val tw: TextWatcher?
                    tw = object : TextWatcher {
                        override fun afterTextChanged(it: Editable?) {
                            val inputValue =
                                if (it.isNullOrEmpty()) 0.0 else it.toString().toDouble()
                            itemData?.allSingleInput =
                                if (it.isNullOrEmpty()) null else it.toString()
                            val allWinnableAmount =
                                getAllSingleWinnableAmount(inputValue, currentOddsType, betList)

                            val maxAmount = getMaxOrMinAmount(isGetMax = true, betList)

                            maxAmount.let { max ->
                                if (inputValue > max) {
                                    et_bet_single.apply {
                                        setText(TextUtil.formatInputMoney(max))
                                        setSelection(text.length)
                                    }
                                    return@afterTextChanged
                                }
                            }

                            when (allWinnableAmount > 0) {
                                true -> {
                                    itemView.apply {
                                        ll_winnable.visibility = View.VISIBLE
                                        //
                                        tv_winnable_amount.text =
                                            TextUtil.formatMoney(allWinnableAmount)
                                    }

                                }
                                else -> {
                                    itemView.ll_winnable.visibility = View.VISIBLE
                                }
                            }

                            betList.forEachIndexed { _, data ->
                                if (data.matchOdd.status != BetStatus.ACTIVATED.code)
                                    return@forEachIndexed

                                if (data.parlayOdds?.max == null || inputValue < (data.parlayOdds?.max
                                        ?: 0)
                                ) {
                                    data.betAmount = inputValue
                                    data.inputBetAmountStr = it.toString()
                                } else {
                                    data.betAmount = (data.parlayOdds?.max ?: 0).toDouble()
                                    data.inputBetAmountStr = (data.parlayOdds?.max ?: 0).toString()
                                }
                            }
                            notifyAllBet()
                            onItemClickListener.refreshBetInfoTotal()
                        }

                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                        }
                    }

                    //keyListener = null
                    removeTextChangedListener(tw)
                    addTextChangedListener(tw)
                    tag = tw
                }
                et_bet_single.isSelected =
                    mSelectedPosition == bindingAdapterPosition && mBetView == BetViewType.SINGLE

                if (et_bet_single.isFocusable) layoutKeyBoard.setMaxBetMoney(getMaxOrMinAmount(isGetMax = true, betList))

                et_bet_single.setOnTouchListener { view, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        et_bet_single.isFocusable = true
                        layoutKeyBoard.showKeyboard(
                            et_bet_single,
                            position,
                            getMaxOrMinAmount(isGetMax = true, betList),
                            getMaxOrMinAmount(isGetMax = false, betList)
                        )
                        //onItemClickListener.onShowParlayKeyboard(et_bet_single, itemData, position, getMaxOrMinAmount(isGetMax = true, betList))
                        onSelectedPositionListener.onSelectChange(
                            bindingAdapterPosition,
                            BetViewType.SINGLE
                        )


                    }
                    false
                }
//                et_container.setOnClickListener {
//                    et_bet_single.isFocusable = true
//                    et_bet_single.setSelection(et_bet_single.text.length)
//                    layoutKeyBoard.showKeyboard(
//                        et_bet_single,
//                        position,
//                        getMaxOrMinAmount(isGetMax = true, betList),
//                        getMaxOrMinAmount(isGetMax = false, betList)
//                    )
//                    //onItemClickListener.onShowParlayKeyboard(et_bet_single, itemData, position, getMaxOrMinAmount(isGetMax = true, betList))
//                    onSelectedPositionListener.onSelectChange(
//                        bindingAdapterPosition,
//                        BetViewType.SINGLE
//                    )
//                }

                et_bet_single.setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus) layoutKeyBoard?.hideKeyboard()
                }

                item_first_single.setOnClickListener {
                    //layoutKeyBoard.hideKeyboard()
                    onItemClickListener.onHideKeyBoard()
                    clearFocus()
                }

                setupItemEnable(hasBetClosed)

                btn_rule_single.setOnClickListener {
                    onItemClickListener.showParlayRule(
                        ParlayType.SINGLE.key,
                        context.getString(ParlayType.SINGLE.ruleStringRes ?: 0)
                    )
                }
            }
        }

        private fun setupItemEnable(hasBetClosed: Boolean) {
            itemView.apply {
//                iv_bet_lock_single.visibility = if (hasBetClosed) View.VISIBLE else View.GONE

                ll_winnable.visibility = if (hasBetClosed) View.GONE else View.VISIBLE

                et_bet_single.apply {
                    isEnabled = !hasBetClosed
                    isFocusable = !hasBetClosed
                    isFocusableInTouchMode = !hasBetClosed
                    filters = arrayOf(MoneyInputFilter())
                }

                //et_container.isEnabled = !hasBetClosed //EditText的click事件
            }
        }

        /**
         * 填充所有單注後獲取總可贏額
         */
        private fun getAllSingleWinnableAmount(
            betAmount: Double,
            currentOddsType: OddsType,
            betList: MutableList<BetInfoListData>
        ): Double {
            var allWinnableAmount = 0.0
            betList.forEach {
                var win = 0.0
                when (currentOddsType) {
                    OddsType.MYS -> {
                        if (getOdds(it.matchOdd, currentOddsType) < 0) {
                            win = betAmount
                        } else {
                            win = betAmount * getOdds(it.matchOdd, currentOddsType)
                        }

                    }
                    OddsType.IDN -> {
                        if (getOdds(it.matchOdd, currentOddsType) < 0) {
                            win = betAmount
                        } else {
                            win = betAmount * getOdds(it.matchOdd, currentOddsType)
                        }
                    }
                    OddsType.EU -> {
                        win = betAmount * (getOdds(it.matchOdd, currentOddsType) - 1)
                    }
                    else -> {
                        win = betAmount * getOdds(it.matchOdd, currentOddsType)
                    }
                }
                allWinnableAmount += win
            }
            return allWinnableAmount
        }

        private fun getAllSingleMinMaxHint(
            context: Context,
            maxAmount: Long,
            minAmount: Long
        ): String {
            return String.format(
                "${context.getString(R.string.edt_hint_deposit_money_new)}",
                TextUtil.formatBetQuota(minAmount),
                TextUtil.formatBetQuota(maxAmount)
            )
        }

        private fun getMaxOrMinAmount(
            isGetMax: Boolean,
            betList: MutableList<BetInfoListData>
        ): Long {
            var min = betList.first().parlayOdds?.min ?: 0
            var max = betList.first().parlayOdds?.max ?: 99999999
            betList.forEach {
                if (it.parlayOdds?.min ?: 0 > min) {
                    min = it.parlayOdds?.min ?: 0
                }
                if (it.parlayOdds?.max ?: 0 < max) {
                    max = it.parlayOdds?.max ?: 99999999
                }
            }

            return when (isGetMax) {
                true -> min(max.toLong(), mUserMoney.toLong())
                else -> min.toLong()
            }
        }


        private fun getAllWinnableAmount(
            betAmount: Double,
            currentOddsType: OddsType,
            betList: MutableList<BetInfoListData>
        ): Double {
            var allWinnableAmount = 0.0
            betList.forEach {
                var realAmount = betAmount
                var win = 0.0
//                var currentOddsType = oddsType
//                if (it.matchOdd.odds == it.matchOdd.malayOdds
//                    || it.matchType == MatchType.OUTRIGHT
//                    || it.matchType == MatchType.OTHER_OUTRIGHT
//                ) {
//                    currentOddsType = OddsType.EU
//                }

                when (currentOddsType) {
                    OddsType.MYS -> {
                        if (getOdds(it.matchOdd, currentOddsType) < 0) {
                            realAmount = betAmount * Math.abs(
                                getOdds(
                                    it.matchOdd,
                                    currentOddsType
                                )
                            )
                            win = betAmount
                        } else {
                            win = betAmount * getOdds(it.matchOdd, currentOddsType)
                        }

                    }
                    OddsType.IDN -> {
                        if (getOdds(it.matchOdd, currentOddsType) < 0) {
                            realAmount = betAmount * Math.abs(
                                getOdds(
                                    it.matchOdd,
                                    currentOddsType
                                )
                            )
                            win = betAmount
                        } else {
                            win = betAmount * getOdds(it.matchOdd, currentOddsType)
                        }
                    }
                    OddsType.EU -> {
                        win = betAmount * (getOdds(it.matchOdd, currentOddsType) - 1)

                    }
                    else -> {
                        win = betAmount * (getOdds(it.matchOdd, currentOddsType))
                    }
                }


                allWinnableAmount += win
            }
            return allWinnableAmount
        }

        private fun setupClickMoreItem(
            btnShowMore: View,
            moreOptionCollapse: Boolean,
            clickEvent: () -> Unit
        ) {
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
            currentOddsType: OddsType,
            hasBetClosed: Boolean,
            onItemClickListener: OnItemClickListener,
            mSelectedPosition: Int,
            mBetView: BetViewType,
            onSelectedPositionListener: OnSelectedPositionListener,
            position: Int,
            userMoney: Double
        ) {
            itemData.let {
                it?.max =
                    if (GameConfigManager.maxParlayBetMoney?.toLong() ?: 0 > itemData?.max?.toLong() ?: 0) itemData?.max
                        ?: 0 else GameConfigManager.maxParlayBetMoney ?: 0
            }
            setupParlayItem(
                itemData,
                currentOddsType,
                hasBetClosed,
                false,
                onItemClickListener,
                mSelectedPosition,
                mBetView,
                onSelectedPositionListener,
                position,
                userMoney
            )
        }
    }

    abstract class BatchParlayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var mUserMoney: Double = 0.0
        private var inputMaxMoney: Long = 0
        protected fun setupParlayItem(
            itemData: ParlayOdd?,
            currentOddsType: OddsType,
            hasBetClosed: Boolean,
            firstItem: Boolean = false,
            onItemClickListener: OnItemClickListener,
            mSelectedPosition: Int,
            mBetView: BetViewType,
            onSelectedPositionListener: OnSelectedPositionListener,
            position: Int,
            userMoney: Double
        ) {
            //設置投注輸入上限額
            setupInputMoney(itemData, userMoney)

            itemView.apply {
                ll_winnable.visibility = View.VISIBLE
                ll_max_bet_amount.visibility = View.GONE

                setupItemEnable(hasBetClosed)


                itemData?.let { data ->
                    tv_parlay_type.text = getParlayName(data.parlayType)

                    tv_parlay_odd.apply {
                        if (firstItem && !hasBetClosed) {
                            visibility = View.VISIBLE
                            text = TextUtil.formatForOdd(getOdds(data, currentOddsType))
                        } else
                            visibility = View.GONE
                    }

                    tv_symbol_odd.visibility =
                        if (firstItem && !hasBetClosed) View.VISIBLE else View.GONE

                    tv_com_count.text = data.num.toString()

                    setupBetAmountInput(
                        data,
                        OddsType.EU,
                        onItemClickListener,
                        mSelectedPosition,
                        mBetView,
                        onSelectedPositionListener,
                        position
                    )

                    setupMaximumLimitView(
                        data,
                        onItemClickListener,
                        mSelectedPosition,
                        mBetView,
                        onSelectedPositionListener,
                        position
                    )

                    setupParlayRuleButton(data, onItemClickListener)

                }
            }
        }

        private fun setupInputMoney(itemData: ParlayOdd?, userMoney: Double) {
            mUserMoney = userMoney
            val parlayMaxBet = itemData?.max ?: 0
            inputMaxMoney = if (parlayMaxBet > 0) {
                min(parlayMaxBet.toLong(), mUserMoney.toLong())
            } else {
                mUserMoney.toLong()
            }
        }

        private fun getParlayName(parlayType: String): String {
            return getParlayStringRes(parlayType)?.let {
                itemView.context.getString(it)
            } ?: ""
        }

        private fun setupItemEnable(hasBetClosed: Boolean) {
            itemView.apply {
//                iv_bet_lock.visibility = if (hasBetClosed) View.VISIBLE else View.GONE
                //viewGrey.visibility = if (hasBetClosed) View.INVISIBLE else View.VISIBLE
                item_parlay_quota_detail.visibility = if (hasBetClosed) View.GONE else View.VISIBLE

                btn_rule.visibility = if (hasBetClosed) View.GONE else View.VISIBLE

                et_bet.apply {
                    isEnabled = !hasBetClosed
                    isFocusable = !hasBetClosed
                    isFocusableInTouchMode = !hasBetClosed
                    filters = arrayOf(MoneyInputFilter())
                }

                et_clickable.isEnabled = !hasBetClosed //EditText的click事件
            }
        }

        private fun setupOddInfo(data: ParlayOdd, currentOddsType: OddsType) {
            itemView.apply {
                if (data.betAmount > 0) {
                    itemView.apply {
                        tv_check_maximum_limit.visibility = View.GONE
                        ll_bet_quota_detail.visibility = View.GONE
                        ll_win_quota_detail.visibility = View.VISIBLE
                        checkMinimumLimit(data)
                    }
                } else {
                    et_bet.setText("")
                    itemView.apply {
                        tv_check_maximum_limit.visibility = View.GONE
                        ll_bet_quota_detail.visibility = View.GONE
                        ll_win_quota_detail.visibility = View.VISIBLE
                        checkMinimumLimit(data)
                    }
                }

                et_bet.setSelection(et_bet.text.length)

                val quota = data.betAmount
                //比照以往計算
                var win = quota * getOdds(data, currentOddsType)
                if (currentOddsType == OddsType.EU) {
                    win -= (quota * data.num)
                }
                tv_win_quota.text = TextUtil.format(win)
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setupBetAmountInput(
            data: ParlayOdd,
            currentOddsType: OddsType,
            onItemClickListener: OnItemClickListener,
            mSelectedPosition: Int,
            mBetView: BetViewType,
            onSelectedPositionListener: OnSelectedPositionListener,
            position: Int
        ) {
            itemView.apply {
                et_bet.apply {
                    if (tag is TextWatcher) {
                        removeTextChangedListener(tag as TextWatcher)
                    }

                    setupOddInfo(data, currentOddsType)
                    setupMinimumLimitMessage(data)
                    onItemClickListener.refreshBetInfoTotal()
                    /* set listener */
                    val tw: TextWatcher?
                    var ignore = false
                    tw = object : TextWatcher {
                        override fun afterTextChanged(it: Editable?) {
                            val inputValue =
                                if (it.isNullOrEmpty() || mUserMoney == 0.0) 0.0 else it.toString().toDouble()

                            if (inputValue == 0.0){
                                if (!ignore) {
                                    ignore = true
                                    setText("0")
                                    setSelection(text.length)
                                    ignore = false
                                }
                                return
                            }

                            if (inputValue > inputMaxMoney) {
                                val maxValue = TextUtil.formatInputMoney(data.max)
                                if (!ignore) {
                                    ignore = true
                                    setText(maxValue)
                                    setSelection(maxValue.length)
                                    ignore = false
                                }
                                return
                            }

                            //查看最高限額
                            if (inputValue <= 0) {
                                data.betAmount = 0.000

                                itemView.apply {
                                    tv_check_maximum_limit.visibility = View.GONE
                                    ll_bet_quota_detail.visibility = View.GONE
                                    ll_win_quota_detail.visibility = View.VISIBLE
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
                                data.inputBetAmountStr = it.toString()
                                inputMaxMoney.let { max ->
                                    if (inputValue > max) {
                                        et_bet.apply {
                                            setText(TextUtil.formatInputMoney(max))
                                            setSelection(text.length)
                                        }
                                        return@afterTextChanged
                                    }
                                }

                                checkMinimumLimit(data, quota)
                                //比照以往計算
                                var win = quota * getOdds(data, currentOddsType)
                                //if (oddsType == OddsType.EU) {
                                win -= (quota * data.num)
                                //}
                                itemView.tv_win_quota.text = TextUtil.format(win)
                            }


                            data.betAmount = TextUtil.formatInputMoney(inputValue).toDouble()
                            data.inputBetAmountStr = it.toString()
                            onItemClickListener.refreshBetInfoTotal()
                        }

                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                        }
                    }

                    removeTextChangedListener(tw)
                    addTextChangedListener(tw)
                    tag = tw
                }
                et_bet.isSelected =
                    mSelectedPosition == bindingAdapterPosition && mBetView == BetViewType.PARLAY

                if (et_bet.isFocusable) layoutKeyBoard.setMaxBetMoney(inputMaxMoney)

                et_bet.setOnTouchListener { view, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        et_bet.isFocusable = true
                        layoutKeyBoard.showKeyboard(
                            et_bet,
                            position,
                            inputMaxMoney,
                            data.min.toLong()
                        )
                        onSelectedPositionListener.onSelectChange(
                            bindingAdapterPosition,
                            BetViewType.PARLAY
                        )

                    }
                    false
                }

                et_bet.setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus) layoutKeyBoard?.hideKeyboard()
                }

                ll_control_connect.setOnClickListener {
                    onItemClickListener.onHideKeyBoard()
                    clearFocus()
                }

//                et_clickable.setOnClickListener {
//                    et_bet.isFocusable = true
//                    //onItemClickListener.onShowParlayKeyboard(et_bet, data, position, data.max.toLong())
//                    layoutKeyBoard.showKeyboard(
//                        et_bet,
//                        position,
//                        data.max.toLong(),
//                        data.min.toLong()
//                    )
//
//                    onSelectedPositionListener.onSelectChange(
//                        bindingAdapterPosition,
//                        BetViewType.PARLAY
//                    )
//                }
            }
        }

        private fun setupMinimumLimitMessage(itemData: ParlayOdd) {
            itemView.apply {
                tv_error_message.text = String.format(
                    context.getString(R.string.bet_info_list_minimum_limit_amount),
                    itemData.min,
                    sConfigData?.systemCurrency
                )
                et_bet.apply {
//                    hint = getLimitHint(
//                        context,
//                        itemData.min,
//                        itemData.max
//                    )
                }
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
            onItemClickListener: OnItemClickListener,
            mSelectedPosition: Int,
            mBetView: BetViewType,
            onSelectedPositionListener: OnSelectedPositionListener,
            position: Int
        ) {
            itemView.apply {
                tv_bet_maximum_limit.text = TextUtil.formatBetQuota(itemData.max)
                tv_check_maximum_limit.setOnClickListener {
                    it.visibility = View.GONE
                    ll_bet_quota_detail.visibility = View.GONE
                }

                et_bet.isSelected =
                    mSelectedPosition == bindingAdapterPosition && mBetView == BetViewType.PARLAY

                ll_bet_quota_detail.setOnClickListener {
                    et_bet.apply {
                        setText(itemData.max.toString())
                        isFocusable = true
                        setSelection(text.length)
                    }
                    layoutKeyBoard.showKeyboard(
                        et_bet,
                        position,
                        inputMaxMoney,
                        itemData.min.toLong()
                    )
                    //onItemClickListener.onShowParlayKeyboard(et_bet, itemData, position, itemData.max.toLong())
                    onSelectedPositionListener.onSelectChange(
                        bindingAdapterPosition,
                        BetViewType.PARLAY
                    )
                }
            }
        }

        private fun setupParlayRuleButton(
            data: ParlayOdd,
            onItemClickListener: OnItemClickListener
        ) {
            itemView.btn_rule.setOnClickListener {
                onItemClickListener.showParlayRule(
                    data.parlayType,
                    getParlayRuleStringRes(data.parlayType)?.let { ruleRes ->
                        itemView.context.getString(
                            ruleRes
                        )
                    }
                        ?: "")
            }
        }

        private fun getLimitHint(context: Context, min: Int, max: Int): String {
            return String.format(
                "${context.getString(R.string.edt_hint_deposit_money_new)}",
                TextUtil.formatBetQuota(min),
                TextUtil.formatBetQuota(max)
            )
        }
    }

    // 警訊
    class CantParlayWarnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface OnItemClickListener {
        fun onDeleteClick(oddsId: String, currentItemCount: Int)
        fun onRechargeClick()
        fun onShowKeyboard(editText: EditText, matchOdd: MatchOdd, position: Int, max: Long)
        fun onShowParlayKeyboard(
            editText: EditText,
            parlayOdd: ParlayOdd?,
            position: Int,
            max: Long
        )

        fun onHideKeyBoard()
        fun saveOddsHasChanged(matchOdd: MatchOdd)
        fun refreshBetInfoTotal()
        fun showParlayRule(parlayType: String, parlayRule: String)
    }

    interface OnSelectedPositionListener {
        fun onSelectChange(position: Int, single: BetViewType)
    }
}