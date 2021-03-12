package org.cxct.sportlottery.ui.game.v3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.fragment_game_v3.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration


class GameV3Fragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameV3FragmentArgs by navArgs()

    private val sportTypeAdapter by lazy {
        SportTypeAdapter().apply {
            sportTypeListener = SportTypeListener {
                viewModel.getGameHallList(args.matchType, it)
                loading()
            }
        }
    }

    private val gameTypeAdapter by lazy {
        GameTypeAdapter().apply {
            gameTypeListener = GameTypeListener {
                //TODO add 滾球賽事
                viewModel.getGameHallList(args.matchType, it)
                loading()
            }
        }
    }

    private val countryAdapter by lazy {
        CountryAdapter().apply {
            countryLeagueListener = CountryLeagueListener {
                viewModel.getLeagueOddsList(args.matchType, it.id)
            }
        }
    }

    private val outrightCountryAdapter by lazy {
        OutrightCountryAdapter().apply {
            outrightCountryLeagueListener = OutrightCountryLeagueListener {
                viewModel.getOutrightOddsList(it.id)
            }
        }
    }

    private val leagueAdapter by lazy {
        LeagueAdapter().apply {
            leagueOddListener = LeagueOddListener {
                //TODO open live and play type page
                it.matchInfo?.id
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_game_v3, container, false).apply {
            setupSportTypeList(this)
            setupGameFilterRow(this)
            setupGameRow(this)
            setupGameListView(this)
        }
    }

    private fun setupSportTypeList(view: View) {
        view.sport_type_list.apply {
            this.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            this.adapter = sportTypeAdapter

            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec
                )
            )
        }
    }

    private fun setupGameFilterRow(view: View) {
        view.game_filter_row.apply {
            matchType = when (args.matchType) {
                MatchType.IN_PLAY -> GameFilterRow.IN_PLAY
                MatchType.TODAY -> GameFilterRow.TODAY
                MatchType.EARLY -> GameFilterRow.EARLY
                MatchType.PARLAY -> GameFilterRow.PARLAY
                MatchType.OUTRIGHT -> GameFilterRow.OUTRIGHT
                MatchType.AT_START -> GameFilterRow.AT_START
            }

            isSearchViewVisible = (args.matchType != MatchType.IN_PLAY)

            searchHint = getString(R.string.game_filter_row_search_hint)

            backClickListener = View.OnClickListener {
                //TODO add back logic to view model
            }

            ouHDPClickListener = View.OnClickListener {
                viewModel.setPlayType(PlayType.OU_HDP)
            }

            x12ClickListener = View.OnClickListener {
                viewModel.setPlayType(PlayType.X12)
            }

            queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    //TODO add query logic to view model
                    return true
                }
            }
        }
    }

    private fun setupGameRow(view: View) {
        view.game_filter_type_list.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            this.adapter = gameTypeAdapter

            addItemDecoration(
                SpaceItemDecoration(
                    context,
                    R.dimen.recyclerview_item_dec_spec
                )
            )
        }

        view.game_filter_game.visibility =
            if (args.matchType == MatchType.EARLY || args.matchType == MatchType.PARLAY || args.matchType == MatchType.OUTRIGHT) {
                View.VISIBLE
            } else {
                View.GONE
            }

        view.game_filter_game.text = when (args.matchType) {
            MatchType.EARLY, MatchType.PARLAY -> {
                resources.getString(R.string.date_row_league)
            }
            MatchType.OUTRIGHT -> {
                resources.getString(R.string.outright_row_entrance)
            }
            else -> {
                null
            }
        }

        view.game_filter_type_list.visibility =
            if (args.matchType == MatchType.EARLY || args.matchType == MatchType.PARLAY) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun setupGameListView(view: View) {
        view.game_list.apply {
            this.layoutManager =
                SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)

            this.addItemDecoration(
                DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {

            viewModel.sportMenuResult.observe(this.viewLifecycleOwner, Observer {
                when (args.matchType) {
                    MatchType.IN_PLAY -> {
                        val itemList = it?.sportMenuData?.menu?.inPlay?.items ?: listOf()

                        sportTypeAdapter.data = itemList
                        game_filter_row.sportName =
                            itemList.find { sportType -> sportType.isSelected }?.name
                    }

                    MatchType.TODAY -> {
                        val itemList = it?.sportMenuData?.menu?.today?.items ?: listOf()

                        sportTypeAdapter.data = itemList
                        game_filter_row.sportName =
                            itemList.find { sportType -> sportType.isSelected }?.name
                    }

                    MatchType.EARLY -> {
                        val itemList = it?.sportMenuData?.menu?.early?.items ?: listOf()

                        sportTypeAdapter.data = itemList
                        game_filter_row.sportName =
                            itemList.find { sportType -> sportType.isSelected }?.name
                    }

                    MatchType.PARLAY -> {
                        val itemList = it?.sportMenuData?.menu?.parlay?.items ?: listOf()

                        sportTypeAdapter.data = itemList
                        game_filter_row.sportName =
                            itemList.find { sportType -> sportType.isSelected }?.name
                    }

                    MatchType.OUTRIGHT -> {
                        val itemList = it?.sportMenuData?.menu?.outright?.items ?: listOf()

                        sportTypeAdapter.data = itemList
                        game_filter_row.sportName =
                            itemList.find { sportType -> sportType.isSelected }?.name
                    }

                    MatchType.AT_START -> {
                        val itemList = it?.sportMenuData?.atStart?.items ?: listOf()

                        sportTypeAdapter.data = itemList
                        game_filter_row.sportName =
                            itemList.find { sportType -> sportType.isSelected }?.name
                    }
                }
            })

            viewModel.curPlayType.observe(viewLifecycleOwner, Observer {
                game_filter_row.playType = it
                leagueAdapter.playType = it
            })

            viewModel.curDate.observe(this.viewLifecycleOwner, Observer {
                gameTypeAdapter.data = it
            })

            viewModel.oddsListGameHallResult.observe(this.viewLifecycleOwner, Observer {
                hideLoading()
                if (it != null && it.success) {
                    game_list.adapter = leagueAdapter.apply {
                        data = it.oddsListData?.leagueOdds ?: listOf()
                    }
                }
            })

            viewModel.leagueListResult.observe(this.viewLifecycleOwner, Observer {
                hideLoading()
                if (it != null && it.success) {
                    game_list.adapter = countryAdapter.apply {
                        data = it.rows ?: listOf()
                    }
                }
            })

            viewModel.outrightSeasonListResult.observe(this.viewLifecycleOwner, Observer {
                hideLoading()
                if (it != null && it.success) {
                    game_list.adapter = outrightCountryAdapter.apply {
                        data = it.rows ?: listOf()
                    }
                }
            })

            viewModel.getGameHallList(args.matchType, true)
            loading()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}