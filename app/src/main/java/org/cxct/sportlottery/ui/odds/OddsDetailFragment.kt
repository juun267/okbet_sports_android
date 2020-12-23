package org.cxct.sportlottery.ui.odds

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_odds_detail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOddsDetailBinding
import org.cxct.sportlottery.ui.home.HomeFragment
import org.cxct.sportlottery.util.TimeUtil
import org.koin.androidx.viewmodel.ext.android.viewModel


private const val GAME_TYPE = "gameType"
private const val MATCH_ID = "matchId"
private const val ODDS_TYPE = "oddsType"

class OddsDetailFragment : Fragment() {

    companion object {
        fun newInstance(gameType: String, matchId: String, oddsType: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(GAME_TYPE, gameType)
                    putString(MATCH_ID, matchId)
                    putString(ODDS_TYPE, oddsType)
                }
            }
    }

    private var gameType: String? = null
    private var matchId: String? = null
    private var oddsType: String? = null

    private val oddsDetailViewModel: OddsDetailViewModel by viewModel()

    private lateinit var dataBinding: FragmentOddsDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gameType = it.getString(GAME_TYPE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_odds_detail, container, false);
        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dataBinding.apply {
            oddsDetailViewModel = this@OddsDetailFragment.oddsDetailViewModel
            lifecycleOwner = this@OddsDetailFragment
        }

        //test
//        oddsDetailViewModel.getOddsDetail("sr:match:24369586", "EU")
        matchId?.let { matchId ->
            oddsType?.let { oddsType ->
                oddsDetailViewModel.getOddsDetail(matchId, oddsType)
                oddsDetailViewModel.oddsDetailResult.observe(requireActivity(), Observer {
                    tv_time.text = TimeUtil.stampToDate(it?.oddsDetailData?.matchOdd?.matchInfo?.startTime!!.toLong())
                })
            }
        }

        gameType?.let { gameType ->
            oddsDetailViewModel.getPlayCateList(gameType)
            oddsDetailViewModel.playCateListResult.observe(requireActivity(), Observer { response ->
                for (element in response!!.rows) {
                    tab_cat.addTab(tab_cat.newTab().setText(element.name))
                }
            })
        }

    }

}