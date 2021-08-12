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
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.DynamicMarket
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.LanguageManager


class GameOutrightMoreFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameOutrightMoreFragmentArgs by navArgs()

    private val outrightOddAdapter by lazy {
        OutrightOddMoreAdapter().apply {
            outrightOddListener = OutrightOddListener(
                { matchOdd, odd ->
                    matchOdd?.let {
                        addOddsDialog(matchOdd, odd)
                    }
                }, { _, _ -> }
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
    }

    override fun onStart() {
        super.onStart()

        subscribeChannelHall(
            args.matchOdd.matchInfo?.gameType,
            PlayCate.OUTRIGHT.value,
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
            visibility = if (args.matchOdd.odds.size > 1) {
                View.VISIBLE
            } else {
                View.GONE
            }

            setOnClickListener {
                showBottomSheetDialog(
                    getString(R.string.bottom_sheet_title_play_type),
                    args.matchOdd.odds.keys.map {
                        StatusSheetData(it, args.matchOdd.dynamicMarkets[it]?.getTranslate())
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

    private fun setupOutrightOddList(oddsKey: String?) {
        outright_more_odd_list.apply {
            adapter = outrightOddAdapter.apply {
                data = args.matchOdd.odds[oddsKey] to args.matchOdd
            }
        }
    }

    private fun initObserver() {
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
            it?.let { oddsChangeEvent ->
                oddsChangeEvent.updateOddsSelectedState()
                oddsChangeEvent.odds?.let { oddTypeSocketMap ->

                    outrightOddAdapter.data?.first?.filterNotNull()
                        ?.forEachIndexed { index: Int, odd: Odd ->
                            val oddsType = outrightOddAdapter.oddsType

                            oddTypeSocketMap.forEach { oddTypeSocketMapEntry ->
                                val oddSocket = oddTypeSocketMapEntry.value.find { oddSocket ->
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
    }

    private fun addOddsDialog(
        matchOdd: MatchOdd,
        odd: Odd,
    ) {
        GameType.getGameType(args.matchOdd.matchInfo?.gameType)?.let { gameType ->
            viewModel.updateMatchBetListForOutRight(
                matchType = MatchType.OUTRIGHT,
                gameType = gameType,
                matchOdd = matchOdd,
                odd = odd
            )
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
                oddTypeSocketMapEntry.value.onEach { odd ->
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