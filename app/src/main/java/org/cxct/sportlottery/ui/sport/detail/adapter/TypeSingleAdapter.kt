package org.cxct.sportlottery.ui.sport.detail.adapter


import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.clickDelay
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val oddsBtn = OddsButtonDetail(parent.context)
        oddsBtn.layoutParams = LinearLayout.LayoutParams(-1, -2)
        return ViewHolder(oddsBtn)
    }


    override fun getItemCount(): Int = oddsDetail.oddArrayList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        Timber.d("===洗刷刷4 刷新布局 position：${position}")
        holder.bindModel(oddsDetail.gameType, oddsDetail.oddArrayList[position])
    }


    inner class ViewHolder(private val btnOdds: OddsButtonDetail) : OddStateViewHolderDetail(btnOdds) {


        fun bindModel(gameType: String?, odd: Odd?) = btnOdds.run {
//            Timber.d("===洗刷刷-1 设置点击事件123")

            setupOdd(odd, oddsType, gameType, matchInfo = oddsDetail.matchInfo)
            setupOddState(this, odd)
            clickDelay {
//                    Timber.d("===洗刷刷-1 设置点击事件")
                odd?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail) }
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = mOddStateRefreshListener

    }


}