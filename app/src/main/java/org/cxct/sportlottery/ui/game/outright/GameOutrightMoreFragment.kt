package org.cxct.sportlottery.ui.game.outright

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_game_outright_more.*
import kotlinx.android.synthetic.main.fragment_game_outright_more.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.BetStatus
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.DynamicMarket
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.SocketUpdateUtil


class GameOutrightMoreFragment : BaseBottomNavigationFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameOutrightMoreFragmentArgs by navArgs()

    private val outrightOddAdapter by lazy {
        OutrightOddMoreAdapter().apply {
            discount = viewModel.userInfo.value?.discount ?: 1.0F

            outrightOddListener = OutrightOddListener(
                { matchOdd, odd, playCateCode ->
                    matchOdd?.let {
                        if(mIsEnabled) {
                            avoidFastDoubleClick()
                            addOddsDialog(matchOdd, odd, playCateCode)
                        }
                    }
                }, { _, _ -> },{ _, _ -> }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_outright_more, container, false).apply {
            this.outright_more_close.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMatchInfo()

        setupOutrightType(args.oddsKey)

        setupOutrightOddList(args.oddsKey)

        initObserver()

        initSocketObserver()

        initBottomNavigation()
    }

    override fun onStart() {
        super.onStart()

        subscribeChannelHall(
            args.matchOdd.matchInfo?.gameType,
            args.matchOdd.matchInfo?.id
        )
    }

    private fun setupMatchInfo() {
        outright_more_league.text = args.matchOdd.matchInfo?.name

        outright_more_date.text =
            getString(R.string.concat_date_time, args.matchOdd.startDate, args.matchOdd.startTime)
    }

    private fun setupOutrightType(oddsKey: String?) {
        outright_more_type.text = args.matchOdd.dynamicMarkets[oddsKey]?.getTranslate()

        outright_more_more.apply {
            visibility = if (args.matchOdd.oddsMap?.size ?: 0 > 1) {
                View.VISIBLE
            } else {
                View.GONE
            }

            setOnClickListener {
                args.matchOdd.oddsMap?.keys?.let { it1 ->
                    showBottomSheetDialog(
                        getString(R.string.bottom_sheet_title_play_type),
                        it1.mapNotNull {
                            var dynamicMarkets = args.matchOdd.dynamicMarkets[it]
                            if (dynamicMarkets != null) StatusSheetData(
                                it,
                                args.matchOdd.dynamicMarkets[it]?.getTranslate()
                            ) else null
                        },
                        StatusSheetData(
                            oddsKey,
                            args.matchOdd.dynamicMarkets[oddsKey]?.getTranslate()
                        ),
                        StatusSheetAdapter.ItemCheckedListener { _, data ->
                            (activity as BaseActivity<*>).bottomSheet.dismiss()

                            setupOutrightType(data.code)
                            setupOutrightOddList(data.code)
                        }
                    )
                }
            }
        }
    }

    private fun setupOutrightOddList(oddsKey: String?) {
        outright_more_odd_list.apply {
            adapter = outrightOddAdapter.apply {
                data = args.matchOdd.oddsMap?.get(oddsKey) to args.matchOdd
            }
        }
    }

    private fun initObserver() {
        viewModel.userInfo.observe(this.viewLifecycleOwner, {
            outrightOddAdapter.discount = it?.discount ?: 1.0F
        })

        viewModel.betInfoList.observe(this.viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { betInfoList ->
                outrightOddAdapter.data?.first?.forEach { odd ->
                    odd?.isSelected = betInfoList.any { betInfoListData ->
                        betInfoListData.matchOdd.oddsId == odd?.id
                    }
                }

                outrightOddAdapter.notifyDataSetChanged()
            }
        })

        viewModel.oddsType.observe(this.viewLifecycleOwner, {
            it?.let { oddsType ->
                outrightOddAdapter.oddsType = oddsType
            }
        })
    }

    private fun initSocketObserver() {
        receiver.oddsChange.observe(this.viewLifecycleOwner, {
            it?.getContentIfNotHandled()?.let { oddsChangeEvent ->
                oddsChangeEvent.odds.let { oddTypeSocketMap ->
                    outrightOddAdapter.data?.first?.filterNotNull()
                        ?.forEachIndexed { index: Int, odd: Odd ->
                            val oddsType = outrightOddAdapter.oddsType

                            oddTypeSocketMap.forEach { oddTypeSocketMapEntry ->
                                val oddSocket = oddTypeSocketMapEntry.value?.find { oddSocket ->
                                    oddSocket?.id == odd.id
                                }

                                oddSocket?.let { oddSocketNonNull ->
                                    when (oddsType) {
                                        OddsType.EU -> {
                                            odd.odds?.let { oddValue ->
                                                oddSocketNonNull.odds?.let { oddSocketValue ->
                                                    when {
                                                        oddValue > oddSocketValue -> {
                                                            odd.oddState = OddState.SMALLER.state
                                                        }
                                                        oddValue < oddSocketValue -> {
                                                            odd.oddState = OddState.LARGER.state
                                                        }
                                                        oddValue == oddSocketValue -> {
                                                            odd.oddState = OddState.SAME.state
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        OddsType.HK -> {
                                            odd.hkOdds?.let { oddValue ->
                                                oddSocketNonNull.hkOdds?.let { oddSocketValue ->
                                                    when {
                                                        oddValue > oddSocketValue -> {
                                                            odd.oddState = OddState.SMALLER.state
                                                        }
                                                        oddValue < oddSocketValue -> {
                                                            odd.oddState = OddState.LARGER.state
                                                        }
                                                        oddValue == oddSocketValue -> {
                                                            odd.oddState = OddState.SAME.state
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    odd.odds = oddSocketNonNull.odds
                                    odd.hkOdds = oddSocketNonNull.hkOdds

                                    odd.status = oddSocketNonNull.status

                                    outrightOddAdapter.notifyItemChanged(index)
                                }
                            }

                        }
                }
            }
        })

        receiver.matchOddsLock.observe(this.viewLifecycleOwner, {
            it?.let { matchOddsLockEvent ->
                if (outrightOddAdapter.data?.second?.matchInfo?.id == matchOddsLockEvent.matchId) {
                    outrightOddAdapter.data?.first?.filterNotNull()
                        ?.forEachIndexed { index: Int, odd: Odd ->
                            odd.status = BetStatus.LOCKED.code

                            outrightOddAdapter.notifyItemChanged(index)
                        }
                }
            }
        })

        receiver.globalStop.observe(this.viewLifecycleOwner, {
            it?.let { globalStopEvent ->
                outrightOddAdapter.data?.first?.filterNotNull()
                    ?.forEachIndexed { index: Int, odd: Odd ->
                        when (globalStopEvent.producerId) {
                            null -> {
                                odd.status = BetStatus.DEACTIVATED.code
                            }
                            else -> {
                                odd.producerId?.let { producerId ->
                                    if (producerId == globalStopEvent.producerId) {
                                        odd.status = BetStatus.DEACTIVATED.code
                                    }
                                }
                            }
                        }

                        outrightOddAdapter.notifyItemChanged(index)
                    }
            }
        })

        receiver.producerUp.observe(this.viewLifecycleOwner, {
            unSubscribeChannelHallAll()
            subscribeChannelHall(
                args.matchOdd.matchInfo?.gameType,
                args.matchOdd.matchInfo?.id
            )
        })
    }

    private fun addOddsDialog(
        matchOdd: MatchOdd,
        odd: Odd,
        playCateCode: String
    ) {
        GameType.getGameType(args.matchOdd.matchInfo?.gameType)?.let { gameType ->

            val fastBetDataBean = FastBetDataBean(
                matchType = MatchType.OUTRIGHT,
                gameType = gameType,
                playCateCode = playCateCode,
                playCateName =  "",
                matchInfo = matchOdd.matchInfo!!,
                matchOdd = matchOdd,
                odd = odd,
                subscribeChannelType = ChannelType.HALL,
                betPlayCateNameMap = null,
            )
            (activity as GameActivity).showFastBetFragment(fastBetDataBean)


//            viewModel.updateMatchBetListForOutRight(
//                matchType = MatchType.OUTRIGHT,
//                gameType = gameType,
//                matchOdd = matchOdd,
//                odd = odd,
//                playCateCode = playCateCode
//            )

        }
    }

    private fun DynamicMarket.getTranslate(): String? {
        return when (LanguageManager.getSelectLanguage(context)) {
            LanguageManager.Language.ZH -> {
                this.zh
            }
            else -> {
                this.en
            }
        }
    }

    private fun OddsChangeEvent.updateOddsSelectedState(): OddsChangeEvent {
        this.odds?.let { oddTypeSocketMap ->
            oddTypeSocketMap.mapValues { oddTypeSocketMapEntry ->
                oddTypeSocketMapEntry.value?.onEach { odd ->
                    odd?.isSelected =
                        viewModel.betInfoList.value?.peekContent()?.any { betInfoListData ->
                            betInfoListData.matchOdd.oddsId == odd?.id
                        }
                }
            }
        }

        return this
    }

    override fun onStop() {
        super.onStop()

        unSubscribeChannelHallAll()
    }
}