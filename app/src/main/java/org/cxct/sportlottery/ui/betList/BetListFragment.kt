package org.cxct.sportlottery.ui.betList

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.event.BetModeChangeEvent
import org.cxct.sportlottery.common.extentions.*
import org.cxct.sportlottery.databinding.FragmentBetListBinding
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.service.MatchOddsRepository
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.common.enums.ChannelType
import org.cxct.sportlottery.service.dispatcher.CashoutMatchStatusDispatcher
import org.cxct.sportlottery.service.dispatcher.CashoutSwitchDispatcher
import org.cxct.sportlottery.service.dispatcher.GlobalStopDispatcher
import org.cxct.sportlottery.service.dispatcher.ProducerUpDispatcher
import org.cxct.sportlottery.ui.betList.adapter.BetListRefactorAdapter
import org.cxct.sportlottery.ui.betList.holder.MAX_BET_VALUE
import org.cxct.sportlottery.ui.betList.listener.OnItemClickListener
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.results.StatusType
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.OkPopupWindow
import org.cxct.sportlottery.view.dialog.BetBalanceDialog
import org.cxct.sportlottery.view.dialog.BasketballDelBetTipDialog
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager
import timber.log.Timber
import java.math.BigDecimal

/**
 * @app_destination 滿版注單(點擊賠率彈出)
 *
 * 畫面會依照注單數量(viewModel.betInfoList)動態調整高度
 * if (size == 1) { 單一注單 } else { 多筆注單 or 空注單 }
 */
class BetListFragment : BaseSocketFragment<BetListViewModel,FragmentBetListBinding>() {


    companion object {
        /**
         * 投注类型
         * PARLAY 串关投注
         * SINGLE 单项投注
         * BASKETBALL_ENDING_CARD 篮球末位比分
         */
        const val SINGLE = 0
        const val PARLAY = 1
        const val BASKETBALL_ENDING_CARD = 2

        //篮球删除投注信息提示弹窗  不再提示标记
        const val BASKETBALL_DEL_TIP_FLAG = "basketball_del_tip_flag"

        @JvmStatic
        fun newInstance(
            betResultListener: BetResultListener,
            showToolbar: Boolean = false,
            currentBetType: Int = 0
        ) = BetListFragment().apply {
            this.betResultListener = betResultListener
            this.showToolbar = showToolbar
            this.currentBetType = currentBetType
        }
    }

    /**
     *  SINGLE 0
     *  PARLAY 1
     *  BASKETBALL_ENDING_CARD 2
     */
    private var currentBetType: Int = 0

    private var oddsType: OddsType = OddsType.EU


    private var betListRefactorAdapter: BetListRefactorAdapter? = null

    private var betParlayListRefactorAdapter: BetListRefactorAdapter? = null

//    private var betSingleListAdapter: BetSingleListAdapter? = null

    private var betAllAmount = 0.0

    private var betResultListener: BetResultListener? = null

    private var showToolbar: Boolean = false

    private var betParlayList: List<ParlayOdd>? = null //紀錄投注時的串關資料

    private var showOddChangeWarn: Boolean = false //賠率是否有變更

    private var showPlatCloseWarn: Boolean = false //盤口是否被關閉

    private var showReceipt: Boolean = false

    private var needUpdateBetLimit = false //是否需要更新投注限額

    private var isOpen = false //记录注单框展开收起状态

    /**
     * 当前所选赔率
     * 0：接受任何赔率变化
     * 1：永不接受赔率变化
     * 2：接受更好赔率变化
     */
    private var currentBetOption = 0


    private var singleParlayList = mutableListOf(
        ParlayOdd(
            max = -1,
            min = -1,
            num = -1,
            odds = 0.0,
            hkOdds = 0.0,
            parlayType = "",
            malayOdds = 0.0,
            indoOdds = 0.0
        )
    )

    override fun onInitView(view: View) {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        initView()
        initObserver()
        initSocketObserver()
        queryData()
    }

    override fun onDestroy() {
        super.onDestroy()
        betListRefactorAdapter?.betList?.let {
            unsubscribeChannel(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvBetList.adapter = null
    }

    private fun initView() {
        initBtnView()
        initBtnEvent()
        initTabLayout()
        initRecyclerView()
        initToolBar()

        binding.llRoot.setOnClickListener {
            //点击外部区域是否清空购物车
            onBackPressed()
        }
        binding.parlayLayout.setOnClickListener {
            println("点击底部区域")
        }

        //設定本金, 可贏的systemCurrencySign
        binding.apply {
            tvBalanceSign.text = getString(R.string.text_account_history_balance)
            tvCurrencySign.text = "(${sConfigData?.systemCurrencySign})"
            tvBalance.text = TextUtil.formatMoney(0.0)
            titleAllBet.text =
                getString(R.string.total_bet_money_colon, sConfigData?.systemCurrencySign)
            titleWinnableAmount.text = getString(R.string.total_all_win_amount)
        }
    }

    private fun initBtnView() {
        //點背景dismiss
        binding.bgDimMount.setOnClickListener {
            onBackPressed()
        }
    }


    fun onBackPressed() {
        if (BetInfoRepository.currentState == SINGLE) {
            exitAnimation(true)
        } else {
            exitAnimation(false)
        }
    }

    private fun initBtnEvent()=binding.run {
        btnBet.apply {
            binding.tvLogin.setOnClickListener {
                needUpdateBetLimit = true
                requireActivity().startLogin()
            }

            binding.clBet.clickDelay {
                addBet()
            }

            binding.tvRemoveClosedSelections.setOnClickListener { removeClosedPlat() }
        }
    }

    private fun initTabLayout() {
        val type = BetInfoRepository.currentBetType
        Timber.d("currentStateSingleOrParlay:${type}")
        binding.btnParlaySingle.text = if (type == 0) {
            getString(R.string.bet_parlay)
        } else {
            refreshLlMoreOption()
            "+" + getString(R.string.bet_single)
        }
    }


    private fun refreshLlMoreOption(showParlayList: Boolean = true) {
        /**
         * @since 只有一張投注單時 串關資料會存在一筆parlayType為1C1的資料
         * @since 投注單無法串關時 串關資料為空(parlayList), 經處理會塞入一項資料(singleParlayList)作為"單項投注"填充所有單注使用
         * @see org.cxct.sportlottery.util.parlaylimit.ParlayLimitUtil.getCom
         * @see singleParlayList
         */
        val currentParlayList = getCurrentParlayList()

        if (currentParlayList.isEmpty() && BetInfoRepository.currentBetType == SINGLE) {
            EventBusUtil.post(BetModeChangeEvent(SINGLE))
        } else if (getCurrentBetList().isEmpty() && BetInfoRepository.currentBetType == BASKETBALL_ENDING_CARD) {
            EventBusUtil.post(BetModeChangeEvent(BASKETBALL_ENDING_CARD))
        } else {
            EventBusUtil.post(BetModeChangeEvent(PARLAY))
        }
        Timber.d("currentParlayList.size():${currentParlayList.size}")
        if (currentParlayList.any {
                val isEmpty = it.parlayType.isNotEmpty()
                val parlayType = it.parlayType != "1C1"
                isEmpty && parlayType
            }) {
            if (showParlayList) {
                binding.clParlayList.visible()
            }
        } else {
            binding.clParlayList.gone()
        }
    }

    private fun initRecyclerView() {
        initAdapter()
        //串关投注项
        val layoutManager = ScrollCenterLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rvBetList.layoutManager = layoutManager
        betListRefactorAdapter?.setHasStableIds(true)
        binding.rvBetList.itemAnimator=null
        binding.rvBetList.adapter = betListRefactorAdapter

        //生成注单项的item
        val parlayLayoutManager =
            ScrollCenterLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rvParlayList.layoutManager = parlayLayoutManager
        binding.rvParlayList.itemAnimator=null
        betParlayListRefactorAdapter?.setHasStableIds(true)
        binding.rvParlayList.adapter = betParlayListRefactorAdapter

        when (BetInfoRepository.currentBetType) {
            SINGLE -> {
                currentBetType = SINGLE
                betListRefactorAdapter?.adapterBetType = BetListRefactorAdapter.BetRvType.SINGLE
                binding.clParlayList.visibility = View.GONE
                binding.clTitle.tvClearAll.gone()
                binding.lineShadow.gone()
                binding.clTitle.tvClose.text = getString(R.string. bottom_sheet_close)
            }

            PARLAY -> {
                currentBetType = PARLAY
                betListRefactorAdapter?.adapterBetType =
                    BetListRefactorAdapter.BetRvType.PARLAY_SINGLE
                refreshLlMoreOption()
                binding.clTitle.tvClearAll.visible()
                binding.clTitle.tvClose.text = getString(R.string.D039)
                binding.clTitle.tvClose.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(resources,R.drawable.ic_cart_collapse,null),null,null,null)
                binding.lineShadow.visible()
            }

            //篮球末位比分
            else -> {
                currentBetType = BASKETBALL_ENDING_CARD
                betListRefactorAdapter?.adapterBetType =
                    BetListRefactorAdapter.BetRvType.BasketballEndingCard
                refreshLlMoreOption()
                binding.clTitle.tvClearAll.visible()
                binding.clTitle.tvClose.visible()
                binding.clTitle.tvClose.text = getString(R.string.D039)
                binding.clTitle.tvClose.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(resources,R.drawable.ic_cart_collapse,null),null,null,null)
                binding.lineShadow.visible()
                binding.lineShadow.gone()
                binding.btnParlaySingle.gone()
                binding.btnBet.updateLayoutParams {
                    width = 240.dp
                }
            }
        }
        BetInfoRepository.isTouched = false
        checkAllAmountCanBet()
        refreshAllAmount()

        binding.clExpandOrStacked.setOnClickListener {
            if (isOpen) {
                binding.clTotalInfo.gone()
                binding.tvExpandOrStacked.text = getString(R.string.expand_more_combinations)
                binding.tvExpandOrStacked.setTextColor(requireActivity().getColor(R.color.color_14366B))
                binding.tvExpandOrStacked.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ResourcesCompat.getDrawable(resources, R.drawable.icon_show_more, null),
                    null
                )
            } else {
                binding.clTotalInfo.visible()
//                betParlayListRefactorAdapter?.closeAllKeyboard()
                binding.tvExpandOrStacked.text = getString(R.string.stacked_combination)
                binding.tvExpandOrStacked.setTextColor(requireActivity().getColor(R.color.color_025BE8))
                binding.tvExpandOrStacked.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ResourcesCompat.getDrawable(resources, R.drawable.icon_hide_more, null),
                    null
                )
            }
            betParlayListRefactorAdapter?.apply {
                BetListRcvUtil.setFitHeight(isOpen, binding.rvParlayList, this)
                notifyDataSetChanged()
            }
            isOpen = !isOpen
        }

        //串关赔率的接受任何赔率变化+
        val userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        val currentOddsChangeOp = userInfo?.oddsChangeOption ?: 0
        currentBetOption = currentOddsChangeOp

        setOnClickListeners(binding.clTitle.tvClearAll, binding.tvDeleteAll) {
            if (BetInfoRepository.currentBetType == BASKETBALL_ENDING_CARD){
                if (!KvUtils.decodeBooleanTure(BASKETBALL_DEL_TIP_FLAG, false)) {
                    val dialog = BasketballDelBetTipDialog(requireContext())
                    dialog.setNegativeClickListener(object :
                        BasketballDelBetTipDialog.OnNegativeListener {
                        override fun negativeClick(isCheck: Boolean) {
                            KvUtils.put(BASKETBALL_DEL_TIP_FLAG, isCheck)
                            adapterItemClickListener.clearCarts()
                            dialog.dismiss()
                        }
                    })
                    dialog.show()
                } else {
                    adapterItemClickListener.clearCarts()
                }
            }else{
                exitAnimation(true)
            }
        }

        binding.clTitle.tvClose.setOnClickListener {
            if (BetInfoRepository.currentBetType == BASKETBALL_ENDING_CARD){
                exitAnimation(false)
            }else{
                onBackPressed()
            }
        }
        binding.btnParlaySingle.clickDelay {
            switchCurrentBetMode()
        }


    }


    private fun initToolBar() {
        binding.clTitle.root.setOnClickListener {
            //只有串关的情况下才会触发点击事件
            if (currentBetType == PARLAY) {
                onBackPressed()
            }
        }

//        binding.clTitle.ivArrow.setOnClickListener {
//            onBackPressed()
//        }
    }
    val adapterItemClickListener = object : OnItemClickListener {

        override fun onOddChangeEndListener() {
            if (context == null) return
            binding.btnBet.setBtnText(getString(R.string.bet_info_list_bet))
            binding.btnBet.resetButtonStyle()
        }

        override fun onOddChangeStartListener(isUp: Boolean) {
            if (context == null) return
            binding.btnBet.setBtnText(getString(R.string.P139))
            binding.btnBet.setOddsButtonChangeStyle()
        }

        override fun onDeleteClick(oddsId: String, currentItemCount: Int) {
            viewModel.removeBetInfoItem(oddsId)
        }


        override fun clearCarts() {
            this@BetListFragment.exitAnimation(true)
        }

        override fun onRechargeClick() {
            if (viewModel.getLoginBoolean()) {
                startActivity(Intent(context, MoneyRechargeActivity::class.java))
            } else {
                requireActivity().startLogin()
            }
        }

        override fun onShowKeyboard(position: Int) {
            (binding.rvBetList.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
                binding.rvBetList, RecyclerView.State(), position
            )
        }

        override fun onHideKeyBoard() {
            betListRefactorAdapter?.betList?.forEach {
                it.isInputBet = false; it.isInputWin = false
            }
        }

        override fun saveOddsHasChanged(matchOdd: MatchOdd) {
            viewModel.saveOddsHasChanged(matchOdd)
        }

        override fun refreshBetInfoTotal(isSingleAdapter: Boolean) {
            if (isSingleAdapter) {
                betListRefactorAdapter?.notifyDataSetChanged()
            }
            checkAllAmountCanBet()
            refreshAllAmount()
        }

        override fun showParlayRule(parlayType: String, parlayRule: String) {
//                showParlayDescription(parlayType, parlayRule)
        }

        override fun onMoreOptionClick() {
            betListRefactorAdapter?.itemCount?.let {
                binding.rvBetList?.scrollToPosition(it - 1)
            }
        }


        override fun onOddsChangeAcceptSelect(tvTextSelect: TextView) {
            try {
                tvTextSelect.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, ResourcesCompat.getDrawable(
                        resources, R.drawable.ic_arrow_down_double, null
                    ), null
                )
                val popupWindow = OkPopupWindow(
                    requireContext(), tvTextSelect.text.toString()
                ) { text, position ->
                    tvTextSelect.text = text
                    currentBetOption = position
                    viewModel.updateOddsChangeOption(currentBetOption)
                }

                popupWindow.setOnDismissListener {
                    tvTextSelect.setCompoundDrawablesWithIntrinsicBounds(
                        null, null, ResourcesCompat.getDrawable(
                            resources, R.drawable.ic_arrow_down_blue, null
                        ), null
                    )
                }
                popupWindow.showUpCenter(tvTextSelect)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        override fun onOddsChangesSetOptionListener(text: String) {
            currentBetOption = OddsModeUtil.currentSelectModeIndexWithText(text)
        }

        override fun addMore() {
            onBackPressed()
        }
    }
    private fun initAdapter() {


        betListRefactorAdapter = BetListRefactorAdapter(
            binding.layoutKeyBoard, adapterItemClickListener
        ) {
            getUserBalance()
        }

//        betSingleListAdapter =
//            BetSingleListAdapter(adapterItemClickListener, binding.layoutKeyBoard)

        betParlayListRefactorAdapter = BetListRefactorAdapter(
            binding.layoutKeyBoard,
            adapterItemClickListener,
        ) { getUserBalance() }.apply {
            adapterBetType = BetListRefactorAdapter.BetRvType.PARLAY
        }
    }

    private fun checkAllAmountCanBet() {
        val betList = getCurrentBetList()
        val parlayList = getCurrentParlayList()
        //僅判斷對應tab裡的amountError
        if (currentBetType == SINGLE || currentBetType == BASKETBALL_ENDING_CARD) {
            betList.getOrNull(0)?.let {
                if (it.amountError) {
                    binding.btnBet.amountCanBet = false
                    return
                }
            }
        } else {
            parlayList.forEach {
                if (it.amountError) {
                    binding.btnBet.amountCanBet = false
                    return
                }
            }
        }
        binding.btnBet.amountCanBet = true
        Timber.d("balanceError5:${binding.btnBet.amountCanBet}")
    }

    private fun refreshAllAmount(newBetList: List<BetInfoListData>? = null) {
        val originalList = newBetList ?: getCurrentBetList()
        val list = (newBetList
            ?: getCurrentBetList()).filter { it.matchOdd.status == BetStatus.ACTIVATED.code }//過濾不能投注的

        val parlayList =
            if (originalList.size == list.size) getCurrentParlayList() else mutableListOf()//單注有不能投注的單則串關不做顯示也不能投注

        //只取得對應tab內的totalBetAmount
        val totalBetAmount = if (currentBetType == 0) {
            list.sumOf { it.realAmount }
        } else {
            parlayList.sumOf { it.betAmount * it.num }
        }

        val winnableAmount = if (currentBetType == 0) {
            list.sumOf {
                var currentOddsType = oddsType
                if (it.matchOdd.odds == it.matchOdd.malayOdds || it.matchType == MatchType.OUTRIGHT || it.matchType == MatchType.OTHER_OUTRIGHT) {
                    currentOddsType = OddsType.EU
                }
                if (it.matchOdd.isOnlyEUType) currentOddsType = OddsType.EU
                getWinnable(it.betAmount, getOdds(it.matchOdd, currentOddsType), currentOddsType)
            }
        } else {
            parlayList.sumOf {
                getComboWinnable(
                    it.betAmount, getOdds(it, OddsType.EU), it.num
                )
            }
        }


        binding.tvTotalBetAmount.text = TextUtil.formatForOdd(totalBetAmount)
        binding.tvTotalWinnableAmount.text =
            "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(winnableAmount, 2)}"


        val betCount = if (currentBetType == SINGLE || currentBetType == BASKETBALL_ENDING_CARD) {
            list.size
        } else {
            parlayList.filter { it.betAmount > 0 }.sumOf { it.num }
        }


        binding.btnBet.apply {
            isParlay = currentBetType == 1
            betCounts = betCount
            currentBetListCounts = getCurrentBetList().size
        }

        betAllAmount = totalBetAmount
        Timber.d("getCurrentBetList:${getCurrentBetList().size} ${getCurrentBetList()}")
    }

    private fun getWinnable(betAmount: Double, odds: Double, oddsType: OddsType): Double {
        return when (oddsType) {
            OddsType.MYS -> {
                if (odds < 0) {
                    betAmount
                } else {
                    multiplyOdds(betAmount, odds)
                }
            }

            OddsType.IDN -> {
                if (odds < 0) {
                    betAmount
                } else {
                    multiplyOdds(betAmount, odds)
                }
            }

            OddsType.EU -> {
                multiplyOdds(betAmount, odds - 1)
            }

            else -> {
                multiplyOdds(betAmount, odds)
            }
        }

    }

    private fun multiplyOdds(betAmount: Double, odds: Double): Double {
        return betAmount.toBigDecimal().multiply(odds.toBigDecimal()).toDouble()
    }

    private fun getComboWinnable(betAmount: Double, odds: Double, num: Int): BigDecimal {
        return betAmount.toBigDecimal().multiply(odds.toBigDecimal())
    }


    private fun clearCarts() {
            runWithCatch { viewModel.betInfoList.removeObservers(viewLifecycleOwner) }  // viewLifecycleOwner 有可能空
            viewModel.removeBetInfoAll()
            setCurrentBetModeSingle()
            EventBusUtil.post(BetModeChangeEvent(SINGLE))
    }

    private fun setCurrentBetModeSingle() {
        currentBetType = SINGLE
        BetInfoRepository.setCurrentBetState(SINGLE)
        betListRefactorAdapter?.adapterBetType = BetListRefactorAdapter.BetRvType.SINGLE
    }

    private fun switchCurrentBetMode() {
            if (binding.btnParlaySingle.text == "+" + getString(R.string.bet_single)) {
                //玩法变成单注
                currentBetType = SINGLE
                BetInfoRepository.setCurrentBetState(SINGLE)
                //按钮变成串关
                binding.btnParlaySingle.text = getString(R.string.bet_parlay)
            } else {
                currentBetType = PARLAY
                BetInfoRepository.setCurrentBetState(PARLAY)
                binding.btnParlaySingle.text = getString(R.string.bet_single)
            }

            when (currentBetType) {
                //單項投注
                SINGLE -> {
                    betListRefactorAdapter?.adapterBetType = BetListRefactorAdapter.BetRvType.SINGLE
//                    binding.clTitle.ivArrow.setImageResource(
//                        R.drawable.ic_single_bet_delete
//                    )
                    setViewGone(binding.clParlayList, binding.clTitle.tvClearAll)
                    BetInfoRepository.switchSingleMode()
                    EventBusUtil.post(BetModeChangeEvent(SINGLE))
                    BetInfoRepository.isTouched = false
                }
                //串關投注
                PARLAY -> {
                    betListRefactorAdapter?.adapterBetType =
                        BetListRefactorAdapter.BetRvType.PARLAY_SINGLE
//                    binding.clTitle.ivArrow.setImageResource(
//                        R.drawable.ic_single_bet_delete
//                    )
                    binding.clTitle.tvClearAll.visible()
                    refreshLlMoreOption()
                    BetInfoRepository.switchParlayMode()
                    //从单关切换成串关会收起购物车，反之不会
                    exitAnimation(false)
                    EventBusUtil.post(BetModeChangeEvent(PARLAY))
                }
            }
            betListRefactorAdapter?.notifyDataSetChanged()
            checkAllAmountCanBet()
            refreshAllAmount()
    }

    private fun getUserBalance(): Double {
        if (!viewModel.getLoginBoolean()) {
            return -1.0
        }
        return viewModel.userMoney.value ?: 0.0
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserver() {
        //是否登入
        viewModel.isLogin.observe(this.viewLifecycleOwner) {
            setupBetButtonType(it)
            if (needUpdateBetLimit) {
                viewModel.getMoneyAndTransferOut()
                viewModel.updateBetLimit()
                needUpdateBetLimit = false
            }
            betListRefactorAdapter?.userLogin = it
//            betSingleListAdapter?.userLogin = it
            betParlayListRefactorAdapter?.userLogin = it
        }

        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.let { money ->
                binding.tvBalance.text = TextUtil.formatMoney(money)
                betListRefactorAdapter?.userMoney = money
                betParlayListRefactorAdapter?.userMoney = money
            }
        }

        viewModel.oddsType.observe(viewLifecycleOwner) {
            betListRefactorAdapter?.oddsType = it
//            betSingleListAdapter?.oddsType = it
            betParlayListRefactorAdapter?.oddsType = it
            oddsType = it
        }


        viewModel.betInfoList.observe(viewLifecycleOwner) {
            it.peekContent().let { list ->
                //注單列表沒東西時關閉fragment
                if (list.size == 0) {
                    setCurrentBetModeSingle()
                    EventBusUtil.post(BetModeChangeEvent(SINGLE))
                    BetInfoRepository.isTouched = false
                    activity?.supportFragmentManager?.popBackStack()
                    return@observe
                }
                if (list.size == 1) {
                    binding.clTitle.view3.gone()
                    binding.clTitle.tvBetListCount.gone()
                } else {
                    binding.clTitle.view3.visible()
                    binding.clTitle.tvBetListCount.visible()
                    binding.clTitle.tvBetListCount.text = list.size.toString()
                }
                betListRefactorAdapter?.betList = list
                betParlayListRefactorAdapter?.betList = list
                subscribeChannel(list)
                refreshAllAmount(list)
                checkAllAmountCanBet()
            }
        }

        //移除注單解除訂閱
        BetInfoRepository.removeItem.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                unSubscribeChannelEvent(it)
            }
            betListRefactorAdapter?.notifyDataSetChanged()
            betParlayListRefactorAdapter?.notifyDataSetChanged()
        }

        //串關列表
        viewModel.parlayList.observe(this.viewLifecycleOwner) {
            if (it.size == 0) {
                binding.clExpandOrStacked.gone()
                betListRefactorAdapter?.hasParlayList = false
                betListRefactorAdapter?.parlayList = singleParlayList
                betParlayListRefactorAdapter?.hasParlayList = false
            } else {
                if (it.size > 1) {
                    binding.clExpandOrStacked.visible()
                } else {
                    binding.clExpandOrStacked.gone()
                }
                betListRefactorAdapter?.hasParlayList = true
                betListRefactorAdapter?.parlayList = it
                betParlayListRefactorAdapter?.hasParlayList = true
                betParlayListRefactorAdapter?.parlayList = it
            }

            if (isOpen) {
                betParlayListRefactorAdapter?.let { it1 ->
                    BetListRcvUtil.setWrapHeight(
                        binding.rvParlayList, it1
                    )
                }
            }
        }

        viewModel.betParlaySuccess.observe(viewLifecycleOwner) {
            showHideCantParlayWarn(!it)
        }

        //投注結果
        viewModel.betAddResult.observe(this.viewLifecycleOwner) {
            val result = it.getContentIfNotHandled()
            showReceipt = result != null
            if (result == null) {
                return@observe
            }

            if (result.success) {
                setBetLoadingVisibility(false)
                viewModel.betInfoList.removeObservers(this.viewLifecycleOwner)
                betResultListener?.onBetResult(result.receipt, betParlayList ?: listOf(), true)
                refreshAllAmount()
                showOddChangeWarn = false
                binding.btnBet.isOddsChanged = false
            } else {
                binding.btnBet.postDelayed({ setBetLoadingVisibility(false) }, 800)
                val msg = if (result.msg.isEmptyStr()) getString(R.string.F042) else result.msg
                SingleToast.showSingleToastNoImage(context(), msg)
            }
        }

        //賠率變更提示
        viewModel.showOddsChangeWarn.observe(this.viewLifecycleOwner) {
            showOddChangeWarn = it
            binding.btnBet.isOddsChanged = it
            Timber.d("isShow: showOddsChangeWarn:$it")
        }

        //盤口關閉提示
        viewModel.showOddsCloseWarn.observe(this.viewLifecycleOwner) {
            binding.btnBet.hasPlatClose = it
            showPlatCloseWarn = it
        }

        viewModel.hasBetPlatClose.observe(this.viewLifecycleOwner) {
            binding.btnBet.hasBetPlatClose = it
        }

    }

    private fun initSocketObserver() {
        MatchOddsRepository.observerMatchStatus(viewLifecycleOwner) {
            if (it.matchStatusCO?.status == StatusType.END_GAME) {
                betListRefactorAdapter?.betList?.let { betInfoList ->
                    betInfoList.forEachIndexed { index, betInfoListData ->
                        if (SocketUpdateUtil.updateOddStatus(betInfoListData, it)) {
                            betListRefactorAdapter?.notifyItemChanged(index)
                            checkAllAmountCanBet()
                        }
                    }
                }
            }
        }

        receiver.matchOddsLock.collectWith(lifecycleScope) { viewModel.updateLockMatchOdd(it) }
        GlobalStopDispatcher.observe(this.viewLifecycleOwner) { globalStopEvent->
            val betRefactorList = betListRefactorAdapter?.betList ?: return@observe
            betRefactorList.forEach { listData ->
                if (globalStopEvent.producerId == null || listData.matchOdd.producerId == globalStopEvent.producerId.value) {
                    listData.matchOdd.status = BetStatus.LOCKED.code
                }
            }
            betListRefactorAdapter?.betList = betRefactorList
            betParlayListRefactorAdapter?.betList = betRefactorList
        }

        ProducerUpDispatcher.observe(viewLifecycleOwner) {
            betListRefactorAdapter?.betList.let { list ->
                betListPageUnSubScribeEvent()
                list?.let { listNotNull ->
                    unsubscribeChannel(listNotNull)
                    subscribeChannel(listNotNull)
                }
            }
        }
        CashoutSwitchDispatcher.observe(viewLifecycleOwner) { event->
            betListRefactorAdapter?.betList?.let { list ->
                list.forEach { it.betInfo?.cashoutStatusShow = event.status }
                betListRefactorAdapter?.notifyDataSetChanged()
            }
        }
        CashoutMatchStatusDispatcher.observe(viewLifecycleOwner) { event->
            betListRefactorAdapter?.betList?.let { list ->
                val map = event.cashoutMatchStatusListList.map { it.matchId to it.status }.toMap()
                list.forEachIndexed { index, betInfoListData ->
                    if (map.containsKey(betInfoListData.matchOdd.matchId)){
                        map[betInfoListData.matchOdd.matchId!!]?.let {
                            betInfoListData.betInfo?.cashoutStatusShow =it
                            betListRefactorAdapter?.notifyItemChanged(index)
                        }
                    }
                }
            }
        }

    }

    private fun getCurrentBetList(): MutableList<BetInfoListData> {
        return betListRefactorAdapter?.betList ?: mutableListOf()
    }

    private fun getCurrentParlayList(): MutableList<ParlayOdd> {
        return betListRefactorAdapter?.parlayList ?: mutableListOf()
    }

    private fun addBet() {
        if (!NetworkUtil.isAvailable(requireContext())) {
            showPromptDialog(
                getString(R.string.prompt), getString(R.string.message_network_no_connect)
            ) {}
            return
        }
        //顯示betLoading
        setBetLoadingVisibility(true)
        val betList = getCurrentBetList()
        val betListFilter = betList.filter { it.matchOdd.status == BetStatus.ACTIVATED.code }

        val parlayList =
            if (betList.size == betListFilter.size) getCurrentParlayList() else mutableListOf()

        val tempParlayList = mutableListOf<ParlayOdd>()
        parlayList.forEach {
            tempParlayList.add(it.copy())
        }
        betParlayList = tempParlayList

        var maxBetMoney = MAX_BET_VALUE.toString()
        var minBetMoney = "0"
        if (betListFilter.isNotEmpty()) {
            //最大投注金额
            //2023/12/19 与H5对比后，发现maxBetMoney 是固定的7个9
//            maxBetMoney = betListFilter[0].betInfo?.maxBetMoneyString.toString()
            //最小投注金额
            betListFilter.getOrNull(0)?.let {
                minBetMoney = if (it.matchType==MatchType.OUTRIGHT) it.betInfo?.minCpBetMoneyString.toString() else it.betInfo?.minBetMoneyString.toString()
            }
        }
        val totalBetAmount: Double = when (currentBetType) {
            //单关
            0 -> {
                betListFilter.sumOf { it.realAmount }
            }
            //篮球
            2 -> {
                if (betListFilter.isEmpty()) {
                    -1.0
                } else {
                    betListFilter[0].betAmount * betListFilter.size
                }
            }
            //串关
            else -> {
                parlayList.sumOf { it.betAmount * it.num }
            }
        }
        if (totalBetAmount < 0) {
            Timber.w("totalBetAmount isEmpty")
            return
        }

        Timber.d("maxBetMoney:$maxBetMoney minBetMoney:$minBetMoney totalBetAmount:$totalBetAmount")
        MultiLanguagesApplication.mInstance.mOddsType.value?.let {
            oddsType = it
        }

        //投注余额不足
        val balanceInsufficient = {
            setBetLoadingVisibility(false)
            //换成弹框提示 去充值
            val dialog= BetBalanceDialog(requireContext())
            dialog.showDialog{
                //跳转充值

            }
        }
        val overMax = {
            setBetLoadingVisibility(false)
            ToastUtil.showToast(requireContext(), R.string.N989)
        }

        val belowMin = {
            setBetLoadingVisibility(false)
            ToastUtil.showToast(requireContext(), R.string.N990)
        }

        val addBetList = {
            viewModel.addBetList(
                getCurrentBetList(), parlayList, oddsType, currentBetType, currentBetOption
            )
        }

        if (currentBetType == 1) {
            //串关
            //金额校验规则
            //1.总投注额大于余额，提示余额不足
            //2.只要有一个投注项投注额大于最大额度，提示超出最大投注额
            //3.投注额不为null并且小于最小投注额，提示低于最低投注额
            //4.全部失败或者全部成功
            if (totalBetAmount > (viewModel.userMoney.value ?: 0.0)) {
                balanceInsufficient()
            } else if (parlayList.any { it.betAmount > maxBetMoney.toDouble() }) {
                overMax()
            } else if (parlayList.any { it.input != null && it.betAmount < minBetMoney.toDouble() }) {
                belowMin()
            } else {
                addBetList()
            }
        } else {
            //单关或篮球末位比分，冠军
            //金额校验规则
            //1.投注额大于当前余额，提示余额不足
            //2.投注额大于最大限额，提示超出最大投注额
            //3.投注额小于最小投注额，提示低于最低投注额
            //4.以上都不满足==>请求后端接口
            if (totalBetAmount > (viewModel.userMoney.value ?: 0.0)) {
                balanceInsufficient()
            } else if (totalBetAmount > maxBetMoney.toDouble()) {
                overMax()
            } else if (totalBetAmount < minBetMoney.toDouble()) {
                belowMin()
            } else {
                addBetList()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshAllAmount()
        checkAllAmountCanBet()
    }

    /**
     * 是否顯示 betLoading
     */
    private fun setBetLoadingVisibility(isVisible: Boolean, keepShowingBetLoading: Boolean = false) {
        binding.blockTouchView.isVisible = isVisible
        if (keepShowingBetLoading) {
            binding.betLoadingView.isVisible = true
        } else {
            binding.betLoadingView.isVisible = isVisible
        }
    }

    /**
     * 投注按鈕狀態(登入、未登入)
     */
    private fun setupBetButtonType(isLogin: Boolean) {
        binding.btnBet.isLogin = isLogin
    }

    /**
     * 移除盤口關閉的投注選項
     */
    private fun removeClosedPlat() {
        viewModel.removeClosedPlatBetInfo()
    }

    private fun queryData() {
        //獲取餘額
        viewModel.getMoneyAndTransferOut()
    }


    /**
     * 同賽事不能串關提示
     * @param show true:顯示, false:隱藏
     * @如果資料只有一筆，也不用顯示
     * @反波膽也不能串關
     */
    private fun showHideCantParlayWarn(show: Boolean) {
        when (currentBetType) {
            //單項投注
            SINGLE -> {
                binding.clParlayList.gone()
            }
            //串關投注
            PARLAY -> {
                refreshLlMoreOption()
            }
        }
    }

    private fun subscribeChannel(list: MutableList<BetInfoListData>) {
        betListPageSubscribeEvent()
        val subscribedList: MutableList<String> = mutableListOf()
        list.forEach { listData ->
            if (listData.subscribeChannelType == ChannelType.HALL) {
                subscribeChannelHall(
                    listData.matchOdd.gameType, listData.matchOdd.matchId
                )
            } else {
                val subscribeMatchId = listData.matchOdd.matchId
                if (!subscribedList.contains(subscribeMatchId)) {
                    subscribedList.add(subscribeMatchId)
                    subscribeChannelEvent(subscribeMatchId)
                }
            }
        }
    }


    private fun unsubscribeChannel(list: MutableList<BetInfoListData>) {
        val unsubscribedList: MutableList<String> = mutableListOf()
        list.forEach { listData ->
            val unsubscribeMatchId = listData.matchOdd.matchId
            if (!unsubscribedList.contains(unsubscribeMatchId)) {
                unsubscribedList.add(unsubscribeMatchId)
                if (listData.subscribeChannelType == ChannelType.HALL) {
                    unSubscribeChannelHall(
                        listData.matchOdd.gameType, unsubscribeMatchId
                    )
                } else {
                    if (!unsubscribedList.contains(unsubscribeMatchId)) {
                        unSubscribeChannelEvent(unsubscribeMatchId)
                    }
                }
            }
        }
        betListPageUnSubScribeEvent()
    }


    interface BetResultListener {
        fun onBetResult(
            betResultData: Receipt?, betParlayList: List<ParlayOdd>, isMultiBet: Boolean
        )
    }

    var llRootHeight = 0

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (enter) {
            binding.llFragmentBetListContent.post {
                binding.llRoot.visible()
                llRootHeight = binding.llRoot.height
                AnimatorUtils.startTranslationY(
                    targetView = binding.llFragmentBetListContent, fromY = llRootHeight, toY = 0
                )
                binding.bgDimMount.animate().alphaBy(1f).setDuration(200).setStartDelay(200).start()
            }
        }
        //else{
        //退出动画为什么不写在else里面？
        //因为当fragment退出时，fragment里面的一切物件都会被系统回收，
        //所以这里的一切操作都是无效的
        //所以这里就想到了另外一个方案，
        //在popBackStackImmediate调用之前先执行动画，
        //动画结束之后在调用 popBackStackImmediate 方法，问题即可解决
        //}
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    private fun exitAnimation(clearCarts: Boolean) {
        binding.bgDimMount.animate().alphaBy(0f).setDuration(300).start()
        AnimatorUtils.startTranslationY(targetView = binding.llFragmentBetListContent,
            fromY = 0,
            toY = llRootHeight,
            onAnimEndListener = {
                if (clearCarts) {
                    clearCarts()
                }
                val fm = activity?.supportFragmentManager ?: return@startTranslationY
                if (fm.isDestroyed) {
                    return@startTranslationY
                }

                if (fm.isStateSaved) {
                    runWithCatch {
                        val beginTransaction = fm.beginTransaction()
                        beginTransaction.remove(this)
                        beginTransaction.commitAllowingStateLoss()
                    }
                }

                runWithCatch { fm.popBackStackImmediate() }
            })
    }
}