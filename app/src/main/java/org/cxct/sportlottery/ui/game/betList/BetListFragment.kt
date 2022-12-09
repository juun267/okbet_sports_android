package org.cxct.sportlottery.ui.game.betList

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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.bottom_sheet_dialog_parlay_description.btn_close
import kotlinx.android.synthetic.main.bottom_sheet_dialog_parlay_description.tv_parlay_rule
import kotlinx.android.synthetic.main.bottom_sheet_dialog_parlay_description.tv_parlay_type
import kotlinx.android.synthetic.main.button_bet.view.cl_bet
import kotlinx.android.synthetic.main.button_bet.view.tv_login
import kotlinx.android.synthetic.main.button_bet.view.tv_remove_closed_selections
import kotlinx.android.synthetic.main.fragment_bank_card.eet_wallet
import kotlinx.android.synthetic.main.fragment_bet_list.bg_dim_mount
import kotlinx.android.synthetic.main.fragment_bet_list.btnParlaySingle
import kotlinx.android.synthetic.main.fragment_bet_list.btn_bet
import kotlinx.android.synthetic.main.fragment_bet_list.ll_root
import kotlinx.android.synthetic.main.fragment_bet_list.rv_bet_list
import kotlinx.android.synthetic.main.fragment_bet_list.rv_parlay_list
import kotlinx.android.synthetic.main.fragment_bet_list.rv_single_list
import kotlinx.android.synthetic.main.fragment_bet_list.tvExpandOrStacked
import kotlinx.android.synthetic.main.include_bet_odds_tips_parlay.btnOddsChangeDes
import kotlinx.android.synthetic.main.include_bet_odds_tips_parlay.ivClearCarts
import kotlinx.android.synthetic.main.include_bet_odds_tips_parlay.tvAcceptOddsChange
import kotlinx.android.synthetic.main.include_bet_odds_tips_parlay.view.tvOddsChangedTips
import kotlinx.android.synthetic.main.snackbar_login_notify.view.tv_notify
import kotlinx.android.synthetic.main.snackbar_my_favorite_notify.view.txv_title
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetListBinding
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.extentions.gone
import org.cxct.sportlottery.extentions.visible
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType
import org.cxct.sportlottery.repository.BetInfoRepository
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.ScrollCenterLayoutManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.results.StatusType
import org.cxct.sportlottery.ui.transactionStatus.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LocalUtils
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds
import org.cxct.sportlottery.util.observe
import org.cxct.sportlottery.widget.OkPopupWindow
import timber.log.Timber

/**
 * @app_destination 滿版注單(點擊賠率彈出)
 *
 * 畫面會依照注單數量(viewModel.betInfoList)動態調整高度
 * if (size == 1) { 單一注單 } else { 多筆注單 or 空注單 }
 */
class BetListFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {
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

//    private var tabPosition = 0 //tab的位置

    private var needUpdateBetLimit = false //是否需要更新投注限額

    private var isOpen = false //记录注单框展开收起状态

    private var currentBetType: Int = 0

    /**
     * 当前所选赔率
     * 0：不接受更新赔率
     * 1：接受更新赔率
     * 2：接受更好的赔率
     */
    private var currentBetOption = 0


    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BET_CONFIRM_TIPS -> {
                    val spannableStringBuilder = SpannableStringBuilder()
                    val text1 = SpannableString(LocalUtils.getString(R.string.text_bet_not_success))
                    val text2 = SpannableString(getString(R.string.waiting))
                    val foregroundSpan = ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireContext(), R.color.color_F75452_E23434
                        )
                    )
                    text2.setSpan(foregroundSpan, 0, text2.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    val text3 = SpannableString(getString(R.string.text_bet_not_success3))
                    val text4 = SpannableString(getString(R.string.label_transaction_status))
                    val foregroundSpan2 = ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireContext(), R.color.color_F75452_E23434
                        )
                    )
                    text4.setSpan(
                        foregroundSpan2, 0, text4.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannableStringBuilder.append(text1)
                    spannableStringBuilder.append(text2)
                    spannableStringBuilder.append(text3)
                    spannableStringBuilder.append(text4)
                    showPromptDialog(
                        title = getString(R.string.prompt),
                        message = spannableStringBuilder,
                        success = true
                    ) {
//                        viewModel.navTranStatus()

                    }
                }
            }
        }
    }

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

    private val deleteAllLayoutAnimationListener by lazy {
        /*object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                binding.apply {
                    llDeleteAll.visibility = View.GONE
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

        }*/
    }

    //提示
    private var snackBarNotify: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bet_list, container, false)
        binding.apply {
            gameViewModel = this@BetListFragment.viewModel
        }

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
            betListRefactorAdapter?.closeAllKeyboard()
            betSingleListAdapter?.closeAllKeyboard()
            betParlayListRefactorAdapter?.closeAllKeyboard()
        }
        binding.clTitle.tvBalance.text = TextUtil.formatMoney(0.0)
        binding.clTitle.ivArrow.rotation = 180f //注單開啟後，箭頭朝下

        //設定本金, 可贏的systemCurrencySign
        binding.apply {
            titleAllBet.text = getString(R.string.total_capital, sConfigData?.systemCurrencySign)
            titleWinnableAmount.text =
                getString(R.string.total_win_amount, sConfigData?.systemCurrencySign)
        }
    }

    private fun initBtnView() {
        //點背景dismiss
        binding.bgDimMount.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
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

        /*ll_odds_close_warn.setOnClickListener {
            removeClosedPlat()
        }*/
    }

    private fun initTabLayout() {
        with(binding.betTypeTabLayout) {
            for (index in 0 until tabCount) {
                val tab = getTabAt(index)
                val tvType = tab?.customView?.findViewById<TextView>(R.id.tvType)
                val divider = tab?.customView?.findViewById<View>(R.id.divider)
                when (index) {
                    0 -> {
                        tvType?.text = getString(R.string.bet_list_single_type)
                    }

                    1 -> {
                        tvType?.text = getString(R.string.bet_list_parlay_type)
                    }
                }

                //隱藏最後一個Tab後的divider
                divider?.visibility = when (index) {
                    tabCount - 1 -> {
                        View.GONE
                    }

                    else -> {
                        View.VISIBLE
                    }
                }
            }
        }
        val type = BetInfoRepository.currentStateSingleOrParlay
        Timber.d("currentStateSingleOrParlay:${type}")
        btnParlaySingle.text = if (type == 0) {
            getString(R.string.bet_parlay)
        } else {
            refreshLlMoreOption()
            getString(R.string.bet_single)
        }

        val lastTab = binding.betTypeTabLayout.getTabAt(binding.betTypeTabLayout.tabCount - 1)
        val tabDivider = lastTab?.customView?.findViewById<View>(R.id.divider)
        tabDivider?.visibility = View.GONE

//        binding.betTypeTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                betListRefactorAdapter?.closeAllKeyboard()
//                betSingleListAdapter?.closeAllKeyboard()
//                betParlayListRefactorAdapter?.closeAllKeyboard()
//                when (tab?.position) {
//                    //單項投注
//                    0 -> {
//                        tabPosition = 0
//                        currentBetType = 0
//                        betListRefactorAdapter?.adapterBetType =
//                            BetListRefactorAdapter.BetRvType.SINGLE
//                        binding.apply {
//                            clParlayList.visibility = View.GONE
//                        }
//                        checkAllAmountCanBet()
//                        refreshAllAmount()
//                    }
//                    //串關投注
//                    1 -> {
//                        tabPosition = 1
//                        currentBetType = 1
//                        betListRefactorAdapter?.adapterBetType =
//                            BetListRefactorAdapter.BetRvType.PARLAY_SINGLE
//                        refreshLlMoreOption()
//                        checkAllAmountCanBet()
//                        refreshAllAmount()
//                    }
//                }
//                checkSingleAndParlayBetLayoutVisible()
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {
//            }
//
//            override fun onTabReselected(tab: TabLayout.Tab?) {
//            }
//
//        })
    }

    /**
     * 檢查是否顯示填充單注or串關layout
     */
    private fun checkSingleAndParlayBetLayoutVisible() {
        binding.apply {
            clSingleList.isVisible = getCurrentBetList().size > 1 && currentBetType == 0
            if (currentBetType == 1) refreshLlMoreOption()
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

        val layoutManager = ScrollCenterLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv_bet_list.layoutManager = layoutManager
        betListRefactorAdapter?.setHasStableIds(true)
        rv_bet_list.adapter = betListRefactorAdapter

        val singleLayoutManager =
            ScrollCenterLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv_single_list.layoutManager = singleLayoutManager
        betSingleListAdapter?.setHasStableIds(true)
        rv_single_list.adapter = betSingleListAdapter

        val parlayLayoutManager =
            ScrollCenterLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv_parlay_list.layoutManager = parlayLayoutManager
        betParlayListRefactorAdapter?.setHasStableIds(true)
        rv_parlay_list.adapter = betParlayListRefactorAdapter

        if (BetInfoRepository.currentStateSingleOrParlay == 0) {
            currentBetType = 0
            betListRefactorAdapter?.adapterBetType = BetListRefactorAdapter.BetRvType.SINGLE
            binding.apply {
                clParlayList.visibility = View.GONE
            }
        } else {
            currentBetType = 1
            betListRefactorAdapter?.adapterBetType = BetListRefactorAdapter.BetRvType.PARLAY_SINGLE
            refreshLlMoreOption()
        }
        checkAllAmountCanBet()
        refreshAllAmount()
        checkSingleAndParlayBetLayoutVisible()
        btnOddsChangeDes.setOnClickListener {
            showOddsChangeTips()
        }
        tvExpandOrStacked.setOnClickListener {
            if (isOpen) {
                tvExpandOrStacked.text = getString(R.string.expand_more_combinations)
                tvExpandOrStacked.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_arrow_down_blue, null),
                    null
                )
            } else {
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

        tvAcceptOddsChange.setOnClickListener {
            tvAcceptOddsChange.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                ResourcesCompat.getDrawable(resources, R.drawable.ic_arrow_up_blue, null),
                null
            )
            val popupWindow = OkPopupWindow(
                requireContext(), tvAcceptOddsChange.text.toString()
            ) { text, position ->
                tvAcceptOddsChange.text = text
                currentBetOption = position
            }
            popupWindow.setOnDismissListener {
                tvAcceptOddsChange.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_arrow_down_blue, null),
                    null
                )
            }
            popupWindow.showUpCenter(it)
        }


        ivClearCarts.setOnClickListener {
            clearCarts()
        }

        btnParlaySingle.setOnClickListener {
            switchCurrentBetMode()
        }
    }


    private fun initToolBar() {
        binding.clTitle.root.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.clTitle.ivArrow.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.clTitle.tvBalanceCurrency.text = sConfigData?.systemCurrencySign
    }

    private fun initAdapter() {
        val adapterItemClickListener = object : BetListRefactorAdapter.OnItemClickListener {
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

            override fun onOddsChangesAcceptTips() {
                showOddsChangeTips()
            }

            override fun onOddsChangeAcceptSelect(tvTextSelect: TextView) {
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
                }
                popupWindow.setOnDismissListener {
                    tvTextSelect.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_arrow_down_blue, null),
                        null
                    )
                }
                popupWindow.showUpCenter(tvTextSelect)
            }

            override fun onOddsChangesWarningTips(isShow: Boolean) {
                Timber.d("isShow:$isShow")
                binding.includeOddsLayout.root.visible()
                binding.includeOddsLayout.tvOddsChangedTips.isVisible = isShow
            }
        }

        betListRefactorAdapter = BetListRefactorAdapter(adapterItemClickListener)
        betSingleListAdapter = BetSingleListAdapter(adapterItemClickListener)
        betParlayListRefactorAdapter = BetListRefactorAdapter(adapterItemClickListener).apply {
            adapterBetType = BetListRefactorAdapter.BetRvType.PARLAY
        }
    }

    private fun showOddsChangeTips() {
        val dialog = CustomAlertDialog(requireContext())
        dialog.setTitle(getString(R.string.str_if_accept_odds_changes_title))
        val message = """
                    ${getString(R.string.str_if_accept_odds_changes_des_subtitle)}
                    
                    ${getString(R.string.str_if_accept_odds_changes_des1)}
                    
                    ${getString(R.string.str_if_accept_odds_changes_des2)}
                """.trimIndent()
        dialog.setMessage(message)
        dialog.setCanceledOnTouchOutside(true)
        dialog.isCancelable = true
        dialog.setNegativeButtonText(null)
        dialog.setPositiveButtonText(getString(R.string.str_ok_i_got_it))
        dialog.setPositiveClickListener {
            dialog.dismiss()
        }
        dialog.show(childFragmentManager, null)
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

//        val totalBetAmount =
//            list.sumByDouble { it.realAmount } + (parlayList.sumByDouble { it.betAmount * it.num })
//        val betCount =
//            list.count { it.betAmount > 0 } + parlayList.filter { it.betAmount > 0 }
//                .sumBy { it.num }
//        val winnableAmount = list.sumByDouble {
//            var currentOddsType = oddsType
//            if (it.matchOdd.odds == it.matchOdd.malayOdds
//                || it.matchType == MatchType.OUTRIGHT
//                || it.matchType == MatchType.OTHER_OUTRIGHT
//            ) {
//                currentOddsType = OddsType.EU
//            }
//            getWinnable(it.betAmount, getOddsNew(it.matchOdd, currentOddsType), currentOddsType)
//        } + parlayList.sumByDouble { getComboWinnable(it.betAmount, getOdds(it, OddsType.EU), it.num) }

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
            val needChangeLineLength = 7
            if (LanguageManager.getSelectLanguage(context) == LanguageManager.Language.VI) {
                llTotalStake.orientation = LinearLayout.VERTICAL
                llEstWinning.orientation = LinearLayout.VERTICAL
                llEstWinning.gravity = Gravity.START
            } else {
                llTotalStake.orientation = LinearLayout.HORIZONTAL
                llEstWinning.orientation = LinearLayout.HORIZONTAL
                llEstWinning.gravity = Gravity.END
            }
            //endregion
            tvTotalBetAmount.text = TextUtil.formatForOdd(totalBetAmount)
            tvTotalWinnableAmount.text = TextUtil.formatForOdd(winnableAmount)
        }

        val betCount = if (currentBetType == 0) {
            list.count { it.betAmount > 0 }
        } else {
            parlayList.filter { it.betAmount > 0 }.sumOf { it.num }
        }
        binding.btnBet.apply {
            isParlay = currentBetType == 1
            betCounts = betCount
        }

        betAllAmount = totalBetAmount
    }

    private fun getWinnable(betAmount: Double, odds: Double, oddsType: OddsType): Double {
        var winnable = 0.0
        when (oddsType) {
            OddsType.MYS -> {
                winnable = if (odds < 0) {
                    betAmount
                } else {
                    betAmount * odds
                }
            }

            OddsType.IDN -> {
                winnable = if (odds < 0) {
                    betAmount
                } else {
                    betAmount * odds
                }
            }

            OddsType.EU -> {
                winnable = betAmount * (odds - 1)
            }

            else -> {
                winnable = betAmount * odds
            }
        }

        return winnable
    }

    private fun getComboWinnable(betAmount: Double, odds: Double, num: Int): Double {
        var winnable = 0.0
        winnable = betAmount * odds
        winnable -= (betAmount * num)
        return winnable
    }


    private fun clearCarts() {
        if (mIsEnabled) {
            avoidFastDoubleClick()
            viewModel.betInfoList.removeObservers(viewLifecycleOwner)
            viewModel.removeBetInfoAll()
            activity?.supportFragmentManager?.popBackStack()
        }
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
            betListRefactorAdapter?.closeAllKeyboard()
            betSingleListAdapter?.closeAllKeyboard()
            betParlayListRefactorAdapter?.closeAllKeyboard()
            when (currentBetType) {
                //單項投注
                0 -> {
//                    tabPosition = 0
                    betListRefactorAdapter?.adapterBetType = BetListRefactorAdapter.BetRvType.SINGLE
                    binding.apply {
                        clParlayList.visibility = View.GONE
                    }
                    BetInfoRepository.switchSingleMode()
                }
                //串關投注
                1 -> {
//                    tabPosition = 1
                    betListRefactorAdapter?.adapterBetType =
                        BetListRefactorAdapter.BetRvType.PARLAY_SINGLE
                    refreshLlMoreOption()
                    BetInfoRepository.switchParlayMode()
                }
            }
            checkAllAmountCanBet()
            refreshAllAmount()
            checkSingleAndParlayBetLayoutVisible()
        }
    }

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
                binding.clTitle.tvBalance.text = TextUtil.formatMoney(money ?: 0.0)
                betListRefactorAdapter?.userMoney = money
                betSingleListAdapter?.userMoney = money
                betParlayListRefactorAdapter?.userMoney = money
            }
        }

        viewModel.oddsType.observe(viewLifecycleOwner) {
            //keyboard?.hideKeyboard()
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
                    activity?.supportFragmentManager?.popBackStack()
                    return@observe
                }
//                btnParlaySingle.text = getString(
//                    if (BetInfoRepository.currentStateSingleOrParlay == 0) {
//                        R.string.bet_single
//                    } else {
//                        R.string.bet_parlay
//                    }
//                )
                //依照注單數量動態調整高度
                if (list.size == 1) {
                    //單一注單
                    binding.llRoot.layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                    //上方tabBar betTypeTabLayout隱藏，下方可贏金額 clTotalInfo也隱藏
//                    binding.betTypeTabLayout.selectTab(binding.betTypeTabLayout.getTabAt(0))
//                    binding.betTypeTabLayout.isVisible = false
                    binding.clTotalInfo.isVisible = false
                } else {
                    //多筆注單
                    binding.llRoot.layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
//                    binding.betTypeTabLayout.isVisible = true
                    binding.clTotalInfo.isVisible = true
                }

//                btn_delete_all.visibility = if (list.size == 0) View.GONE else View.VISIBLE
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
                betListRefactorAdapter?.hasParlayList = false
                Timber.d("parlayList 设置数据 singleParlayList")
                betListRefactorAdapter?.parlayList = singleParlayList
                betSingleListAdapter?.parlayList = singleParlayList

                betParlayListRefactorAdapter?.hasParlayList = false
            } else {
                betListRefactorAdapter?.hasParlayList = true
                Timber.d("parlayList 设置数据 it")
                betListRefactorAdapter?.parlayList = it
                betSingleListAdapter?.hasParlayList = true
                betSingleListAdapter?.parlayList = it

                betParlayListRefactorAdapter?.hasParlayList = true
                Timber.d("parlayList 设置数据 it")
                betParlayListRefactorAdapter?.parlayList = it
            }
        }

        viewModel.betParlaySuccess.observe(viewLifecycleOwner) {
            showHideCantParlayWarn(!it)
        }

        //投注結果
        viewModel.betAddResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled().let { result ->
                showReceipt = result != null
                result?.let { resultNotNull ->
                    if (resultNotNull.success) {
                        setBetLoadingVisibility(false, keepShowingBetLoading = true)
                        //多筆和單筆投注單，下注成功後的行為不同
//                        if (isMultiBet) {
                        //多筆的是直接 replace fragment
                        viewModel.betInfoList.removeObservers(this.viewLifecycleOwner)
//                        } else {
//                            //單筆的要關掉再顯示 dialog
//                        }
                        betResultListener?.onBetResult(
                            resultNotNull.receipt, betParlayList ?: listOf(), true
                        )
                        refreshAllAmount()
                        showOddChangeWarn = false
                        btn_bet.isOddsChanged = false
                        if (result.receipt?.singleBets?.any { singleBet -> singleBet.status == 0 } == true || result.receipt?.parlayBets?.any { parlayBet -> parlayBet.status == 0 } == true) {
                            mHandler.removeMessages(BET_CONFIRM_TIPS)
                            mHandler.sendMessage(Message().apply {
                                what = BET_CONFIRM_TIPS
                            })
                        }
                    } else {
                        setBetLoadingVisibility(false)
                        showErrorPromptDialog(getString(R.string.prompt), resultNotNull.msg) {}
                    }
                }
            }
        }

        //賠率變更提示
        viewModel.showOddsChangeWarn.observe(this.viewLifecycleOwner) {
            showOddChangeWarn = it
            btn_bet.isOddsChanged = it
            Timber.d("isShow: showOddsChangeWarn:$it")
//            when (it) {
//                true -> {
////                    includeOddsLayout.visible()
////                    betParlayListRefactorAdapter?.showOddsChangedWarn()
//                }
//                false -> {
////                    includeOddsLayout.gone()
////                    betParlayListRefactorAdapter?.hideOddsChangedWarn()
//                }
//            }
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
            getCurrentBetList(), parlayList, oddsType, currentBetType,currentBetOption
        )
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
        return if (this.isNullOrEmpty() && !showReceipt) {
            activity?.supportFragmentManager?.popBackStack()
            true
        } else {
            false
        }
    }

    private fun setSnackBarNotify(
        myFavoriteNotifyType: Int? = null,
        isGameClose: Boolean? = false,
        gameType: GameType? = null,
        isLogin: Boolean? = true
    ) {
        val title = when {
            isLogin == false -> getString(R.string.login_notify)
            isGameClose == true -> String.format(
                getString(R.string.message_no_sport_game), getString(gameType?.string ?: 0)
            )

            else -> {
                when (myFavoriteNotifyType) {

                    MyFavoriteNotifyType.SPORT_ADD.code -> getString(R.string.myfavorite_notify_league_add)

                    MyFavoriteNotifyType.SPORT_REMOVE.code -> getString(R.string.myfavorite_notify_league_remove)

                    else -> ""
                }
            }
        }

        val layout =
            if (isLogin == true) R.layout.snackbar_my_favorite_notify else R.layout.snackbar_login_notify

        snackBarNotify = activity?.let {
            Snackbar.make(
                this@BetListFragment.requireView(), title, Snackbar.LENGTH_LONG
            ).apply {
                val snackView: View = layoutInflater.inflate(
                    layout, activity?.findViewById(android.R.id.content), false
                )
                if (isLogin == true) snackView.txv_title.text = title
                else snackView.tv_notify.text = title

                (this.view as Snackbar.SnackbarLayout).apply {
                    findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
                        visibility = View.INVISIBLE
                    }
                    background.alpha = 0
                    addView(snackView, 0)
                    setPadding(0, 0, 0, 0)
                }
            }
        }
        snackBarNotify?.show()
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
        if (show && betListRefactorAdapter?.betList?.size ?: 0 > 1) {
            betListRefactorAdapter?.showCantParlayWarn()
        } else {
            betListRefactorAdapter?.hideCantParlayWarn()
        }

        when (currentBetType) {
            //單項投注
            0 -> {
                with(binding) {
                    clParlayList.visibility = View.GONE
                }
            }
            //串關投注
            1 -> {
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