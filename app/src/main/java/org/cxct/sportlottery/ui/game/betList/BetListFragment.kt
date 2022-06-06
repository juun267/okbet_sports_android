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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.bottom_sheet_dialog_parlay_description.*
import kotlinx.android.synthetic.main.button_bet.view.*
import kotlinx.android.synthetic.main.button_fast_bet_setting.view.*
import kotlinx.android.synthetic.main.content_bet_info_item.view.*
import kotlinx.android.synthetic.main.fragment_bet_list.*
import kotlinx.android.synthetic.main.snackbar_login_notify.view.*
import kotlinx.android.synthetic.main.snackbar_my_favorite_notify.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetListBinding
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.bet.list.FastBetSettingDialog
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.language.SwitchLanguageActivity
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.transactionStatus.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.util.*

/**
 * @app_destination 注單列表
 */
class BetListFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {
    private lateinit var binding: FragmentBetListBinding

    private var isAutoCloseWhenNoData = false

    private var oddsType: OddsType = OddsType.EU

    private var discount = 1.0F

    private var betListRefactorAdapter: BetListRefactorAdapter? = null

    private var betAllAmount = 0.0

    private var betResultListener: BetResultListener? = null

    private var showToolbar: Boolean = false

    private var betParlayList: List<ParlayOdd>? = null //紀錄投注時的串關資料

    private var showOddChangeWarn: Boolean = false //賠率是否有變更

    private var showPlatCloseWarn: Boolean = false //盤口是否被關閉

    private var showReceipt: Boolean = false

    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BET_CONFIRM_TIPS -> {
                    val spannableStringBuilder = SpannableStringBuilder()
                    val text1 = SpannableString(getString(R.string.text_bet_not_success))
                    val text2 = SpannableString(getString(R.string.text_bet_not_success2))
                    val foregroundSpan =
                        ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.color_F75452_b73a20))
                    text2.setSpan(foregroundSpan, 0, text2.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    val text3 = SpannableString(getString(R.string.text_bet_not_success3))
                    val text4 = SpannableString(getString(R.string.text_bet_not_success4))
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

        initKeyBoard(viewModel.getLoginBoolean())
        fl_title.setOnClickListener { betListRefactorAdapter?.closeAllKeyboard() }
        cl_total_info.setOnClickListener { betListRefactorAdapter?.closeAllKeyboard() }
    }

    private fun initBtnView() {
        binding.btnBet.apply {
            tv_quota.text = TextUtil.formatBetQuota(0)
        }

        binding.buttonFastBetSetting.apply {
            cl_fast_bet.setOnClickListener { _ ->
                fragmentManager?.let { it ->
                    FastBetSettingDialog().show(
                        it,
                        FastBetSettingDialog::class.java.simpleName
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    private fun initBtnEvent() {
        binding.btnBet.apply {
            tv_login.setOnClickListener {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }

            cl_bet.setOnClickListener { addBet() }

            tv_accept_odds_change.setOnClickListener { addBet() }
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

        val lastTab = binding.betTypeTabLayout.getTabAt(binding.betTypeTabLayout.tabCount - 1)
        val tabDivider = lastTab?.customView?.findViewById<View>(R.id.divider)
        tabDivider?.visibility = View.GONE

        binding.betTypeTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    //單項投注
                    0 -> {
                        //TODO set betSingleType in betListRefactorAdapter
                    }
                    //串關投注
                    1 -> {
                        //TODO set betParlayType in betListRefactorAdapter
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
    }

    private fun initRecyclerView() {
        initAdapter()

        val layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
        rv_bet_list.layoutManager = layoutManager
        betListRefactorAdapter?.setHasStableIds(true)
        rv_bet_list.adapter = betListRefactorAdapter
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
                btnRegister.setOnClickListener { startActivity(Intent(context, RegisterActivity::class.java)) }
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
        binding.tvBalanceCurrency.text = sConfigData?.systemCurrency
        initDeleteAllOnClickEvent()
    }

    private fun initAdapter() {
        betListRefactorAdapter =
            BetListRefactorAdapter(object : BetListRefactorAdapter.OnItemClickListener {
                override fun onDeleteClick(oddsId: String, currentItemCount: Int) {
                    isAutoCloseWhenNoData = betListRefactorAdapter?.betList?.size ?: 0 <= 1
                    viewModel.removeBetInfoItem(oddsId)
                }

                override fun onRechargeClick() {
                    if (viewModel.getLoginBoolean()) {
                        startActivity(Intent(context, MoneyRechargeActivity::class.java))
                    } else {
                        startActivity(Intent(context, LoginActivity::class.java))
                    }
                }

                override fun onShowKeyboard(editText: EditText, matchOdd: MatchOdd, position: Int, max: Long) {
                    //keyboard?.showKeyboard(editText, position, max)
                }

                override fun onShowParlayKeyboard(
                    editText: EditText,
                    parlayOdd: ParlayOdd?,
                    position: Int,
                    max: Long
                ) {
                    //keyboard?.showKeyboard(editText, position, max)
                }

                override fun onHideKeyBoard() {
                    betListRefactorAdapter?.closeAllKeyboard()
                }

                override fun saveOddsHasChanged(matchOdd: MatchOdd) {
                    viewModel.saveOddsHasChanged(matchOdd)
                }

                override fun refreshBetInfoTotal() {
                    checkAllAmountCanBet()
                    refreshAllAmount()
                    btn_bet.isOddsChanged = false //輸入金額的行為視為接受當前賠率
                }

                override fun showParlayRule(parlayType: String, parlayRule: String) {
                    showParlayDescription(parlayType, parlayRule)
                }

                override fun onMoreOptionClick() {
                    betListRefactorAdapter?.itemCount?.let {
                        rv_bet_list?.scrollToPosition(it - 1)
                    }
                }
            })
    }

    private fun checkAllAmountCanBet() {
        val betList = getCurrentBetList()
        val parlayList = getCurrentParlayList()
        betList.forEach {
            if (it.amountError) {
                btn_bet.amountCanBet = false
                return
            }
        }
        parlayList.forEach {
            if (it.amountError) {
                btn_bet.amountCanBet = false
                return
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

        val totalBetAmount =
            list.sumByDouble { it.realAmount } + (parlayList.sumByDouble { it.betAmount * it.num })
        val betCount =
            list.count { it.betAmount > 0 } + parlayList.filter { it.betAmount > 0 }
                .sumBy { it.num }
        val winnableAmount = list.sumByDouble {
            var currentOddsType = oddsType
            if (it.matchOdd.odds == it.matchOdd.malayOdds
                || it.matchType == MatchType.OUTRIGHT
                || it.matchType == MatchType.OTHER_OUTRIGHT
            ) {
                currentOddsType = OddsType.EU
            }
            getWinnable(it.betAmount, getOddsNew(it.matchOdd, currentOddsType), currentOddsType)
        } + parlayList.sumByDouble { getComboWinnable(it.betAmount, getOdds(it, OddsType.EU), it.num) }

        binding.apply {
            tvAllBetCount.text = betCount.toString()
            tvTotalBetAmount.text =
                "${TextUtil.formatMoney(totalBetAmount)} ${sConfigData?.systemCurrency}"
            tvTotalWinnableAmount.text =
                "${TextUtil.formatMoney(winnableAmount)} ${sConfigData?.systemCurrency}"
        }

        setupBtnBetAmount(totalBetAmount)
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


    private fun setupBtnBetAmount(totalBetAmount: Double?) {
        try {
            val totalBetAmountNotNull = totalBetAmount ?: 0.0
            totalBetAmountNotNull.let {
                binding.apply {
                    btnBet.tv_quota.text = TextUtil.formatMoney(it)
                    betAllAmount = it
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    private fun initKeyBoard(loginBoolean: Boolean) {
//        keyboard =
//            KeyBoardUtil(
//                binding.kvKeyboard,
//                null,
//                sConfigData?.presetBetAmount ?: mutableListOf(),
//                loginBoolean,
//                GameConfigManager.maxBetMoney?.toLong(),
//                object : KeyBoardUtil.KeyBoardViewListener {
//                    override fun showLoginNotice() {
//                        setSnackBarNotify(isLogin = false)
//                    }
//
//                    override fun showOrHideKeyBoardBackground(isShow: Boolean, position: Int?) {
//                        shadow.visibility = if (isShow) View.VISIBLE else View.GONE
//                        ll_keyboard_bg.visibility = if (isShow) View.VISIBLE else View.GONE
//                        if (position != null) {
//                            rv_bet_list.scrollToPosition(position)
//                        }
//                    }
//                })
    }

    private fun initObserver() {
        //是否登入
        viewModel.isLogin.observe(this.viewLifecycleOwner) {
            setupUserBalanceView(it)
            setupBetButtonType(it)
            initKeyBoard(it)
            updateCommonToolbarLoginStatus(it)
            betListRefactorAdapter?.userLogin = it
        }

        viewModel.infoCenterRepository.unreadNoticeList.observe(viewLifecycleOwner, {
            updateCommonToolbarNotice(it.isNotEmpty())
        })

        viewModel.userMoney.observe(viewLifecycleOwner) {
            it?.let { money ->
                tv_balance.text = TextUtil.formatMoney(money ?: 0.0)
                betListRefactorAdapter?.userMoney = money
            }
        }

        viewModel.oddsType.observe(viewLifecycleOwner) {
            //keyboard?.hideKeyboard()
            betListRefactorAdapter?.oddsType = it
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
                cl_no_data.visibility = if (list.size == 0) View.VISIBLE else View.GONE
//                btn_delete_all.visibility = if (list.size == 0) View.GONE else View.VISIBLE
                tv_bet_list_count.text = list.size.toString()
                betListRefactorAdapter?.betList = list

                subscribeChannel(list)
                refreshAllAmount(list)
                checkAllAmountCanBet()

                if (list.size == 0 && isAutoCloseWhenNoData) activity?.onBackPressed()
            }
        }

        //移除注單解除訂閱
        viewModel.betInfoRepository.removeItem.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                unSubscribeChannelEvent(it)
            }
            betListRefactorAdapter?.notifyDataSetChanged()
        }

        //串關列表
        viewModel.parlayList.observe(this.viewLifecycleOwner) {
            if (it.size == 0) {
                betListRefactorAdapter?.hasParlayList = false
                betListRefactorAdapter?.parlayList = singleParlayList
            } else {
                betListRefactorAdapter?.hasParlayList = true
                betListRefactorAdapter?.parlayList = it
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
                        betResultListener?.onBetResult(
                            resultNotNull.receipt,
                            betParlayList ?: listOf()
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
        }

        //盤口關閉提示
        viewModel.showOddsCloseWarn.observe(this.viewLifecycleOwner) {
            showPlatCloseWarn = it
            showHideWarn()
        }

        viewModel.hasBetPlatClose.observe(this.viewLifecycleOwner) {
            btn_bet.hasBetPlatClose = it
        }

    }

    private fun initSocketObserver() {
        receiver.matchOddsChange.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsChangeEvent ->
                viewModel.updateMatchOdd(matchOddsChangeEvent)
            }
        }

        receiver.oddsChange.observe(this.viewLifecycleOwner) {
            it?.let { oddsChangeEvent ->
                SocketUpdateUtil.updateMatchOdds(oddsChangeEvent)
                viewModel.updateMatchOdd(oddsChangeEvent)
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

        val totalBetAmount =
            betListFilter.sumByDouble { it.realAmount } + (parlayList.sumByDouble { it.betAmount * it.num })
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
            oddsType
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
     * 顯示、隱藏使用者餘額(登入、未登入)
     */
    private fun setupUserBalanceView(isLogin: Boolean) {
        if (isLogin) {
            tv_balance.visibility = View.VISIBLE
            tv_balance_currency.visibility = View.VISIBLE
            tv_balance_currency.text = sConfigData?.systemCurrency
        } else {
            tv_balance.visibility = View.GONE
            tv_balance_currency.visibility = View.GONE
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
     */
    private fun showHideCantParlayWarn(show: Boolean) {
        if (show && betListRefactorAdapter?.betList?.size ?: 0 > 1) {
            betListRefactorAdapter?.showCantParlayWarn()
        } else {
            betListRefactorAdapter?.hideCantParlayWarn()
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
        fun onBetResult(betResultData: Receipt?, betParlayList: List<ParlayOdd>)
    }

}