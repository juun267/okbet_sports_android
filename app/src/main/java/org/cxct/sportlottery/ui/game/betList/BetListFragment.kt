package org.cxct.sportlottery.ui.game.betList

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Single
import kotlinx.android.synthetic.main.bottom_sheet_dialog_parlay_description.btn_close
import kotlinx.android.synthetic.main.bottom_sheet_dialog_parlay_description.tv_parlay_rule
import kotlinx.android.synthetic.main.bottom_sheet_dialog_parlay_description.tv_parlay_type
import kotlinx.android.synthetic.main.button_bet.view.cl_bet
import kotlinx.android.synthetic.main.button_bet.view.tv_login
import kotlinx.android.synthetic.main.button_bet.view.tv_remove_closed_selections
import kotlinx.android.synthetic.main.fragment_bet_list.*
import kotlinx.android.synthetic.main.include_bet_odds_tips_parlay.btnOddsChangeDes
import kotlinx.android.synthetic.main.include_bet_odds_tips_parlay.ivClearCarts
import kotlinx.android.synthetic.main.include_bet_odds_tips_parlay.tvAcceptOddsChange
import kotlinx.android.synthetic.main.include_bet_odds_tips_parlay_warn.llParlayWarn
import kotlinx.android.synthetic.main.publicity_promotion_announcement_view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetListBinding
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.event.BetModeChangeEvent
import org.cxct.sportlottery.extentions.gone
import org.cxct.sportlottery.extentions.visible
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.ScrollCenterLayoutManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.betList.adapter.BetListRefactorAdapter
import org.cxct.sportlottery.ui.game.betList.adapter.BetSingleListAdapter
import org.cxct.sportlottery.ui.game.betList.listener.OnItemClickListener
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.results.StatusType
import org.cxct.sportlottery.ui.transactionStatus.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.widget.OkPopupWindow
import timber.log.Timber
import java.lang.Exception

/**
 * @app_destination 滿版注單(點擊賠率彈出)
 *
 * 畫面會依照注單數量(viewModel.betInfoList)動態調整高度
 * if (size == 1) { 單一注單 } else { 多筆注單 or 空注單 }
 */
class BetListFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {


    companion object {
        private const val BET_CONFIRM_TIPS = 1001

        /**
         * 投注类型
         * PARLAY 串关投注
         * SINGLE 单项投注
         */
        const val SINGLE = 0
        const val PARLAY = 1

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment BetListFragment.
         */
        @JvmStatic
        fun newInstance(betResultListener: BetResultListener, showToolbar: Boolean = false) =
            BetListFragment().apply {
                this.betResultListener = betResultListener
                this.showToolbar = showToolbar
            }
    }

    /**
     *  SINGLE
     *  PARLAY
     */
    private var currentBetType: Int = 0

    private lateinit var binding: FragmentBetListBinding

    private var oddsType: OddsType = OddsType.EU

    private var discount = 1.0F

    private var betListRefactorAdapter: BetListRefactorAdapter? = null

    private var betParlayListRefactorAdapter: BetListRefactorAdapter? = null

    private var betSingleListAdapter: BetSingleListAdapter? = null

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

    //提示
    private var snackBarNotify: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =/*DataBindingUtil.inflate(inflater, R.layout.fragment_bet_list, container, false)*/
            FragmentBetListBinding.inflate(layoutInflater)
//        binding.apply {
////            gameViewModel = this@BetListFragment.viewModel
//        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        initDiscount()
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
        rv_bet_list.adapter = null
    }

    private fun initDiscount() {
        discount = viewModel.userInfo.value?.discount ?: 1.0F
    }

    private fun initView() {
        initBtnView()
        initBtnEvent()
        initTabLayout()
        initRecyclerView()
        initToolBar()

        ll_root.setOnClickListener {
//            betListRefactorAdapter?.closeAllKeyboard()
//            betSingleListAdapter?.closeAllKeyboard()
//            betParlayListRefactorAdapter?.closeAllKeyboard()
            onBackPressed()
        }
        parlayLayout.setOnClickListener {
            println("点击底部区域")
        }
//        binding.clTitle.tvBalance.text = TextUtil.formatMoney(0.0)
        binding.tvBalance.text = "${sConfigData?.systemCurrencySign}${TextUtil.formatMoney(0.0)}"
        binding.clTitle.ivArrow.rotation = 180f //注單開啟後，箭頭朝下

        //設定本金, 可贏的systemCurrencySign
        binding.apply {
//            titleAllBet.text = getString(R.string.total_capital, sConfigData?.systemCurrencySign)
//            titleWinnableAmount.text = getString(R.string.total_win_amount)
        }
    }

    private fun initBtnView() {
        //點背景dismiss
        binding.bgDimMount.setOnClickListener {
            onBackPressed()
        }
    }


    private fun onBackPressed() {
        if (currentBetType == SINGLE) {
            clearCarts()
        } else {
            activity?.onBackPressed()
        }
    }


    private fun initBtnEvent() {
        binding.btnBet.apply {
            tv_login.setOnClickListener {
                needUpdateBetLimit = true
                MultiLanguagesApplication.mInstance.doNotReStartPublicity = true
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }

            cl_bet.setOnClickListener {
                if (mIsEnabled) {
                    avoidFastDoubleClick()
                    addBet()
                }
            }

            tv_remove_closed_selections.setOnClickListener { removeClosedPlat() }
        }
    }

    private fun initTabLayout() {
        val type = BetInfoRepository.currentStateSingleOrParlay
        Timber.d("currentStateSingleOrParlay:${type}")
        btnParlaySingle.text = if (type == 0) {
            getString(R.string.bet_parlay)
        } else {
            refreshLlMoreOption()
            getString(R.string.bet_single)
        }
    }

    /**
     * 檢查是否顯示填充單注or串關layout
     */
    private fun checkSingleAndParlayBetLayoutVisible() {
        binding.apply {
//            clSingleList.isVisible = getCurrentBetList().size > 1 && currentBetType == 0
//            if (currentBetType == 1) refreshLlMoreOption()
        }
    }

    private fun refreshLlMoreOption(showParlayList: Boolean = true) {
        binding.apply {
            /**
             * @since 只有一張投注單時 串關資料會存在一筆parlayType為1C1的資料
             * @since 投注單無法串關時 串關資料為空(parlayList), 經處理會塞入一項資料(singleParlayList)作為"單項投注"填充所有單注使用
             * @see org.cxct.sportlottery.util.parlaylimit.ParlayLimitUtil.getCom
             * @see singleParlayList
             */
            val currentParlayList = getCurrentParlayList()
            if (currentParlayList.isEmpty()) return@apply
            Timber.d("currentParlayList.size():${currentParlayList.size}")
            if (currentParlayList.any {
                    val isEmpty = it.parlayType.isNotEmpty()
                    val parlayType = it.parlayType != "1C1"
                    isEmpty && parlayType
                }) {
                if (showParlayList) {
                    clParlayList.visibility = View.VISIBLE
                }
            } else {
                clParlayList.visibility = View.GONE
            }
        }
    }

    private fun initRecyclerView() {
        initAdapter()

        //单项投注项
//        val singleLayoutManager =
//            ScrollCenterLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//        rv_single_list.layoutManager = singleLayoutManager
//        (rv_single_list.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
//
//        betSingleListAdapter?.setHasStableIds(true)
//        rv_single_list.adapter = betSingleListAdapter

        //串关投注项
        val layoutManager = ScrollCenterLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv_bet_list.layoutManager = layoutManager
        betListRefactorAdapter?.setHasStableIds(true)
        (rv_bet_list.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rv_bet_list.adapter = betListRefactorAdapter

        //生成注单项的item
        val parlayLayoutManager =
            ScrollCenterLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv_parlay_list.layoutManager = parlayLayoutManager
        (rv_parlay_list.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        betParlayListRefactorAdapter?.setHasStableIds(true)
        rv_parlay_list.adapter = betParlayListRefactorAdapter

        if (BetInfoRepository.currentStateSingleOrParlay == 0) {
            currentBetType = 0
            betListRefactorAdapter?.adapterBetType = BetListRefactorAdapter.BetRvType.SINGLE
            binding.apply {
                clParlayList.visibility = View.GONE
                clTitle.ivArrow.setImageResource(R.drawable.ic_single_bet_delete)
            }

        } else {
            currentBetType = 1
            betListRefactorAdapter?.adapterBetType = BetListRefactorAdapter.BetRvType.PARLAY_SINGLE
            refreshLlMoreOption()
            binding.clTitle.ivArrow.setImageResource(
                R.drawable.ic_arrow_up_double
            )
        }
        checkAllAmountCanBet()
        refreshAllAmount()
        checkSingleAndParlayBetLayoutVisible()

        clExpandOrStacked.setOnClickListener {
            if (isOpen) {
                tvExpandOrStacked.text = getString(R.string.expand_more_combinations)
                tvExpandOrStacked.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_arrow_down_blue, null),
                    null
                )
            } else {
                betParlayListRefactorAdapter?.closeAllKeyboard()
                tvExpandOrStacked.text = getString(R.string.stacked_combination)
                tvExpandOrStacked.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_arrow_up_blue, null),
                    null
                )

            }
            betParlayListRefactorAdapter?.apply {
                BetListRcvUtil.setFitHeight(isOpen, rv_parlay_list, this)
            }
            isOpen = !isOpen
        }

        //串关赔率的接受任何赔率变化
        val userInfo = MultiLanguagesApplication.getInstance()?.userInfo()
        val currentOddsChangeOp = userInfo?.oddsChangeOption ?: 0
        currentBetOption = currentOddsChangeOp

        binding.tvDeleteAll.setOnClickListener {
            clearCarts()
        }

        btnParlaySingle.setOnClickListener {
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

        binding.clTitle.ivArrow.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initAdapter() {
        val adapterItemClickListener = object : OnItemClickListener {
            override fun onDeleteClick(oddsId: String, currentItemCount: Int) {
                betListRefactorAdapter?.closeAllKeyboard()
                betSingleListAdapter?.closeAllKeyboard()
                betParlayListRefactorAdapter?.closeAllKeyboard()
                viewModel.removeBetInfoItem(oddsId)
            }

            override fun onRechargeClick() {
                if (viewModel.getLoginBoolean()) {
                    startActivity(Intent(context, MoneyRechargeActivity::class.java))
                } else {
                    startActivity(Intent(context, LoginActivity::class.java))
                }
            }

            override fun onShowKeyboard(position: Int) {
                (rv_bet_list.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
                    rv_bet_list, RecyclerView.State(), position
                )
            }

            override fun onShowParlayKeyboard(position: Int) {
                (rv_parlay_list.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
                    rv_parlay_list, RecyclerView.State(), position
                )
                betParlayListRefactorAdapter?.apply {
                    BetListRcvUtil.setWrapHeight(rv_parlay_list, this)
                }
            }

            override fun onHideKeyBoard() {
                betListRefactorAdapter?.betList?.forEach {
                    it.isInputBet = false; it.isInputWin = false
                }
                betListRefactorAdapter?.closeAllKeyboard()
                betSingleListAdapter?.closeAllKeyboard()
                betParlayListRefactorAdapter?.closeAllKeyboard()
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
//                    btn_bet.isOddsChanged = false //輸入金額一樣顯示接受的文案
            }

            override fun showParlayRule(parlayType: String, parlayRule: String) {
                showParlayDescription(parlayType, parlayRule)
            }

            override fun onMoreOptionClick() {
                betListRefactorAdapter?.itemCount?.let {
                    rv_bet_list?.scrollToPosition(it - 1)
                }
            }


            override fun onOddsChangeAcceptSelect(tvTextSelect: TextView) {
                try {
                    tvTextSelect.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_arrow_up_blue, null),
                        null
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
        }

        betListRefactorAdapter =
            BetListRefactorAdapter(adapterItemClickListener) { getUserBalance() }
        betSingleListAdapter = BetSingleListAdapter(adapterItemClickListener)
        betParlayListRefactorAdapter =
            BetListRefactorAdapter(adapterItemClickListener) { getUserBalance() }.apply {
                adapterBetType = BetListRefactorAdapter.BetRvType.PARLAY
            }
    }

    private fun checkAllAmountCanBet() {
        val betList = getCurrentBetList()
        val parlayList = getCurrentParlayList()
        //僅判斷對應tab裡的amountError
        if (currentBetType == 0) {
            betList.forEach {
                if (it.amountError) {
                    btn_bet.amountCanBet = false
                    return
                }
            }
        } else {
            parlayList.forEach {
                if (it.amountError) {
                    btn_bet.amountCanBet = false
                    return
                }
            }
        }
        btn_bet.amountCanBet = true
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

        binding.apply {
            val totalBetString = TextUtil.formatForOdd(totalBetAmount)
            val winnableString = TextUtil.formatForOdd(winnableAmount)
            //region 依照語系or長度自動換行
//            val needChangeLineLength = 7
//            if (LanguageManager.getSelectLanguage(context) == LanguageManager.Language.VI) {
//                llTotalStake.orientation = LinearLayout.VERTICAL
//                llEstWinning.orientation = LinearLayout.VERTICAL
//                llEstWinning.gravity = Gravity.START
//            } else {
//                llTotalStake.orientation = LinearLayout.HORIZONTAL
//                llEstWinning.orientation = LinearLayout.HORIZONTAL
//                llEstWinning.gravity = Gravity.END
//            }
            //endregion
//            tvTotalBetAmount.text = TextUtil.formatForOdd(totalBetAmount)
//            tvTotalWinnableAmount.text =
//                "${sConfigData?.systemCurrencySign} ${TextUtil.formatForOdd(winnableAmount)}"
        }

        val betCount = if (currentBetType == 0) {
            list.count { it.betAmount > 0 }
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

    private fun getComboWinnable(betAmount: Double, odds: Double, num: Int): Double {
        var winnable = betAmount.toBigDecimal().multiply(odds.toBigDecimal())
        return winnable.subtract(betAmount.toBigDecimal().multiply(num.toBigDecimal())).toDouble()
    }


    private fun clearCarts() {
        if (mIsEnabled) {
            avoidFastDoubleClick()
            viewModel.betInfoList.removeObservers(viewLifecycleOwner)
            viewModel.removeBetInfoAll()
            setCurrentBetModeSingle()
            EventBusUtil.post(BetModeChangeEvent(SINGLE))
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    private fun setCurrentBetModeSingle() {
        currentBetType = SINGLE
        BetInfoRepository.setCurrentBetState(SINGLE)
        betListRefactorAdapter?.adapterBetType = BetListRefactorAdapter.BetRvType.SINGLE
    }

    private fun switchCurrentBetMode() {
        if (mIsEnabled) {
            avoidFastDoubleClick()
            if (btnParlaySingle.text == getString(R.string.bet_single)) {
                //玩法变成单注
                currentBetType = SINGLE
                BetInfoRepository.setCurrentBetState(SINGLE)
                //按钮变成串关
                btnParlaySingle.text = getString(R.string.bet_parlay)
            } else {
                currentBetType = PARLAY
                BetInfoRepository.setCurrentBetState(PARLAY)
                btnParlaySingle.text = getString(R.string.bet_single)
            }
//            betListRefactorAdapter?.closeAllKeyboard()
//            betSingleListAdapter?.closeAllKeyboard()
//            betParlayListRefactorAdapter?.closeAllKeyboard()
            when (currentBetType) {
                //單項投注
                SINGLE -> {
                    betListRefactorAdapter?.adapterBetType = BetListRefactorAdapter.BetRvType.SINGLE
                    binding.clParlayList.gone()
//                    binding.clTotalInfo.gone()
                    binding.clTitle.ivArrow.setImageResource(
                        R.drawable.ic_single_bet_delete
                    )
                    BetInfoRepository.switchSingleMode()
                    EventBusUtil.post(BetModeChangeEvent(SINGLE))
                }
                //串關投注
                PARLAY -> {
                    betListRefactorAdapter?.adapterBetType =
                        BetListRefactorAdapter.BetRvType.PARLAY_SINGLE
//                    binding.clTotalInfo.gone()
                    binding.clTitle.ivArrow.setImageResource(
                        R.drawable.ic_arrow_up_double
                    )
                    refreshLlMoreOption()
                    BetInfoRepository.switchParlayMode()
                    //从单关切换成串关会收起购物车，反之不会
                    activity?.supportFragmentManager?.popBackStack()
                    EventBusUtil.post(BetModeChangeEvent(PARLAY))
                }
            }
            betListRefactorAdapter?.notifyDataSetChanged()
            checkAllAmountCanBet()
            refreshAllAmount()
            checkSingleAndParlayBetLayoutVisible()
        }
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
                viewModel.getMoney()
                viewModel.updateBetLimit()
                needUpdateBetLimit = false
            }
            betListRefactorAdapter?.userLogin = it
            betSingleListAdapter?.userLogin = it
            betParlayListRefactorAdapter?.userLogin = it
        }


        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.let { money ->
//                binding.clTitle.tvBalance.text = TextUtil.formatMoney(money)
//                binding.clTitle.tvBalanceCurrency.text = sConfigData?.systemCurrencySign
                binding.tvBalance.text =
                    "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(money)}"
                betListRefactorAdapter?.userMoney = money
                betSingleListAdapter?.userMoney = money
                betParlayListRefactorAdapter?.userMoney = money
            }
        }

        viewModel.oddsType.observe(viewLifecycleOwner) {
            betListRefactorAdapter?.oddsType = it
            betSingleListAdapter?.oddsType = it
            betParlayListRefactorAdapter?.oddsType = it
            oddsType = it
        }

        viewModel.userInfo.observe(viewLifecycleOwner) {
            it?.discount?.let { newDiscount ->
                if (discount == newDiscount) return@observe

                viewModel.updateBetInfoDiscount(discount, newDiscount)
                discount = newDiscount
            }
        }

        viewModel.betInfoList.observe(viewLifecycleOwner) {
            it.peekContent().let { list ->
                //注單列表沒東西時關閉fragment
                if (list.size == 0) {
                    setCurrentBetModeSingle()
                    activity?.supportFragmentManager?.popBackStack()
                    return@observe
                }
                //依照注單數量動態調整高度
                if (list.size == 1) {
                    //單一注單
                    binding.llRoot.layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                } else {
                    //多筆注單
                    binding.llRoot.layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
                }
                if (list.isNotEmpty()) {
                    binding.clTitle.tvBetListCount.text = list.size.toString()
                    betListRefactorAdapter?.betList = list
                    betSingleListAdapter?.betList = list
                    betParlayListRefactorAdapter?.betList = list

                    checkSingleAndParlayBetLayoutVisible()

                    subscribeChannel(list)
                    refreshAllAmount(list)
                    checkAllAmountCanBet()
                }
            }
        }

        //移除注單解除訂閱
        viewModel.betInfoRepository.removeItem.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                unSubscribeChannelEvent(it)
            }
            betListRefactorAdapter?.notifyDataSetChanged()
            betSingleListAdapter?.notifyDataSetChanged()
            betParlayListRefactorAdapter?.notifyDataSetChanged()
        }

        //串關列表
        viewModel.parlayList.observe(this.viewLifecycleOwner) {
            if (it.size == 0) {
                clExpandOrStacked.gone()
                betListRefactorAdapter?.hasParlayList = false
                betListRefactorAdapter?.parlayList = singleParlayList
                betSingleListAdapter?.parlayList = singleParlayList
                betParlayListRefactorAdapter?.hasParlayList = false
            } else {
                if (it.size > 1) {
                    clExpandOrStacked.visible()
                } else {
                    clExpandOrStacked.gone()
                }
                betListRefactorAdapter?.hasParlayList = true
                betListRefactorAdapter?.parlayList = it
                betSingleListAdapter?.hasParlayList = true
                betSingleListAdapter?.parlayList = it

                betParlayListRefactorAdapter?.hasParlayList = true
                betParlayListRefactorAdapter?.parlayList = it
            }
            binding.clParlayList.requestLayout()
        }

        viewModel.betParlaySuccess.observe(viewLifecycleOwner) {
            showHideCantParlayWarn(!it)
        }

        //投注結果
        viewModel.betAddResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled().let { result ->
                showReceipt = result != null
                if (result != null) {
                    Timber.d("失败了进来了：${result.success}")
                    if (result.success) {
                        setBetLoadingVisibility(false)
                        //多筆和單筆投注單，下注成功後的行為不同
//                        if (isMultiBet) {
                        //多筆的是直接 replace fragment
                        viewModel.betInfoList.removeObservers(this.viewLifecycleOwner)
//                        } else {
//                            //單筆的要關掉再顯示 dialog
//                        }
                        betResultListener?.onBetResult(
                            result.receipt, betParlayList ?: listOf(), true
                        )
                        refreshAllAmount()
                        showOddChangeWarn = false
                        btn_bet.isOddsChanged = false
                    } else {
                        btn_bet?.postDelayed({
                            setBetLoadingVisibility(false)
                        }, 800)
//                        showErrorPromptDialog(getString(R.string.prompt), result.msg) {}
                    }
                }
            }
        }

        //賠率變更提示
        viewModel.showOddsChangeWarn.observe(this.viewLifecycleOwner) {
            showOddChangeWarn = it
            btn_bet.isOddsChanged = it
            Timber.d("isShow: showOddsChangeWarn:$it")
        }

        //盤口關閉提示
        viewModel.showOddsCloseWarn.observe(this.viewLifecycleOwner) {
            btn_bet.hasPlatClose = it
            showPlatCloseWarn = it
        }

        viewModel.hasBetPlatClose.observe(this.viewLifecycleOwner) {
            btn_bet.hasBetPlatClose = it
        }

    }

    private fun initSocketObserver() {
        receiver.matchStatusChange.observe(viewLifecycleOwner) {
            it?.let {
                if (it.matchStatusCO?.status == StatusType.END_GAME.code) {
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
        }

        receiver.matchOddsLock.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsLockEvent ->
                viewModel.updateLockMatchOdd(matchOddsLockEvent)
            }
        }

        receiver.globalStop.observe(this.viewLifecycleOwner) {
            it?.let { globalStopEvent ->
                val betRefactorList = betListRefactorAdapter?.betList
                betRefactorList?.forEach { listData ->
                    if (globalStopEvent.producerId == null || listData.matchOdd.producerId == globalStopEvent.producerId) {
                        listData.matchOdd.status = BetStatus.LOCKED.code
                    }
                }
                betListRefactorAdapter?.betList = betRefactorList
                betSingleListAdapter?.betList = betRefactorList
                betParlayListRefactorAdapter?.betList = betRefactorList
            }
        }

        receiver.producerUp.observe(this.viewLifecycleOwner) {
            it?.let {
                betListRefactorAdapter?.betList.let { list ->
                    betListPageUnSubScribeEvent()
                    list?.let { listNotNull ->
                        unsubscribeChannel(listNotNull)
                        subscribeChannel(listNotNull)
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

        //只取得對應tab內的totalBetAmount
        val totalBetAmount = if (currentBetType == 0) {
            betListFilter.sumOf { it.realAmount }
        } else {
            parlayList.sumOf { it.betAmount * it.num }
        }
//        val totalBetAmount =
//            betListFilter.sumByDouble { it.realAmount } + (parlayList.sumByDouble { it.betAmount * it.num })

        if (totalBetAmount.toString().isEmpty()) {
            Timber.w("totalBetAmount isEmpty")
            return
        }

        //下注總金額大於用戶餘額，提示餘額不足
        if (totalBetAmount > (viewModel.userMoney.value ?: 0.0)) {
            setBetLoadingVisibility(false)
            showErrorPromptDialog(
                getString(R.string.prompt), getString(R.string.bet_info_bet_balance_insufficient)
            ) {}
            return
        }
        MultiLanguagesApplication.mInstance.mOddsType.value?.let {
            oddsType = it
        }
        viewModel.addBetList(
            getCurrentBetList(), parlayList, oddsType, currentBetType, currentBetOption
        )
    }

    override fun onResume() {
        super.onResume()
        refreshAllAmount()
        checkAllAmountCanBet()
    }

    /**
     * 是否顯示 betLoading
     */
    private fun setBetLoadingVisibility(
        isVisible: Boolean, keepShowingBetLoading: Boolean = false
    ) {
        binding.apply {
            blockTouchView.isVisible = isVisible
            if (keepShowingBetLoading) {
                betLoadingView.isVisible = true
            } else {
                betLoadingView.isVisible = isVisible
            }
        }
    }

    private fun MutableList<BetInfoListData>.isEmptyBetList(): Boolean {
        return if (this.isEmpty() && !showReceipt) {
            activity?.supportFragmentManager?.popBackStack()
            true
        } else {
            false
        }
    }

    /**
     * 投注按鈕狀態(登入、未登入)
     */
    private fun setupBetButtonType(isLogin: Boolean) {
        btn_bet.isLogin = isLogin
    }

    /**
     * 移除盤口關閉的投注選項
     */
    private fun removeClosedPlat() {
        viewModel.removeClosedPlatBetInfo()
    }

    private fun queryData() {
        //獲取餘額
        viewModel.getMoney()
    }

    /**
     * 顯示串關說明
     */
    private fun showParlayDescription(parlayType: String, parlayRule: String) {
        val bottomSheetView =
            layoutInflater.inflate(R.layout.bottom_sheet_dialog_parlay_description, null)
        val dialog = BottomSheetDialog(context ?: requireContext())
        dialog.apply {
            setContentView(bottomSheetView)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            view.apply {
                tv_parlay_type.text =
                    getParlayStringRes(parlayType)?.let { context.getString(it) } ?: ""
                tv_parlay_rule.text = parlayRule
            }
            btn_close.setOnClickListener {
                dismiss()
            }
            show()
        }
    }


    /**
     * 同賽事不能串關提示
     * @param show true:顯示, false:隱藏
     * @如果資料只有一筆，也不用顯示
     * @反波膽也不能串關
     */
    private fun showHideCantParlayWarn(show: Boolean) {
        //TODO 現在只有串關投注才會顯示次提示
//        if (show && (betListRefactorAdapter?.betList?.size ?: 0) > 1) {
//            llParlayWarn.visible()
//        } else {
//            llParlayWarn.gone()
//        }

        when (currentBetType) {
            //單項投注
            SINGLE -> {
                binding.clParlayList.gone()
//                binding.clTotalInfo.gone()
            }
            //串關投注
            PARLAY -> {
                refreshLlMoreOption()
//                binding.clTotalInfo.gone()
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

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (enter) {
            bg_dim_mount.animate().alphaBy(1f).setDuration(200).setStartDelay(200).start()
        } else {
            bg_dim_mount.alpha = 0f
        }
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

}