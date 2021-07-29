package org.cxct.sportlottery.ui.game.betList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottom_sheet_dialog_parlay_description.*
import kotlinx.android.synthetic.main.button_bet.view.*
import kotlinx.android.synthetic.main.fragment_bet_list.*
import kotlinx.android.synthetic.main.view_bet_info_keyboard.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetListBinding
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.enum.SpreadState
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.KeyBoardUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds

/**
 * A simple [Fragment] subclass.
 * Use the [BetListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BetListFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {
    private lateinit var binding: FragmentBetListBinding

    private var oddsType: OddsType = OddsType.EU

    private var keyboard: KeyBoardUtil? = null

    private var betListDiffAdapter: BetListDiffAdapter? = null

    private var betAllAmount = 0.0

    private val deleteAllLayoutAnimationListener by lazy {
        object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                binding.llDeleteAll.visibility = View.GONE
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

        initView()
        initObserver()
        initSocketObserver()

        queryData()
    }

    private fun getParlayList() {
        viewModel.getBetInfoListForParlay()
    }

    private fun initView() {
        initBtnView()
        initRecyclerView()
        initToolBar()

        initKeyBoard()
    }

    private fun initBtnView() {
        binding.btnBet.tv_quota.text = TextUtil.formatBetQuota(0)
    }

    private fun initRecyclerView() {
        initDiffAdapter()

        binding.apply {
            rvBetList.layoutManager =
                LinearLayoutManager(this@BetListFragment.context, LinearLayoutManager.VERTICAL, true).apply {
                    stackFromEnd = true
                }
            rvBetList.adapter = betListDiffAdapter
        }
    }

    private fun initToolBar() {
        binding.ivArrow.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
        initDeleteAllOnClickEvent()
    }

    private fun initDiffAdapter() {
        betListDiffAdapter = BetListDiffAdapter(
            object : BetListDiffAdapter.OnItemClickListener {
                override fun onDeleteClick(oddsId: String, currentItemCount: Int) {
                    viewModel.removeBetInfoItem(oddsId)
                    //當前item為最後一個時
                    if (currentItemCount == 1)
                        activity?.supportFragmentManager?.popBackStack()
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

                override fun refreshAmount() {
                    refreshAllAmount()
                }

                override fun showParlayRule(parlayType: String, parlayRule: String) {
                    showParlayDescription(parlayType, parlayRule)
                }
            }
        ).apply {
            //展開查看所有多個選項時將滾動至底部
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    if (positionStart == 0 && betListDiffAdapter?.moreOptionCollapse == true && needScrollToBottom) {
                        rv_bet_list.smoothScrollToPosition(0)
                        needScrollToBottom = false
                    }
                }
            })
        }
    }

    private fun refreshAllAmount(newBetList: List<BetInfoListData>? = null) {
        val list = newBetList ?: betListDiffAdapter?.let { getCurrentBetList(it) }
        val parlayList = betListDiffAdapter?.let { getCurrentParlayList(it) }
        val totalBetAmount = (list?.sumByDouble { it.betAmount } ?: 0.0) + (parlayList?.sumByDouble { it.betAmount * it.num } ?: 0.0)
        val betCount = (list?.count { it.betAmount > 0 } ?: 0) + (parlayList?.filter { it.betAmount > 0 }?.sumBy { it.num } ?: 0)
        val winnableAmount = (list?.sumByDouble { it.betAmount * getOdds(it.matchOdd, oddsType) } ?: 0.0) + (parlayList?.sumByDouble { it.betAmount * getOdds(it, oddsType) } ?: 0.0)

        binding.apply {
            tvAllBetCount.text = betCount.toString()
            tvTotalBetAmount.text = "${TextUtil.formatBetQuota(totalBetAmount)} ${getString(R.string.currency)}"
            tvTotalWinnableAmount.text = "${TextUtil.formatMoney(winnableAmount)} ${getString(R.string.currency)}"
        }

        setupBtnBetAmount(totalBetAmount)
    }

    private fun setupBtnBetAmount(totalBetAmount: Double?) {
        try {
            val totalBetAmountNotNull = totalBetAmount ?: 0.0
            totalBetAmountNotNull.let {
                binding.apply {
                    btnBet.tv_quota.text = TextUtil.formatBetQuota(it)
                    betAllAmount = it
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initDeleteAllOnClickEvent() {
        val exitAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_left_to_right_exit).apply {
            setAnimationListener(deleteAllLayoutAnimationListener)
            duration = 300
        }
        binding.apply {
            btnDeleteAll.setOnClickListener {
                val enterAnimation = AnimationUtils.loadAnimation(context, R.anim.push_right_to_left_enter).apply {
                    duration = 300
                }

                llDeleteAll.visibility = View.VISIBLE
                btnDeleteAllConfirm.startAnimation(enterAnimation)
            }
            btnDeleteAllCancel.setOnClickListener { btnDeleteAllConfirm.startAnimation(exitAnimation) }

            btnDeleteAllConfirm.setOnClickListener {
                btnDeleteAllConfirm.startAnimation(exitAnimation)
                viewModel.removeBetInfoAll()
                activity?.supportFragmentManager?.popBackStack()
            }
        }
    }

    private fun initKeyBoard() {
        keyboard = KeyBoardUtil(binding.kvKeyboard, null)
    }

    private fun initObserver() {
        viewModel.userMoney.observe(viewLifecycleOwner, {
            it.let { money -> tv_balance.text = TextUtil.formatMoney(money ?: 0.0) }
        })

        viewModel.betInfoList.observe(viewLifecycleOwner, {
            it.peekContent().let { list ->
                tv_bet_list_count.text = list.size.toString()
                betListDiffAdapter?.betList = list

                getParlayList()
                refreshAllAmount(list)
                checkBetInfo(list)
            }
        })

        //移除注單解除訂閱
        viewModel.betInfoRepository.removeItem.observe(viewLifecycleOwner, {
            service.unsubscribeEventChannel(it)
        })

        //串關列表
        viewModel.parlayList.observe(this.viewLifecycleOwner, {
            betListDiffAdapter?.parlayList = it
        })
    }

    private fun initSocketObserver() {
        receiver.userMoney.observe(viewLifecycleOwner, {
            it?.let { money -> tv_balance.text = TextUtil.formatMoney(money) }
        })

        receiver.matchOddsChange.observe(viewLifecycleOwner, {
            if (it == null) return@observe
            viewModel.updateMatchOdd(it)
        })

        receiver.oddsChange.observe(viewLifecycleOwner, {
            if (it == null) return@observe
            viewModel.updateMatchOdd(it)
        })

        receiver.globalStop.observe(viewLifecycleOwner, {
            if (it == null) return@observe
            val betList = betListDiffAdapter?.let { list -> getCurrentBetList(list) }
            betList?.forEach { listData ->
                if (it.producerId == null || listData.matchOdd.producerId == it.producerId) {
                    listData.matchOdd.status = BetStatus.LOCKED.code
                }
            }
            betListDiffAdapter?.betList = (betList ?: mutableListOf())
            checkBetInfo(betList ?: mutableListOf())
        })

        receiver.producerUp.observe(viewLifecycleOwner, {
            if (it == null) return@observe

            betListDiffAdapter?.apply {
                val betInfList = getCurrentBetList(this)
                unsubscribeChannel(betInfList)
                subscribeChannel(betInfList)
            }

        })
    }

    private fun getCurrentBetList(betListDiffAdapter: BetListDiffAdapter): MutableList<BetInfoListData> {
        return betListDiffAdapter.betList
    }

    private fun getCurrentParlayList(betListDiffAdapter: BetListDiffAdapter): MutableList<ParlayOdd> {
        return betListDiffAdapter.parlayList
    }

    /**
     * 檢查注單中賠率、盤口狀態
     */
    private fun checkBetInfo(betInfoList: MutableList<BetInfoListData>) {
        checkBetInfoOddChanged(betInfoList)
        checkBetInfoPlatStatus(betInfoList)
    }

    /**
     * 判斷是否有賠率變更
     */
    private fun checkBetInfoOddChanged(betInfoList: MutableList<BetInfoListData>) {
        var hasOddChanged = false
        betInfoList.forEach {
            if (it.matchOdd.spreadState != SpreadState.SAME.state || it.matchOdd.oddState != OddState.SAME.state) {
                hasOddChanged = true
            }
        }
        showHideOddsChangeWarn(hasOddChanged)
    }

    /**
     * 判斷是否有賠率關閉
     */
    private fun checkBetInfoPlatStatus(betInfoList: MutableList<BetInfoListData>) {
        var hasPlatClose = false
        betInfoList.forEach {
            when (it.matchOdd.status) {
                BetStatus.LOCKED.code, BetStatus.DEACTIVATED.code -> {
                    hasPlatClose = true
                    return@forEach
                }
                else -> { //BetStatus.ACTIVATED.code
                    it.matchOdd.betAddError != null
                }
            }
        }
        showHideOddsCloseWarn(hasPlatClose)
    }

    private fun queryData() {
        //獲取餘額
        viewModel.getMoney()
    }

    /**
     * 顯示串關說明
     */
    private fun showParlayDescription(parlayType: String, parlayRule: String) {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_dialog_parlay_description, null)
        val dialog = BottomSheetDialog(context ?: requireContext())
        dialog.apply {
            setContentView(bottomSheetView)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            view.apply {
                tv_parlay_type.text = parlayType
                tv_parlay_rule.text = parlayRule
            }
            btn_close.setOnClickListener {
                dismiss()
            }
            show()
        }
    }

    /**
     * 賠率更改提示
     * @param show true:顯示, false:隱藏
     */
    private fun showHideOddsChangeWarn(show: Boolean) {
        val visibilityControl = if (show) View.VISIBLE else View.GONE

        btn_bet.isOddsChanged = show
        tv_warn_odds_change.visibility = visibilityControl
    }

    /**
     * 投注關閉提示
     * @param show true:顯示, false:隱藏
     */
    private fun showHideOddsCloseWarn(show: Boolean) {
        ll_odds_close_warn.visibility = if (show) View.VISIBLE else View.GONE
    }

    /**
     * 同賽事不能串關提示
     * @param show true:顯示, false:隱藏
     */
    private fun showHideCantParlayWarn(show: Boolean) {
        ll_cant_parlay_warn.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun subscribeChannel(list: MutableList<BetInfoListData>) {
        list.forEach { listData ->
            if (listData.matchType == MatchType.OUTRIGHT) {
                service.subscribeHallChannel(listData.matchOdd.gameType, PlayCate.OUTRIGHT.value, listData.matchOdd.matchId)
            } else {
                service.subscribeEventChannel(listData.matchOdd.matchId)
            }
        }
    }


    private fun unsubscribeChannel(list: MutableList<BetInfoListData>) {
        list.forEach { listData ->
            if (listData.matchType == MatchType.OUTRIGHT) {
                service.unsubscribeHallChannel(listData.matchOdd.gameType, PlayCate.OUTRIGHT.value, listData.matchOdd.matchId)
            } else {
                service.unsubscribeEventChannel(listData.matchOdd.matchId)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment BetListFragment.
         */
        @JvmStatic
        fun newInstance() = BetListFragment()
    }
}