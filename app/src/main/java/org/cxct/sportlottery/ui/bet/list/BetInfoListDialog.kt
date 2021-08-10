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
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_bet_info_list.*
import kotlinx.android.synthetic.main.view_bet_info_keyboard.*
import kotlinx.android.synthetic.main.view_bet_info_title.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBetInfoListBinding
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.network.bet.Odd
import org.cxct.sportlottery.network.bet.add.BetAddRequest
import org.cxct.sportlottery.network.bet.add.betReceipt.BetAddResult
import org.cxct.sportlottery.network.bet.add.Stake
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.error.BetAddErrorParser
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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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


        //20210603 紀錄問題：修正 notifyDataSetChanged() 造成 EditText focus 錯亂問題
        //https://blog.csdn.net/chenli_001/article/details/114752021
        betInfoListAdapter.setHasStableIds(true)
        rv_bet_list.itemAnimator = null


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
                subscribeChannelHall(
                    listData.matchOdd.gameType,
                    PlayCate.OUTRIGHT.value,
                    listData.matchOdd.matchId
                )
            } else {
                subscribeChannelEvent(listData.matchOdd.matchId)
            }
        }
    }


    private fun unsubscribeChannel(list: MutableList<BetInfoListData>) {
        list.forEach { listData ->
            if (listData.matchType == MatchType.OUTRIGHT) {
                unSubscribeChannelHall(
                    listData.matchOdd.gameType,
                    PlayCate.OUTRIGHT.value,
                    listData.matchOdd.matchId
                )
            } else {
                unSubscribeChannelEvent(listData.matchOdd.matchId)
            }
        }
    }


    private fun observeData() {

        viewModel.userMoney.observe(this.viewLifecycleOwner, {
            it?.let { money -> setMoney(money) }
        })

        viewModel.betInfoRepository.betInfoList.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { list ->
                if (list.isNullOrEmpty()) {
                    dismiss()
                } else {
                    if (!isSubScribe) {
                        isSubScribe = true
                        subscribeChannel(list)
                    }
                    betInfoListAdapter.betInfoList = list
                }
            }
        })

        viewModel.betInfoRepository.removeItem.observe(this.viewLifecycleOwner, { event ->
            event.getContentIfNotHandled()?.let {
                unSubscribeChannelEvent(it)
            }
        })

        viewModel.betAddResult.observe(this.viewLifecycleOwner,
            {
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

        viewModel.userInfo.observe(this,
            {
                betInfoListAdapter.isNeedRegister =
                    (it == null) || (it.testFlag == TestFlag.GUEST.index)
            })

        viewModel.oddsType.observe(this.viewLifecycleOwner,
            {
                oddsType = it
                betInfoListAdapter.oddsType = it
            })
    }

    private fun initSocketObserver() {
        receiver.matchOddsChange.observe(this.viewLifecycleOwner, {
            it?.let { matchOddsChangeEvent ->
                viewModel.updateMatchOdd(matchOddsChangeEvent)
            }
        })

        receiver.oddsChange.observe(this.viewLifecycleOwner, {
            it?.let { oddsChangeEvent ->
                viewModel.updateMatchOdd(oddsChangeEvent)
            }
        })

        receiver.globalStop.observe(this.viewLifecycleOwner, {
            it?.let { globalStopEvent ->
                val list = betInfoListAdapter.betInfoList
                list.forEach { listData ->
                    if (globalStopEvent.producerId == null || listData.matchOdd.producerId == globalStopEvent.producerId) {
                        listData.matchOdd.status = BetStatus.LOCKED.code
                    }
                }
                betInfoListAdapter.betInfoList = list
            }
        })

        receiver.producerUp.observe(this.viewLifecycleOwner, {
            it?.let {
                unsubscribeChannel(betInfoListAdapter.betInfoList)
                subscribeChannel(betInfoListAdapter.betInfoList)
            }
        })
    }

    private fun changeBetInfoContentByMessage(result: BetAddResult) {
        getBetAddError(result.code)?.let { betAddError ->
            if (!result.success) {
                val errorData = BetAddErrorParser.getBetAddErrorData(result.msg)
                errorData?.let { viewModel.updateMatchOdd(it, betAddError) }
            }
        }
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
            showErrorPromptDialog(
                getString(R.string.prompt),
                getString(R.string.bet_info_bet_balance_insufficient)
            ) {}
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
                            getOdds(betInfoListData.matchOdd, oddsType),
                            stake
                        )
                    ),
                    listOf(Stake(parlayType ?: "", stake)),
                    1,
                    oddsType.code,
                    2
                ), betInfoListData.matchType
            )
        }
    }


    override fun onAddMoreClick(betInfoList: BetInfoListData) {
        val bundle = Bundle().apply {
            putString("gameType", betInfoList.matchOdd.gameType)
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
        playName.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            matchOdd.playName.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val spreadEnd = matchOdd.spread.length + 2
        val spread = SpannableString("  ${matchOdd.spread}")
        spread.setSpan(
            ForegroundColorSpan(colorOrange),
            0,
            spreadEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spread.setSpan(StyleSpan(Typeface.BOLD), 0, spreadEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val oddsEnd = TextUtil.formatForOdd(getOdds(matchOdd, oddsType)).length + 3
        val odds = SpannableString(" @ ${TextUtil.formatForOdd(getOdds(matchOdd, oddsType))}")
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
        unSubscribeChannelEventAll()
        getSubscribingInOddsDetail()?.let {
            subscribeChannelEvent(it)
        }
    }
}