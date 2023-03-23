package org.cxct.sportlottery.ui.game.betList.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_bet_list_batch_control_v3.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.BetStatus
import org.cxct.sportlottery.common.OddsType
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.betList.holder.BetInfoChangeViewHolder
import org.cxct.sportlottery.ui.game.betList.listener.OnItemClickListener
import org.cxct.sportlottery.util.KeyboardView
import org.cxct.sportlottery.util.MoneyInputFilter
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds
import kotlin.math.abs

class BetSingleListAdapter(
    val onItemClickListener: OnItemClickListener,
    val keyboardView: KeyboardView
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class BetViewType { SINGLE, PARLAY, NULL }
    private val attachedViewSet = HashSet<RecyclerView.ViewHolder>()

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

    var userLogin: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return BatchSingleInMoreOptionViewHolder(
                layoutInflater.inflate(
                    R.layout.item_bet_list_batch_control_v3,
                    parent,
                    false
                )
            )
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
            is BatchSingleInMoreOptionViewHolder ->{
                holder.bind(
                    parlayList?.getOrNull(0),
                    betList ?: mutableListOf(),
                    currentOddsType,
                    onItemClickListener,
                    { notifyDataSetChanged() },
                    mSelectedPosition,
                    mBetView,
                    position,
                    hasBetClosedForSingle,
                    userMoney,
                    userLogin,
                    keyboardView
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        attachedViewSet.add(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        //隱藏畫面外的鍵盤
        when (holder) {
            is BatchSingleInMoreOptionViewHolder -> {
//                holder.itemView.layoutKeyBoard.hideKeyboard()
            }
        }
        super.onViewDetachedFromWindow(holder)
        attachedViewSet.remove(holder)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        attachedViewSet.clear()
    }

    //使用HasStabledIds需複寫回傳的position, 若仍使用super.getItemId(position), 數據刷新會錯亂.
    //https://blog.csdn.net/karsonNet/article/details/80598435
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun closeAllKeyboard() {
        attachedViewSet.forEach {
//            it.itemView.findViewById<KeyboardView>(R.id.layoutKeyBoard)?.hideKeyboard()
        }
    }

    //填充單注
    class BatchSingleInMoreOptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var mUserMoney: Double = 0.0
        private var mUserLogin: Boolean = false
        private var mHasBetClosedForSingle: Boolean = false
        private var maxBet: Double = 0.0
        private var minBet: Double = 0.0

        fun bind(
            itemData: ParlayOdd?,
            betList: MutableList<BetInfoListData>,
            currentOddsType: OddsType,
            onItemClickListener: OnItemClickListener,
            notifyAllBet: () -> Unit,
            mSelectedPosition: Int,
            mBetView: BetViewType,
            position: Int,
            hasBetClosedForSingle: Boolean,
            userMoney: Double,
            userLogin: Boolean,
            keyboardView: KeyboardView
        ) {
            mUserMoney = userMoney
            mUserLogin = userLogin
            mHasBetClosedForSingle = hasBetClosedForSingle
            maxBet = getMaxOrMinAmount(true, betList)
            minBet = getMaxOrMinAmount(false, betList)

            itemView.apply {
                itemData?.let {
                    setupSingleItem(
                        betList,
                        itemData,
                        currentOddsType,
                        onItemClickListener,
                        notifyAllBet,
                        mSelectedPosition,
                        mBetView,
                        position,
                        keyboardView
                    )
                }
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun setupSingleItem(
            betList: MutableList<BetInfoListData>,
            itemData: ParlayOdd,
            currentOddsType: OddsType,
            onItemClickListener: OnItemClickListener,
            notifyAllBet: () -> Unit,
            mSelectedPosition: Int,
            mBetView: BetViewType,
            position: Int,
            keyboardView: KeyboardView
        ) {
            itemView.apply {

                et_bet_single.apply {
                    if (tag is TextWatcher) {
                        removeTextChangedListener(tag as TextWatcher)
                    }
                    onFocusChangeListener = null
                    filters = arrayOf(MoneyInputFilter())
                }

//                tv_single_count.text = betList.size.toString()
                val singleType = "${context.getString(R.string.bet_list_single)}*${betList.size}"
                tv_single_type.text = singleType

                val initValue = if (itemData.singleInput != null) itemData.allSingleInput else ""

                if (itemData.isInputBet) {
//                    layoutKeyBoard.setupMaxBetMoney(getMaxOrMinAmount(isGetMax = true, betList))
                }

                et_bet_single.apply {

                    if (!initValue.isNullOrEmpty()) setText(initValue) else text.clear()
                    et_bet_single.setSelection(et_bet_single.text.length)
                    setEtBetSingleBackground(itemData)

                    /* set listener */
                    val tw: TextWatcher?
                    tw = object : TextWatcher {
                        override fun afterTextChanged(it: Editable?) {
//                            val inputValue =
//                                if (it.isNullOrEmpty()) 0.0 else it.toString().toDouble()
//                            itemData?.allSingleInput =
//                                if (it.isNullOrEmpty()) null else it.toString()

                            val inputValue: Double
                            if (it.isNullOrEmpty()) {
                                inputValue = 0.0
                                itemData.allSingleInput = null
                                itemData.singleInput = null
                                //更新可贏額
//                                if (itemData.isInputBet) itemView.et_win_single.text.clear()
                            } else {
                                inputValue = it.toString().toDouble()
                                itemData.allSingleInput = it.toString()
                                itemData.singleInput = it.toString()

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

                                //更新可贏額
//                                if (allWinnableAmount > 0) {
//                                    itemView.et_win_single.setText(TextUtil.formatInputMoney(allWinnableAmount))
//                                }
                            }
                            setEtBetSingleBackground(itemData)

                            betList.forEachIndexed { _, data ->
                                if (data.matchOdd.status != BetStatus.ACTIVATED.code)
                                    return@forEachIndexed

                                checkBetLimitSingle(data, inputValue)

                                if (data.parlayOdds?.max == null || inputValue < (data.parlayOdds?.max
                                        ?: 0)
                                ) {
                                    data.betAmount = inputValue
                                    data.inputBetAmountStr = it.toString()
                                    data.input = it.toString()
                                } else {
                                    data.betAmount = (data.parlayOdds?.max ?: 0).toDouble()
                                    data.inputBetAmountStr = (data.parlayOdds?.max ?: 0).toString()
                                    data.input = (data.parlayOdds?.max ?: 0).toString()
                                }

                                if (itemData.allSingleInput == null) data.input = null

                                var dataOddsType = currentOddsType
                                data.apply {
                                    if (matchOdd.isOnlyEUType
                                        || matchOdd.odds == matchOdd.malayOdds
                                        || matchType == MatchType.OUTRIGHT
                                        || matchType == MatchType.OTHER_OUTRIGHT
                                    ) {
                                        dataOddsType = OddsType.EU
                                    }
                                }

                                //region 設置realAmount
                                var realAmount = data.betAmount
                                when (dataOddsType) {
                                    OddsType.MYS -> {
                                        if (getOdds(data.matchOdd, dataOddsType) < 0) {
                                            realAmount = data.betAmount * abs(
                                                getOdds(
                                                    data.matchOdd,
                                                    dataOddsType
                                                )
                                            )
                                        }
                                    }
                                    OddsType.IDN -> {
                                        if (getOdds(data.matchOdd, dataOddsType) < 0) {
                                            realAmount = data.betAmount * abs(
                                                getOdds(
                                                    data.matchOdd,
                                                    dataOddsType
                                                )
                                            )
                                        }
                                    }
                                    else -> {
                                        //do nothing
                                    }
                                }

                                data.realAmount = realAmount
                                //endregion
                            }
                            notifyAllBet()
                            onItemClickListener.refreshBetInfoTotal(isSingleAdapter = true)
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

                et_bet_single.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        et_bet_single.isFocusable = true
                        onItemClickListener.onHideKeyBoard()
                        keyboardView.setupMaxBetMoney(getMaxOrMinAmount(isGetMax = true, betList))
                        keyboardView.showKeyboard(
                            et_bet_single,
                            position
                        )
                    }
                    false
                }

                et_bet_single.setOnFocusChangeListener { _, hasFocus ->
//                    if (!hasFocus) layoutKeyBoard?.hideKeyboard()
                    itemData.isInputBet = hasFocus
                    if (hasFocus) et_bet_single.setSelection(et_bet_single.text.length)
                    setEtBetSingleBackground(itemData)
                }

                cl_item_background_single.setOnClickListener {
                    onItemClickListener.onHideKeyBoard()
                    clearFocus()
                }
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
                when (val currentOddsTypeChecked =
                    if (it.matchOdd.isOnlyEUType) OddsType.EU else currentOddsType) {
                    OddsType.MYS -> {
                        win = if (getOdds(it.matchOdd, currentOddsTypeChecked) < 0) {
                            betAmount
                        } else {
                            betAmount * getOdds(it.matchOdd, currentOddsTypeChecked)
                        }

                    }
                    OddsType.IDN -> {
                        win = if (getOdds(it.matchOdd, currentOddsTypeChecked) < 0) {
                            betAmount
                        } else {
                            betAmount * getOdds(it.matchOdd, currentOddsTypeChecked)
                        }
                    }
                    OddsType.EU -> {
                        win = betAmount * (getOdds(it.matchOdd, currentOddsTypeChecked) - 1)
                    }
                    else -> {
                        win = betAmount * getOdds(it.matchOdd, currentOddsTypeChecked)
                    }
                }
                allWinnableAmount += win
            }
            return allWinnableAmount
        }

        private fun getMaxOrMinAmount(
            isGetMax: Boolean,
            betList: MutableList<BetInfoListData>
        ): Double {
            val defaultMax = 9999999L
            var min = betList.firstOrNull()?.parlayOdds?.min ?: 0
            var max = betList.firstOrNull()?.parlayOdds?.max ?: defaultMax
            betList.forEach {
                if ((it.parlayOdds?.min ?: 0) > min) {
                    min = it.parlayOdds?.min ?: 0
                }
                if ((it.parlayOdds?.max ?: 0) < max) {
                    max = it.parlayOdds?.max ?: defaultMax
                }
            }
            return when (isGetMax) {
                true -> if (mUserLogin) max.toDouble() else defaultMax.toDouble() //未登入最大值9位數
                else -> min.toDouble()
            }
        }

        private fun checkBetLimitSingle(
            itemData: BetInfoListData,
            betAmount: Double
        ) {
            itemView.apply {
                var amountError = false
                //未登录的情况下，最大限额为7个9
                val maxBet = if (mUserLogin) (itemData.parlayOdds?.max
                    ?: 0).toDouble() else 9999999.toDouble()
                val minBet = (itemData.parlayOdds?.min ?: 0).toDouble()
                amountError = if (betAmount > maxBet) {
                    //最大限額
                    true
                } else {
                    //最小限額
                    betAmount != 0.0 && betAmount < minBet
                }
                val balanceError = betAmount != 0.0 && betAmount > mUserMoney
                itemData.amountError = if (balanceError) true else amountError
            }
        }

        private fun setEtBetSingleBackground(itemData: ParlayOdd) {
            itemView.apply {
                if (mHasBetClosedForSingle) {
                    et_bet_single.setBackgroundResource(R.drawable.bg_radius_2_edittext_unfocus)
                    et_bet_single.isEnabled = false
                } else {
                    et_bet_single.isEnabled = true
                    if (itemData.isInputBet) {
                        et_bet_single.setBackgroundResource(R.drawable.bg_radius_2_edittext_focus)
                    } else {
                        et_bet_single.setBackgroundResource(R.drawable.bg_radius_2_edittext_unfocus)
                    }
                }

                if (LoginRepository.isLogin.value == true) {
                    //更新bet single editText hint
                    val hint = context.getString(
                        R.string.hint_bet_limit_range,
                        minBet.toLong().toString(),
                        maxBet.toLong().toString()
                    )
                    //限額用整數提示
                    tv_hint_single_default.text = hint
                    val etBetHasInput = !et_bet_single.text.isNullOrEmpty()
//                if (etBetHasInput) {
                    tv_hint_single_default.isVisible = !etBetHasInput //僅輸入金額以後隱藏
//                } else {
//                    tv_hint_single_default.isVisible = !itemData.isInputBet
//                }
                } else {
                    tv_hint_single_default.isVisible = false
                }
            }
        }
    }
}
