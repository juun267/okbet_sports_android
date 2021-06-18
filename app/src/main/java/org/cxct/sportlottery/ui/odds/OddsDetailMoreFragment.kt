package org.cxct.sportlottery.ui.odds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_bottom_sheet_odds_detail_more.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.ui.base.BaseBottomSheetFragment
import org.cxct.sportlottery.ui.game.GameViewModel

class OddsDetailMoreFragment : BaseBottomSheetFragment<GameViewModel>(GameViewModel::class), OddsDetailMoreAdapter.OnItemClickListener {

    private var matchOddList: MutableList<MoreGameEntity> = mutableListOf()

    private var matchId: String? = null

    private var changeGameListener: ChangeGameListener? = null

    companion object {

        const val MATCH_ID = "matchId"
        const val MATCH_INFO_LIST = "matchInfoList"

        fun newInstance(
            matchId: String,
            matchInfoList: Array<MatchInfo>,
            changeGameListener: ChangeGameListener
        ) = OddsDetailMoreFragment().apply {
            arguments = Bundle().apply {
                putString(MATCH_ID, matchId)
                putParcelableArray(MATCH_INFO_LIST, matchInfoList)
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
            matchOddList = it.getParcelableArray(MATCH_INFO_LIST)?.map { parcelable ->
                val info = parcelable as MatchInfo
                MoreGameEntity(
                    awayName = info.awayName,
                    endTime = info.endTime,
                    homeName = info.homeName,
                    id = info.id,
                    playCateNum = info.playCateNum,
                    startTime = info.startTime,
                    status = info.status
                )
            }?.toMutableList() ?: mutableListOf()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_bottom_sheet_odds_detail_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        tv_close.setOnClickListener { dismissAllowingStateLoss() }
        rv_more.apply {
            adapter = OddsDetailMoreAdapter(matchOddList, this@OddsDetailMoreFragment)
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onItemClick(matchId: String) {
        changeGameListener?.refreshData(matchId)
        dismissAllowingStateLoss()
    }
}