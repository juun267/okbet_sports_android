package org.cxct.sportlottery.ui.bet.list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_bet_info_item_action.*
import kotlinx.android.synthetic.main.content_bet_info_item_action.tv_bet
import kotlinx.android.synthetic.main.content_bet_info_item_action.view.*
import kotlinx.android.synthetic.main.dialog_bet_info_list.*
import kotlinx.android.synthetic.main.dialog_bet_info_list.iv_close
import kotlinx.android.synthetic.main.dialog_bet_info_parlay_list.*
import kotlinx.android.synthetic.main.play_category_bet_btn.view.*
import kotlinx.android.synthetic.main.view_bet_info_keyboard.*
import kotlinx.android.synthetic.main.view_bet_info_title.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBetInfoParlayListBinding
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.Stake
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.error.HttpError
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.ui.base.BaseSocketDialog
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.odds.OddsDetailFragment
import org.cxct.sportlottery.util.*

@Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
class BetInfoListParlayDialog : BaseSocketDialog<GameViewModel>(GameViewModel::class), BetInfoListMatchOddAdapter.OnItemClickListener,
    BetInfoListParlayAdapter.OnTotalQuotaListener {


    private var isSubScribe = false


    private var oddsType: OddsType = OddsType.EU


    private var keyboard: KeyBoardUtil? = null


    private var money: Double = 0.0


    private lateinit var matchOddAdapter: BetInfoListMatchOddAdapter
    private lateinit var parlayAdapter: BetInfoListParlayAdapter


    init {
        setStyle(R.style.Common)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding: DialogBetInfoParlayListBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_bet_info_parlay_list, container, false)
        binding.apply {
            gameViewModel = this@BetInfoListParlayDialog.viewModel
            lifecycleOwner = this@BetInfoListParlayDialog.viewLifecycleOwner
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeData()
        getParlayList()
        getMoney()
    }


    private fun initUI() {
        iv_close.setOnClickListener { dismiss() }
        tv_clean.setOnClickListener { viewModel.removeBetInfoAll() }

        tv_close.setOnClickListener {
            keyboard?.hideKeyboard()
        }

        tv_add_more.setOnClickListener { dismiss() }
        tv_bet.setOnClickListener(object : OnForbidClickListener() {
            override fun forbidClick(view: View?) {
                addBet()
            }
        })

        tv_register.setOnClickListener {
            context?.startActivity(Intent(context, RegisterActivity::class.java))
        }

        tv_to_game_rule.setOnClickListener {
            JumpUtil.toInternalWeb(requireContext(), Constants.getGameRuleUrl(requireContext(), Constants.COMBO), getString(R.string.game_rule))
        }

        rl_expandable.setOnClickListener {
            el_expandable_area.apply {
                if (this.isExpanded) {
                    iv_arrow_up.animate().rotation(180f).setDuration(200).start()
                    el_expandable_area.collapse(true)
                } else {
                    iv_arrow_up.animate().rotation(0f).setDuration(200).start()
                    el_expandable_area.setExpanded(true, true)
                }
            }
        }

        matchOddAdapter = BetInfoListMatchOddAdapter(requireContext(), this@BetInfoListParlayDialog)
        parlayAdapter = BetInfoListParlayAdapter(this@BetInfoListParlayDialog)

        rv_match_odd_list.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = matchOddAdapter
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_bet_info_list
                )
            )
        }

        rv_parlay_list.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = parlayAdapter
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_bet_info_list
                )
            )
        }

        keyboard = KeyBoardUtil(kv_keyboard, ll_keyboard)

    }


    private fun observeData() {

        viewModel.userMoney.observe(this.viewLifecycleOwner, {
            it?.let { money -> setMoney(money) }
        })

        viewModel.betInfoResult.observe(this.viewLifecycleOwner, { it ->
            val eventResult = it.getContentIfNotHandled()
            eventResult?.success?.let { success ->
                if (!success && eventResult.code != HttpError.BET_INFO_CLOSE.code) {
                    showErrorPromptDialog(getString(R.string.prompt), eventResult.msg) {}
                }
            }
        })

        viewModel.matchOddList.observe(this.viewLifecycleOwner, {
            if (!isSubScribe) {
                isSubScribe = true
                it.forEach { matchOdd ->
                    service.subscribeEventChannel(matchOdd.matchId)
                }
            }
            matchOddAdapter.matchOddList = it

            changeBetButtonClickable(!checkGameType())

        })

        viewModel.parlayList.observe(this.viewLifecycleOwner, {
            parlayAdapter.modify(it)
        })

        viewModel.betInfoRepository.betInfoList.observe(this.viewLifecycleOwner, {
            when(it.size){
                0 -> {dismiss()}
                1 -> {changeBetButtonClickable(false)}
            }
        })

        viewModel.betInfoRepository.removeItem.observe(this.viewLifecycleOwner, {
            service.unsubscribeEventChannel(it)
        })

        viewModel.betAddResult.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                showPromptDialog(
                    title = getString(R.string.prompt),
                    message = if (result.success) getString(R.string.bet_info_add_bet_success) else result.msg,
                    success = result.success
                ) {}
            }
        })

        viewModel.userInfo.observe(this.viewLifecycleOwner, {
            val isNeedRegister = (it == null) || (it.testFlag == TestFlag.GUEST.index)

            tv_bet.visibility = if (isNeedRegister) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }

            tv_register.visibility = if (isNeedRegister) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        })

        viewModel.oddsType.observe(this.viewLifecycleOwner, {
            oddsType = it
            matchOddAdapter.oddsType = it
            parlayAdapter.oddsType = it
        })

        receiver.userMoney.observe(viewLifecycleOwner, {
            it?.let { money -> setMoney(money) }
        })

        receiver.matchOddsChange.observe(this.viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            viewModel.updateMatchOdd(it)
        })

        receiver.globalStop.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            val list = matchOddAdapter.matchOddList
            list.forEach { listData ->
                if (it.producerId == null || listData.producerId == it.producerId) {
                    listData.status = BetStatus.LOCKED.code
                }
            }
            matchOddAdapter.matchOddList = list
        })

        receiver.producerUp.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer

            //0331 取消全部訂閱
            service.unsubscribeAllEventChannel()

            val list = matchOddAdapter.matchOddList
            list.forEach { listData ->

                //0331 重新訂閱所以項目
                service.subscribeEventChannel(listData.matchId)

            }
            matchOddAdapter.matchOddList = list
        })
    }


    private fun getParlayList() {
        viewModel.getBetInfoListForParlay(null)
    }


    private fun getMoney() {
        viewModel.getMoney()
    }


    private fun getSubscribingInOddsDetail(): String? {
        var matchId: String? = null
        val oddsDetail = parentFragmentManager.findFragmentByTag(GameActivity.Page.ODDS_DETAIL.name)
        if (oddsDetail?.isAdded == true) {
            matchId = (oddsDetail as OddsDetailFragment).matchId
        }
        return matchId
    }


    private fun setMoney(money: Double) {
        this.money = money
        tv_money.text = getString(R.string.bet_info_current_money, TextUtil.formatMoney(money))
    }


    private fun addBet() {
        val matchList: MutableList<Odd> = mutableListOf()
        for (i in matchOddAdapter.matchOddList.indices) {
            matchList.add(Odd(matchOddAdapter.matchOddList[i].oddsId, getOdds(matchOddAdapter.matchOddList[i], oddsType)))
        }
        val parlayList: MutableList<Stake> = mutableListOf()
        for (i in parlayAdapter.parlayOddList.indices) {
            if (parlayAdapter.sendBetQuotaList[i] > 0) {
                parlayList.add(Stake(parlayAdapter.parlayOddList[i].parlayType, parlayAdapter.sendBetQuotaList[i]))
            }
        }

        if (parlayAdapter.sendBetQuotaList.sum() > money) {
            showErrorPromptDialog(getString(R.string.prompt), getString(R.string.bet_info_bet_balance_insufficient)) {}
            return
        }

        viewModel.addBet(
            BetAddRequest(
                matchList,
                parlayList,
                1,
                oddsType.code
            ), MatchType.PARLAY
        )
    }


    override fun onDeleteClick(position: Int) {
        viewModel.removeBetInfoItemAndRefresh(matchOddAdapter.matchOddList[position].oddsId)
    }


    override fun onOddChange() {
        if (tv_bet.isClickable) {
            tv_bet.apply {
                setTextColor(ContextCompat.getColor(tv_bet.context, R.color.white))
                text = getString(R.string.bet_info_list_odds_change)
            }
        }
    }


    override fun count(totalWin: Double, totalBet: Double) {
        tv_win_quota.text = TextUtil.format(totalWin)
        tv_bet_quota.text = TextUtil.format(totalBet)
    }


    override fun sendOutStatus(parlayOddList: MutableList<ParlayOdd>) {
        changeBetButtonClickable(if (checkGameType()) false else parlayOddList.find {
            !it.sendOutStatus
        } == null)
    }


    override fun onShowKeyboard(editText: EditText) {
        keyboard?.showKeyboard(editText)
    }


    private fun changeBetButtonClickable(boolean: Boolean) {
        tv_bet.apply {
            isClickable = if (!boolean) {
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_radius_4_button_unselected)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                false
            } else {
                background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_radius_4_button_orangelight)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
                true
            }
        }
    }


    private fun checkGameType(): Boolean {
        return matchOddAdapter.matchOddList.any {
            it.gameType != matchOddAdapter.matchOddList[0].gameType
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        service.unsubscribeAllEventChannel()
        getSubscribingInOddsDetail()?.let {
            service.subscribeEventChannel(it)
        }
    }


}