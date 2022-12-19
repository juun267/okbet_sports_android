package org.cxct.sportlottery.ui.game.betList

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_bet_info_item_v3.view.*
import kotlinx.android.synthetic.main.content_bet_info_item_v3.view.layoutKeyBoard
import kotlinx.android.synthetic.main.include_bet_odds_tips_single.view.btnOddsChangeDes
import kotlinx.android.synthetic.main.include_bet_odds_tips_single.view.tvAcceptOddsChange
import kotlinx.android.synthetic.main.include_bet_odds_tips_single.view.tvMaxBetMoney
import kotlinx.android.synthetic.main.include_bet_odds_tips_single.view.tvOddsChangedTips
import kotlinx.android.synthetic.main.item_bet_list_batch_control_connect_v3.view.*
import kotlinx.android.synthetic.main.item_bet_list_batch_control_v3.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.extentions.gone
import org.cxct.sportlottery.extentions.visible
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.game.betList.BetListRefactorAdapter.BetRvType.*
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.transactionStatus.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.BetPlayCateFunction.getNameMap
import org.cxct.sportlottery.widget.OkPopupWindow
import timber.log.Timber
import kotlin.math.abs

class BetListRefactorAdapter(private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private enum class ViewType { Bet, Parlay, Warn, Single, OddsWarn }
    enum class BetViewType { SINGLE, PARLAY, NULL }

    private val attachedViewSet = HashSet<RecyclerView.ViewHolder>()

    /**
     * @property SINGLE 單項投注
     * @property PARLAY_SINGLE 串關投注的單項投注項
     * @property PARLAY 串關投注的串關投注項
     */
    enum class BetRvType { SINGLE, PARLAY_SINGLE, PARLAY }

    var adapterBetType: BetRvType = SINGLE
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    val betListLiveData  = MutableLiveData<MutableList<BetInfoListData>?>()

    var betList: MutableList<BetInfoListData>? = mutableListOf()
        set(value) {
            field = value
            //判斷是否有注單封盤
            hasBetClosed =
                value?.find { it.matchOdd.status != BetStatus.ACTIVATED.code || it.pointMarked } != null

            hasBetClosedForSingle =
                value?.find { it.matchOdd.status != BetStatus.ACTIVATED.code } != null
            if(adapterBetType == PARLAY){
                Timber.d("betList的长度:${field?.size}")
                betListLiveData.value = value
            }
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

    var moreOptionCollapse = false

    var needScrollToBottom = false //用來紀錄是否為點擊更多選項需滾動至底部

    var isCantParlayWarn = false

    private var isOddsChangedWarn = false //顯示賠率變更提示

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ViewType.Bet.ordinal -> BetInfoItemViewHolder(
                layoutInflater.inflate(
                    R.layout.content_bet_info_item_v3, parent, false
                )
            )

            ViewType.Warn.ordinal -> CantParlayWarnViewHolder(
                layoutInflater.inflate(
                    R.layout.content_cant_parlay_warn, parent, false
                )
            )

            ViewType.Single.ordinal -> BatchSingleInMoreOptionViewHolder(
                layoutInflater.inflate(
                    R.layout.item_bet_list_batch_control_v3, parent, false
                )
            )

            ViewType.OddsWarn.ordinal -> OddsChangedWarnViewHolder(
                layoutInflater.inflate(
                    R.layout.content_odds_changed_warn, parent, false
                )
            )

            else -> BatchParlayConnectViewHolder(
                layoutInflater.inflate(
                    R.layout.item_bet_list_batch_control_connect_v3, parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var currentOddsType = oddsType
        betList?.getOrNull(position)?.apply {
            if (matchOdd.isOnlyEUType || matchOdd.odds == matchOdd.malayOdds || matchType == MatchType.OUTRIGHT || matchType == MatchType.OTHER_OUTRIGHT) {
                currentOddsType = OddsType.EU
            }
        }

        when (holder) {
            is BetInfoItemViewHolder -> {
                betList?.getOrNull(position)?.let { betInfoListData ->
                    holder.bind(
                        betInfoListData,
                        currentOddsType,
                        itemCount,
                        onItemClickListener,
                        betList?.size ?: 0,
                        mSelectedPosition,
                        onSelectedPositionListener,
                        position,
                        userMoney,
                        userLogin,
                        adapterBetType
                    )
                }
            }

            is BatchParlayConnectViewHolder -> {
                holder.bind(
                    ////region 20220607 投注單版面調整
                    parlayList?.getOrNull(
                        when (isOddsChangedWarn) {
                            true -> position - 1
                            false -> position
                        }
                    ),
//                    parlayList?.getOrNull(position - 1 - (betList?.size ?: 0)),
                    //endregion
                    currentOddsType,
                    hasBetClosed,
                    onItemClickListener,
                    mSelectedPosition,
                    mBetView,
                    onSelectedPositionListener,
                    position,
                    userMoney,
                    userLogin
                )
            }

            is CantParlayWarnViewHolder -> {
            }

            is BatchSingleInMoreOptionViewHolder -> {
                holder.bind(
                    parlayList?.getOrNull(0),
                    betList ?: mutableListOf(),
                    currentOddsType,
                    onItemClickListener,
                    { notifyDataSetChanged() },
                    mSelectedPosition,
                    mBetView,
                    onSelectedPositionListener,
                    position,
                    hasBetClosedForSingle,
                    userMoney,
                    userLogin
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        //region 20220607 投注單版面調整
        return when (adapterBetType) {
//            BetRvType.SINGLE -> {
//                when {
//                    betList?.size ?: 0 > 1 && position == itemCount - 1 -> {
//                        ViewType.Single.ordinal
//                    }
//                    else -> {
//                        ViewType.Bet.ordinal
//                    }
//                }
//            }
            BetRvType.PARLAY_SINGLE -> {
                when {
                    isCantParlayWarn && position == itemCount - 1 -> {
                        ViewType.Warn.ordinal
                    }

                    else -> {
                        ViewType.Bet.ordinal
                    }
                }
            }

            BetRvType.PARLAY -> {
                when {
                    isOddsChangedWarn && position == 0 -> {
                        ViewType.OddsWarn.ordinal
                    }

                    else -> {
                        ViewType.Parlay.ordinal
                    }
                }
            }

            else -> {
                ViewType.Bet.ordinal
            }
        }/*
        val betSize = betList?.size ?: 0
        val parlaySize = parlayList?.size ?: 0
        return when {
            isCantParlayWarn && position == (itemCount - 1) -> {
                ViewType.Warn.ordinal
            }
            position < betSize -> ViewType.Bet.ordinal
            position == betSize -> ViewType.ParlayFirst.ordinal
            position == betSize + 1 && parlaySize > 0 && moreOptionCollapse -> ViewType.Single.ordinal
            else -> ViewType.Parlay.ordinal
        }
        */
        //endregion
    }

    override fun getItemCount(): Int {
        //region 20220607 投注單版面調整
        return getListSize()/*
        var size = getListSize()
        if (isCantParlayWarn) {
            size++
        }
        return size
        */
        //endregion
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        attachedViewSet.add(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        //隱藏畫面外的鍵盤
        when (holder) {
            is BetInfoItemViewHolder -> {
                holder.itemView.layoutKeyBoard.hideKeyboard()
            }

            is BatchSingleInMoreOptionViewHolder -> {
                holder.itemView.layoutKeyBoard.hideKeyboard()
            }

            is BatchParlayConnectViewHolder -> {
                holder.itemView.layoutKeyBoard.hideKeyboard()
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

    fun showCantParlayWarn() {
        isCantParlayWarn = true
        notifyDataSetChanged()
    }

    fun hideCantParlayWarn() {
        isCantParlayWarn = false
        notifyDataSetChanged()
    }

    fun showOddsChangedWarn() {
        isOddsChangedWarn = true
        notifyDataSetChanged()
    }

    fun hideOddsChangedWarn() {
        isOddsChangedWarn = false
        notifyDataSetChanged()
    }

    fun closeAllKeyboard() {
        attachedViewSet.forEach {
            it.itemView.findViewById<KeyboardView>(R.id.layoutKeyBoard)?.hideKeyboard()
        }
    }

    fun getListSize(): Int {
        //region 20220607 投注單版面調整
        val betListSize = betList?.size ?: 0
        return when (adapterBetType) {
            SINGLE -> {

//                if (betListSize > 1) {
//                    betListSize + 1
//                } else {
                betListSize
//                }
            }

            BetRvType.PARLAY_SINGLE -> {
                if (isCantParlayWarn) {
                    //多加一項不能串關提示訊息
                    betListSize + 1
                } else {
                    betListSize
                }
            }

            BetRvType.PARLAY -> {
                when {
                    betListSize < 2 -> {
                        0
                    }

                    else -> {
                        val parlayListSize = parlayList?.size ?: 0
                        if (isOddsChangedWarn) {
                            parlayListSize + 1
                        } else {
                            parlayListSize
                        }
                    }
                }
            }

            else -> {
                0
            }
        }/*
        val betListSize = betList?.size ?: 0
        val parlayListSize = when {
            betListSize < 2 -> 0
            betListSize == 2 || !moreOptionCollapse -> 1
            else -> (parlayList?.size ?: 0)
        }
        return if (betList?.size ?: 0 > 1 && moreOptionCollapse)
            betListSize + parlayListSize + 1
        else
            betListSize + parlayListSize
        */
        //endregion
    }

    //單注
    class BetInfoItemViewHolder(itemView: View) : BetInfoChangeViewHolder(itemView) {
        private var inputMaxMoney: Double = 0.0
        private var inputMinMoney: Double = 0.0
        private var inputWinMaxMoney: Double = 0.0
        private var inputWinMinMoney: Double = 0.0
        private var mUserMoney: Double = 0.0
        private var mUserLogin: Boolean = false
        fun bind(
            itemData: BetInfoListData,
            currentOddsType: OddsType,
            itemCount: Int,
            onItemClickListener: OnItemClickListener,
            betListSize: Int,
            mSelectedPosition: Int,
            onSelectedPositionListener: OnSelectedPositionListener,
            position: Int,
            userMoney: Double,
            userLogin: Boolean,
            adapterBetType: BetRvType?
        ) {
            mUserMoney = userMoney
            mUserLogin = userLogin
            //設置投注限額
            setupInputLimit(itemData)
            //設置可贏限額
            inputWinMaxMoney = inputMaxMoney * getOddsAndSaveRealAmount(itemData, currentOddsType)
            inputWinMinMoney = inputMinMoney * getOddsAndSaveRealAmount(itemData, currentOddsType)
//            Timber.e("inputMaxMoney: $inputMaxMoney")
//            Timber.e("inputWinMaxMoney: $inputWinMaxMoney")

            itemView.apply {
                //region 20220714 投注單版面調整
                GameType.getGameType(itemData.matchOdd.gameType)?.let {
                    ivSportLogo.setImageResource(GameType.getBetListGameTypeIcon(it))
                }

                //不支援串關
                //僅有串關的單注才會出現此提示
                val cannotParlay =
                    adapterBetType == BetRvType.PARLAY_SINGLE && itemData.pointMarked && betListSize > 1

                if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) {
                    when (adapterBetType) {
                        SINGLE -> {
//                        cl_to_win.isVisible = true
                            setupContainerUI(isVisible = true, isLock = false, cannotParlay = false)
                        }

                        else -> {
//                        cl_to_win.isVisible = false
                            setupContainerUI(isVisible = false, isLock = false, cannotParlay)
                        }
                    }
                } else {
                    setupContainerUI(isVisible = false, isLock = true, cannotParlay)
                }
                setupBetAmountInput(
                    itemData,
                    if (itemData.matchOdd.isOnlyEUType) OddsType.EU else currentOddsType,
                    onItemClickListener,
                    betListSize,
                    mSelectedPosition,
                    onSelectedPositionListener,
                    position,
                    adapterBetType
                )
                //endregion

                setupDeleteButton(itemData, itemCount, onItemClickListener)
                setupOddsChangeButton(onItemClickListener)
                top_space.visibility = if (position == 0) View.VISIBLE else View.GONE
//                bottom_view.visibility = if(position == betListSize -1) View.GONE else View.VISIBLE
            }
        }

        private fun setupOddsChangeButton(onItemClickListener: OnItemClickListener) {
            if (itemView.tvAcceptOddsChange.isVisible) {
                val currentIndex = KvUtils.decodeInt(KV_STR_SELECT_ODDS_MODE)
                val currentSelectText = OddsModeUtil.currentSelectModeText(currentIndex)
                itemView.tvAcceptOddsChange.text = currentSelectText
                onItemClickListener.onOddsChangesSetOptionListener(currentSelectText)
                itemView.tvAcceptOddsChange.setOnClickListener {
                    onItemClickListener.onOddsChangeAcceptSelect(itemView.tvAcceptOddsChange)
                }

                itemView.btnOddsChangeDes.setOnClickListener {
                    onItemClickListener.onOddsChangesAcceptTips()
                }

                itemView.tvMaxBetMoney.text = String.format(itemView.context.getString(R.string.str_max_bet_money),TextUtil.formatInputMoney(inputMaxMoney))

            }
        }

        private fun setupContainerUI(isVisible: Boolean, isLock: Boolean, cannotParlay: Boolean) {
            itemView.apply {
                //editText container
                cl_editText_container.isVisible = isVisible
                includeOddsLayout.isVisible = isVisible
                //提示文字Container
                ll_single_tips.isVisible = isVisible
                //不支援串關的提示
                tv_no_parlay.isVisible = cannotParlay
                //盤口關閉的提示
                if (isLock) layoutKeyBoard.hideKeyboard()
                tv_bet_lock.isVisible = isLock
                tv_bet_lock.text =
                    if (cannotParlay) context.getString(R.string.bet_info_no_parlay_hint)
                    else context.getString(R.string.bet_info_bet_lock_hint)
            }
        }

        private fun setupInputLimit(itemData: BetInfoListData) {
            val maxBet = itemData.parlayOdds?.max ?: 0
            //未登录的情况下，最大限额为7个9
            inputMaxMoney = if (mUserLogin) maxBet.toDouble() else 9999999.toDouble()
            val minBet = itemData.parlayOdds?.min ?: 0
            inputMinMoney = minBet.toDouble()
        }

        var isSingleBetFirstOpenKeyboard = true

        @SuppressLint("ClickableViewAccessibility")
        private fun setupBetAmountInput(
            itemData: BetInfoListData,
            currentOddsType: OddsType,
            onItemClickListener: OnItemClickListener,
            betListSize: Int,
            mSelectedPosition: Int,
            onSelectedPositionListener: OnSelectedPositionListener,
            position: Int,
            adapterBetType: BetRvType?
        ) {
            itemView.apply {
                //移除TextChangedListener
                et_bet.apply {
                    if (tag is TextWatcher) {
                        removeTextChangedListener(tag as TextWatcher)
                    }
                    onFocusChangeListener = null
                    filters = arrayOf(MoneyInputFilter())
                }
                et_win.apply {
                    if (tag is TextWatcher) {
                        removeTextChangedListener(tag as TextWatcher)
                    }
                    onFocusChangeListener = null
                    filters = arrayOf(MoneyInputFilter())
                }
                //設定editText內容
                et_bet.apply {
                    if (itemData.input != null) setText(itemData.inputBetAmountStr) else text.clear()
                    setSelection(text.length)
                }
                et_win.apply {
                    if (itemData.input != null) {
                        val win =
                            itemData.betAmount * getOddsAndSaveRealAmount(itemData, currentOddsType)
                        setText(TextUtil.formatInputMoney(win))
                    } else text.clear()
                    setSelection(text.length)
                }
                checkBetLimit(itemData)

                setupOddInfo(
                    itemData, currentOddsType, betListSize, onItemClickListener, adapterBetType
                )

                if (itemData.isInputWin) {
                    layoutKeyBoard.setupMaxBetMoney(inputWinMaxMoney)
                } else {
                    layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
                }

                val tw: TextWatcher?
                tw = object : TextWatcher {
                    override fun afterTextChanged(it: Editable?) {
                        if (it.isNullOrEmpty()) {
                            itemData.betAmount = 0.000
                            itemData.inputBetAmountStr = ""
                            itemData.input = null

                            itemData.realAmount = 0.0
                            //更新可贏額
                            if (itemData.isInputBet) et_win.text.clear()
                        } else {

                            val quota = it.toString().toDouble()
                            itemData.betAmount = quota
                            itemData.inputBetAmountStr = it.toString()
                            itemData.input = it.toString()
                            if (itemData.isInputBet) {
                                inputMaxMoney.let { max ->
                                    if (quota > max) {
                                        et_bet.apply {
                                            setText(TextUtil.formatInputMoney(max))
                                            setSelection(text.length)
                                        }
                                        return@afterTextChanged
                                    }
                                }
                            }

                            val win = itemData.betAmount * getOddsAndSaveRealAmount(
                                itemData, currentOddsType
                            )
//                            Timber.d("win: $win")
                            //更新可贏額
                            if (itemData.isInputBet) et_win.setText(TextUtil.formatInputMoney(win))
                        }
                        checkBetLimit(itemData)
                        onItemClickListener.refreshBetInfoTotal()
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?, start: Int, before: Int, count: Int
                    ) {
                    }
                }

                val tw2: TextWatcher?
                tw2 = object : TextWatcher {
                    override fun afterTextChanged(it: Editable?) {
                        if (it.isNullOrEmpty()) {
                            itemData.betWin = 0.000
                            itemData.inputBetWinStr = ""
                            itemData.inputWin = ""

                            itemData.realAmount = 0.0
                            //更新下注額
                            if (itemData.isInputWin) et_bet.text.clear()
                        } else {

                            val quota = it.toString().toDouble()
                            itemData.betWin = quota
                            itemData.inputBetWinStr = it.toString()
                            itemData.inputWin = TextUtil.formatInputMoney(quota)
                            if (itemData.isInputWin) {
                                inputWinMaxMoney.let { max ->
                                    if (quota > max) {
                                        et_win.apply {
                                            setText(TextUtil.formatInputMoney(max))
                                            setSelection(text.length)
                                        }
                                        return@afterTextChanged
                                    }
                                }
                            }

                            val bet = itemData.betWin / getOddsAndSaveRealAmount(
                                itemData, currentOddsType
                            )
//                            Timber.d("bet: $bet")
                            //更新下注額
                            if (itemData.isInputWin) et_bet.setText(TextUtil.formatInputMoney(bet))
                        }
                        setEtBackground(itemData)
                        onItemClickListener.refreshBetInfoTotal()
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?, start: Int, before: Int, count: Int
                    ) {
                    }
                }

                //et_bet.keyListener = null

                et_bet.addTextChangedListener(tw)
                et_bet.tag = tw
                et_bet.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) {
                            et_bet.isFocusable = true
                            onItemClickListener.onHideKeyBoard()
                            layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
                            layoutKeyBoard.showKeyboard(
                                et_bet, position
                            )
                            onSelectedPositionListener.onSelectChange(
                                bindingAdapterPosition, BetViewType.SINGLE
                            )
                            onItemClickListener.onShowKeyboard(position)
                        }
                    }
                    false
                }
                //單筆注單展開時，預設開啟輸入本金的鍵盤
                if (betListSize == 1 && isSingleBetFirstOpenKeyboard) {
                    isSingleBetFirstOpenKeyboard = false
                    et_bet.requestFocus()
                    itemData.isInputBet = true
                    layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
                    if(adapterBetType ==SINGLE){
                        layoutKeyBoard.showKeyboard(
                            et_bet, position
                        )
                    }
                }
                et_bet.setOnFocusChangeListener { _, hasFocus ->
//                    if (!hasFocus) layoutKeyBoard?.hideKeyboard() //兩個輸入匡會互搶focus不能這樣關閉鍵盤
                    itemData.isInputBet = hasFocus
                    if (hasFocus) {
                        et_bet.setSelection(et_bet.text.length)
                    }
                    setEtBackground(itemData)
                }

                et_win.addTextChangedListener(tw2)
                et_win.tag = tw2
                et_win.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) {
                            et_win.isFocusable = true
                            onItemClickListener.onHideKeyBoard()
                            layoutKeyBoard.setupMaxBetMoney(inputWinMaxMoney)
                            layoutKeyBoard.showKeyboard(
                                et_win, position
                            )
                            onSelectedPositionListener.onSelectChange(
                                bindingAdapterPosition, BetViewType.SINGLE
                            )
                            onItemClickListener.onShowKeyboard(position)
                        }
                    }
                    false
                }
                et_win.setOnFocusChangeListener { _, hasFocus ->
//                    if (!hasFocus) layoutKeyBoard?.hideKeyboard() //兩個輸入匡會互搶focus不能這樣關閉鍵盤
                    itemData.isInputWin = hasFocus
                    if (hasFocus) {
                        et_win.setSelection(et_win.text.length)
                    }
                    setEtBackground(itemData)
                }

                cl_item_background.setOnClickListener {
                    onItemClickListener.onHideKeyBoard()
                    clearFocus()
                }
            }
        }

        var oddsId = ""
        var oldOdds = ""
        var handler = Handler(Looper.getMainLooper())
        private val totalAnimationDuration = 3000L //動畫總共呈現時間
        private val totalAnimationTipsDur = 5000L //
        private val animationDuration = 750L //單次動畫持續時間
        private val delayResetTime = totalAnimationDuration - animationDuration * 2

        var repeatCount = 0
        private fun setAnimation(ivArrow: ImageView, tvOdds: TextView, isUp: Boolean) {
            if (repeatCount == 0) {
                handler.removeCallbacksAndMessages(null)
                resetOddsUI()
            }
            val anim = if (isUp) R.anim.arrow_up else R.anim.arrow_down
            val animation = AnimationUtils.loadAnimation(ivArrow.context, anim)
            animation.duration = animationDuration
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    if (isUp) {
                        ivArrow.setImageResource(R.drawable.ic_arrow_odd_up)
                        tvOdds.setTextColor(
                            ContextCompat.getColor(
                                tvOdds.context, R.color.color_34CB8A_1D9F51
                            )
                        )
                    } else {
                        ivArrow.setImageResource(R.drawable.ic_arrow_odd_down)
                        tvOdds.setTextColor(
                            ContextCompat.getColor(
                                tvOdds.context, R.color.color_D35555_D35555
                            )
                        )
                    }
                    ivArrow.isVisible = true
                    repeatCount += 1
                }

                override fun onAnimationEnd(animation: Animation?) {
                    if (repeatCount == 2) {
                        repeatCount = 0
                        handler.postDelayed({
                            resetOddsUI()
                        }, delayResetTime)
                    } else {
                        setAnimation(ivArrow, tvOdds, isUp)
                    }
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }
            })
            ivArrow.startAnimation(animation)
        }

        private fun resetOddsUI() {
            itemView.apply {
                ivOddsArrow.clearAnimation()
                ivOddsArrow.isVisible = false
                tvOdds.setTextColor(ContextCompat.getColor(context, R.color.color_025BE8))
            }
        }

        private fun setupOddInfo(
            itemData: BetInfoListData,
            currentOddsType: OddsType,
            betListSize: Int,
            onItemClickListener: OnItemClickListener,
            adapterBetType: BetRvType?
        ) {
            itemView.apply {
//                if (itemData.matchOdd.odds == itemData.matchOdd.malayOdds
//                    || itemData.matchType == MatchType.OUTRIGHT
//                    || itemData.matchType == MatchType.OTHER_OUTRIGHT
//                ) {
//                    currentOddsType = OddsType.EU
//                }

                //setupOddsContent(itemData, oddsType = currentOddsType, tv_odds_content)
                if (oddsId == itemData.matchOdd.oddsId && itemData.matchOdd.status == BetStatus.ACTIVATED.code && oldOdds != "" && oldOdds != TextUtil.formatForOdd(
                        getOdds(itemData.matchOdd, currentOddsType)
                    )
                ) {
                    //賠率變動更新箭頭和文字色碼
                    repeatCount = 0
                    if (itemData.matchOdd.oddState == OddState.LARGER.state) {
                        setAnimation(ivOddsArrow, tvOdds, true)
                    } else if (itemData.matchOdd.oddState == OddState.SMALLER.state) {
                        setAnimation(ivOddsArrow, tvOdds, false)
                    }
                    odds_change_layout.visibility = if (handler != null) {
                        Timber.d("Ray===> 显示赔率变化")
                        if (adapterBetType == SINGLE) {
                            includeOddsLayout.tvOddsChangedTips.visible()
                            handler?.postDelayed({
                                includeOddsLayout.tvOddsChangedTips.gone()
                            }, totalAnimationTipsDur)
                        } else {
                            onItemClickListener.onOddsChangesWarningTips(true)
                            handler?.postDelayed({
                                onItemClickListener.onOddsChangesWarningTips(false)
                            }, totalAnimationTipsDur)
                        }
                        View.GONE

                    } else {
                        Timber.d("Ray===> 隐藏赔率变化")
                        if (adapterBetType == SINGLE) {
                            includeOddsLayout.gone()
                            includeOddsLayout.tvOddsChangedTips.gone()
                        } else {
                            onItemClickListener.onOddsChangesWarningTips(false)
                        }
                        View.GONE

                    }

                    handler?.postDelayed({
                        Timber.d("odds_change_layout隐藏 postDelay")
                        odds_change_layout?.visibility = View.GONE
                    }, totalAnimationDuration)
//                    tv_odd_content_changed.text =
//                        if (itemData.matchOdd.playCode == PlayCate.LCS.value) context.getString(
//                            R.string.bet_info_odd_content_changed2,
//                            tvOdds.text,
//                            TextUtil.formatForOddPercentage(
//                                getOdds(
//                                    itemData.matchOdd,
//                                    currentOddsType
//                                ) - 1
//                            )
//                        ) else
//                            context.getString(
//                                R.string.bet_info_odd_content_changed2,
//                                oldOdds,
//                                TextUtil.formatForOdd(getOdds(itemData.matchOdd, currentOddsType))
//                            )
                }
                var spread = ""
                spread =
                    if (itemData.matchOdd.spread.isEmpty() || !PlayCate.needShowSpread(itemData.matchOdd.playCode) || itemData.matchType == MatchType.OUTRIGHT) {
                        ""
                    } else {
                        itemData.matchOdd.spread
                    }
                //依照設計(比例 207:375)，最大寬度為螢幕寬的55%
                tv_odds_content.maxWidth = ScreenUtil.getScreenWidth(context) * 55 / 100
                tv_odds_content.text = itemData.matchOdd.playName
                if (itemData.matchOdd.status == BetStatus.ACTIVATED.code && oldOdds != TextUtil.formatForOdd(
                        getOdds(itemData.matchOdd, currentOddsType)
                    )
                ) {
                    oddsId = itemData.matchOdd.oddsId
                    oldOdds = TextUtil.formatForOdd(getOdds(itemData.matchOdd, currentOddsType))
                }
                //反波膽顯示 %
                var tvOdd =
                    "@ " + TextUtil.formatForOdd(getOdds(itemData.matchOdd, currentOddsType))
                if (itemData.matchOdd.playCode == PlayCate.LCS.value) tvOdd =
                    "@ " + TextUtil.formatForOddPercentage(
                        getOdds(
                            itemData.matchOdd, currentOddsType
                        ) - 1
                    )

                tvOdds.text = if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) tvOdd else {
                    resetOddsUI()
                    tvOdd
                }

                //特別處理playCode為SCO時, 此處不顯示
                if (itemData.matchOdd.playCode != PlayCate.SCO.value) {
                    if (itemData.matchOdd.extInfo != null) {
                        tvContent.text = itemData.matchOdd.extInfo + spread
                    } else {
                        tvContent.text = spread
                    }
                }

                //設定playCateCode為OU時, container背景, 文字大小和顏色
                if (itemData.matchOdd.playCode == PlayCate.OU.value) {
                    odds_content_container.setBackgroundResource(R.drawable.bg_gradient_bet_ou)
                    tv_odds_content.setOUStyle(true)
                    tvContent.setOUStyle(true)
                } else {
                    odds_content_container.setBackgroundResource(R.color.transparent)
                    tv_odds_content.setOUStyle(false)
                    tvContent.setOUStyle(false)
                }

                //設定隊伍名稱, 聯賽名稱, 開賽時間
                when (itemData.matchType) {
                    MatchType.OUTRIGHT -> {
                        tv_match.text = itemData.outrightMatchInfo?.name
                        tv_league_name.isVisible = false
                        tv_start_time.isVisible = false
                    }

                    else -> {
                        val matchName =
                            "${itemData.matchOdd.homeName}${context.getString(R.string.verse_3)}${itemData.matchOdd.awayName}"
                        tv_match.text = matchName
                        tv_league_name.text = itemData.matchOdd.leagueName
                        tv_league_name.isVisible = true
                        itemData.matchOdd.startTime?.let {
                            tv_start_time.text = TimeUtil.stampToDateHMS(it)
                            tv_start_time.isVisible = false
                        }
                    }
                }

                //玩法名稱 目前詳細玩法裡面是沒有給betPlayCateNameMap，所以顯示邏輯沿用舊版
                val nameOneLine = { inputStr: String ->
                    inputStr.replace("\n", "-")
                }

                val inPlay = System.currentTimeMillis() > itemData.matchOdd.startTime ?: 0
                when {
                    itemData.betPlayCateNameMap.isNullOrEmpty() -> {
                        tv_name.text = when (inPlay && itemData.matchType != MatchType.OUTRIGHT) {
                            true -> {
//                                    when {
//                                        PlayCate.isIntervalCornerPlayCate(itemData.matchOdd.playCode) -> {
//                                            itemData.matchOdd.playCateName
//                                        }
//                                        PlayCate.needShowCurrentCorner(itemData.matchOdd.playCode) -> {
//                                            if (itemData.matchOdd.homeCornerKicks == null || itemData.matchOdd.awayCornerKicks == null) {
//                                                itemData.matchOdd.playCateName
//                                            } else {
//                                                context.getString(
//                                                    R.string.bet_info_in_play_score,
//                                                    itemData.matchOdd.playCateName,
//                                                    itemData.matchOdd.homeCornerKicks.toString(),
//                                                    itemData.matchOdd.awayCornerKicks.toString()
//                                                )
//                                            }
//                                        }
//                                        else -> {
                                context.getString(
                                    R.string.bet_info_in_play_score,
                                    itemData.matchOdd.playCateName,
                                    itemData.matchOdd.homeScore.toString(),
                                    itemData.matchOdd.awayScore.toString()
                                )
//                                        }
//                                    }
                            }

                            else -> itemData.matchOdd.playCateName
                        }
                    }

                    else -> {
                        val playCateName = itemData.betPlayCateNameMap?.getNameMap(
                            itemData.matchOdd.gameType, itemData.matchOdd.playCode
                        )?.get(LanguageManager.getSelectLanguage(context).key) ?: ""
                        tv_name.text = when (inPlay && itemData.matchType != MatchType.OUTRIGHT) {
                            true -> {
//                                    when {
//                                        PlayCate.isIntervalCornerPlayCate(itemData.matchOdd.playCode) -> {
//                                            nameOneLine(playCateName)
//                                        }
//                                        PlayCate.needShowCurrentCorner(itemData.matchOdd.playCode) -> {
//                                            if (itemData.matchOdd.homeCornerKicks == null || itemData.matchOdd.awayCornerKicks == null) {
//                                                nameOneLine(playCateName)
//                                            } else {
//                                                context.getString(
//                                                    R.string.bet_info_in_play_score,
//                                                    playCateName,
//                                                    itemData.matchOdd.homeCornerKicks.toString(),
//                                                    itemData.matchOdd.awayCornerKicks.toString()
//                                                )
//                                            }
//                                        }
//                                        else -> {
                                context.getString(
                                    R.string.bet_info_in_play_score,
                                    playCateName,
                                    itemData.matchOdd.homeScore.toString(),
                                    itemData.matchOdd.awayScore.toString()
                                )
//                                        }
//                                    }
                            }

                            else -> nameOneLine(playCateName)
                        }
                    }
                }
                //加上OddsType名稱
                val tvNamePlusOddsTypeName =
                    "${tv_name.text} [${context.getString(currentOddsType.res)}]"
                tv_name.text = tvNamePlusOddsTypeName

                //前面加上MatchType名稱
                itemData.matchType?.let {
                    val matchTypeName = context.getString(it.resId)
                    tv_name.text = matchTypeName.plus(" ${tv_name.text}")
                }
            }
        }

        private fun TextView.setOUStyle(isOUType: Boolean) {
            if (isOUType) {
                setTextColor(ContextCompat.getColor(context, R.color.color_141931_F9F9F9))
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
            } else {
                setTextColor(ContextCompat.getColor(context, R.color.color_FFFFFF_414655))
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            }
        }

        private fun getOddsAndSaveRealAmount(
            itemData: BetInfoListData, currentOddsType: OddsType
        ): Double {
            var odds = 0.0
            var realAmount = itemData.betAmount
            when (currentOddsType) {
                OddsType.MYS -> {
                    if (getOdds(itemData.matchOdd, currentOddsType) < 0) {
                        realAmount = itemData.betAmount * Math.abs(
                            getOdds(
                                itemData.matchOdd, currentOddsType
                            )
                        )
//                        win = itemData.betAmount
                        odds = 1.0
                    } else {
//                        win = itemData.betAmount * getOdds(
//                            itemData.matchOdd,
//                            currentOddsType
//                        )
                        odds = getOdds(
                            itemData.matchOdd, currentOddsType
                        )
                    }

                }

                OddsType.IDN -> {
                    if (getOdds(itemData.matchOdd, currentOddsType) < 0) {
                        realAmount = itemData.betAmount * Math.abs(
                            getOdds(
                                itemData.matchOdd, currentOddsType
                            )
                        )
//                        win = itemData.betAmount
                        odds = 1.0
                    } else {
//                        win = itemData.betAmount * getOdds(
//                            itemData.matchOdd,
//                            currentOddsType
//                        )
                        odds = getOdds(
                            itemData.matchOdd, currentOddsType
                        )
                    }
                }

                OddsType.EU -> {
//                    win = itemData.betAmount * (getOdds(
//                        itemData.matchOdd,
//                        currentOddsType
//                    ) - 1)
                    odds = (getOdds(
                        itemData.matchOdd, currentOddsType
                    ) - 1)
                }

                else -> {
//                    win = itemData.betAmount * getOdds(
//                        itemData.matchOdd,
//                        currentOddsType
//                    )
                    odds = getOdds(
                        itemData.matchOdd, currentOddsType
                    )
                }
            }

            itemData.realAmount = realAmount
            odds = ArithUtil.toOddFormat(odds).toDouble()
//            Timber.e("odds: $odds")
            return odds
        }

        private fun setEtBackground(itemData: BetInfoListData) {
            itemView.apply {
                if (itemData.amountError) {
                    et_bet.setBackgroundResource(R.drawable.bg_radius_2_edittext_error)
                } else {
                    if (itemData.isInputBet) {
                        et_bet.setBackgroundResource(R.drawable.bg_radius_2_edittext_focus)
                    } else {
                        et_bet.setBackgroundResource(R.drawable.bg_radius_2_edittext_unfocus)
                    }
                }
                et_win.setBackgroundResource(
                    if (itemData.isInputWin) R.drawable.bg_radius_2_edittext_focus
                    else R.drawable.bg_radius_2_edittext_unfocus
                )

                //更新bet editText hint
                val betHint = context.getString(
                    R.string.hint_bet_limit_range,
                    inputMinMoney.toLong().toString(),
                    inputMaxMoney.toLong().toString()
                )
                //限額用整數提示
                tv_hint_default.text = betHint
                if (LoginRepository.isLogin.value == true) {
                    val etBetHasInput = !et_bet.text.isNullOrEmpty()
//                if (etBetHasInput) {
                    tv_hint_default.isVisible = !etBetHasInput //僅輸入金額以後隱藏
//                } else {
//                    tv_hint_default.isVisible = !itemData.isInputBet
//                }

                    //更新win editText hint
                    val winHint = context.getString(
                        R.string.hint_bet_limit_range,
                        inputWinMinMoney.toLong().toString(),
                        inputWinMaxMoney.toLong().toString()
                    )
                    //限額用整數提示
                    tv_win_hint_default.text = winHint
                    et_win.isEnabled = true
                    val etWinHasInput = !et_win.text.isNullOrEmpty()
//                if (etWinHasInput) {
                    tv_win_hint_default.isVisible = false //僅輸入金額以後隱藏
//                } else {
//                    tv_win_hint_default.isVisible = !itemData.isInputWin
//                }
                } else {
                    tv_hint_default.isVisible = false
                    et_win.isEnabled = false
                    tv_win_hint_default.isVisible = false
                }
            }
        }

        private fun checkBetLimit(
            itemData: BetInfoListData
        ) {
            itemView.apply {
                val betAmount = itemData.betAmount
                var amountError: Boolean
                val balanceError: Boolean
                if (!itemData.input.isNullOrEmpty() && betAmount == 0.000) {
                    tvErrorMessage.isVisible = false
                    //請輸入正確投注額
                    tvPleaseEnterCorrectAmount.visibility =
                        if (!itemData.input.isNullOrEmpty() && betAmount == 0.000) {
                            amountError = true
                            View.VISIBLE
                        } else {
                            amountError = false
                            View.GONE
                        }
                } else {
                    tvPleaseEnterCorrectAmount.isVisible = false
                    if (betAmount > inputMaxMoney) {
                        //超過最大限額
                        amountError = true
                        tvErrorMessage.apply {
                            text = context.getString(R.string.bet_info_list_maximum_limit_amount)
                            isVisible = true
                        }
                    } else {
                        tvErrorMessage.apply {
                            isVisible = false
                            if (betAmount != 0.0 && betAmount < inputMinMoney) {
                                //低於最小限額
                                amountError = true
                                text =
                                    context.getString(R.string.bet_info_list_minimum_limit_amount)
                                isVisible = true
                            } else {
                                amountError = false
                                isVisible = false
                            }
                        }
                    }
                }
                tvBalanceInsufficientMessage.visibility =
                    if (betAmount != 0.0 && betAmount > mUserMoney) {
                        tvErrorMessage.isVisible = false //同時滿足限額和餘額不足提示條件，優先顯示餘額不足
                        balanceError = true
                        View.VISIBLE
                    } else {
                        balanceError = false
                        View.GONE
                    }
                itemData.amountError = balanceError || amountError
            }
            setEtBackground(itemData)
        }

        private fun setupDeleteButton(
            itemData: BetInfoListData, itemCount: Int, onItemClickListener: OnItemClickListener
        ) {
            itemView.tv_close.setOnClickListener {
                onItemClickListener.onDeleteClick(itemData.matchOdd.oddsId, itemCount)
            }
        }
    }


    //串關
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
            userMoney: Double,
            userLogin: Boolean
        ) {
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
                userMoney,
                userLogin
            )
        }
    }

    //填充單注
    class BatchSingleInMoreOptionViewHolder(itemView: View) : BatchParlayViewHolder(itemView) {
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
            onSelectedPositionListener: OnSelectedPositionListener,
            position: Int,
            hasBetClosedForSingle: Boolean,
            userMoney: Double,
            userLogin: Boolean
        ) {
            mUserMoney = userMoney
            mUserLogin = userLogin
            mHasBetClosedForSingle = hasBetClosedForSingle
            maxBet = getMaxOrMinAmount(true, betList)
            minBet = getMaxOrMinAmount(false, betList)

            itemView.apply {

                setupItemEnable(hasBetClosedForSingle)

                itemData?.let {
                    setupSingleItem(
                        betList,
                        itemData,
                        currentOddsType,
                        onItemClickListener,
                        notifyAllBet,
                        mSelectedPosition,
                        mBetView,
                        onSelectedPositionListener,
                        position
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
            onSelectedPositionListener: OnSelectedPositionListener,
            position: Int
        ) {
            itemView.apply {

                et_bet_single.apply {
                    if (tag is TextWatcher) {
                        removeTextChangedListener(tag as TextWatcher)
                    }
                    onFocusChangeListener = null
                    filters = arrayOf(MoneyInputFilter())
                }

//                if (et_bet_single.text.isNullOrEmpty())
//                    ll_winnable.visibility = View.VISIBLE
//                else
//                    ll_winnable.visibility = View.VISIBLE

//                tv_single_count.text = betList.size.toString()
                val singleType = "${context.getString(R.string.bet_list_single)}*${betList.size}"
                tv_single_type.text = singleType

                val initValue = if (itemData.singleInput != null) itemData.allSingleInput else ""

                if (itemData.isInputBet) {
                    layoutKeyBoard.setupMaxBetMoney(getMaxOrMinAmount(isGetMax = true, betList))
                }

                //init winnable amount
//                if (!initValue.isNullOrEmpty()) {
//                    et_win_single.setText(
//                        TextUtil.formatInputMoney(
//                            getAllSingleWinnableAmount(
//                                if (initValue.isNullOrEmpty()) 0.0 else initValue.toDouble(),
//                                currentOddsType,
//                                betList
//                            )
//                        )
//                    )
//                }
//                tv_winnable_amount.text = TextUtil.formatMoney(
//                    getAllSingleWinnableAmount(
//                        if (initValue.isNullOrEmpty()) 0.0 else initValue.toDouble(),
//                        currentOddsType,
//                        betList
//                    )
//                )
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
                                if (data.matchOdd.status != BetStatus.ACTIVATED.code) return@forEachIndexed

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
                                    if (matchOdd.isOnlyEUType || matchOdd.odds == matchOdd.malayOdds || matchType == MatchType.OUTRIGHT || matchType == MatchType.OTHER_OUTRIGHT) {
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
                                                    data.matchOdd, dataOddsType
                                                )
                                            )
                                        }
                                    }

                                    OddsType.IDN -> {
                                        if (getOdds(data.matchOdd, dataOddsType) < 0) {
                                            realAmount = data.betAmount * abs(
                                                getOdds(
                                                    data.matchOdd, dataOddsType
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
                            onItemClickListener.refreshBetInfoTotal()
                        }

                        override fun beforeTextChanged(
                            s: CharSequence?, start: Int, count: Int, after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?, start: Int, before: Int, count: Int
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

                et_bet_single.setOnTouchListener { view, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        et_bet_single.isFocusable = true
                        onItemClickListener.onHideKeyBoard()
                        layoutKeyBoard.setupMaxBetMoney(getMaxOrMinAmount(isGetMax = true, betList))
                        layoutKeyBoard.showKeyboard(
                            et_bet_single, position
                        )
                        //onItemClickListener.onShowParlayKeyboard(et_bet_single, itemData, position, getMaxOrMinAmount(isGetMax = true, betList))
                        onSelectedPositionListener.onSelectChange(
                            bindingAdapterPosition, BetViewType.SINGLE
                        )
                        onItemClickListener.onShowKeyboard(position)
                    }
                    false
                }

                et_bet_single.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) layoutKeyBoard?.hideKeyboard()
                    itemData.isInputBet = hasFocus
                    if (hasFocus) et_bet_single.setSelection(et_bet_single.text.length)
                    setEtBetSingleBackground(itemData)
                }

                cl_item_background_single.setOnClickListener {
                    onItemClickListener.onHideKeyBoard()
                    clearFocus()
                }

//                btn_rule_single.setOnClickListener {
//                    onItemClickListener.showParlayRule(
//                        ParlayType.SINGLE.key,
//                        context.getString(ParlayType.SINGLE.ruleStringRes ?: 0)
//                    )
//                }
            }
        }

        private fun setupItemEnable(hasBetClosed: Boolean) {
            itemView.apply {
//                iv_bet_lock_single.visibility = if (hasBetClosed) View.VISIBLE else View.GONE

//                ll_winnable.visibility = if (hasBetClosed) View.INVISIBLE else View.VISIBLE

//                btn_rule_single.visibility = if (hasBetClosed) View.GONE else View.VISIBLE

                //et_container.isEnabled = !hasBetClosed //EditText的click事件
            }
        }

        /**
         * 填充所有單注後獲取總可贏額
         */
        private fun getAllSingleWinnableAmount(
            betAmount: Double, currentOddsType: OddsType, betList: MutableList<BetInfoListData>
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
            isGetMax: Boolean, betList: MutableList<BetInfoListData>
        ): Double {

            val defaultMax = 9999999L
            var min = betList.firstOrNull()?.parlayOdds?.min ?: 0
            var max = betList.firstOrNull()?.parlayOdds?.max ?: defaultMax
            betList.forEach {
                if (it.parlayOdds?.min ?: 0 > min) {
                    min = it.parlayOdds?.min ?: 0
                }
                if (it.parlayOdds?.max ?: 0 < max) {
                    max = it.parlayOdds?.max ?: defaultMax
                }
            }

            return when (isGetMax) {
                true -> if (mUserLogin) max.toDouble() else defaultMax.toDouble() //未登入最大值9位數
                else -> min.toDouble()
            }
        }

        private fun checkBetLimitSingle(
            itemData: BetInfoListData, betAmount: Double
        ) {
            itemView.apply {
                var amountError = false
                val maxBet = (itemData.parlayOdds?.max ?: 0).toDouble()
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

    abstract class BatchParlayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var mUserMoney: Double = 0.0
        private var mUserLogin: Boolean = false
        private var inputMaxMoney: Double = 0.0
        private var inputMinMoney: Double = 0.0
        private var mHasBetClosed: Boolean = false
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
            userMoney: Double,
            userLogin: Boolean
        ) {
            mUserMoney = userMoney
            mUserLogin = userLogin
            mHasBetClosed = hasBetClosed
            //設置投注輸入上限額
            setupInputMoney(itemData)

            itemView.apply {

                setupItemEnable(hasBetClosed)

                itemData?.let { data ->
                    tv_parlay_type.text = if (data.num == 1) {
                        getParlayName(data.parlayType).plus("*").plus(data.num.toString())
                    } else {
                        getParlayName(data.parlayType).plus("*").plus(data.num.toString())
                    }

//                    //item_bet_list_batch_control_connect_v3 無 tv_parlay_odd, tv_symbol_odd
//                    tv_parlay_odd.apply {
//                        if (firstItem && !hasBetClosed) {
//                            visibility = View.VISIBLE
//                            text = TextUtil.formatForOdd(getOdds(data, currentOddsType))
//                        } else
//                            visibility = View.GONE
//                    }
//
//                    tv_symbol_odd.visibility =
//                        if (firstItem && !hasBetClosed) View.VISIBLE else View.GONE

//                    tv_com_count.text = data.num.toString()
                    setupBetAmountInput(
                        data,
                        OddsType.EU,
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

        private fun setupInputMoney(itemData: ParlayOdd?) {
            val parlayMaxBet = itemData?.max ?: 0
            //未登录的情况下，最大限额为7个9
            inputMaxMoney = if (mUserLogin) parlayMaxBet.toDouble() else 9999999.toDouble()
            val parlayMinBet = itemData?.min ?: 0
            inputMinMoney = parlayMinBet.toDouble()
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
//                btn_rule.visibility = if (hasBetClosed) View.GONE else View.VISIBLE
                ll_hint_container.isVisible = !hasBetClosed
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
                et_bet_parlay.apply {
                    //第1步：為了避免TextWatcher在第2步被調用，提前移除
                    if (tag is TextWatcher) {
                        removeTextChangedListener(tag as TextWatcher)
                    }

                    //第2步：移除TextWatcher之後，設置EditText的value
                    if (data.input != null) setText(data.inputBetAmountStr) else text.clear()
                    setSelection(text.length)
                }
                onFocusChangeListener = null

                if (data.isInputBet) {
                    layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
                }
                checkBetLimitParlay(data)

                et_bet_parlay.apply {
                    /* set listener */
                    val tw: TextWatcher?
                    tw = object : TextWatcher {
                        override fun afterTextChanged(it: Editable?) {
                            if (it.isNullOrEmpty()) {
                                data.betAmount = 0.000
                                data.inputBetAmountStr = ""
                                data.input = null
                            } else {
                                val quota = it.toString().toDouble()
                                data.betAmount = quota
                                data.inputBetAmountStr = it.toString()
                                data.input = it.toString()

                                inputMaxMoney.let { max ->
                                    if (quota > max) {
                                        et_bet_parlay.apply {
                                            setText(TextUtil.formatInputMoney(max))
                                            setSelection(text.length)
                                        }
                                        return@afterTextChanged
                                    }
                                }
                            }
                            checkBetLimitParlay(data)
                            onItemClickListener.refreshBetInfoTotal()
                        }

                        override fun beforeTextChanged(
                            s: CharSequence?, start: Int, count: Int, after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?, start: Int, before: Int, count: Int
                        ) {
                        }
                    }

                    removeTextChangedListener(tw)
                    addTextChangedListener(tw)
                    tag = tw
                }

                et_bet_parlay.setOnTouchListener { view, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        et_bet_parlay.isFocusable = true
                        layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
                        layoutKeyBoard.showKeyboard(
                            et_bet_parlay, position, isParlay = true
                        )
                        onSelectedPositionListener.onSelectChange(
                            bindingAdapterPosition, BetViewType.PARLAY
                        )
                        onItemClickListener.onShowParlayKeyboard(position)
                    }
                    false
                }

                et_bet_parlay.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) layoutKeyBoard?.hideKeyboard()
                    data.isInputBet = hasFocus
                    if (hasFocus) et_bet_parlay.setSelection(et_bet_parlay.text.length)
                    setEtBetParlayBackground(data)
                }

                ll_control_connect.setOnClickListener {
                    onItemClickListener.onHideKeyBoard()
                    clearFocus()
                }
            }
        }

        private fun setEtBetParlayBackground(itemData: ParlayOdd) {
            itemView.apply {
                if (mHasBetClosed) {
                    et_bet_parlay.setBackgroundResource(R.drawable.bg_radius_2_edittext_unfocus)
                    et_bet_parlay.isEnabled = false
                } else {
                    et_bet_parlay.isEnabled = true
                    if (itemData.amountError) {
                        et_bet_parlay.setBackgroundResource(R.drawable.bg_radius_2_edittext_error)
                    } else if (itemData.isInputBet) {
                        et_bet_parlay.setBackgroundResource(R.drawable.bg_radius_2_edittext_focus)
                    } else {
                        et_bet_parlay.setBackgroundResource(R.drawable.bg_radius_2_edittext_unfocus)
                    }
                }

                //更新bet parlay editText hint
//                val betHint = context.getString(
//                    R.string.min_bet_format,
//                    inputMinMoney.toLong().toString()
//                )
                val betHint = context.getString(
                    R.string.hint_bet_limit_range,
                    inputMinMoney.toLong().toString(),
                    inputMaxMoney.toLong().toString()
                )
                if (LoginRepository.isLogin.value == true) {
                    //限額用整數提示
                    tv_hint_parlay_default.text = betHint
                    val etBetHasInput = !et_bet_parlay.text.isNullOrEmpty()
//                if (etBetHasInput) {
                    tv_hint_parlay_default.isVisible = !etBetHasInput //僅輸入金額以後隱藏
//                } else {
//                    tv_hint_parlay_default.isVisible = !itemData.isInputBet
//                }
                } else {
                    tv_hint_parlay_default.isVisible = false
                }
            }
        }

        private fun checkBetLimitParlay(
            itemData: ParlayOdd, betAmount: Double = itemData.betAmount
        ) {
            itemView.apply {
                var amountError: Boolean
                val balanceError: Boolean
                if (!itemData.input.isNullOrEmpty() && betAmount == 0.000) {
                    tvErrorMessageParlay.isVisible = false
                    //請輸入正確投注額
                    tvPleaseEnterCorrectAmountParlay.visibility =
                        if (!itemData.input.isNullOrEmpty() && betAmount == 0.000) {
                            amountError = true
                            View.VISIBLE
                        } else {
                            amountError = false
                            View.GONE
                        }
                } else {
                    tvPleaseEnterCorrectAmountParlay.isVisible = false
                    if (betAmount > inputMaxMoney) {
                        //超過最大限額
                        amountError = true
                        tvErrorMessageParlay.apply {
                            text = context.getString(R.string.bet_info_list_maximum_limit_amount)
                            isVisible = true
                        }
                    } else {
                        tvErrorMessageParlay.apply {
                            isVisible = false
                            if (betAmount != 0.0 && betAmount < inputMinMoney) {
                                //低於最小限額
                                amountError = true
                                text =
                                    context.getString(R.string.bet_info_list_minimum_limit_amount)
                                isVisible = true
                            } else {
                                amountError = false
                                isVisible = false
                            }
                        }
                    }
                }
                tvBalanceInsufficientMessageParlay.visibility =
                    if (betAmount != 0.0 && betAmount > mUserMoney) {
                        tvErrorMessageParlay.isVisible = false //同時滿足限額和餘額不足提示條件，優先顯示餘額不足
                        balanceError = true
                        View.VISIBLE
                    } else {
                        balanceError = false
                        View.GONE
                    }
                itemData.amountError = if (balanceError) true else amountError
            }
            setEtBetParlayBackground(itemData)
        }

        private fun setupParlayRuleButton(
            data: ParlayOdd, onItemClickListener: OnItemClickListener
        ) {
//            itemView.btn_rule.setOnClickListener {
//                onItemClickListener.showParlayRule(
//                    data.parlayType,
//                    getParlayRuleStringRes(data.parlayType)?.let { ruleRes ->
//                        itemView.context.getString(
//                            ruleRes
//                        )
//                    }
//                        ?: "")
//            }
        }
    }

    // 警訊
    class CantParlayWarnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class OddsChangedWarnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface OnItemClickListener {
        fun onDeleteClick(oddsId: String, currentItemCount: Int)
        fun onRechargeClick()
        fun onShowKeyboard(position: Int)
        fun onShowParlayKeyboard(position: Int)
        fun onHideKeyBoard()
        fun saveOddsHasChanged(matchOdd: MatchOdd)
        fun refreshBetInfoTotal(isSingleAdapter: Boolean = false)
        fun showParlayRule(parlayType: String, parlayRule: String)
        fun onMoreOptionClick()
        fun onOddsChangesAcceptTips()
        fun onOddsChangeAcceptSelect(tvTextSelect: TextView)
        fun onOddsChangesWarningTips(isShow: Boolean)

        fun onOddsChangesSetOptionListener(text:String)
    }

    interface OnSelectedPositionListener {
        fun onSelectChange(position: Int, single: BetViewType)
    }
}