package org.cxct.sportlottery.ui.betList.holder

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.setOnClickListener
import org.cxct.sportlottery.common.extentions.setViewVisible
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ContentBetInfoItemV3BaseketballEndingCardBinding
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.betList.BetInfoListData
import org.cxct.sportlottery.ui.betList.adapter.BetListRefactorAdapter
import org.cxct.sportlottery.ui.betList.listener.OnItemClickListener
import org.cxct.sportlottery.ui.betList.listener.OnSelectedPositionListener
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.KvUtils.BASKETBALL_DEL_TIP_FLAG
import org.cxct.sportlottery.util.drawable.DrawableUtils
import org.cxct.sportlottery.view.dialog.BasketballDelBetTipDialog
import timber.log.Timber

class BasketballEndingCardViewHolder(
    private val contentView: ContentBetInfoItemV3BaseketballEndingCardBinding,
    val userBalance: () -> Double,
) : RecyclerView.ViewHolder(contentView.root) {

    private var inputMaxMoney: Double = 0.0
    private var inputMinMoney: Double = 0.0
    private var inputWinMaxMoney: Double = 0.0
    private var inputWinMinMoney: Double = 0.0
    private var mUserMoney: Double = 0.0
    private var mUserLogin: Boolean = false

    fun bind(
        betList: MutableList<BetInfoListData>?,
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
        adapterBetType: BetListRefactorAdapter.BetRvType?
    ) {
        mUserMoney = userMoney
        mUserLogin = userLogin
        //設置投注限額
        setupInputLimit(itemData)

        contentView.apply {
            layoutKeyBoard.setUserMoney(mUserMoney)
            layoutKeyBoard.setGameType(itemData.matchOdd.playCode)
            if (betList != null) {
                layoutKeyBoard.setBetItemCount(betList.size)
            }
            setupBetAmountInput(
                betList,
                itemData,
                if (itemData.matchOdd.isOnlyEUType) OddsType.EU else currentOddsType,
                onItemClickListener,
                betListSize,
                mSelectedPosition,
                onSelectedPositionListener,
                position,
                adapterBetType
            )
        }
    }


    private fun setupInputLimit(itemData: BetInfoListData) {
        val maxBet = itemData.parlayOdds?.max ?: 0
        //未登录的情况下，最大限额为7个9
        inputMaxMoney = if (mUserLogin) maxBet.toDouble() else 9999999.toDouble()
        val minBet = itemData.parlayOdds?.min ?: 0
        inputMinMoney = minBet.toDouble()
    }



    @SuppressLint("ClickableViewAccessibility")
    private fun setupBetAmountInput(
        betList: MutableList<BetInfoListData>?,
        itemData: BetInfoListData,
        currentOddsType: OddsType,
        onItemClickListener: OnItemClickListener,
        betListSize: Int,
        mSelectedPosition: Int,
        onSelectedPositionListener: OnSelectedPositionListener,
        position: Int,
        adapterBetType: BetListRefactorAdapter.BetRvType?
    ) = contentView.run {
        fun showTotalStakeWinAmount( bet: Double){
            val totalBet = TextUtil.formatMoney(bet * betListSize, 2)
            val totalCanWin = TextUtil.formatMoney(bet * itemData.matchOdd.odds, 2)
            tvTotalStakeAmount.text = "${sConfigData?.systemCurrencySign}${totalBet}"
            tvTotalWinAmount.text = "${sConfigData?.systemCurrencySign}${totalCanWin}"
        }
        //移除TextChangedListener
        etBet.apply {
            if (tag is TextWatcher) {
                removeTextChangedListener(tag as TextWatcher)
            }
            onFocusChangeListener = null
            filters = arrayOf(MoneyInputFilter())
        }

        Timber.d("itemData:${itemData}")
        var lastSelectPo = 0

        val rcvBasketballAdapter = object :
            BaseQuickAdapter<BetInfoListData, BaseViewHolder>(R.layout.item_bet_basketball_ending_cart) {
            override fun convert(holder: BaseViewHolder, item: BetInfoListData) {

                val tvMatchOdds = holder.getView<TextView>(R.id.tvMatchOdds)
                tvMatchOdds.background = DrawableUtils.getBasketballBetListButton(root)
                holder.setText(R.id.tvMatchOdds, item.matchOdd.playName)
                val tvHide = holder.getView<TextView>(R.id.tvHide)
                tvHide.background = DrawableUtils.getBasketballDeleteButton(root)

                if (item.isClickForBasketball == true) {
                    tvHide.visible()
                } else {
                    tvHide.gone()
                }

                //设置+More
                if (holder.layoutPosition == data.size - 1) {
                    holder.setGone(R.id.tvMatchOdds, true).setVisible(R.id.tvBsMore, true)
                        .setText(R.id.tvBsMore, R.string.N920)
                    val tvBsMore = holder.getView<TextView>(R.id.tvBsMore)
                    tvBsMore.background = DrawableUtils.getBasketballPlusMore(root)
                    tvBsMore.setOnClickListener {
                        onItemClickListener.addMore()
                    }
                } else {
                    holder.setVisible(R.id.tvMatchOdds, true).setGone(R.id.tvBsMore, true)
                }

                //点击赔率
                tvMatchOdds.setOnClickListener {
                    //刷新上一次点击的区域
                    if (data.size > lastSelectPo) {
                        data[lastSelectPo].isClickForBasketball = false
                        notifyItemChanged(lastSelectPo)
                    }
                    val currentPosition = holder.layoutPosition
                    //记录本次点击的区域
                    if (data.size > currentPosition) {
                        data[currentPosition].isClickForBasketball = true
                        notifyItemChanged(currentPosition)
                        lastSelectPo = currentPosition
                    }
                }

                //蒙版点击事件
                tvHide.setOnClickListener {
                    data[holder.layoutPosition].isClickForBasketball = false
                    lastSelectPo = 0
                    onItemClickListener.onDeleteClick(data[holder.layoutPosition].matchOdd.oddsId, itemCount)
                }
            }
        }
        rcvBasketballScore.adapter = rcvBasketballAdapter
        val newList = mutableListOf<BetInfoListData>()
        if (betList != null) {
            newList.addAll(betList)
        }
        newList.sortBy { it.matchOdd.playName.split("-")[1].toInt() }
        newList.sortBy { it.matchOdd.playName.split("-")[0].toInt() }
        newList.add(newList[0])
        newList.forEach {
            it.isClickForBasketball = false
        }
        rcvBasketballAdapter.setNewInstance(newList)
        rcvBasketballScore.layoutManager = GridLayoutManager(root.context, 5)
        tvBasketBetListCount.text = "X${betList?.size}"

        setOnClickListener(rcvBasketballScore,clItemBackground){
            rcvBasketballAdapter.data.forEach { itemD->
                itemD.isClickForBasketball = false
            }
            it.clearFocus()
            rcvBasketballAdapter.notifyDataSetChanged()
        }
        //設定editText內容
        etBet.apply {
            if (itemData.input == null) {
                val minBet = itemData.parlayOdds?.min ?: 0
                itemData.input = minBet.toString()
            }
            itemData.inputBetAmountStr = itemData.input
            itemData.betAmount = itemData.input!!.toDouble()
            setText(itemData.inputBetAmountStr)
            setSelection(text.length)

            //显示总投注
            val bet = itemData.inputBetAmountStr!!.toDouble()
            showTotalStakeWinAmount(bet)
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
                Timber.d("textChange:${it.toString()}")
                if (it.isNullOrEmpty()) {
                    itemData.betAmount = 0.000
                    itemData.inputBetAmountStr = ""
                    itemData.input = null
                    itemData.realAmount = 0.0
                    tvTotalStakeAmount.text = ""
                    tvTotalWinAmount.text = ""
                } else {
                    val quota = it.toString().toDouble()
                    itemData.betAmount = quota
                    itemData.inputBetAmountStr = it.toString()
                    itemData.input = it.toString()
                    val max = inputMaxMoney.coerceAtMost(quota.coerceAtLeast(userBalance()))
                    if (quota > max) {
                        etBet.apply {
                            setText(TextUtil.formatInputMoney(max))
                            setSelection(text.length)
                        }
                        return
                    }

                    //总投注
                    val bet = it.toString().toDouble()
                    showTotalStakeWinAmount(bet)
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

        etBet.addTextChangedListener(tw)
        etBet.tag = tw
        etBet.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) {
                    etBet.isFocusable = true
                    onItemClickListener.onHideKeyBoard()
                    layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
                    layoutKeyBoard.showKeyboard(
                        etBet, position
                    )
                    onSelectedPositionListener.onSelectChange(
                        bindingAdapterPosition, BetListRefactorAdapter.BetViewType.SINGLE
                    )
                    onItemClickListener.onShowKeyboard(position)
                }
            }
            false
        }
        //單筆注單展開時，預設開啟輸入本金的鍵盤
        if (betListSize == 1) {
            etBet.requestFocus()
            itemData.isInputBet = true
            layoutKeyBoard.setupMaxBetMoney(inputMaxMoney)
            layoutKeyBoard.showKeyboard(
                etBet, position
            )

        }
        etBet.setOnFocusChangeListener { _, hasFocus ->
            itemData.isInputBet = hasFocus
            if (hasFocus) {
                etBet.setSelection(etBet.text.length)
            }
            setEtBackground(itemData)
        }
//        clItemBackground.setOnClickListener {
//            clItemBackground.clearFocus()
//        }
    }

    var oddsId = ""
    var oldOdds = ""
    var handler = Handler(Looper.getMainLooper())


    private fun setupOddInfo(
        itemData: BetInfoListData,
        currentOddsType: OddsType,
        betListSize: Int,
        onItemClickListener: OnItemClickListener,
        adapterBetType: BetListRefactorAdapter.BetRvType?
    ) = contentView.run {

        if (itemData.matchOdd.status == BetStatus.ACTIVATED.code && oldOdds != TextUtil.formatForOdd(
                getOdds(itemData.matchOdd, currentOddsType)
            )
        ) {
            oddsId = itemData.matchOdd.oddsId
            oldOdds = TextUtil.formatForOdd(getOdds(itemData.matchOdd, currentOddsType))
        }
        //反波膽顯示 %
        var tvOdd = "@ " + TextUtil.formatForOdd(
            getOdds(
                itemData.matchOdd,
                currentOddsType,
                adapterBetType == BetListRefactorAdapter.BetRvType.SINGLE
            )
        )
        if (itemData.matchOdd.playCode == PlayCate.LCS.value) tvOdd =
            "@ " + TextUtil.formatForOddPercentage(
                getOdds(
                    itemData.matchOdd,
                    currentOddsType,
                    adapterBetType == BetListRefactorAdapter.BetRvType.SINGLE
                ) - 1
            )

        tvOdds.text = if (itemData.matchOdd.status == BetStatus.ACTIVATED.code) tvOdd else {
            tvOdd
        }

        oddsContentContainer.setBackgroundResource(R.color.transparent)
        //設定隊伍名稱, 聯賽名稱, 開賽時間
        tvMatchHome.text = itemData.matchOdd.homeName
        tvMatchHome.maxWidth = 160.dp
        tvMatchAway.text = itemData.matchOdd.awayName
        setViewVisible(tvVs, tvMatchAway, tvLeagueName)
        tvLeagueName.text = itemData.matchOdd.leagueName?.trim()

        btnBasketballDeleteAll.background = DrawableUtils.getBasketballDeleteAllDrawable(root)
        btnBasketballDeleteAll.setOnClickListener {
            if (!KvUtils.decodeBooleanTure(BASKETBALL_DEL_TIP_FLAG, false)) {
                val dialog = BasketballDelBetTipDialog(root.context)
                dialog.setNegativeClickListener(object :
                    BasketballDelBetTipDialog.OnNegativeListener {
                    override fun negativeClick(isCheck: Boolean) {
                        KvUtils.put(BASKETBALL_DEL_TIP_FLAG, isCheck)
                        onItemClickListener.clearCarts()
                        dialog.dismiss()
                    }
                })
                dialog.show()
            } else {
                onItemClickListener.clearCarts()
            }
        }


        val view = View.inflate(tvMatchHome.context, R.layout.popupwindow_tips, null)
        val pop = PopupWindow(tvMatchHome.context).apply {
            contentView = view
            setBackgroundDrawable(null)
            isOutsideTouchable = true
        }
        val textView = view.findViewById<TextView>(R.id.tvContent)
        val imageView = view.findViewById<ImageView>(R.id.ivPopupWindowTipsBg)
        val showPopAsTop: (TextView, String?) -> Unit = { it, it2 ->
            if (pop.isShowing) {
                pop.dismiss()
            }

            it.setTextColor(it.context.getColor(R.color.color_025BE8))
            textView.text = it2
            val xOff: Int
            val yOff = (-50).dp
            if (it == tvMatchAway) {
                xOff = (-20).dp
                imageView.background =
                    AppCompatResources.getDrawable(it.context, R.drawable.bg_popup_tips_right)
            } else {
                xOff = (-5).dp
                imageView.background =
                    AppCompatResources.getDrawable(it.context, R.drawable.bg_popup_tips_left)
            }
            pop.showAsDropDown(it, xOff, yOff)
        }

        setOnClickListener(tvLeagueName, tvMatchHome, tvMatchAway) {
            when (it) {
                tvLeagueName -> {
                    showPopAsTop(tvLeagueName, itemData.matchOdd.leagueName)
                }

                tvMatchHome -> {
                    showPopAsTop(tvMatchHome, itemData.matchOdd.homeName)
                }

                tvMatchAway -> {
                    showPopAsTop(tvMatchAway, itemData.matchOdd.awayName)
                }
            }
        }

        pop.setOnDismissListener {
            tvLeagueName.setTextColor(tvLeagueName.context.getColor(R.color.color_9BB3D9_535D76))
            tvMatchHome.setTextColor(tvLeagueName.context.getColor(R.color.color_A7B2C4))
            tvMatchAway.setTextColor(tvLeagueName.context.getColor(R.color.color_A7B2C4))
        }

    }

    private fun setEtBackground(itemData: BetInfoListData) {
        contentView.apply {
            if (itemData.amountError) {
                Timber.d("setEtBackground error")
                etBet.setBackgroundResource(R.drawable.bg_radius_2_edittext_error)
            } else {
                if (itemData.isInputBet) {
                    Timber.d("setEtBackground itemData.isInputBet true")
                    etBet.setBackgroundResource(R.drawable.bg_radius_2_edittext_focus)
                } else {
                    Timber.d("setEtBackground itemData.isInputBet false")
                    etBet.setBackgroundResource(R.drawable.bg_radius_2_edittext_unfocus)
                }
            }

            //更新bet editText hint
            val betHint = root.context.getString(
                R.string.hint_bet_limit_range,
                inputMinMoney.toLong().toString(),
                inputMaxMoney.toLong().toString()
            )

            //更新win editText hint
            val winHint = root.context.getString(
                R.string.hint_bet_limit_range,
                inputWinMinMoney.toLong().toString(),
                inputWinMaxMoney.toLong().toString()
            )

            if (LoginRepository.isLogin.value == true) {
                etBet.hint = betHint
            } else {
                etBet.hint = ""
            }
        }
    }

    private fun checkBetLimit(
        itemData: BetInfoListData
    ) {
        contentView.apply {
            val betAmount = itemData.betAmount
            val balanceError: Boolean
            val amountError: Boolean = if (!itemData.input.isNullOrEmpty() && betAmount == 0.000) {
                !itemData.input.isNullOrEmpty()
            } else {
                if (betAmount > inputMaxMoney) {
                    //超過最大限額
                    true
                } else {
                    betAmount != 0.0 && betAmount < inputMinMoney
                }
            }

//            Timber.d("用户余额:$mUserMoney")
            if (betAmount != 0.0 && betAmount > mUserMoney) {
                balanceError = true
                View.VISIBLE
            } else {
                balanceError = false
                View.GONE
            }
            Timber.d("balanceError1:${balanceError} amountError:$amountError")
            itemData.amountError = balanceError || amountError
            Timber.d("balanceError2:${itemData.amountError} ")
        }
        setEtBackground(itemData)
    }


}