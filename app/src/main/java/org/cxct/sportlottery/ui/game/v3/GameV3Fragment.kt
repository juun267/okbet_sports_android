package org.cxct.sportlottery.ui.game.v3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.fragment_game_v3.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration


class GameV3Fragment : BaseSocketFragment<GameViewModel>(GameViewModel::class) {

    private val args: GameV3FragmentArgs by navArgs()

    private val sportTypeAdapter by lazy {
        SportTypeAdapter().apply {
            sportTypeListener = SportTypeListener {
                viewModel.getGameHallList(args.matchType, it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_game_v3, container, false).apply {
            setupSportTypeRow(this)
            setupGameFilterRow(this)
        }
    }

    private fun setupSportTypeRow(view: View) {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.sportMenuResult.observe(this.viewLifecycleOwner, Observer {
            hideLoading()

            when (args.matchType) {
                MatchType.IN_PLAY -> {
                    val sportTypeList = it?.sportMenuData?.menu?.inPlay?.items ?: listOf()

                    updateSportTypeRow(sportTypeList)
                    updateGameFilterRow(sportTypeList)
                }

                MatchType.TODAY -> {
                    val sportTypeList = it?.sportMenuData?.menu?.today?.items ?: listOf()

                    updateSportTypeRow(sportTypeList)
                    updateGameFilterRow(sportTypeList)
                }

                MatchType.EARLY -> {
                    val sportTypeList = it?.sportMenuData?.menu?.early?.items ?: listOf()

                    updateSportTypeRow(sportTypeList)
                    updateGameFilterRow(sportTypeList)
                }

                MatchType.PARLAY -> {
                    val sportTypeList = it?.sportMenuData?.menu?.parlay?.items ?: listOf()

                    updateSportTypeRow(sportTypeList)
                    updateGameFilterRow(sportTypeList)
                }

                MatchType.OUTRIGHT -> {
                    val sportTypeList = it?.sportMenuData?.menu?.outright?.items ?: listOf()

                    updateSportTypeRow(sportTypeList)
                    updateGameFilterRow(sportTypeList)
                }
                MatchType.AT_START -> {
                    val sportTypeList = it?.sportMenuData?.atStart?.items ?: listOf()

                    updateSportTypeRow(sportTypeList)
                    updateGameFilterRow(sportTypeList)
                }
            }
        })

        viewModel.curPlayType.observe(viewLifecycleOwner, Observer {
            game_filter_row.playType = it
        })

        viewModel.getGameHallList(args.matchType, true)
        loading()
    }

    private fun updateSportTypeRow(itemList: List<Item>) {
        sportTypeAdapter.data = itemList
    }

    private fun updateGameFilterRow(itemList: List<Item>) {
        game_filter_row.sportType = itemList.find { sportType -> sportType.isSelected }?.name
    }
}