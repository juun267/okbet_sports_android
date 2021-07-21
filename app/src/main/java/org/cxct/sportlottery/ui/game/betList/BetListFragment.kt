package org.cxct.sportlottery.ui.game.betList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bottom_sheet_dialog_parlay_description.*
import kotlinx.android.synthetic.main.fragment_bet_list.*
import kotlinx.android.synthetic.main.view_bet_info_keyboard.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBetListBinding
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.common.CateMenuCode
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.KeyBoardUtil
import org.cxct.sportlottery.util.TextUtil

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

    private fun initView() {
        initBtnView()
        initRecyclerView()
        initDeleteAllOnClickEvent()
        initKeyBoard()
    }

    private fun initBtnView() {
        binding.apply {
            btnBet.background = ContextCompat.getDrawable(context ?: requireContext(), R.color.colorSilverLight)
            tvBtnBet.setTextColor(ContextCompat.getColor(context ?: requireContext(), R.color.colorGray))
            tvBtnBetAmount.apply {
                setTextColor(ContextCompat.getColor(context ?: requireContext(), R.color.colorGray))
                text = "${TextUtil.formatMoney(0.0)} ${getString(R.string.currency)}"
            }
        }
    }

    private fun initRecyclerView() {
        initDiffAdapter()

        binding.apply {
            rvBetList.layoutManager =
                LinearLayoutManager(this@BetListFragment.context, LinearLayoutManager.VERTICAL, false)
            rvBetList.adapter = betListDiffAdapter
        }
    }

    private fun initDiffAdapter() {
        betListDiffAdapter = BetListDiffAdapter(
            object : BetListDiffAdapter.OnItemClickListener {
                override fun onDeleteClick(oddsId: String) {
                    viewModel.removeBetInfoItem(oddsId)
                }

                override fun onShowKeyboard(editText: EditText, matchOdd: MatchOdd) {
                    keyboard?.showKeyboard(editText)
                }

                override fun saveOddsHasChanged(matchOdd: MatchOdd) {
                    viewModel.saveOddsHasChanged(matchOdd)
                }

                override fun refreshAmount() {
                    refreshAllAmount()
                }

            }
        )
    }

    private fun refreshAllAmount(newBetList: List<BetInfoListData>? = null) {
        val list = newBetList ?: betListDiffAdapter?.currentList
        val totalBetAmount = list?.sumByDouble { it.betAmount }
        val betCount = list?.count { it.betAmount > 0 }

        binding.apply {
            tvAllBetCount.text = betCount.toString()
            tvTotalBetAmount.text = "${TextUtil.formatMoney(totalBetAmount ?: 0.0)} ${getString(R.string.currency)}"
        }

        setupBtnBetAmount(totalBetAmount)
    }

    private fun setupBtnBetAmount(totalBetAmount: Double?) {
        try {
            val totalBetAmountNotNull = totalBetAmount ?: 0.0
            totalBetAmountNotNull.let {
                binding.apply {
                    tvBtnBetAmount.text = "${TextUtil.formatMoney(it)} ${getString(R.string.currency)}"
                    if (it > 0) {
                        tvBtnBet.setTextColor(ContextCompat.getColor(context ?: requireContext(), android.R.color.white))
                        tvBtnBetAmount.setTextColor(ContextCompat.getColor(context ?: requireContext(), android.R.color.white))
                        btnBet.background = ContextCompat.getDrawable(context ?: requireContext(), R.color.colorBlue)
                    } else {
                        tvBtnBet.setTextColor(ContextCompat.getColor(context ?: requireContext(), R.color.colorGray))
                        tvBtnBetAmount.setTextColor(ContextCompat.getColor(context ?: requireContext(), R.color.colorGray))
                        btnBet.background = ContextCompat.getDrawable(context ?: requireContext(), R.color.colorSilverLight)

                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initDeleteAllOnClickEvent() {
        binding.apply {
            btnDeleteAll.setOnClickListener {
                val enterAnimation = AnimationUtils.loadAnimation(context, R.anim.push_right_to_left_enter).apply {
                    duration = 300
                }

                llDeleteAll.visibility = View.VISIBLE
                btnDeleteAllConfirm.startAnimation(enterAnimation)
            }
            btnDeleteAllCancel.setOnClickListener {
                val exitAnimation = AnimationUtils.loadAnimation(context, R.anim.pop_left_to_right_exit).apply {
                    setAnimationListener(deleteAllLayoutAnimationListener)
                    duration = 300
                }

                btnDeleteAllConfirm.startAnimation(exitAnimation)
            }

            btnDeleteAllConfirm.setOnClickListener { viewModel.removeBetInfoAll() }
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
                val newList = list.toMutableList()
                tv_bet_list_count.text = newList.size.toString()
                betListDiffAdapter?.submitList(newList)
                refreshAllAmount(newList)
            }
        })

        //移除注單解除訂閱
        viewModel.betInfoRepository.removeItem.observe(viewLifecycleOwner, {
            service.unsubscribeEventChannel(it)
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
            val list = betListDiffAdapter?.currentList
            list?.forEach { listData ->
                if (it.producerId == null || listData.matchOdd.producerId == it.producerId) {
                    listData.matchOdd.status = BetStatus.LOCKED.code
                }
            }
            betListDiffAdapter?.submitList(list)
        })

        receiver.producerUp.observe(viewLifecycleOwner, {
            if (it == null) return@observe

            betListDiffAdapter?.apply {
                unsubscribeChannel(currentList)
                subscribeChannel(currentList)
            }

        })
    }

    private fun queryData() {
        //獲取餘額
        viewModel.getMoney()
    }

    /**
     * 顯示串關說明
     */
    private fun showParlayDescription() {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_dialog_parlay_description, null)
        val dialog = BottomSheetDialog(context ?: requireContext())
        dialog.apply {
            setContentView(bottomSheetView)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
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

        btn_refresh_odds.visibility = visibilityControl
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
                service.subscribeHallChannel(listData.matchOdd.gameType, CateMenuCode.OUTRIGHT.code, listData.matchOdd.matchId)
            } else {
                service.subscribeEventChannel(listData.matchOdd.matchId)
            }
        }
    }


    private fun unsubscribeChannel(list: MutableList<BetInfoListData>) {
        list.forEach { listData ->
            if (listData.matchType == MatchType.OUTRIGHT) {
                service.unsubscribeHallChannel(listData.matchOdd.gameType, CateMenuCode.OUTRIGHT.code, listData.matchOdd.matchId)
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