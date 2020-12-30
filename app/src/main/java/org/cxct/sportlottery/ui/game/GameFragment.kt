package org.cxct.sportlottery.ui.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_game.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.util.SpaceItemDecoration


/**
 * A simple [Fragment] subclass.
 * Use the [GameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameFragment : BaseFragment<MainViewModel>(MainViewModel::class) {
    private val args: GameFragmentArgs by navArgs()
    private val gameTypeAdapter = GameTypeAdapter(GameTypeListener {
        viewModel.getLeagueList(args.matchType, it)
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false).apply {

            this.hall_game_type_list.apply {
                this.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                this.adapter = gameTypeAdapter
                addItemDecoration(
                    SpaceItemDecoration(
                        context,
                        R.dimen.recyclerview_item_dec_spec
                    )
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getLeagueList(args.matchType)

        viewModel.sportMenuResult.observe(this.viewLifecycleOwner, Observer {

            when (args.matchType) {
                MatchType.IN_PLAY -> {
                    gameTypeAdapter.data = it.sportMenuData?.inPlay ?: listOf()
                }
                MatchType.TODAY -> {
                    gameTypeAdapter.data = it.sportMenuData?.today ?: listOf()
                }
                MatchType.EARLY -> {
                    gameTypeAdapter.data = it.sportMenuData?.early ?: listOf()
                }
                MatchType.PARLAY -> {
                    gameTypeAdapter.data = it.sportMenuData?.parlay ?: listOf()
                }
                else -> {
                }
            }
        })

        viewModel.leagueListResult.observe(this.viewLifecycleOwner, Observer {

        })

        viewModel.leagueListMsg.observe(this.viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })
    }

    companion object {
        @JvmStatic
        fun newInstance() {
        }
    }
}
