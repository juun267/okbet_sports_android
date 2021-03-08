package org.cxct.sportlottery.ui.game.v3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game_v3.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MatchType
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.sportMenuResult.observe(this.viewLifecycleOwner, Observer {
            hideLoading()

            when (args.matchType) {
                MatchType.IN_PLAY -> {
                    sportTypeAdapter.data = it?.sportMenuData?.menu?.inPlay?.items ?: listOf()
                }
                MatchType.TODAY -> {
                    sportTypeAdapter.data = it?.sportMenuData?.menu?.today?.items ?: listOf()
                }
                MatchType.EARLY -> {
                    sportTypeAdapter.data = it?.sportMenuData?.menu?.early?.items ?: listOf()
                }
                MatchType.PARLAY -> {
                    sportTypeAdapter.data = it?.sportMenuData?.menu?.parlay?.items ?: listOf()
                }
                MatchType.OUTRIGHT -> {
                    sportTypeAdapter.data = it?.sportMenuData?.menu?.outright?.items ?: listOf()
                }
                MatchType.AT_START -> {
                    sportTypeAdapter.data = it?.sportMenuData?.atStart?.items ?: listOf()
                }
            }
        })

        viewModel.getGameHallList(args.matchType, true)
        loading()
    }
}