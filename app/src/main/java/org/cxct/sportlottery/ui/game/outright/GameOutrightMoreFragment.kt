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
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.DynamicMarket
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.StatusSheetAdapter
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.game.GameViewModel
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
}