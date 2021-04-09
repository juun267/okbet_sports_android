package org.cxct.sportlottery.ui.bet.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_bet_info_list.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBetInfoListBinding
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.Stake
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.ui.base.BaseSocketDialog
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.odds.OddsDetailFragment
import org.cxct.sportlottery.util.SpaceItemDecoration
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds
import org.cxct.sportlottery.util.getOddsTypeCode


class BetInfoListDialog : BaseSocketDialog<GameViewModel>(GameViewModel::class),
    BetInfoListAdapter.OnItemClickListener {


    private lateinit var binding: DialogBetInfoListBinding


    private lateinit var betInfoListAdapter: BetInfoListAdapter


    private var deletePosition: Int = -1


    private var isSubScribe = false


    private var oddsType: String = OddsType.EU.value


    init {
        setStyle(R.style.Common)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_bet_info_list, container, false)
        binding.apply {
            gameViewModel = this@BetInfoListDialog.viewModel
            lifecycleOwner = this@BetInfoListDialog.viewLifecycleOwner
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeData()
        initSocketObserver()
        getMoney()
    }


    private fun initUI() {
        iv_close.setOnClickListener {
            dismiss()
        }

        betInfoListAdapter = BetInfoListAdapter(requireContext(), this@BetInfoListDialog)

        rv_bet_list.apply {
            adapter = betInfoListAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec_bet_info_list
                )
            )
        }
    }


    private fun observeData() {

        viewModel.userMoney.observe(this.viewLifecycleOwner, {
            it?.let { money -> setMoney(money) }
        })

        viewModel.betInfoRepository.betInfoList.observe(this.viewLifecycleOwner, {
            if (it.size == 0) {
                dismiss()
            } else {
                if (!isSubScribe) {
                    isSubScribe = true
                    it.forEach { betInfoListData ->
                        service.subscribeEventChannel(betInfoListData.matchOdd.matchId)
                    }
                }
                betInfoListAdapter.modify(it, deletePosition)
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

        viewModel.userInfo.observe(this, {
            betInfoListAdapter.isNeedRegister =
                (it == null) || (it.testFlag == TestFlag.GUEST.index)
        })

        viewModel.oddsType.observe(this.viewLifecycleOwner, {
            oddsType = it
            betInfoListAdapter.oddsType = it
        })

    }


    private fun initSocketObserver() {

        receiver.userMoney.observe(viewLifecycleOwner, {
            it?.let { money -> setMoney(money) }
        })

        receiver.matchOddsChange.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            val newList: MutableList<org.cxct.sportlottery.network.odds.detail.Odd> =
                mutableListOf()
            it.odds?.forEach { map ->
                val value = map.value
                value.odds?.forEach { odd ->
                    if (odd != null)
                        newList.add(odd)
                }
            }
            betInfoListAdapter.updatedBetInfoList = newList
        })

        receiver.globalStop.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            val list = betInfoListAdapter.betInfoList
            list.forEach { listData ->
                if (it.producerId == null || listData.matchOdd.producerId == it.producerId) {
                    listData.matchOdd.status = BetStatus.LOCKED.code
                }
            }
            betInfoListAdapter.betInfoList = list
        })

        receiver.producerUp.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer

            //0331 取消全部訂閱
            service.unsubscribeAllEventChannel()

            val list = betInfoListAdapter.betInfoList
            list.forEach { listData ->

                //0331 重新訂閱所以項目
                service.subscribeEventChannel(listData.matchOdd.matchId)

            }
            betInfoListAdapter.betInfoList = list
        })

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
        tv_money.text = getString(R.string.bet_info_current_money, TextUtil.formatMoney(money))
    }


    override fun onDeleteClick(position: Int) {
        //mock模式下 因為回傳內容都一樣 所以不會移除
        viewModel.removeBetInfoItem(betInfoListAdapter.betInfoList[position].matchOdd.oddsId)
        deletePosition = position
    }


    override fun onBetClick(betInfoListData: BetInfoListData, stake: Double) {
        viewModel.addBet(
            BetAddRequest(
                listOf(Odd(betInfoListData.matchOdd.oddsId, getOdds(betInfoListData.matchOdd, oddsType))),
                listOf(Stake(betInfoListData.parlayOdds.parlayType, stake)),
                1,
                getOddsTypeCode(oddsType)
            ), betInfoListData.matchType
        )
    }


    override fun onAddMoreClick(betInfoList: BetInfoListData) {
        val bundle = Bundle().apply {
            putString("gameType", betInfoList.matchOdd.gameType)
            putString("matchId", betInfoList.matchOdd.matchId)
            putString("matchType", betInfoList.matchType?.postValue)
        }
        val intent = Intent(context, GameActivity::class.java).apply {
            putExtras(bundle)
        }
        context?.startActivity(intent)
        dismiss()
    }


    override fun onRegisterClick() {
        context?.startActivity(Intent(context, RegisterActivity::class.java))
    }


    override fun onDestroy() {
        super.onDestroy()
        service.unsubscribeAllEventChannel()
        getSubscribingInOddsDetail()?.let {
            service.subscribeEventChannel(it)
        }
    }


}