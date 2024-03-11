package org.cxct.sportlottery.ui.sport.detail.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.clickDelay
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.common.PlayCode
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.detail.OddStateViewHolderDetail
import org.cxct.sportlottery.ui.sport.detail.OddsDetailListData
import org.cxct.sportlottery.ui.sport.detail.OnOddClickListener
import org.cxct.sportlottery.ui.sport.oddsbtn.OddsButtonDetail
import org.cxct.sportlottery.util.TextUtil

@SuppressLint("NotifyDataSetChanged")
class TypeOneListAdapter(
    private var oddsDetail: OddsDetailListData,
    private val onOddClickListener: OnOddClickListener,
    private val oddsType: OddsType,
    private val isOddPercentage: Boolean? = false,
    private val onMoreClickListener: OnMoreClickListener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mOddStateRefreshListener by lazy {
        object : OddStateViewHolderDetail.OddStateChangeListener {
            override fun refreshOddButton(odd: Odd) {
                notifyItemChanged(oddsDetail.needShowItem.indexOf(oddsDetail.needShowItem.find { o -> o == odd }))
            }
        }
    }

    var mOddsDetail: OddsDetailListData? = null
        set(value) {
            field = value
            oddsDetail = value as OddsDetailListData
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int {
        return when (onMoreClickListener) {
            null -> return ItemType.ITEM.ordinal
            else -> when (position) {
                oddsDetail.needShowItem.size -> ItemType.MORE.ordinal
                else -> ItemType.ITEM.ordinal
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ItemType.ITEM.ordinal) {
            val oddsBtn = OddsButtonDetail(parent.context)
            oddsBtn.layoutParams = LinearLayout.LayoutParams(-1, -2)
            ViewHolder(oddsBtn)
        } else {
            MoreViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.content_type_more_item, parent, false))
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        when (holder) {
            is ViewHolder -> holder.bindModel(oddsDetail.needShowItem[position])
            is MoreViewHolder -> {
                holder.bind()
            }
            else -> {
            }
        }

    override fun getItemCount(): Int {
        return if (onMoreClickListener == null)
            oddsDetail.needShowItem.size
        else oddsDetail.needShowItem.size + 1
    }

    inner class ViewHolder(private val btnOdds: OddsButtonDetail) : OddStateViewHolderDetail(btnOdds) {

        private fun checkKey(key: String): Boolean {
            return TextUtil.compareWithGameKey(oddsDetail.gameType, key)
        }

        fun bindModel(odd: Odd?) {

            btnOdds?.apply {
                setupOdd(odd,
                    oddsType,
                    isOddPercentage = isOddPercentage,
                    matchInfo = oddsDetail.matchInfo,
                    adapterName = TypeOneListAdapter::class.java.name
                )
                setupOddState(this, odd)
                clickDelay {
                    odd?.let { o -> onOddClickListener.getBetInfoList(o, oddsDetail) }
                }
                when {
                    checkKey(PlayCate.HT_FT.value) -> {
                        binding.tvName.text = when (odd?.playCode) {
                            PlayCode.HT_FT_H_H.value -> this.context.getString(R.string.odds_button_name_home) + "/" + this.context.getString(
                                R.string.odds_button_name_home)
                            PlayCode.HT_FT_H_D.value -> this.context.getString(R.string.odds_button_name_home) + "/" + this.context.getString(
                                R.string.draw_name)
                            PlayCode.HT_FT_H_C.value -> this.context.getString(R.string.odds_button_name_home) + "/" + this.context.getString(
                                R.string.odds_button_name_away)
                            PlayCode.HT_FT_D_H.value -> this.context.getString(R.string.draw_name) + "/" + this.context.getString(
                                R.string.odds_button_name_home)
                            PlayCode.HT_FT_D_D.value -> this.context.getString(R.string.draw_name) + "/" + this.context.getString(
                                R.string.draw_name)
                            PlayCode.HT_FT_D_C.value -> this.context.getString(R.string.draw_name) + "/" + this.context.getString(
                                R.string.odds_button_name_away)
                            PlayCode.HT_FT_C_H.value -> this.context.getString(R.string.odds_button_name_away) + "/" + this.context.getString(
                                R.string.odds_button_name_home)
                            PlayCode.HT_FT_C_D.value -> this.context.getString(R.string.odds_button_name_away) + "/" + this.context.getString(
                                R.string.draw_name)
                            PlayCode.HT_FT_C_C.value -> this.context.getString(R.string.odds_button_name_away) + "/" + this.context.getString(
                                R.string.odds_button_name_away)
                            else -> ""
                        }
                        binding.tvSpread.text = ""
                    }
                    oddsDetail.gameType.startsWith(PlayCate.DC.value) -> {
                        binding.tvName.text = when (odd?.playCode) {
                            PlayCode.DC_H_D.value -> this.context.getString(R.string.odds_button_name_home) + "/" + this.context.getString(
                                R.string.draw_name)
                            PlayCode.DC_C_D.value -> this.context.getString(R.string.odds_button_name_away) + "/" + this.context.getString(
                                R.string.draw_name)
                            PlayCode.DC_H_C.value -> this.context.getString(R.string.odds_button_name_home) + "/" + this.context.getString(
                                R.string.odds_button_name_away)
                            else -> ""
                        }
                        binding.tvSpread.text = ""
                    }
                }
            }
        }

        override val oddStateChangeListener: OddStateChangeListener
            get() = mOddStateRefreshListener
    }

    inner class MoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvExpandControl: TextView = itemView.findViewById(R.id.tv_expand_control)
        fun bind() {
            tvExpandControl.apply {
                setOnClickListener {
                    onMoreClickListener?.click()
                }
                text = if (oddsDetail.isMoreExpand) context.getString(R.string.odds_detail_less) else context.getString(R.string.display_more)
            }
        }
    }

    interface OnMoreClickListener {
        fun click()
    }

    enum class ItemType {
        ITEM, MORE
    }

}