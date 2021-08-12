package org.cxct.sportlottery.ui.game.outright

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_game_outright_more.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TimeUtil


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

            setupMatchInfo(this)

            setupOutrightType(this)

            setupOutrightOddList(this)

            this.outright_more_close.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupMatchInfo(view: View) {
        view.outright_more_league.text = args.matchOdd.matchInfo?.leagueName

        args.matchOdd.matchInfo?.startTime?.let { startTime ->
            view.outright_more_date.text = TimeUtil.stampToDateHM(startTime)
        }
    }

    private fun setupOutrightType(view: View) {
        view.outright_more_type.text =
            args.matchOdd.dynamicMarkets[args.oddsKey]?.let {
                when (LanguageManager.getSelectLanguage(context)) {
                    LanguageManager.Language.ZH -> {
                        it.zh
                    }
                    else -> {
                        it.en
                    }
                }
            }


        view.outright_more_more.apply {
            visibility = if (args.matchOdd.odds.size > 1) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun setupOutrightOddList(view: View) {
        view.outright_more_odd_list.apply {
            adapter = outrightOddAdapter.apply {
                data = args.matchOdd.odds[args.oddsKey] to args.matchOdd
            }
        }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
}