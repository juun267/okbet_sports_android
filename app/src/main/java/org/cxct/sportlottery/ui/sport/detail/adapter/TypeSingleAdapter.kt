package org.cxct.sportlottery.ui.sport.detail.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.detail.OddStateViewHolderDetail
import org.cxct.sportlottery.ui.sport.detail.OddsDetailListData
import org.cxct.sportlottery.ui.sport.detail.OnOddClickListener
import org.cxct.sportlottery.ui.sport.oddsbtn.OddsButtonDetail


class TypeSingleAdapter (
    var oddsDetail: OddsDetailListData,
    var onOddClickListener: OnOddClickListener,
    var oddsType: OddsType
) : RecyclerView.Adapter<TypeSingleAdapter.ViewHolder>() {


    fun setOddsDetailData(oddsDetail: OddsDetailListData){
        this.oddsDetail = oddsDetail
    }


    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolderDetail.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
//                notifyItemChanged(oddsDetail.oddArrayList.indexOf(oddsDetail.oddArrayList.find { o -> o == odd }))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_grid_item, parent, false))


    override fun getItemCount(): Int = oddsDetail.oddArrayList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        Timber.d("===洗刷刷4 刷新布局 position：${position}")
        holder.bindModel(oddsDetail.gameType, oddsDetail.oddArrayList[position])
    }


    inner class ViewHolder(view: View) : OddStateViewHolderDetail(view) {

        private val btnOdds = itemView.findViewById<OddsButtonDetail>(R.id.button_odds)

        fun bindModel(gameType: String?, odd: Odd?) {
//            Timber.d("===洗刷刷-1 设置点击事件123")
            btnOdds?.apply {
                setupOdd(odd, oddsType, gameType, matchInfo = oddsDetail.matchInfo)
                setupOddState(this, odd)
                setOnClickListener {
//                    Timber.d("===洗刷刷-1 设置点击事件")
                    odd?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail) }
                }
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = mOddStateRefreshListener

    }


}