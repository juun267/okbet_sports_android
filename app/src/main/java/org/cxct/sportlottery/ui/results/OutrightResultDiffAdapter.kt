package org.cxct.sportlottery.ui.results

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_outright_result_outright.view.*
import kotlinx.android.synthetic.main.item_outright_result_title.view.*
import kotlinx.android.synthetic.main.item_outright_result_title.view.iv_arrow
import kotlinx.android.synthetic.main.item_outright_result_title.view.ll_title_background
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.TimeUtil

class OutrightResultDiffAdapter(private val outrightItemClickListener: OutrightItemClickListener) : ListAdapter<OutrightResultData, RecyclerView.ViewHolder>(OutrightResultDiffCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            OutrightType.TITLE.ordinal -> {
                OutrightTitleViewHolder.from(parent)
            }
            OutrightType.OUTRIGHT.ordinal -> {
                OutrightViewHolder.from(parent)
            }
            else -> NoDataViewHolder.from(parent)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).dataType.ordinal
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is OutrightTitleViewHolder -> {
                holder.apply {
                    bind(getItem(adapterPosition), outrightItemClickListener)
                }
            }
            is OutrightViewHolder -> {
                holder.apply {
                    setupBottomLine(position, holder.bottomLine)
                    bind(getItem(adapterPosition))
                }
            }
        }
    }

    private fun setupBottomLine(position: Int, bottomLine: View) {
        bottomLine.visibility = if (position + 1 < itemCount && getItemViewType(position + 1) != OutrightType.TITLE.ordinal) View.VISIBLE else View.GONE
    }

    class OutrightTitleViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.item_outright_result_title, viewGroup, false)
                return OutrightTitleViewHolder(view)
            }
        }

        fun bind(outrightResultData: OutrightResultData, outrightItemClickListener: OutrightItemClickListener) {
            setupView(itemView, outrightResultData)
            setupEvent(itemView, outrightResultData, outrightItemClickListener)
        }

        private fun setupView(itemView: View, outrightResultData: OutrightResultData) {
            itemView.apply {
                val seasonData = outrightResultData.seasonData
                tv_season.text = seasonData?.name
                titleArrowRotate(itemView, outrightResultData)
            }
        }

        private fun setupEvent(itemView: View, outrightResultData: OutrightResultData, outrightItemClickListener: OutrightItemClickListener) {
            itemView.apply {
                setOnClickListener {
                    outrightItemClickListener.seasonClick(outrightResultData)
                    titleArrowRotate(itemView, outrightResultData)
                }
            }
        }


        private fun titleArrowRotate(itemView: View, outrightResultData: OutrightResultData) {
            itemView.apply {
                if (outrightResultData.seasonExpanded) {
                    ll_title_background.setBackgroundResource(R.drawable.bg_shape_top_8dp_blue_stroke_no_bottom_stroke)
                    iv_arrow.rotation = 0f
                } else {
                    ll_title_background.setBackgroundResource(R.drawable.bg_shape_8dp_blue_stroke)
                    iv_arrow.rotation = 180f
                }
            }
        }
    }

    class OutrightViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(viewGroup: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(viewGroup.context)
                val view = layoutInflater.inflate(R.layout.item_outright_result_outright, viewGroup, false)
                return OutrightViewHolder(view)
            }
        }

        val bottomLine: View = itemView.findViewById(R.id.bottom_line)

        fun bind(outrightResultData: OutrightResultData) {
            itemView.apply {
                val seasonData = outrightResultData.seasonData
                val outrightData = outrightResultData.outrightData
                if (seasonData?.end != null) tv_date.text = TimeUtil.timeFormat(seasonData.end, TimeUtil.YMD_HM_FORMAT_2)
                tv_content.text = outrightData?.playCateName
                tv_winner.text = outrightData?.playName

                when (outrightResultData.isLastOutrightData) {
                    true -> {
                        tv_date.setBackgroundResource(R.drawable.bg_shape_bottom_8dp_gray_stroke_no_top_stroke)
                    }
                    false -> {
                        tv_date.setBackgroundResource(R.drawable.bg_no_top_bottom_stroke_gray)
                    }
                }
            }
        }
    }

    //無資料
    class NoDataViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun from(parent: ViewGroup): NoDataViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_game_no_record, parent, false)

                return NoDataViewHolder(view)
            }
        }
    }
}

class OutrightResultDiffCallBack : DiffUtil.ItemCallback<OutrightResultData>() {
    override fun areItemsTheSame(oldItem: OutrightResultData, newItem: OutrightResultData): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: OutrightResultData, newItem: OutrightResultData): Boolean {
        return oldItem == newItem
    }

}

class OutrightItemClickListener(private val seasonClickListener: (clickItem: OutrightResultData) -> Unit) {
    fun seasonClick(clickItem: OutrightResultData) = seasonClickListener.invoke(clickItem)
}