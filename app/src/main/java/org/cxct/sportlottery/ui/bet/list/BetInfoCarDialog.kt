package org.cxct.sportlottery.ui.bet.list


import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.button_bet.view.*
import kotlinx.android.synthetic.main.content_bet_info_item.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_betinfo_item.*
import kotlinx.android.synthetic.main.view_bet_info_keyboard.*
import kotlinx.android.synthetic.main.view_bet_info_keyboard.kv_keyboard
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogBottomSheetBetinfoItemBinding
import org.cxct.sportlottery.network.bet.info.MatchOdd
import org.cxct.sportlottery.ui.base.BaseSocketBottomSheetFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.KeyBoardUtil
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds


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
            field?.let { matchOdd = it.matchOdd }
        }


    private var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            matchOdd?.let { setupOddsContent(it) }
        }


    private var matchOdd: MatchOdd? = null
        set(value) {
            field = value
            field?.let { setupData(it) }
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


    private lateinit var playNameSpan: SpannableString
    private lateinit var spreadSpan: SpannableString
    private lateinit var oddsSpan: SpannableString


    private var addFlag = false


    init {
        setStyle(STYLE_NORMAL, R.style.LightBackgroundBottomSheet)
    }


    private val keyboard: KeyBoardUtil by lazy {
        KeyBoardUtil(kv_keyboard, null)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogBottomSheetBetinfoItemBinding.inflate(inflater, container, false)
        binding.apply {
            gameViewModel = this@BetInfoCarDialog.viewModel
            lifecycleOwner = this@BetInfoCarDialog.viewLifecycleOwner
            dialog = this@BetInfoCarDialog
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initKeyBoard()
        initBetButton()
        initObserve()
        initSocketObserver()
        getCurrentMoney()
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!addFlag) viewModel.removeBetInfoAll()
    }


    private fun initKeyBoard() {
        et_bet.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                keyboard.showKeyboard(view as EditText)
            }
            false
        }
    }


    private fun initBetButton(){
        button_bet.apply {
            tv_login.setOnClickListener {
                requireContext().startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
            cl_bet.setOnClickListener {

            }
            tv_accept_odds_change.setOnClickListener {

            }
        }
    }


    private fun initObserve() {
        viewModel.betInfoSingle.observe(this.viewLifecycleOwner, {
            betInfoListData = it.peekContent()
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
    }


    private fun initSocketObserver() {
        receiver.userMoney.observe(this.viewLifecycleOwner, {
            currentMoney = it
        })

        receiver.matchOddsChange.observe(this.viewLifecycleOwner, {
            it?.let { changeEvent -> viewModel.updateMatchOdd(changeEvent) }
        })
    }


    private fun setupCurrentMoney(money: Double) {
        tv_current_money.text = getString(R.string.bet_info_current_rmb, TextUtil.formatMoney(money))
    }


    private fun setupCurrentMoneyView(isLogin: Boolean) {
        tv_current_money.visibility = if (isLogin) View.VISIBLE else View.GONE
    }


    private fun setupBetButtonType(isLogin: Boolean){
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

        setupOddsContent(matchOdd)
    }


    private fun setupOddsContent(matchOdd: MatchOdd) {
        val colorRedDark = ContextCompat.getColor(requireContext(), R.color.colorRedDark)

        playNameSpan = SpannableString(matchOdd.playName)
        playNameSpan.setSpan(StyleSpan(Typeface.BOLD), 0, matchOdd.playName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val spreadEnd = matchOdd.spread.length + 1
        spreadSpan = SpannableString(" ${matchOdd.spread}")
        spreadSpan.setSpan(ForegroundColorSpan(colorRedDark), 0, spreadEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spreadSpan.setSpan(StyleSpan(Typeface.BOLD), 0, spreadEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        setupOddsSpannableString(matchOdd, false)
    }


    private fun setupOddsSpannableString(matchOdd: MatchOdd, isChanged: Boolean) {
        val textColor = ContextCompat.getColor(requireContext(), if (isChanged) R.color.colorWhite else R.color.colorBlackLight)
        val backgroundColor = ContextCompat.getColor(requireContext(), if (isChanged) R.color.colorRed else R.color.colorWhite)

        val oddsEnd = TextUtil.formatForOdd(getOdds(matchOdd, oddsType)).length
        oddsSpan = SpannableString(TextUtil.formatForOdd(getOdds(matchOdd, oddsType)))
        oddsSpan.setSpan(ForegroundColorSpan(textColor), 0, oddsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        oddsSpan.setSpan(StyleSpan(Typeface.BOLD), 0, oddsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        oddsSpan.setSpan(BackgroundColorSpan(backgroundColor), 0, oddsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val oddContentBuilder = SpannableStringBuilder()

        oddContentBuilder.append(playNameSpan)
        oddContentBuilder.append(spreadSpan)
        oddContentBuilder.append(" ＠ ")

        tv_odds_content.text = oddContentBuilder.append(oddsSpan)
    }


    fun addToBetInfoList() {
        addFlag = true
        dismiss()
    }


    private fun getCurrentMoney() {
        viewModel.getMoney()
    }


}