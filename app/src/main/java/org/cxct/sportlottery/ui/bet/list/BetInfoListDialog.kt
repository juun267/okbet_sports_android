package org.cxct.sportlottery.ui.bet.list


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_bet_info_list.*
import kotlinx.android.synthetic.main.view_bet_info_keyboard.*
import kotlinx.android.synthetic.main.view_bet_info_title.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBetInfoListBinding
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.BetAddResult
import org.cxct.sportlottery.network.bet.add.Stake
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.common.CateMenuCode
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.error.BetAddErrorParser
import org.cxct.sportlottery.network.odds.list.BetStatus
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.ui.base.BaseSocketDialog
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.odds.OddsDetailFragment
import org.cxct.sportlottery.util.*


@SuppressLint("SetTextI18n")
class BetInfoListDialog : BaseSocketDialog<GameViewModel>(GameViewModel::class),
    BetInfoListAdapter.OnItemClickListener {


    private lateinit var binding: DialogBetInfoListBinding


    private lateinit var betInfoListAdapter: BetInfoListAdapter


    private var deletePosition: Int = -1


    private var isSubScribe = false


    private var oddsType: OddsType = OddsType.EU


    private var keyboard: KeyBoardUtil? = null


    private var money: Double = 0.0


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

        tv_clean.setOnClickListener {
            viewModel.removeBetInfoAll()
        }

        tv_close.setOnClickListener {
            keyboard?.hideKeyboard()
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

        keyboard = KeyBoardUtil(kv_keyboard, ll_keyboard)
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
                    subscribeChannel(it)
                }
                betInfoListAdapter.betInfoList = it
            }
        })

        viewModel.betInfoRepository.removeItem.observe(this.viewLifecycleOwner, {
            service.unsubscribeEventChannel(it)
        })

        viewModel.betAddResult.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                showPromptDialog(
                    title = getString(R.string.prompt),
                    message = messageByResultCode(requireContext(), result),
                    success = result.success
                ) {
                    changeBetInfoContentByMessage(result)
                }
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


    private fun changeBetInfoContentByMessage(result: BetAddResult) {
        if (!result.success) {
            val errorData = BetAddErrorParser.getBetAddErrorData(result.msg)
            errorData?.let { viewModel.updateMatchOdd(it, getBetAddError(result.code)) }
        }
    }


    private fun initSocketObserver() {

        receiver.userMoney.observe(viewLifecycleOwner, {
            it?.let { money -> setMoney(money) }
        })

        receiver.matchOddsChange.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            viewModel.updateMatchOdd(it)
        })

        receiver.oddsChange.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            viewModel.updateMatchOdd(it)
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

            unsubscribeChannel(betInfoListAdapter.betInfoList)
            subscribeChannel(betInfoListAdapter.betInfoList)
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
        this.money = money
        tv_money.text = getString(R.string.bet_info_current_money, TextUtil.formatMoney(money))
    }


    override fun onDeleteClick(position: Int) {
        //mock模式下 因為回傳內容都一樣 所以不會移除
        viewModel.removeBetInfoItem(betInfoListAdapter.betInfoList[position].matchOdd.oddsId)
        deletePosition = position
    }


    override fun onBetClick(betInfoListData: BetInfoListData, stake: Double) {
        if (stake > money) {
            showErrorPromptDialog(getString(R.string.prompt), getString(R.string.bet_info_bet_balance_insufficient)) {}
            return
        }

        val parlayType = if (betInfoListData.matchType == MatchType.OUTRIGHT) {
            MatchType.OUTRIGHT.postValue
        } else {
            betInfoListData.parlayOdds?.parlayType
        }

        betInfoListData.parlayOdds?.let {
            viewModel.addBet(
                BetAddRequest(
                    listOf(
                        Odd(
                            betInfoListData.matchOdd.oddsId,
                            getOdds(betInfoListData.matchOdd, oddsType)
                        )
                    ),
                    listOf(Stake(parlayType ?: "", stake)),
                    1,
                    oddsType.code
                ), betInfoListData.matchType
            )
        }
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


    override fun onShowKeyboard(editText: EditText, matchOdd: MatchOdd) {

        val colorOrange = ContextCompat.getColor(requireContext(), R.color.colorOrange)


        val playName = SpannableString(matchOdd.playName)
        playName.setSpan(StyleSpan(Typeface.BOLD), 0, matchOdd.playName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val spreadEnd = matchOdd.spread.length + 2
        val spread = SpannableString("  ${matchOdd.spread}")
        spread.setSpan(ForegroundColorSpan(colorOrange), 0, spreadEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spread.setSpan(StyleSpan(Typeface.BOLD), 0, spreadEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val oddsEnd = getOdds(matchOdd, oddsType).toString().length + 3
        val odds = SpannableString(" @ ${getOdds(matchOdd, oddsType)}")
        odds.setSpan(ForegroundColorSpan(colorOrange), 0, oddsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        odds.setSpan(StyleSpan(Typeface.BOLD), 0, oddsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val builder = SpannableStringBuilder()

        builder.append("${matchOdd.playCateName} - ")
        builder.append(playName)
        builder.append(spread)
        builder.append(odds)

        tv_match_info.text = builder

        keyboard?.showKeyboard(editText)
    }


    override fun saveOddsHasChanged(matchOdd: MatchOdd) {
        viewModel.saveOddsHasChanged(matchOdd)
    }


    override fun onDestroy() {
        super.onDestroy()
        service.unsubscribeAllEventChannel()
        getSubscribingInOddsDetail()?.let {
            service.subscribeEventChannel(it)
        }
    }


}