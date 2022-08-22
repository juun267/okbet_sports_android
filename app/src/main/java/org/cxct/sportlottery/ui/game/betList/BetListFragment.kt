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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.bottom_sheet_dialog_parlay_description.*
import kotlinx.android.synthetic.main.button_bet.view.*
import kotlinx.android.synthetic.main.fragment_bet_list.*
import kotlinx.android.synthetic.main.snackbar_login_notify.view.*
import kotlinx.android.synthetic.main.snackbar_my_favorite_notify.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetListBinding
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.common.ScrollCenterLayoutManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.results.StatusType
import org.cxct.sportlottery.ui.transactionStatus.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds

/**
 * @app_destination 滿版注單(點擊賠率彈出)
 *
 * 畫面會依照注單數量(viewModel.betInfoList)動態調整高度
 * if (size == 1) { 單一注單 } else { 多筆注單 or 空注單 }
 */
class BetListFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {
    private lateinit var binding: FragmentBetListBinding

    private var isAutoCloseWhenNoData = false

    private var oddsType: OddsType = OddsType.EU

    private var discount = 1.0F

    private var betListRefactorAdapter: BetListRefactorAdapter? = null

    private var betParlayListRefactorAdapter: BetListRefactorAdapter? = null

    private var betAllAmount = 0.0

    private var betResultListener: BetResultListener? = null

    private var showToolbar: Boolean = false

    private var betParlayList: List<ParlayOdd>? = null //紀錄投注時的串關資料

    private var showOddChangeWarn: Boolean = false //賠率是否有變更

    private var showPlatCloseWarn: Boolean = false //盤口是否被關閉

    private var showReceipt: Boolean = false

    private var tabPosition = 0 //tab的位置

    private var isMultiBet = false //是否為多筆注單

    private var needUpdateBetLimit = false //是否需要更新投注限額

    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BET_CONFIRM_TIPS -> {
                    val spannableStringBuilder = SpannableStringBuilder()
                    val text1 = SpannableString(getString(R.string.text_bet_not_success))
                    val text2 = SpannableString(getString(R.string.waiting))
                    val foregroundSpan =
                        ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.color_F75452_b73a20))
                    text2.setSpan(foregroundSpan, 0, text2.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    val text3 = SpannableString(getString(R.string.text_bet_not_success3))
                    val text4 = SpannableString(getString(R.string.label_transaction_status))
                    val foregroundSpan2 =
                        ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.color_F75452_b73a20))
                    text4.setSpan(foregroundSpan2, 0, text4.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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
        initCommonToolbar()
        initToolBar()

        ll_root.setOnClickListener {
            betListRefactorAdapter?.closeAllKeyboard()
            betParlayListRefactorAdapter?.closeAllKeyboard()
        }
        tv_balance.text = TextUtil.formatMoney(0.0)
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

            cl_bet.setOnClickListener { addBet() }

            tv_remove_closed_selections.setOnClickListener { removeClosedPlat() }
        }

        /*ll_odds_close_warn.setOnClickListener {
            removeClosedPlat()
        }*/

        binding.apply {
            llMoreOption.setOnClickListener {
                if (clParlayList.isVisible) {
                    ivArrowMoreOptions.setImageResource(R.drawable.ic_arrow_gray_top)
                    clParlayList.visibility = View.GONE
                } else {
                    tvMoreOptionsCount.text = "(${getCurrentParlayList().size})"
                    ivArrowMoreOptions.setImageResource(R.drawable.ic_arrow_gray_down)
                    clParlayList.visibility = View.VISIBLE
                }
            }
        }
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

        val lastTab = binding.betTypeTabLayout.getTabAt(binding.betTypeTabLayout.tabCount - 1)
        val tabDivider = lastTab?.customView?.findViewById<View>(R.id.divider)
        tabDivider?.visibility = View.GONE

        binding.betTypeTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                betListRefactorAdapter?.closeAllKeyboard()
                betParlayListRefactorAdapter?.closeAllKeyboard()
                when (tab?.position) {
                    //單項投注
                    0 -> {
                        tabPosition = 0
                        betListRefactorAdapter?.adapterBetType = BetListRefactorAdapter.BetRvType.SINGLE
                        binding.apply {
                            llMoreOption.visibility = View.GONE
                            clParlayList.visibility = View.GONE
                        }
                        checkAllAmountCanBet()
                        refreshAllAmount()
                    }
                    //串關投注
                    1 -> {
                        tabPosition = 1
                        betListRefactorAdapter?.adapterBetType = BetListRefactorAdapter.BetRvType.PARLAY_SINGLE
                        refreshLlMoreOption()
                        checkAllAmountCanBet()
                        refreshAllAmount()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
    }

    private fun refreshLlMoreOption(showParlayList: Boolean = true) {
        binding.apply {
            /**
             * @since 只有一張投注單時 串關資料會存在一筆parlayType為1C1的資料
             * @since 投注單無法串關時 串關資料為空(parlayList), 經處理會塞入一項資料(singleParlayList)作為"單項投注"填充所有單注使用
             * @see org.cxct.sportlottery.util.parlaylimit.ParlayLimitUtil.getCom
             * @see singleParlayList
             */
            if (getCurrentParlayList().any { it.parlayType.isNotEmpty() && it.parlayType != "1C1" }) {
                llMoreOption.visibility = View.VISIBLE
                tvMoreOptionsCount.text = "(${getCurrentParlayList().size})"
                if (showParlayList) clParlayList.visibility = View.VISIBLE
                if (clParlayList.isVisible) ivArrowMoreOptions.setImageResource(R.drawable.ic_arrow_gray_down)
                else ivArrowMoreOptions.setImageResource(R.drawable.ic_arrow_gray_top)
            } else {
                llMoreOption.visibility = View.GONE
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

        val parlayLayoutManager = ScrollCenterLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv_parlay_list.layoutManager = parlayLayoutManager
        betParlayListRefactorAdapter?.setHasStableIds(true)
        rv_parlay_list.adapter = betParlayListRefactorAdapter
        //rv_bet_list.itemAnimator = null
//        rv_bet_list.addItemDecoration(
//            DividerItemDecoration(
//                context,
//                LinearLayoutManager.VERTICAL
//            ).apply {
//                ContextCompat.getDrawable(
//                    context ?: requireContext(),
//                    R.drawable.divider_color_white8
//                )?.let {
//                    setDrawable(it)
//                }
//            })
    }

    private fun initCommonToolbar() {
        if (showToolbar) {
            //20220606 調整樣式, 注單之外無法互動
            /*with(binding.toolBar) {
                toolBar.visibility = View.VISIBLE
                ivLogo.setOnClickListener { removeBetListFragment() }
                ivNotice.setOnClickListener { clickNotice() }
                ivMenu.setOnClickListener { clickMenu() }
                ivLanguage.setImageResource(LanguageManager.getLanguageFlag(context))
                btnLogin.setOnClickListener { startActivity(Intent(context, LoginActivity::class.java)) }
                btnRegister.setOnClickListener { startActivity(Intent(context,  if (isOKPlat()) RegisterOkActivity::class.java else RegisterActivity::class.java )) }
            }*/
        }
    }

    private fun updateCommonToolbarLoginStatus(isLogin: Boolean) {
        if (!showToolbar) return

        //20220606 調整樣式, 注單之外無法互動
        /*with(binding.toolBar) {
            if (isLogin) {
                btnLogin.visibility = View.GONE
                btnRegister.visibility = View.GONE
                toolbarDivider.visibility = View.GONE
                ivHead.visibility = View.GONE
                tvOddsType.visibility = View.GONE
                ivNotice.visibility = View.VISIBLE
                ivMenu.visibility = View.VISIBLE
            } else {
                btnLogin.visibility = View.VISIBLE
                btnRegister.visibility = View.VISIBLE
                toolbarDivider.visibility = View.VISIBLE
                ivHead.visibility = View.GONE
                tvOddsType.visibility = View.GONE
                ivNotice.visibility = View.GONE
                ivMenu.visibility = View.GONE
            }
        }*/
    }

    private fun updateCommonToolbarNotice(hasNotice: Boolean) {
        //20220606 調整樣式, 注單之外無法互動
//        binding.toolBar.ivNotice.setImageResource(if (hasNotice) R.drawable.icon_bell_with_red_dot else R.drawable.icon_bell)
    }


    private fun removeBetListFragment() {
        when (activity) {
            is GamePublicityActivity -> (activity as GamePublicityActivity).removeBetListFragment()
        }
    }

    private fun clickNotice() {
        when (activity) {
            is GamePublicityActivity -> (activity as GamePublicityActivity).fragmentClickNotice()
        }
    }

    private fun clickMenu() {
        when (activity) {
            is GamePublicityActivity -> (activity as GamePublicityActivity).clickMenuEvent()
        }
    }

    private fun initToolBar() {
        binding.clTitle.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.ivArrow.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.tvBalanceCurrency.text = sConfigData?.systemCurrencySign
        initDeleteAllOnClickEvent()
    }

    private fun initAdapter() {
        val adapterItemClickListener = object : BetListRefactorAdapter.OnItemClickListener {
                override fun onDeleteClick(oddsId: String, currentItemCount: Int) {
                    isAutoCloseWhenNoData = betListRefactorAdapter?.betList?.size ?: 0 <= 1
                    betListRefactorAdapter?.closeAllKeyboard()
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
                    (rv_bet_list.layoutManager as ScrollCenterLayoutManager)
                        .smoothScrollToPosition(rv_bet_list, RecyclerView.State(), position)
                }

                override fun onShowParlayKeyboard(position: Int) {
                    (rv_parlay_list.layoutManager as ScrollCenterLayoutManager)
                        .smoothScrollToPosition(rv_parlay_list, RecyclerView.State(), position)
                }

                override fun onHideKeyBoard() {
                    betListRefactorAdapter?.betList?.forEach {
                        it.isInputBet = false; it.isInputWin = false
                    }
                    betListRefactorAdapter?.closeAllKeyboard()
                    betParlayListRefactorAdapter?.closeAllKeyboard()
                }

                override fun saveOddsHasChanged(matchOdd: MatchOdd) {
                    viewModel.saveOddsHasChanged(matchOdd)
                }

                override fun refreshBetInfoTotal() {
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
            }

        betListRefactorAdapter = BetListRefactorAdapter(adapterItemClickListener)
        betParlayListRefactorAdapter = BetListRefactorAdapter(adapterItemClickListener).apply {
            adapterBetType = BetListRefactorAdapter.BetRvType.PARLAY
        }
    }

    private fun checkAllAmountCanBet() {
        val betList = getCurrentBetList()
        val parlayList = getCurrentParlayList()
        //僅判斷對應tab裡的amountError
        if (tabPosition == 0) {
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
        val list =
            (newBetList
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
        val totalBetAmount = if (tabPosition == 0) {
            list.sumByDouble { it.realAmount }
        } else {
            parlayList.sumByDouble { it.betAmount * it.num }
        }

        val winnableAmount = if (tabPosition == 0) {
            list.sumByDouble {
                var currentOddsType = oddsType
                if (it.matchOdd.odds == it.matchOdd.malayOdds
                    || it.matchType == MatchType.OUTRIGHT
                    || it.matchType == MatchType.OTHER_OUTRIGHT
                ) {
                    currentOddsType = OddsType.EU
                }
                if (it.matchOdd.isOnlyEUType) currentOddsType = OddsType.EU
                getWinnable(it.betAmount, getOdds(it.matchOdd, currentOddsType), currentOddsType)
            }
        } else {
            parlayList.sumByDouble {
                getComboWinnable(
                    it.betAmount,
                    getOdds(it, OddsType.EU),
                    it.num
                )
            }
        }

        binding.apply {
            tvTotalBetAmount.text =
                "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(totalBetAmount)}"
            tvTotalWinnableAmount.text =
                "${sConfigData?.systemCurrencySign} ${TextUtil.formatMoney(winnableAmount)}"
        }

        val betCount = if (tabPosition == 0) {
            list.count { it.betAmount > 0 }
        } else {
            parlayList.filter { it.betAmount > 0 }.sumBy { it.num }
        }
        binding.btnBet.apply {
            isParlay = tabPosition == 1
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

    private fun initDeleteAllOnClickEvent() {
        /*val exitAnimation =
            AnimationUtils.loadAnimation(context, R.anim.pop_left_to_right_exit).apply {
                setAnimationListener(deleteAllLayoutAnimationListener)
                duration = 300
            }
        binding.apply {
            btnDeleteAll.setOnClickListener {
                val enterAnimation =
                    AnimationUtils.loadAnimation(context, R.anim.push_right_to_left_enter).apply {
                        duration = 300
                    }

                llDeleteAll.visibility = View.VISIBLE
                btnDeleteAllConfirm.startAnimation(enterAnimation)
            }
            btnDeleteAllCancel.setOnClickListener {
                btnDeleteAllConfirm.startAnimation(exitAnimation)
            }

            btnDeleteAllConfirm.setOnClickListener {
                btnDeleteAllConfirm.startAnimation(exitAnimation)
                isAutoCloseWhenNoData = true
                viewModel.removeBetInfoAll()
            }
        }*/

        binding.btnDeleteAll.setOnClickListener {
            isAutoCloseWhenNoData = true
            viewModel.removeBetInfoAll()
        }
    }

    private fun initObserver() {
        //是否登入
        viewModel.isLogin.observe(this.viewLifecycleOwner) {
            setupBetButtonType(it)
            updateCommonToolbarLoginStatus(it)
            if (needUpdateBetLimit) {
                viewModel.updateBetLimit()
                needUpdateBetLimit = false
            }
            betListRefactorAdapter?.userLogin = it
            betParlayListRefactorAdapter?.userLogin = it
        }

        viewModel.infoCenterRepository.unreadNoticeList.observe(viewLifecycleOwner, {
            updateCommonToolbarNotice(it.isNotEmpty())
        })

        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.let { money ->
                tv_balance.text = TextUtil.formatMoney(money ?: 0.0)
                betListRefactorAdapter?.userMoney = money
                betParlayListRefactorAdapter?.userMoney = money
            }
        }

        viewModel.oddsType.observe(viewLifecycleOwner) {
            //keyboard?.hideKeyboard()
            betListRefactorAdapter?.oddsType = it
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
                if (list.size == 0 && isAutoCloseWhenNoData) {
                    activity?.onBackPressed()
                    return@observe
                }

                //顯示無資料畫面
                cl_no_data.visibility = if (list.size == 0) View.VISIBLE else View.GONE

                //依照注單數量動態調整高度
                if (list.size == 1) {
                    //單一注單
                    binding.llRoot.layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                    //上方tabBar betTypeTabLayout隱藏，下方可贏金額 clTotalInfo也隱藏
                    binding.betTypeTabLayout.selectTab(binding.betTypeTabLayout.getTabAt(0))
                    binding.betTypeTabLayout.isVisible = false
                    binding.clTotalInfo.isVisible = false
                    binding.lineShadow.isVisible = false
                    isMultiBet = false
                } else if (!isAutoCloseWhenNoData) {
                    //多筆注單 or 空注單
                    binding.llRoot.layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
                    binding.betTypeTabLayout.isVisible = true
                    binding.clTotalInfo.isVisible = true
                    binding.lineShadow.isVisible = true
                    isMultiBet = true
                }

//                btn_delete_all.visibility = if (list.size == 0) View.GONE else View.VISIBLE
                tv_bet_list_count.text = list.size.toString()
                betListRefactorAdapter?.betList = list
                betParlayListRefactorAdapter?.betList = list

                subscribeChannel(list)
                refreshAllAmount(list)
                checkAllAmountCanBet()
            }
        }

        //移除注單解除訂閱
        viewModel.betInfoRepository.removeItem.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                unSubscribeChannelEvent(it)
            }
            betListRefactorAdapter?.notifyDataSetChanged()
            betParlayListRefactorAdapter?.notifyDataSetChanged()
        }

        //串關列表
        viewModel.parlayList.observe(this.viewLifecycleOwner) {
            if (it.size == 0) {
                betListRefactorAdapter?.hasParlayList = false
                betListRefactorAdapter?.parlayList = singleParlayList

                betParlayListRefactorAdapter?.hasParlayList = false
            } else {
                betListRefactorAdapter?.hasParlayList = true
                betListRefactorAdapter?.parlayList = it

                betParlayListRefactorAdapter?.hasParlayList = true
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
                    hideLoading()
                    if (resultNotNull.success) {
                        //多筆和單筆投注單，下注成功後的行為不同
                        if (isMultiBet) {
                            //多筆的是直接 replace fragment
                            viewModel.betInfoList.removeObservers(this.viewLifecycleOwner)
                        } else {
                            //單筆的要關掉再顯示 dialog
                            isAutoCloseWhenNoData = true
                        }
                        betResultListener?.onBetResult(
                            resultNotNull.receipt,
                            betParlayList ?: listOf(),
                            isMultiBet
                        )
                        refreshAllAmount()
                        showOddChangeWarn = false
                        btn_bet.isOddsChanged = false
                        showHideWarn()
                        if (result.receipt?.singleBets?.any { singleBet -> singleBet.status == 0 } == true || result.receipt?.parlayBets?.any { parlayBet -> parlayBet.status == 0 } == true) {
                            mHandler.removeMessages(BET_CONFIRM_TIPS)
                            mHandler.sendMessage(Message().apply {
                                what = BET_CONFIRM_TIPS
                            })
                        }
                    } else {
                        showErrorPromptDialog(getString(R.string.prompt), resultNotNull.msg) {}
                    }
                }
            }
        }

        //賠率變更提示
        viewModel.showOddsChangeWarn.observe(this.viewLifecycleOwner) {
            showOddChangeWarn = it
            btn_bet.isOddsChanged = it
            showHideWarn()
            when (it) {
                true -> betParlayListRefactorAdapter?.showOddsChangedWarn()
                false -> betParlayListRefactorAdapter?.hideOddsChangedWarn()
            }
        }

        //盤口關閉提示
        viewModel.showOddsCloseWarn.observe(this.viewLifecycleOwner) {
            btn_bet.hasPlatClose = it
            showPlatCloseWarn = it
            showHideWarn()
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
        loading()
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
        val totalBetAmount = if (tabPosition == 0) {
            betListFilter.sumByDouble { it.realAmount }
        } else {
            parlayList.sumByDouble { it.betAmount * it.num }
        }
//        val totalBetAmount =
//            betListFilter.sumByDouble { it.realAmount } + (parlayList.sumByDouble { it.betAmount * it.num })

        //下注總金額大於用戶餘額，提示餘額不足
        if (totalBetAmount > (viewModel.userMoney.value ?: 0.0)) {
            hideLoading()
            showErrorPromptDialog(
                getString(R.string.prompt),
                getString(R.string.bet_info_bet_balance_insufficient)
            ) {}
            return
        }

        viewModel.addBetList(
            getCurrentBetList(),
            parlayList,
            oddsType,
            tabPosition
        )
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
                getString(R.string.message_no_sport_game),
                getString(gameType?.string ?: 0)
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
                this@BetListFragment.requireView(),
                title,
                Snackbar.LENGTH_LONG
            ).apply {
                val snackView: View = layoutInflater.inflate(
                    layout,
                    activity?.findViewById(android.R.id.content),
                    false
                )
                if (isLogin == true)
                    snackView.txv_title.text = title
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
        isAutoCloseWhenNoData = true
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
     * 盤口關閉、賠率變更提示文字
     */
    private fun showHideWarn() {
        when {
            //有盤口關閉就不顯示賠率更動提示
            /*showPlatCloseWarn && showOddChangeWarn -> {
                //盤口關閉且賠率更動
                ll_odds_close_warn.visibility = View.VISIBLE
                tv_odds_closed_changed.visibility = View.GONE
                tv_warn_odds_change.visibility = View.GONE
            }*/
            //20220606 新介面都不顯示了
            /*showPlatCloseWarn -> {
                ll_odds_close_warn.visibility = View.VISIBLE
                tv_odds_closed_changed.visibility = View.GONE
                tv_warn_odds_change.visibility = View.GONE
            }
            showOddChangeWarn -> {
                ll_odds_close_warn.visibility = View.GONE
                tv_warn_odds_change.visibility = View.VISIBLE
            }
            else -> {
                ll_odds_close_warn.visibility = View.GONE
                tv_warn_odds_change.visibility = View.GONE
            }*/
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

        when (binding.betTypeTabLayout.selectedTabPosition) {
            //單項投注
            0 -> {
                with(binding) {
                    llMoreOption.visibility = View.GONE
                    clParlayList.visibility = View.GONE
                }
            }
            //串關投注
            1 -> {
                refreshLlMoreOption(false)
            }
        }
    }

    private fun subscribeChannel(list: MutableList<BetInfoListData>) {
        betListPageSubscribeEvent()
        val subscribedList: MutableList<String> = mutableListOf()
        list.forEach { listData ->
            if (listData.subscribeChannelType == ChannelType.HALL) {
                subscribeChannelHall(
                    listData.matchOdd.gameType,
                    listData.matchOdd.matchId
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
                        listData.matchOdd.gameType,
                        unsubscribeMatchId
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
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment BetListFragment.
         */
        @JvmStatic
        fun newInstance(betResultListener: BetResultListener, showToolbar: Boolean = false) = BetListFragment().apply {
            this.betResultListener = betResultListener
            this.showToolbar = showToolbar
        }
    }

    interface BetResultListener {
        fun onBetResult(betResultData: Receipt?, betParlayList: List<ParlayOdd>, isMultiBet: Boolean)
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