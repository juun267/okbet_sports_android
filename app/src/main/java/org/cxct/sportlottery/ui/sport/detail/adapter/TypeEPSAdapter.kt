package org.cxct.sportlottery.ui.sport.detail.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.detail.OddStateViewHolderDetail
import org.cxct.sportlottery.ui.sport.detail.OddsDetailListData
import org.cxct.sportlottery.ui.sport.detail.OnOddClickListener
import org.cxct.sportlottery.ui.sport.oddsbtn.OddsButtonDetail


/**
 * @author kevin
 * @create 2021/7/30
 * @description
 */
class TypeEPSAdapter : RecyclerView.Adapter<TypeEPSAdapter.ViewHolder>() {

    private var oddsDetail: OddsDetailListData? = null
    private var onOddClickListener: OnOddClickListener? = null
    private var oddsType: OddsType? = null

    fun setData(
        oddsDetail: OddsDetailListData,
        onOddClickListener: OnOddClickListener,
        oddsType: OddsType
    ) {
        this.oddsDetail = oddsDetail
        this.onOddClickListener = onOddClickListener
        this.oddsType = oddsType
    }


    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolderDetail.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                oddsDetail?.oddArrayList?.apply {
                    notifyItemChanged(indexOf(oddsDetail?.oddArrayList?.find { o -> o == odd }))
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.content_type_eps_item, parent, false)
        )


    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bindModel(oddsDetail?.oddArrayList?.getOrNull(position))


    override fun getItemCount(): Int = oddsDetail?.oddArrayList?.size ?: 0


    inner class ViewHolder(view: View) : OddStateViewHolderDetail(view) {

        private val tvName = itemView.findViewById<TextView>(R.id.tv_name)

        private val btnOdds = itemView.findViewById<OddsButtonDetail>(R.id.button_odds)

        fun bindModel(odd: Odd?) {
            tvName.text = odd?.name

            btnOdds.apply {
                setupOddState(this, odd)
                clickDelay {
                    odd?.let { odd ->
                        onOddClickListener?.getBetInfoList(
                            odd,
                            oddsDetail
                        )
                    }
                }
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = mOddStateRefreshListener
    }


}