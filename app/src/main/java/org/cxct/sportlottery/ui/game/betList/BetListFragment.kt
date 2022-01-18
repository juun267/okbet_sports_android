package org.cxct.sportlottery.ui.game.betList

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottom_sheet_dialog_parlay_description.*
import kotlinx.android.synthetic.main.button_bet.view.*
import kotlinx.android.synthetic.main.button_fast_bet_setting.view.*
import kotlinx.android.synthetic.main.content_bet_info_item.view.*
import kotlinx.android.synthetic.main.fragment_bet_list.*
import kotlinx.android.synthetic.main.view_bet_info_keyboard.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetListBinding
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.bet.add.betReceipt.Receipt
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.bet.list.FastBetSettingDialog
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.transactionStatus.ParlayType.Companion.getParlayStringRes
import org.cxct.sportlottery.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [BetListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BetListFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {
    private lateinit var binding: FragmentBetListBinding

    private var oddsType: OddsType = OddsType.EU

    private var discount = 1.0F

    private var keyboard: KeyBoardUtil? = null

    private var betListRefactorAdapter: BetListRefactorAdapter? = null

    private var betAllAmount = 0.0

    private var betResultListener: BetResultListener? = null

    private var betParlayList: List<ParlayOdd>? = null //紀錄投注時的串關資料

    private var showOddChangeWarn: Boolean = false //賠率是否有變更

    private var showPlatCloseWarn: Boolean = false //盤口是否被關閉

    private var showReceipt: Boolean = false

    private val deleteAllLayoutAnimationListener by lazy {
        object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
//                binding.llDeleteAll.visibility = View.GONE
                binding.apply {
                    llDeleteAll.visibility = View.GONE
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

        }
    }

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
        initRecyclerView()
        initToolBar()

        initKeyBoard()
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

    private fun initBtnEvent() {
        binding.btnBet.apply {
            tv_login.setOnClickListener {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }

            cl_bet.setOnClickListener { addBet() }

            tv_accept_odds_change.setOnClickListener { addBet() }
        }

        ll_odds_close_warn.setOnClickListener {
            removeClosedPlat()
        }
    }

    private fun initRecyclerView() {
        initAdapter()

        val layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
        rv_bet_list.layoutManager = layoutManager
        betListRefactorAdapter?.setHasStableIds(true)
        rv_bet_list.adapter = betListRefactorAdapter
        rv_bet_list.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            ).apply {
                ContextCompat.getDrawable(
                    context ?: requireContext(),
                    R.drawable.divider_color_white8
                )?.let {
                    setDrawable(it)
                }
            })
    }

    private fun initToolBar() {
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
                    viewModel.removeBetInfoItem(oddsId)
                }

                override fun onShowKeyboard(editText: EditText, matchOdd: MatchOdd) {
                    keyboard?.showKeyboard(editText)
                }

                override fun onShowParlayKeyboard(editText: EditText, parlayOdd: ParlayOdd?) {
                    keyboard?.showKeyboard(editText)
                }

                override fun onHideKeyBoard() {
                    keyboard?.hideKeyboard()
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
            if(it.matchOdd.odds == it.matchOdd.malayOdds
                || it.matchType == MatchType.OUTRIGHT
                || it.matchType == MatchType.OTHER_OUTRIGHT){
                currentOddsType = OddsType.EU
            }
            getWinnable(it.betAmount, getOddsNew(it.matchOdd, currentOddsType), currentOddsType)
        } + parlayList.sumByDouble { getWinnable(it.betAmount, getOdds(it, oddsType), OddsType.EU) }

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
//        var winnable = betAmount * odds
//        if (oddsType == OddsType.EU) {
//            winnable -= betAmount
//        }

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
        val exitAnimation =
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
                viewModel.removeBetInfoAll()
            }
        }
    }

    private fun initKeyBoard() {
        keyboard =
            KeyBoardUtil(binding.kvKeyboard, null, sConfigData?.presetBetAmount ?: mutableListOf())
    }

    private fun initObserver() {
        //是否登入
        viewModel.isLogin.observe(this.viewLifecycleOwner) {
            setupUserBalanceView(it)
            setupBetButtonType(it)
        }

        viewModel.userMoney.observe(viewLifecycleOwner) {
            it.let { money -> tv_balance.text = TextUtil.formatMoney(money ?: 0.0) }
        }

        viewModel.oddsType.observe(viewLifecycleOwner) {
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
                gray_view.visibility = if (list.size == 0) View.GONE else View.VISIBLE

                tv_bet_list_count.text = list.size.toString()
                betListRefactorAdapter?.betList = list

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
        }

        //串關列表
        viewModel.parlayList.observe(this.viewLifecycleOwner) {
            betListRefactorAdapter?.parlayList = it
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
            showPlatCloseWarn && showOddChangeWarn -> {
                //盤口關閉且賠率更動
                ll_odds_close_warn.visibility = View.VISIBLE
                tv_odds_closed_changed.visibility = View.VISIBLE
                tv_warn_odds_change.visibility = View.GONE
            }
            showPlatCloseWarn -> {
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
            }
        }
    }

    /**
     * 同賽事不能串關提示
     * @param show true:顯示, false:隱藏
     * @如果資料只有一筆，也不用顯示
     */
    private fun showHideCantParlayWarn(show: Boolean) {
        ll_cant_parlay_warn.visibility =
            if (show && betListRefactorAdapter?.betList?.size ?: 0 > 1) View.VISIBLE else View.GONE
    }

    private fun subscribeChannel(list: MutableList<BetInfoListData>) {
        betListPageSubscribeEvent()
        val subscribedList: MutableList<String> = mutableListOf()
        list.forEach { listData ->
            if (listData.subscribeChannelType == ChannelType.HALL) {
                subscribeChannelHall(
                    listData.matchOdd.gameType,
                    listData.playCateMenuCode,
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
            if (listData.subscribeChannelType == ChannelType.HALL) {
                unSubscribeChannelHall(
                    listData.matchOdd.gameType,
                    listData.playCateMenuCode,
                    listData.matchOdd.matchId
                )
            } else {
                val unsubscribeMatchId = listData.matchOdd.matchId
                if (!unsubscribedList.contains(unsubscribeMatchId)) {
                    unsubscribedList.add(unsubscribeMatchId)
                    unSubscribeChannelEvent(unsubscribeMatchId)
                }
            }
        }
        betListPageUnSubScribeEvent()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment BetListFragment.
         */
        @JvmStatic
        fun newInstance(betResultListener: BetResultListener) = BetListFragment().apply {
            this.betResultListener = betResultListener
        }
    }

    interface BetResultListener {
        fun onBetResult(betResultData: Receipt?, betParlayList: List<ParlayOdd>)
    }
}