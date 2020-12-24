package org.cxct.sportlottery.ui.odds

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_odds_detail.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentOddsDetailBinding
import org.cxct.sportlottery.network.odds.detail.OddsDetailResult
import org.cxct.sportlottery.network.playcate.PlayCateListResult
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.home.HomeFragment
import org.cxct.sportlottery.ui.menu.results.SettlementViewModel
import org.cxct.sportlottery.util.TextUtil
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

    private val oddsDetailViewModel: OddsDetailViewModel by viewModel()

    private var gameType: String? = null
    private var matchId: String? = null
    private var oddsType: String? = null

    private val oddsDetailListData = ArrayList<OddsDetailListData>()

    private lateinit var dataBinding: FragmentOddsDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gameType = it.getString(GAME_TYPE)
            matchId = it.getString(MATCH_ID)
            oddsType = it.getString(ODDS_TYPE)
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

        list()

        matchId = "a"
        oddsType = "b"
        gameType = "c"

        matchId?.let { matchId ->
            oddsType?.let { oddsType ->
                oddsDetailViewModel.getOddsDetail(matchId, oddsType)
            }
        }

        oddsDetailViewModel.oddsDetailResult.observe(requireActivity(), Observer {
            tv_time.text = TimeUtil.stampToDate(it!!.oddsDetailData?.matchOdd?.matchInfo?.startTime!!.toLong())
            it.oddsDetailData?.matchOdd?.odds?.forEach { (key, value) ->
                oddsDetailListData.add(OddsDetailListData(key, TextUtil.split(value.typeCodes), value.name, value.odds, false))
            }

            gameType?.let { gameType ->
                oddsDetailViewModel.getPlayCateList(gameType)
            }
        })

        oddsDetailViewModel.playCateListResult.observe(requireActivity(), Observer { result ->
            when (result) {
                is PlayCateListResult -> {
                    for (element in result.rows) {
                        tab_cat.addTab(tab_cat.newTab().setText(element.name), false)
                    }
                    tab()
                }
            }
        })

    }


    private fun tab() {
        tab_cat.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                (rv_detail.adapter as OddsDetailListAdapter).notifyDataSetChangedByCode(oddsDetailViewModel.playCateListResult.value!!.rows[tab!!.position].code)
            }
        }
        )
        tab_cat.getTabAt(0)?.select()
    }

    private fun list() {
        rv_detail.apply {
            adapter = OddsDetailListAdapter(oddsDetailListData)
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

}