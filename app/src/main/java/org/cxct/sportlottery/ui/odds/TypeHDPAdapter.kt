package org.cxct.sportlottery.ui.odds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.odds.detail.Odd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.menu.OddsType

class TypeHDPAdapter(
    private val oddsDetail: OddsDetailListData,
    private val onOddClickListener: OnOddClickListener,
    private val betInfoList: MutableList<BetInfoListData>,
    private val curMatchId: String?,
    private val oddsType: OddsType
) : RecyclerView.Adapter<TypeHDPAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_hdp_item, parent, false))
    }


    override fun getItemCount(): Int {
        return oddsDetail.oddArrayList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel(oddsDetail.oddArrayList[position])
    }


    inner class ViewHolder(view: View) : OddViewHolder(view) {
        fun bindModel(odd: Odd) {
            setData(odd, onOddClickListener, betInfoList, curMatchId, BUTTON_SPREAD_TYPE_END, oddsType, oddsDetail.gameType)
        }
    }


}