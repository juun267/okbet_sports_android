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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlinx.android.synthetic.main.button_bet.view.*
import kotlinx.android.synthetic.main.content_bet_info_item.*
import kotlinx.android.synthetic.main.content_bet_info_item_quota_detail.*
import kotlinx.android.synthetic.main.content_bet_info_item_quota_detail.view.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.*
import kotlinx.android.synthetic.main.view_bet_info_close_message.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBetInfoItemBinding
import org.cxct.sportlottery.databinding.ContentBetListBatchControlBinding
import org.cxct.sportlottery.databinding.ItemBetListBatchControlConnectBinding
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.bet.list.INPLAY
import org.cxct.sportlottery.ui.bet.list.OddSpannableString
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds

@SuppressLint("ClickableViewAccessibility")
class BetListDiffAdapter(private val onItemClickListener: OnItemClickListener) : ListAdapter<DataItem, RecyclerView.ViewHolder>(BetListDiffCallBack()) {
    var focusPosition = -1

    val changeHandler = Handler()

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

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

    var moreOptionCollapse = false

    var needScrollToBottom = false //用來紀錄是否為點擊更多選項需滾動至底部
    private fun submitData(doFirst: () -> Unit = {}) {
        doFirst.invoke()
        val itemList = when {
            betList.isEmpty() -> listOf(DataItem.NoData())
            betList.size == 1 -> betList.map { DataItem.BetInfoData(it) }
            else -> {
                betList.map { DataItem.BetInfoData(it) } + when (moreOptionCollapse) {
                    //查看所有多個選項未展開
                    true -> {
                        parlayList.mapIndexed { index, parlayOdd ->
                            if (index == 0) DataItem.ParlayData(
                                parlayOdd = parlayOdd,
                                firstItem = true
                            ) else DataItem.ParlayData(parlayOdd)
                        }
                    }
                    //查看所有多個選項未展開,只加入第一項
                    false -> {
                        parlayList.filterIndexed { index, _ -> index < 1 }.map {
                            DataItem.ParlayData(
                                parlayOdd = it,
                                firstItem = true
                            )
                        }
                    }
                }
            }
        }
        submitList(itemList.reversed())
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
        val itemData = getItem(holder.adapterPosition)
        when (holder) {
            is ViewHolder -> holder.bind((itemData as DataItem.BetInfoData).betInfoListData, oddsType)
            is BatchSingleViewHolder -> holder.bind((itemData as DataItem.ParlayData).parlayOdd, parlayList.size, betList)
            is BatchParlayViewHolder -> holder.bind((itemData as DataItem.ParlayData).parlayOdd)
        }
    }

    private fun fromViewHolder(viewGroup: ViewGroup): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        val binding: ContentBetInfoItemBinding =
            ContentBetInfoItemBinding.inflate(layoutInflater, viewGroup, false)
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
        return BatchParlayViewHolder(binding)
    }

    inner class ViewHolder(val binding: ContentBetInfoItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(itemData: BetInfoListData, oddsType: OddsType) {
            binding.apply {
                //view binding data
                parlayOdd = itemData.parlayOdds

                /* check input focus */
                if (adapterPosition == focusPosition) {
                    etBet.requestFocus()
                    etBet.setSelection(binding.etBet.text.length)
                } else {
                    etBet.clearFocus()
                }

                setupBetAmountInput(binding, itemData)

                setupOddInfo(binding, itemData)

                setupOddStatus(binding, itemData)

                setupDeleteButton(binding, itemData)

                setupMaximumLimitView(binding)


                //TODO 賠率變化、顯示賠率更新提示
                /*if (itemData.matchOdd.spreadState != SpreadState.SAME.state || itemData.matchOdd.oddState != OddState.SAME.state) {
                    tv_odd_content_changed.visibility = View.VISIBLE
                    button_bet.isOddsChanged = true
                }*/
            }

        }

        private fun setupOddInfo(binding: ContentBetInfoItemBinding, itemData: BetInfoListData) {
            binding.apply {
                //是否為無法串關注單
                vPoint.visibility = if (itemData.pointMarked) View.VISIBLE else View.GONE
                OddSpannableString.setupOddsContent(itemData.matchOdd, oddsType, tvOddsContent)
                tvMatch.text = "${itemData.matchOdd.homeName}${root.context.getString(R.string.verse_)}${itemData.matchOdd.awayName}"
                tvName.text = if (itemData.matchOdd.inplay == INPLAY) {
                    root.context.getString(
                        R.string.bet_info_in_play_score,
                        itemData.matchOdd.playCateName,
                        itemData.matchOdd.homeScore.toString(),
                        itemData.matchOdd.awayScore.toString()
                    )
                } else itemData.matchOdd.playCateName

                //投注額
                (parlayList.firstOrNull()?.allSingleInput ?: itemData.input)?.let {
                    etBet.setText(it)
                }
            }
        }

        private fun setupOddStatus(binding: ContentBetInfoItemBinding, itemData: BetInfoListData) {
            binding.apply {
                if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) {
                    clItemBackground.setBackgroundColor(ContextCompat.getColor(root.context, R.color.colorWhite))
                    ivBetLock.visibility = View.GONE
                    etBet.apply {
                        isFocusable = true
                        isFocusableInTouchMode = true
                    }
                    root.cl_quota_detail.visibility = View.VISIBLE

                } else {
                    clItemBackground.setBackgroundColor(ContextCompat.getColor(root.context, R.color.colorWhite2))
                    ivBetLock.visibility = View.VISIBLE
                    etBet.apply {
                        isFocusable = false
                        isFocusableInTouchMode = false
                    }
                    onItemClickListener.onHideKeyBoard()
                    root.cl_quota_detail.visibility = View.GONE
                }
            }
        }

        private fun setupDeleteButton(binding: ContentBetInfoItemBinding, itemData: BetInfoListData) {
            binding.ivClose.setOnClickListener {
                onItemClickListener.onDeleteClick(itemData.matchOdd.oddsId, itemCount)
            }
        }

        private fun setupMaximumLimitView(binding: ContentBetInfoItemBinding) {
            binding.root.apply {
                tv_check_maximum_limit.setOnClickListener {
                    it.visibility = View.GONE
                    ll_bet_quota_detail.visibility = View.VISIBLE
                }
            }
        }

        /**
         * setup TextWatcher, OnFocusChangeListener
         */
        private fun setupBetAmountInput(binding: ContentBetInfoItemBinding, itemData: BetInfoListData) {
            binding.apply {
                /* set listener */
                val tw = object : TextWatcher {
                    override fun afterTextChanged(it: Editable?) {
                        if (it.isNullOrEmpty()) {
                            itemData.betAmount = 0.000
                            itemData.input = ""
                            root.apply {
                                tv_check_maximum_limit.visibility = View.VISIBLE
                                ll_bet_quota_detail.visibility = View.GONE
                                ll_win_quota_detail.visibility = View.GONE
                            }
                        } else {

                            //輸入時 直接顯示可贏額
                            root.apply {
                                tv_check_maximum_limit.visibility = View.GONE
                                ll_bet_quota_detail.visibility = View.GONE
                                ll_win_quota_detail.visibility = View.VISIBLE
                            }

                            val quota = it.toString().toDouble()
                            itemData.betAmount = quota
                            itemData.input = TextUtil.formatInputMoney(quota)
                            itemData.parlayOdds?.max?.let { max ->
                                if (quota > max) {
                                    etBet.apply {
                                        setText(max.toString())
                                        setSelection(max.toString().length)
                                    }
                                    return@afterTextChanged
                                }
                            }

                            //比照以往計算
                            var win = quota * getOdds(itemData.matchOdd, oddsType)
                            if (oddsType == OddsType.EU) {
                                win -= quota
                            }
                            root.tv_win_quota.text = TextUtil.format(win)

                        }
                        onItemClickListener.refreshAmount()
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
            }
        }

        //TODO 賠率變動尚未完成
        private fun setChangeOdds(position: Int, matchOdd: MatchOdd) {}
    }

    inner class BatchSingleViewHolder(val binding: ContentBetListBatchControlBinding) : BatchViewHolder(binding) {
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

                        setupParlayItem(binding.itemFirstConnect, itemData)
                        setupSingleItem(betList, itemData)
                    }
                    else -> {
                        itemFirstConnect.llConnect.visibility = View.VISIBLE
                        llMoreOption.visibility = View.VISIBLE

                        setupParlayItem(binding.itemFirstConnect, itemData)
                        setupSingleItem(betList, itemData)

                        setupClickMoreItem(llMoreOption)
                    }
                }
            }

        }

        private fun setupSingleItem(betList: MutableList<BetInfoListData>, itemData: ParlayOdd?) {
            binding.itemFirstSingle.apply {
                llWinnable.visibility = View.INVISIBLE
                tvSingleCount.text = betList.size.toString()
                etBet.apply {
                    setText(itemData?.allSingleInput)
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

                            //單注填充所有單注選項, 刷新單注選項資料, 因資料做過反轉故投注單為最後一項開始
                            changeHandler.post { notifyItemRangeChanged(itemCount - betList.size, betList.size) }
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

        private fun setupClickMoreItem(btnShowMore: View) {
            btnShowMore.setOnClickListener {
                val collapse = !moreOptionCollapse
                submitData {
                    moreOptionCollapse = collapse
                    needScrollToBottom = true
                }
            }
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

    inner class BatchParlayViewHolder(val binding: ItemBetListBatchControlConnectBinding) : BatchViewHolder(binding) {
        fun bind(itemData: ParlayOdd?) {
            setupParlayItem(binding, itemData)
        }
    }

    abstract inner class BatchViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        var parlayInputError: Boolean = false
        protected fun setupParlayItem(binding: ItemBetListBatchControlConnectBinding, itemData: ParlayOdd?) {
            binding.apply {
                llWinnable.visibility = View.GONE
                llMaxBetAmount.visibility = View.GONE

                itemData?.let { data ->
                    tvParlayType.text = TextUtil.replaceParlayByC(data.parlayType)

                    val itemOdd = TextUtil.formatForOdd(getOdds(data, oddsType))
                    tvParlayOdd.text = itemOdd
                    tvComCount.text = data.num.toString()

                    setupBetAmountInput(this, data, itemOdd)

                    checkInput(binding, etBet.text.toString(), data)

                    setupMaximumLimitView(binding, data)

                    setupParlayRuleButton(binding, data)

                }
            }
        }

        private fun setupBetAmountInput(binding: ItemBetListBatchControlConnectBinding, data: ParlayOdd, itemOdd: String) {
            binding.apply {
                etBet.apply {
                    /* set listener */
                    val tw = object : TextWatcher {
                        override fun afterTextChanged(it: Editable?) {
                            val inputValue = if (it.isNullOrEmpty()) 0.0 else it.toString().toDouble()
                            val winnableAmount = itemOdd.toDouble() * inputValue
                            when (winnableAmount > 0) {
                                true -> {
                                    llWinnable.visibility = View.VISIBLE
                                    tvWinnableAmount.text = TextUtil.formatMoney(winnableAmount)
                                    llMaxBetAmount.visibility = View.GONE
                                    btnMaximumLimit.visibility = View.GONE
                                }
                                else -> {
                                    llWinnable.visibility = View.GONE
                                    btnMaximumLimit.visibility = View.VISIBLE
                                }
                            }

                            checkInput(binding, it.toString(), data)
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

                    setText(data.betAmount.let {
                        if (it > 0) it.toString() else ""
                    })
                }
            }
        }

        private fun setupMaximumLimitView(binding: ItemBetListBatchControlConnectBinding, data: ParlayOdd) {
            binding.apply {
                btnMaximumLimit.setOnClickListener {
                    it.visibility = View.GONE
                    llMaxBetAmount.visibility = View.VISIBLE
                    tvMaxBetAmount.text = data.max.toString()
                }
            }
        }

        private fun setupParlayRuleButton(binding: ItemBetListBatchControlConnectBinding, data: ParlayOdd) {
            binding.apply {
                btnRule.setOnClickListener {
                    onItemClickListener.showParlayRule(data.parlayType, data.parlayRule ?: "")
                }
            }
        }

        private fun checkInput(binding: ItemBetListBatchControlConnectBinding, it: String, itemData: ParlayOdd?) {
            binding.apply {
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
    }

    inner class NoDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

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

class BetListDiffCallBack : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.oddsId == newItem.oddsId
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }

}

sealed class DataItem {
    abstract var oddsId: String?
    abstract var pointMarked: Boolean?

    data class BetInfoData(val betInfoListData: BetInfoListData, override var oddsId: String? = betInfoListData.matchOdd.oddsId, override var pointMarked: Boolean? = betInfoListData.pointMarked) :
        DataItem()

    data class ParlayData(val parlayOdd: ParlayOdd?, var firstItem: Boolean = false, override var oddsId: String? = null, override var pointMarked: Boolean? = null) : DataItem()

    class NoData : DataItem() {
        override var oddsId: String? = null
        override var pointMarked: Boolean? = null
    }
}