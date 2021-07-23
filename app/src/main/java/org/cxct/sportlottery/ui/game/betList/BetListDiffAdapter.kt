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
import org.cxct.sportlottery.databinding.ContentBetListBatchControlBinding
import org.cxct.sportlottery.databinding.ContentBetListItemBinding
import org.cxct.sportlottery.databinding.ItemBetListBatchControlConnectBinding
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

@SuppressLint("ClickableViewAccessibility")
class BetListDiffAdapter(private val onItemClickListener: OnItemClickListener) : ListAdapter<DataItem, RecyclerView.ViewHolder>(BetListDiffCallBack()) {
    var focusPosition = -1


    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val mHandler by lazy { Handler() }

    private enum class ViewType { Bet, Parlay, ParlayFirst, NoData }

    //TODO review : 利用Tag紀錄betList, parlayList更新狀態,皆處於非更新中時,才提交新的資料(submitList()),或是取得新的parlayList時才提交
    var betList: MutableList<BetInfoListData> = mutableListOf()
        set(value) {
            field = value
            submitData()
        }

    var parlayList: MutableList<ParlayOdd> = mutableListOf()
        set(value) {
            field = value
            submitData()
        }

    private fun submitData() {
        val itemList = when {
            betList.isEmpty() -> listOf(DataItem.NoData())
            betList.size == 1 -> betList.map { DataItem.BetInfoData(it) }
            else -> {
                betList.map { DataItem.BetInfoData(it) } + parlayList.mapIndexed { index, parlayOdd ->
                    if (index == 0) DataItem.ParlayData(
                        parlayOdd = parlayOdd,
                        firstItem = true
                    ) else DataItem.ParlayData(parlayOdd)
                }
            }
        }
        submitList(itemList)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.NoData -> ViewType.NoData.ordinal
            is DataItem.ParlayData -> {
                if ((getItem(position) as DataItem.ParlayData).firstItem)
                    ViewType.ParlayFirst.ordinal
                else
                    ViewType.Parlay.ordinal
            }
            else -> ViewType.Bet.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.NoData.ordinal -> fromNoData(parent)
            ViewType.Parlay.ordinal -> fromBatchParlayViewHolder(parent)
            ViewType.ParlayFirst.ordinal -> fromBatchSingleViewHolder(parent)
            else -> fromViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Timber.e("Dean, bind view holder itemCount = $itemCount , currentList = $currentList")
        val itemData = getItem(holder.adapterPosition)
        when (holder) {
            is ViewHolder -> holder.bind((itemData as DataItem.BetInfoData).betInfoListData, oddsType)
            is BatchSingleViewHolder -> holder.bind((itemData as DataItem.ParlayData).parlayOdd, parlayList.size, betList)
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

    private fun fromBatchSingleViewHolder(viewGroup: ViewGroup): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        val binding = ContentBetListBatchControlBinding.inflate(layoutInflater, viewGroup, false)
        return BatchSingleViewHolder(binding)
    }

    private fun fromBatchParlayViewHolder(viewGroup: ViewGroup): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        val binding = ItemBetListBatchControlConnectBinding.inflate(layoutInflater, viewGroup, false)
        return BatchParlayViewHolder(binding.root)
    }

    inner class ViewHolder(val binding: ContentBetListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        var inputError: Boolean = false

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

                Timber.e("Dean, parlayList[0].allSingleInput = ${parlayList[0].allSingleInput}")
                Timber.e("Dean, itemData.input = ${itemData.input}")
                (parlayList[0].allSingleInput ?: itemData.input)?.let {
                    etBet.setText(it)
                }

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
                            moreClickable = false,
                            platClose = true
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
                            moreClickable = true,
                            platClose = false
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
            moreTextBg: Int, moreTextColor: Int, moreClickable: Boolean,
            platClose: Boolean
        ) {
            binding.llBet.visibility = betVisible
            binding.tvCloseWarning.apply {
                visibility = warningVisible
            }

            //盤口狀態
            binding.apply {
                if (platClose) {
                    llItem.background = ContextCompat.getDrawable(root.context, R.color.colorWhite2)
                    ivLock.visibility = View.VISIBLE
                } else {
                    llItem.background = ContextCompat.getDrawable(root.context, R.color.colorWhite)
                    ivLock.visibility = View.GONE
                }
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

    inner class BatchSingleViewHolder(val binding: ContentBetListBatchControlBinding) : RecyclerView.ViewHolder(binding.root) {
        var parlayInputError: Boolean = false

        fun bind(itemData: ParlayOdd?, parlayListSize: Int, betList: MutableList<BetInfoListData>) {
            binding.apply {
                when (parlayListSize) {
                    0 -> {
                        itemFirstConnect.llConnect.visibility = View.GONE
                        llMoreOption.visibility = View.GONE
                    }
                    1 -> {
                        itemFirstConnect.llConnect.visibility = View.VISIBLE
                        llMoreOption.visibility = View.GONE

                        setupParlayItem(itemData)
                        setupSingleItem(betList, itemData)
                    }
                    else -> {
                        itemFirstConnect.llConnect.visibility = View.VISIBLE
                        llMoreOption.visibility = View.VISIBLE

                        setupParlayItem(itemData)
                        setupSingleItem(betList, itemData)
                        //TODO 點擊查看所有多個選項
                        setupClickMoreItem()
                    }
                }
            }
        }

        private fun setupParlayItem(itemData: ParlayOdd?) {
            binding.itemFirstConnect.apply {
                llWinnable.visibility = View.GONE
                llMaxBetAmount.visibility = View.GONE

                itemData?.let { data ->
                    tvParlayType.text = TextUtil.replaceParlayByC(data.parlayType)

                    val itemOdd = TextUtil.formatForOdd(getOdds(data, oddsType))
                    tvParlayOdd.text = itemOdd
                    tvComCount.text = data.num.toString()

                    etBet.apply {
                        /* set listener */
                        val tw = object : TextWatcher {
                            override fun afterTextChanged(it: Editable?) {
                                val inputValue = if (it.isNullOrEmpty()) 0.0 else it.toString().toDouble()
                                val winnableAmount = itemOdd.toDouble() * inputValue
                                when (winnableAmount > 0) {
                                    true -> {
                                        binding.itemFirstConnect.apply {
                                            llWinnable.visibility = View.VISIBLE
                                            tvWinnableAmount.text = TextUtil.formatMoney(winnableAmount)
                                            llMaxBetAmount.visibility = View.GONE
                                            btnMaximumLimit.visibility = View.GONE
                                        }
                                    }
                                    else -> {
                                        binding.itemFirstConnect.apply {
                                            llWinnable.visibility = View.GONE
                                            btnMaximumLimit.visibility = View.VISIBLE
                                        }
                                    }
                                }

                                if (TextUtils.isEmpty(it)) {
                                    data.input = ""
                                } else {
                                    data.input = it.toString()
                                }

                                checkInput(it.toString(), data)
                                data.betAmount = inputValue
                            }

                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        }

                        val fc = View.OnFocusChangeListener { _, hasFocus ->
                            if (hasFocus && adapterPosition != focusPosition)
                                etBet.clearFocus()
                        }

                        onFocusChangeListener = fc
                        addTextChangedListener(tw)
                        tag = tw

                        setOnTouchListener { _, event ->
                            //若回傳true則不會觸發onTouchEvent,onClick
                            if (event.action == MotionEvent.ACTION_UP) {
                                focusPosition = adapterPosition
                                onItemClickListener.onShowParlayKeyboard(this, data)
                            }
                            false
                        }
                    }

                    checkInput(etBet.text.toString(), data)

                    btnMaximumLimit.setOnClickListener {
                        it.visibility = View.GONE
                        llMaxBetAmount.visibility = View.VISIBLE
                        tvMaxBetAmount.text = data.max.toString()
                    }
                }
            }
        }

        private fun setupSingleItem(betList: MutableList<BetInfoListData>, itemData: ParlayOdd?) {
            binding.itemFirstSingle.apply {
                llWinnable.visibility = View.INVISIBLE
                tvSingleCount.text = betList.size.toString()
                etBet.apply {
                    /* set listener */
                    val tw = object : TextWatcher {
                        override fun afterTextChanged(it: Editable?) {
                            val inputValue = if (it.isNullOrEmpty()) 0.0 else it.toString().toDouble()
                            itemData?.allSingleInput = if (it.isNullOrEmpty()) "" else it.toString()
                            val allWinnableAmount = getAllSingleWinnableAmount(inputValue, betList)
                            when (allWinnableAmount > 0) {
                                true -> {
                                    binding.itemFirstSingle.apply {
                                        llWinnable.visibility = View.VISIBLE
                                        tvWinnableAmount.text = TextUtil.formatMoney(allWinnableAmount)
                                    }
                                }
                                else -> {
                                    binding.itemFirstSingle.llWinnable.visibility = View.INVISIBLE
                                }
                            }

                            if (TextUtils.isEmpty(it)) {
                                itemData?.input = ""
                            } else {
                                itemData?.input = it.toString()
                            }

                            //單注填充所有單注選項, 刷新單注選項資料
                            notifyItemRangeChanged(0, betList.size)
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    }

                    val fc = View.OnFocusChangeListener { _, hasFocus ->
                        if (hasFocus && adapterPosition != focusPosition)
                            etBet.clearFocus()
                    }

                    onFocusChangeListener = fc
                    addTextChangedListener(tw)
                    tag = tw

                    setOnTouchListener { _, event ->
                        //若回傳true則不會觸發onTouchEvent,onClick
                        if (event.action == MotionEvent.ACTION_UP) {
                            focusPosition = adapterPosition
                            onItemClickListener.onShowParlayKeyboard(this, itemData)
                        }
                        false
                    }
                }
            }
        }

        private fun setupClickMoreItem() {}

        private fun checkInput(it: String, itemData: ParlayOdd?) {
            binding.itemFirstConnect.apply {
                itemData?.let { itemDataNotnull ->
                    if (TextUtils.isEmpty(it)) {
                        etBet.setBackgroundResource(R.drawable.effect_select_bet_edit_text_fill_color_white)
                        itemDataNotnull.betAmount = 0.000
                        tvErrorMessage.visibility = View.GONE
                    } else {
                        val quota = it.toDouble()
                        itemData.betAmount = quota
                        when {
                            quota > itemDataNotnull.max -> {
                                parlayInputError = true
                                tvErrorMessage.text = binding.root.context.getString(R.string.bet_info_list_bigger_than_max_limit)
                                etBet.setBackgroundResource(R.drawable.bg_radius_4_edittext_error)
                            }
                            quota < itemDataNotnull.min -> {
                                parlayInputError = true
                                tvErrorMessage.text = binding.root.context.getString(R.string.bet_info_list_less_than_minimum_limit)
                                etBet.setBackgroundResource(R.drawable.bg_radius_4_edittext_error)
                            }
                            else -> {
                                parlayInputError = false
                                etBet.setBackgroundResource(R.drawable.effect_select_bet_edit_text_fill_color_white)
                            }
                        }
                        var win = it.toDouble() * getOdds(matchOdd, oddsType)

                        if (oddsType == OddsType.EU) {
                            win -= quota
                        }

                        tvErrorMessage.visibility = if (parlayInputError) View.VISIBLE else View.GONE
                        etBet.setTextColor(
                            if (parlayInputError) ContextCompat.getColor(binding.root.context, R.color.colorRedDark) else ContextCompat.getColor(binding.root.context, R.color.colorBlackLight)
                        )
                    }
                }
            }
            onItemClickListener.refreshAmount()
        }

        /**
         * 填充所有單注後獲取總可贏額
         */
        private fun getAllSingleWinnableAmount(betAmount: Double, betList: MutableList<BetInfoListData>): Double {
            var allWinnableAmount = 0.0
            betList.forEach {
                allWinnableAmount += getOdds(it.matchOdd, oddsType) * betAmount
            }
            return allWinnableAmount
        }
    }

    inner class BatchParlayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    inner class NoDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface OnItemClickListener {
        fun onDeleteClick(oddsId: String, currentItemCount: Int)
        fun onShowKeyboard(editText: EditText, matchOdd: MatchOdd)
        fun onShowParlayKeyboard(editText: EditText, parlayOdd: ParlayOdd?)
        fun saveOddsHasChanged(matchOdd: MatchOdd)
        fun refreshAmount()
    }
}

class BetListDiffCallBack : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.oddsId == newItem.oddsId && oldItem.oddsId != null
    }

}

sealed class DataItem {
    abstract var oddsId: String?

    data class BetInfoData(val betInfoListData: BetInfoListData, override var oddsId: String? = betInfoListData.matchOdd.oddsId) : DataItem()

    data class ParlayData(val parlayOdd: ParlayOdd?, var firstItem: Boolean = false, override var oddsId: String? = null) : DataItem()

    class NoData : DataItem() {
        override var oddsId: String? = null
    }
}