package org.cxct.sportlottery.ui.bet.list


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.button_bet.view.*
import kotlinx.android.synthetic.main.content_bet_info_item.*
import kotlinx.android.synthetic.main.content_bet_info_item_quota_detail.*
import kotlinx.android.synthetic.main.dialog_bet_record_detail_list.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.*
import kotlinx.android.synthetic.main.view_bet_info_close_message.*
import kotlinx.android.synthetic.main.view_bet_info_keyboard.*
import kotlinx.android.synthetic.main.view_bet_info_keyboard.kv_keyboard
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBottomSheetBetinfoItemBinding
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.enum.SpreadState
import org.cxct.sportlottery.network.bet.add.BetAddResult
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.error.BetAddErrorParser
import org.cxct.sportlottery.ui.base.BaseSocketBottomSheetFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.login.afterTextChanged
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*


/**
 * @author Kevin
 * @create 2021/7/8
 * @description
 */
@SuppressLint("SetTextI18n", "ClickableViewAccessibility")
class BetInfoCarDialog : BaseSocketBottomSheetFragment<GameViewModel>(GameViewModel::class) {


    private lateinit var binding: DialogBottomSheetBetinfoItemBinding


    private var betInfoListData: BetInfoListData? = null
        set(value) {
            field = value
            field?.let {
                matchOdd = it.matchOdd
                parlayOdd = it.parlayOdds
            }
        }


    private var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            matchOdd?.let {
                setupData(it)
            }
        }


    private var matchOdd: MatchOdd? = null
        set(value) {
            field = value
            field?.let {
                if (!subscribeFlag) {
                    subscribeChannel(it)
                    subscribeFlag = true
                }
                setupData(it)
            }
        }


    private var parlayOdd: ParlayOdd? = null
        set(value) {
            field = value
            field?.let {
                binding.parlayOdd = it
                binding.executePendingBindings()
            }
        }

    private var currentMoney: Double? = null
        set(value) {
            field = value
            field?.let { setupCurrentMoney(it) }
        }


    private var isLogin: Boolean? = null
        set(value) {
            field = value
            field?.let {
                setupCurrentMoneyView(it)
                setupBetButtonType(it)
            }
        }


    private var subscribeFlag = false


    init {
        setStyle(STYLE_NORMAL, R.style.LightBackgroundBottomSheet)
    }


    private val keyboard: KeyBoardUtil by lazy {
        KeyBoardUtil(kv_keyboard, null)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogBottomSheetBetinfoItemBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = this@BetInfoCarDialog.viewLifecycleOwner
            dialog = this@BetInfoCarDialog
        }.executePendingBindings()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClose()
        initKeyBoard()
        initBetButton()
        initQuota()
        initEditText()
        initObserve()
        initSocketObserver()
        getCurrentMoney()
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.removeBetInfoSingle()
        OddSpannableString.clearHandler()
    }


    override fun onStop() {
        super.onStop()
        service.unsubscribeEventChannel(matchOdd?.matchId)
    }


    private fun initClose() {
        iv_close.setOnClickListener {
            dismiss()
        }
    }


    private fun initKeyBoard() {
        et_bet.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (matchOdd?.status == BetStatus.ACTIVATED.code) keyboard.showKeyboard(view as EditText)
            }
            false
        }
    }


    private fun initBetButton() {
        button_bet.apply {
            tv_login.setOnClickListener {
                requireContext().startActivity(Intent(requireContext(), LoginActivity::class.java))
            }

            cl_bet.setOnClickListener {
                addBetSingle()
            }

            tv_accept_odds_change.setOnClickListener {
                addBetSingle()
            }

            isCanSendOut = false
        }
    }


    private fun initQuota() {
        tv_check_maximum_limit.setOnClickListener {
            it.visibility = View.GONE
            ll_bet_quota_detail.visibility = View.VISIBLE
        }

        ll_bet_quota_detail.setOnClickListener {
            it.visibility = View.GONE
            ll_win_quota_detail.visibility = View.VISIBLE

            et_bet.apply {
                setText(parlayOdd?.max.toString())
                isFocusable = true
                setSelection(text.length)
            }
            keyboard.showKeyboard(et_bet)
        }
    }


    private fun initEditText() {
        et_bet.afterTextChanged {

            button_bet.tv_quota.text = TextUtil.format(if (it.isEmpty()) 0.0 else it.toDouble())

            if (it.isEmpty()) {

                tv_check_maximum_limit.visibility = View.VISIBLE
                ll_bet_quota_detail.visibility = View.GONE
                ll_win_quota_detail.visibility = View.GONE

            } else {

                //輸入時 直接顯示可贏額
                tv_check_maximum_limit.visibility = View.GONE
                ll_bet_quota_detail.visibility = View.GONE
                ll_win_quota_detail.visibility = View.VISIBLE

                val quota = it.toDouble()

                betInfoListData?.parlayOdds?.max?.let { max ->
                    if (quota > max) {
                        et_bet.setText(max.toString())
                        et_bet.setSelection(max.toString().length)
                        return@afterTextChanged
                    }
                }

                //比照以往計算
                var win = quota * getOdds(matchOdd, oddsType)
                if (oddsType == OddsType.EU) {
                    win -= quota
                }
                tv_win_quota.text = TextUtil.format(win)

            }
        }
    }


    private fun initObserve() {
        viewModel.betInfoList.observe(this.viewLifecycleOwner, {
            it.peekContent().let { list ->
                if (list.isNotEmpty()) {
                    betInfoListData = list[0]
                }
            }
        })

        viewModel.oddsType.observe(this.viewLifecycleOwner, {
            oddsType = it
        })

        viewModel.isLogin.observe(this.viewLifecycleOwner, {
            isLogin = it
        })

        viewModel.userMoney.observe(this.viewLifecycleOwner, {
            currentMoney = it
        })

        viewModel.betAddResult.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                showPromptDialog(
                    title = getString(R.string.prompt),
                    message = messageByResultCode(requireContext(), result),
                    success = result.success
                ) {
                    changeBetInfoContentByMessage(result)
                    dismiss()
                }
            }
        })

        viewModel.showBetInfoSingle.observe(this.viewLifecycleOwner, { event ->
            event?.peekContent()?.let {
                if(!it)dismiss()
            }
        })
    }


    private fun initSocketObserver() {
        receiver.userMoney.observe(this.viewLifecycleOwner, {
            currentMoney = it
        })

        receiver.matchOddsChange.observe(this.viewLifecycleOwner, {
            it?.let { event ->
                viewModel.updateMatchOdd(event)
            }
        })

        receiver.producerUp.observe(this.viewLifecycleOwner, {
            service.unsubscribeAllEventChannel()
            service.subscribeEventChannel(matchOdd?.matchId)
        })

        receiver.globalStop.observe(viewLifecycleOwner, { event ->
            if (matchOdd?.producerId == null || matchOdd?.producerId == event?.producerId) {
                matchOdd?.status = BetStatus.LOCKED.code
                matchOdd?.let { setupData(it) }
            }
        })
    }


    private fun setupCurrentMoney(money: Double) {
        tv_current_money.text = getString(R.string.bet_info_current_rmb, TextUtil.formatMoney(money))
    }


    private fun setupCurrentMoneyView(isLogin: Boolean) {
        tv_current_money.visibility = if (isLogin) View.VISIBLE else View.GONE
    }


    private fun setupBetButtonType(isLogin: Boolean) {
        button_bet.isLogin = isLogin
    }


    private fun setupData(matchOdd: MatchOdd) {
        tv_match.text = "${matchOdd.homeName}${getString(R.string.verse_)}${matchOdd.awayName}"

        tv_name.text = if (matchOdd.inplay == INPLAY) {
            getString(
                R.string.bet_info_in_play_score,
                matchOdd.playCateName,
                matchOdd.homeScore.toString(),
                matchOdd.awayScore.toString()
            )
        } else matchOdd.playCateName

        if (matchOdd.status == BetStatus.ACTIVATED.code) {
            cl_item_background.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))
            iv_bet_lock.visibility = View.GONE
            et_bet.isFocusable = true
            et_bet.isFocusableInTouchMode = true
            cl_quota_detail.visibility = View.VISIBLE
            cl_close_waring.visibility = View.GONE
        } else {
            cl_item_background.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite2))
            iv_bet_lock.visibility = View.VISIBLE
            et_bet.isFocusable = false
            et_bet.isFocusableInTouchMode = false
            keyboard.hideKeyboard()
            cl_quota_detail.visibility = View.GONE
            cl_close_waring.visibility = View.VISIBLE
        }

        if (matchOdd.spreadState != SpreadState.SAME.state || matchOdd.oddState != OddState.SAME.state) {
            tv_odd_content_changed.visibility = View.VISIBLE
            button_bet.isOddsChanged = true
        }

        OddSpannableString.setupOddsContent(matchOdd, oddsType, tv_odds_content)
    }


    private fun subscribeChannel(matchOdd: MatchOdd) {
        service.subscribeEventChannel(matchOdd.matchId)
    }


    fun addToBetInfoList() {
        viewModel.addInBetInfo()
    }


    private fun getCurrentMoney() {
        viewModel.getMoney()
    }


    private fun addBetSingle() {
        if (matchOdd?.status == BetStatus.LOCKED.code || matchOdd?.status == BetStatus.DEACTIVATED.code) return

        val stake = if (et_bet.text.toString().isEmpty()) 0.0 else et_bet.text.toString().toDouble()


        if (stake > currentMoney ?: 0.0) {
            showErrorPromptDialog(getString(R.string.prompt), getString(R.string.bet_info_bet_balance_insufficient)) {}
            return
        }

        betInfoListData?.let {
            viewModel.addBetSingle(stake, it)
        }
    }


    private fun changeBetInfoContentByMessage(result: BetAddResult) {
        getBetAddError(result.code)?.let { betAddError ->
            if (!result.success) {
                val errorData = BetAddErrorParser.getBetAddErrorData(result.msg)
                errorData?.let { viewModel.updateMatchOdd(it, betAddError) }
            }
        }
    }


}