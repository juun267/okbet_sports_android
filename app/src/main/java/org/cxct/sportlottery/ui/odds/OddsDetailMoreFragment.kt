package org.cxct.sportlottery.ui.odds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_bottom_sheet_odds_detail_more.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.match.Match
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.base.BaseBottomSheetFragment
import org.cxct.sportlottery.ui.home.MainViewModel

class OddsDetailMoreFragment : BaseBottomSheetFragment<MainViewModel>(MainViewModel::class), OddsDetailMoreAdapter.OnItemClickListener {

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
        getData()
    }

    private fun initUI() {
        tv_close.setOnClickListener { dismissAllowingStateLoss() }
        rv_more.apply {
            adapter = OddsDetailMoreAdapter(matchOddList, this@OddsDetailMoreFragment)
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun getData() {

        val list: MutableLiveData<List<*>> = viewModel.getMoreList()

        var m: Any?
        for (i in list.value?.indices!!) {
            when (list.value!![i]) {
                is Match -> {
                    m = (list.value!![i] as Match)
                    if (m.id != matchId) {
                        matchOddList.add(
                            MoreGameEntity(m.awayName, m.endTime, m.homeName, m.id, m.playCateNum, m.startTime.toString(), m.status)
                        )
                    }
                }

                is MatchOdd -> {
                    m = (list.value!![i] as MatchOdd).matchInfo
                    if (m.id != matchId) {
                        matchOddList.add(
                            MoreGameEntity(m.awayName, m.endTime, m.homeName, m.id, m.playCateNum, m.startTime, m.status)
                        )
                    }
                }
            }
        }
        rv_more.adapter?.notifyDataSetChanged()

    }

    override fun onItemClick(matchId: String) {
        changeGameListener?.refreshData(matchId)
        dismissAllowingStateLoss()
    }


}