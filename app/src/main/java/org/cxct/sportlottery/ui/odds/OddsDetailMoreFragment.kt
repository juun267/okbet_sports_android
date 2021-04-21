package org.cxct.sportlottery.ui.odds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_bottom_sheet_odds_detail_more.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.match.Match
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.base.BaseBottomSheetFragment
import org.cxct.sportlottery.ui.game.GameViewModel

class OddsDetailMoreFragment : BaseBottomSheetFragment<GameViewModel>(GameViewModel::class), OddsDetailMoreAdapter.OnItemClickListener {

    private var matchOddList: MutableList<MoreGameEntity> = mutableListOf()

    private var matchId: String? = null

    private var changeGameListener: ChangeGameListener? = null

    companion object {

        const val MATCH_ID = "matchId"

        fun newInstance(matchId: String, changeGameListener: ChangeGameListener) = OddsDetailMoreFragment().apply {
            arguments = Bundle().apply {
                putString(MATCH_ID, matchId)
            }
            this.changeGameListener = changeGameListener
        }
    }

    interface ChangeGameListener {
        fun refreshData(matchId: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            matchId = it.getString(MATCH_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_bottom_sheet_odds_detail_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initObserve()
    }

    private fun initUI() {
        tv_close.setOnClickListener { dismissAllowingStateLoss() }
        rv_more.apply {
            adapter = OddsDetailMoreAdapter(matchOddList, this@OddsDetailMoreFragment)
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun initObserve(){
        viewModel.oddsDetailMoreList.observe(viewLifecycleOwner, { list ->
            list?.indices?.let { range ->
                for (i in range) {
                    list.let { value ->
                        val m: Any?
                        when (value[i]) {
                            is Match -> {
                                m = (value[i] as Match)
                                m.apply {
                                    if (m.id != matchId) {
                                        matchOddList.add(MoreGameEntity(m.awayName, m.endTime.toString(), m.homeName, m.id, m.playCateNum, m.startTime.toString(), m.status))
                                    }
                                }
                            }
                            is MatchOdd -> {
                                m = (value[i] as MatchOdd).matchInfo
                                m?.apply {
                                    if (m.id != matchId) {
                                        matchOddList.add(
                                            MoreGameEntity(m.awayName, m.endTime, m.homeName, m.id, m.playCateNum, m.startTime, m.status)
                                        )
                                    }
                                }
                            }

                            else -> {}
                        }
                    }
                }
            }
            rv_more.adapter?.notifyDataSetChanged()
        })

    }

    override fun onItemClick(matchId: String) {
        changeGameListener?.refreshData(matchId)
        dismissAllowingStateLoss()
    }

}