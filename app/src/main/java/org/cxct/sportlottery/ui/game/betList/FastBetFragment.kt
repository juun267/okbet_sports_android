package org.cxct.sportlottery.ui.game.betList

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.button_bet.view.*
import kotlinx.android.synthetic.main.content_bet_info_item.*
import kotlinx.android.synthetic.main.content_bet_info_item.view.*
import kotlinx.android.synthetic.main.content_bet_info_item_quota_detail.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.button_bet
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.button_fast_bet_setting
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.kv_keyboard
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.tv_add_to_bet_info
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.tv_current_money
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.tv_odd_content_changed
import kotlinx.android.synthetic.main.fragment_bottom_sheet_betinfo_item.*
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.snackbar_login_notify.view.*
import kotlinx.android.synthetic.main.snackbar_my_favorite_notify.view.*
import kotlinx.android.synthetic.main.view_bet_info_close_message.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentBottomSheetBetinfoItemBinding
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.enum.SpreadState
import org.cxct.sportlottery.network.bet.add.betReceipt.BetAddResult
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.network.bet.info.ParlayOdd
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType
import org.cxct.sportlottery.network.error.BetAddErrorParser
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.bet.list.*
import org.cxct.sportlottery.ui.bet.list.receipt.BetInfoCarReceiptDialog
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.login.afterTextChanged
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.*

const val INPLAY: Int = 1

class FastBetFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private lateinit var binding: FragmentBottomSheetBetinfoItemBinding

    //提示
    private var snackBarNotify: Snackbar? = null

    private var discount = 1.0F

    private var betInfoListData: BetInfoListData? = null
        set(value) {
            field = value
            field?.let {
                betPlayCateNameMap = it.betPlayCateNameMap
                matchOdd = it.matchOdd
                parlayOdd = it.parlayOdds
            }
        }

    private var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            matchOdd?.let {
                setupData(it, betPlayCateNameMap)
            }
        }


    private var matchOdd: MatchOdd? = null
        set(value) {
            field = value
            field?.let {
                setupData(it, betPlayCateNameMap)
            }
        }

    private var betPlayCateNameMap: Map<String?, Map<String?, String?>?>? = null

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

    private val keyboard: KeyBoardUtil by lazy {
        KeyBoardUtil(
            kv_keyboard,
            null,
            sConfigData?.presetBetAmount ?: mutableListOf(),
            isLogin ?: false,
            GameConfigManager.maxBetMoney?.toLong(),
            object : KeyBoardUtil.KeyBoardViewListener {
                override fun showLoginNotice() {
                    setSnackBarNotify(isLogin = false)
                }

                override fun showOrHideKeyBoardBackground(isShow: Boolean, position: Int?) {
                    ll_keyboard_bg.visibility = if(isShow) View.VISIBLE else View.GONE
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetBetinfoItemBinding.inflate(inflater, container, false)
        binding.apply {
//            lifecycleOwner = this@FastBetFragment.viewLifecycleOwner
//            dialog = this@BetInfoCarDialog
        }.executePendingBindings()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.background = ColorDrawable(Color.TRANSPARENT);
        initDiscount()
        initClose()
        initKeyBoard()
        initBetButton()
        initQuota()
        initEditText()
        initObserve()
        initSocketObserver()
        getCurrentMoney()
    }

    private fun initDiscount() {
        discount = viewModel.userInfo.value?.discount ?: 1.0F
    }

    private fun initClose() {
        iv_close.setOnClickListener {
            viewModel.removeBetInfoSingle()
            dismiss()
        }
    }

    private fun dismiss() {
        activity?.onBackPressed()
        OddSpannableString.clearHandler()
    }


    private fun initKeyBoard() {
        et_bet.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (matchOdd?.status == BetStatus.ACTIVATED.code) keyboard.showKeyboard(
                    view as EditText,
                    null,
                    betInfoListData?.parlayOdds?.max?.toLong() ?: GameConfigManager.maxBetMoney?.toLong() ?: 0
                )
            }
            false
        }
    }


    private fun initBetButton() {
        ll_root.setOnClickListener {  }

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

        tv_add_to_bet_info.setOnClickListener {
            addToBetInfoList()
        }

        button_fast_bet_setting.setOnClickListener {
            showSettingDialog()
        }
    }


    private fun initQuota() {
        tv_check_maximum_limit.setOnClickListener {
            it.visibility = View.GONE
            ll_bet_quota_detail.visibility = View.GONE
        }

        ll_bet_quota_detail.setOnClickListener {
            it.visibility = View.GONE
            ll_win_quota_detail.visibility = View.VISIBLE

            et_bet.apply {
                setText(parlayOdd?.max.toString())
                isFocusable = true
                setSelection(text.length)
            }
            keyboard.showKeyboard(et_bet, null, betInfoListData?.parlayOdds?.max?.toLong() ?: GameConfigManager.maxBetMoney?.toLong() ?: 0)
        }
    }


    private fun initEditText() {
        et_bet.apply {
            filters = arrayOf(MoneyInputFilter())
        }

        et_bet.afterTextChanged {
            button_bet.tv_quota.text =
                TextUtil.formatMoney(if (it.isEmpty()) 0.0 else (it.toDoubleOrNull() ?: 0.0))

            if (it.isEmpty()) {
                button_bet.tv_quota.text = TextUtil.formatBetQuota(0)
                tvRealAmount.text = ArithUtil.toMoneyFormat(0.0)
                tv_check_maximum_limit.visibility = View.GONE
                ll_bet_quota_detail.visibility = View.GONE
                ll_win_quota_detail.visibility = View.VISIBLE

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

                betInfoListData?.betAmount = quota
                //計算實際下注金額
                var realAmount = quota
                var win = 0.0
                var currentOddsType = oddsType
                if (matchOdd?.odds == matchOdd?.malayOdds
                    || betInfoListData?.matchType == MatchType.OUTRIGHT
                    || betInfoListData?.matchType == MatchType.OTHER_OUTRIGHT
                ) {
                    currentOddsType = OddsType.EU
                }
                when (currentOddsType) {
                    OddsType.MYS -> {
                        if (getOdds(matchOdd, currentOddsType) < 0) {
                            realAmount = quota * kotlin.math.abs(getOdds(matchOdd, currentOddsType))
                            tvRealAmount.text = ArithUtil.toMoneyFormat(realAmount)
                            win = quota
                        } else {
                            win = quota * getOdds(matchOdd, currentOddsType)
                            tvRealAmount.text = ArithUtil.toMoneyFormat(realAmount)
                        }

                    }
                    OddsType.IDN -> {
                        if (getOdds(matchOdd, currentOddsType) < 0) {
                            realAmount = quota * kotlin.math.abs(getOdds(matchOdd, currentOddsType))
                            tvRealAmount.text = ArithUtil.toMoneyFormat(realAmount)
                            win = quota
                        } else {
                            win = quota * getOdds(matchOdd, currentOddsType)
                            tvRealAmount.text = ArithUtil.toMoneyFormat(realAmount)
                        }
                    }
                    OddsType.EU -> {
                        win = quota * (getOdds(matchOdd, currentOddsType) - 1)
                        tvRealAmount.text = ArithUtil.toMoneyFormat(realAmount)

                    }
                    else -> {
                        win = quota * getOdds(matchOdd, currentOddsType)
                        tvRealAmount.text = ArithUtil.toMoneyFormat(realAmount)
                    }
                }
                button_bet.tv_quota.text = TextUtil.format(realAmount)

                //比照以往計算
                //var win = quota * getOdds(matchOdd, oddsType)
//                if (oddsType == OddsType.EU) {
//                    win -= quota
//                }
                tv_win_quota.text = TextUtil.format(win)

                button_bet.isOddsChanged = false //輸入金額行為, 視為接受當前賠率

                viewModel.updateBetAmount(it)

            }

            checkMinQuota(if (it.isEmpty()) 0.0 else it.toDouble())
        }
    }

    private fun getLimitHint(context: Context, min: Int, max: Int): String {
        return String.format(
            "${context.getString(R.string.edt_hint_deposit_money_new)}",
            TextUtil.formatBetQuota(min),
            TextUtil.formatBetQuota(max)
        )
    }

    private fun checkMinQuota(quota: Double) {
        betInfoListData?.parlayOdds?.min?.let { min ->
            if (quota < min) {
                tv_error_message.text = String.format(
                    (context
                        ?: requireContext()).getString(R.string.bet_info_list_minimum_limit_amount),
                    min,
                    sConfigData?.systemCurrency
                )
                tv_error_message.visibility = View.VISIBLE
                button_bet.amountCanBet = false
            } else {
                tv_error_message.visibility = View.GONE
                button_bet.amountCanBet = true
            }
        }
    }


    private fun initObserve() {
        viewModel.userInfo.observe(this.viewLifecycleOwner) {
            it?.discount?.let { newDiscount ->
                if (discount == newDiscount) return@observe

                viewModel.updateBetInfoDiscount(discount, newDiscount)
                discount = newDiscount
            }
        }

        viewModel.betInfoList.observe(this.viewLifecycleOwner) {
            it.peekContent().let { list ->
                if (list.isNotEmpty()) {
                    betInfoListData = list.getOrNull(0)
                    if (list.size > 1) {
                        dismiss()
                    }

                    val betAmount = betInfoListData?.betAmount ?: 0.0
//                    var win = betAmount * getOdds(matchOdd, oddsType)
//                    if (oddsType == OddsType.EU) {
//                        win -= betAmount
//                    }
                    var realAmount = 0.00
                    var win = 0.0
                    var currentOddsType = oddsType
                    if (matchOdd?.odds == matchOdd?.malayOdds
                        || betInfoListData?.matchType == MatchType.OUTRIGHT
                        || betInfoListData?.matchType == MatchType.OTHER_OUTRIGHT
                    ) {
                        currentOddsType = OddsType.EU
                    }
                    when (currentOddsType) {
                        OddsType.MYS -> {
                            if (getOdds(matchOdd, currentOddsType) < 0) {
                                realAmount = betAmount * kotlin.math.abs(
                                    getOdds(
                                        matchOdd,
                                        currentOddsType
                                    )
                                )
                                tvRealAmount.text = ArithUtil.toMoneyFormat(realAmount)
                                win = betAmount
                            } else {
                                win = betAmount * getOdds(matchOdd, currentOddsType)
                                tvRealAmount.text = ArithUtil.toMoneyFormat(betAmount)
                            }

                        }
                        OddsType.IDN -> {
                            if (getOdds(matchOdd, currentOddsType) < 0) {
                                realAmount = betAmount * kotlin.math.abs(
                                    getOdds(
                                        matchOdd,
                                        currentOddsType
                                    )
                                )
                                tvRealAmount.text = ArithUtil.toMoneyFormat(realAmount)
                                win = betAmount
                            } else {
                                win = betAmount * getOdds(matchOdd, currentOddsType)
                                tvRealAmount.text = ArithUtil.toMoneyFormat(betAmount)
                            }
                        }
                        OddsType.EU -> {
                            win = betAmount * (getOdds(matchOdd, currentOddsType) - 1)
                            tvRealAmount.text = ArithUtil.toMoneyFormat(betAmount)

                        }
                        else -> {
                            win = betAmount * getOdds(matchOdd, currentOddsType)
                            tvRealAmount.text = ArithUtil.toMoneyFormat(betAmount)
                        }
                    }

                    tv_win_quota.text = TextUtil.format(win)
                } else {
                    dismiss()
                }
            }
        }

        viewModel.oddsType.observe(this.viewLifecycleOwner) {
            oddsType = it
        }

        viewModel.isLogin.observe(this.viewLifecycleOwner) {
            isLogin = it
        }

        viewModel.userMoney.observe(this.viewLifecycleOwner) {
            currentMoney = it
        }

        viewModel.betAddResult.observe(this.viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { result ->
                if (!result.success) {
                    showPromptDialog(
                        title = getString(R.string.prompt),
                        message = messageByResultCode(requireContext(), result),
                        success = result.success
                    ) {
                        changeBetInfoContentByMessage(result)
                        dismiss()
                    }
                } else {
                    showBottomSheetDialog(result)
                }
            }
        }

        viewModel.showBetInfoSingle.observe(this.viewLifecycleOwner) { event ->
            event?.peekContent()?.let {
                if (!it) dismiss()
            }
        }

        viewModel.hasBetPlatClose.observe(this.viewLifecycleOwner) {
            button_bet.hasBetPlatClose = it
        }
    }

    private fun initSocketObserver() {
        receiver.matchOddsChange.observe(this.viewLifecycleOwner) {
            it?.let { matchOddsChangeEvent ->
                viewModel.updateMatchOdd(matchOddsChangeEvent)
            }
        }

        receiver.globalStop.observe(this.viewLifecycleOwner) {
            it?.let { globalStopEvent ->
                if (matchOdd?.producerId == null || matchOdd?.producerId == globalStopEvent.producerId) {
                    matchOdd?.status = BetStatus.LOCKED.code
                    matchOdd?.let { setupData(it, betPlayCateNameMap) }
                }
            }
        }

        receiver.producerUp.observe(this.viewLifecycleOwner) {
            it?.let {
                unSubscribeChannelEventAll()
                subscribeChannelEvent(matchOdd?.matchId)
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
    }

    private fun showBottomSheetDialog(result: BetAddResult) {
        context?.let {
            dismiss()
            BetInfoCarReceiptDialog(result).show(
                parentFragmentManager,
                BetInfoCarReceiptDialog::class.java.simpleName
            )
        }
    }

    private fun setupCurrentMoney(money: Double) {
        tv_current_money.text = "${TextUtil.formatMoney(money)} ${sConfigData?.systemCurrency}"

    }


    private fun setupCurrentMoneyView(isLogin: Boolean) {
        ivBetMoney.visibility = if (isLogin) View.VISIBLE else View.GONE
        tv_current_money.visibility = if (isLogin) View.VISIBLE else View.GONE
    }


    private fun setupBetButtonType(isLogin: Boolean) {
        button_bet.isLogin = isLogin
    }


    private fun setupData(
        matchOdd: MatchOdd,
        betPlayCateNameMap: Map<String?, Map<String?, String?>?>?
    ) {
        //隊伍名稱
        tv_match.text =
            if (betInfoListData?.matchType == MatchType.OUTRIGHT) betInfoListData?.outrightMatchInfo?.name
            else "${matchOdd.homeName}${getString(R.string.verse_)}${matchOdd.awayName}"

        //玩法名稱 目前詳細玩法裡面是沒有給betPlayCateNameMap，所以顯示邏輯沿用舊版
        val nameOneLine = { inputStr: String ->
            inputStr.replace("\n", "-")
        }

        val inPlay = System.currentTimeMillis() > matchOdd.startTime ?: 0
        when {
            betPlayCateNameMap.isNullOrEmpty() -> {
                tv_name.text = if (inPlay) {
                    getString(
                        R.string.bet_info_in_play_score,
                        nameOneLine(matchOdd.playCateName),
                        matchOdd.homeScore.toString(),
                        matchOdd.awayScore.toString()
                    )
                } else nameOneLine(matchOdd.playCateName)
            }
            else -> {
                tv_name.text = if (inPlay) {
                    getString(
                        R.string.bet_info_in_play_score,
                        betPlayCateNameMap?.get(matchOdd.playCode)
                            ?.get(LanguageManager.getSelectLanguage(context).key) ?: "",
                        matchOdd.homeScore.toString(),
                        matchOdd.awayScore.toString()
                    )
                } else nameOneLine(
                    betPlayCateNameMap?.get(matchOdd.playCode)
                        ?.get(LanguageManager.getSelectLanguage(context).key) ?: ""
                )
            }
        }

        if (matchOdd.status == BetStatus.ACTIVATED.code) {
            cl_item_background.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorWhite
                )
            )
            iv_bet_lock.visibility = View.GONE
            et_bet.isFocusable = true
            et_bet.isFocusableInTouchMode = true
            cl_quota_detail.visibility = View.VISIBLE
            cl_close_waring.visibility = View.GONE
        } else {
            cl_item_background.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorWhite2
                )
            )
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



        betInfoListData?.let { betInfoData ->
            et_bet.apply {
                hint = getLimitHint(
                    context,
                    betInfoListData?.parlayOdds?.min ?: 0,
                    betInfoListData?.parlayOdds?.max ?: 9999
                )
            }
            //[Martin] 判斷馬來盤與歐洲盤賠率是否一樣 若相同 則該項玩法是不支持馬來盤or印尼下注的 則將oddsType
            val currentOddsType = if (betInfoData.matchOdd.odds == betInfoData.matchOdd.malayOdds
                || betInfoData.matchType == MatchType.OUTRIGHT
                || betInfoData.matchType == MatchType.OTHER_OUTRIGHT
            ) {
                OddsType.EU
            } else {
                oddsType
            }
            betInfoData.singleBetOddsType = currentOddsType
            OddSpannableString.setupOddsContent(betInfoData, currentOddsType, tv_odds_content)
        }
    }

    fun addToBetInfoList() {
        viewModel.addInBetInfo()
    }

    fun showSettingDialog() {
        fragmentManager?.let { it ->
            FastBetSettingDialog().show(
                it,
                FastBetSettingDialog::class.java.simpleName
            )
        }
    }


    private fun getCurrentMoney() {
        viewModel.getMoney()
    }


    private fun addBetSingle() {
        if (matchOdd?.status == BetStatus.LOCKED.code || matchOdd?.status == BetStatus.DEACTIVATED.code) return
        val stake = if (et_bet.text.toString().isEmpty()) 0.0 else et_bet.text.toString().toDouble()

        if (stake > currentMoney ?: 0.0) {
            showErrorPromptDialog(
                getString(R.string.prompt),
                getString(R.string.bet_info_bet_balance_insufficient)
            ) {}
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
                this@FastBetFragment.requireView(),
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


}
