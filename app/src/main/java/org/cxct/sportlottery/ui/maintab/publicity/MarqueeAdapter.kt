package org.cxct.sportlottery.ui.maintab.publicity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R


open class MarqueeAdapter : RecyclerView.Adapter<MarqueeAdapter.DetailViewHolder>() {

    companion object {
        const val TYPE_ITEM = 100
        const val TYPE_BLANK = 200
    }

    private var mDataList: MutableList<String> = mutableListOf()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DetailViewHolder {
        val layoutView = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.content_marquee, viewGroup, false)

        //開頭結尾的空白過場，寬度設置跟 父層 Layout 一樣
        if (viewType == TYPE_BLANK) layoutView.minimumWidth = viewGroup.measuredWidth

        return DetailViewHolder(layoutView)
    }

    override fun getItemCount(): Int {
        return if (mDataList.size > 0) Int.MAX_VALUE else 0
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_BLANK
            else -> TYPE_ITEM
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: DetailViewHolder, position: Int) {
        try {
            if (getItemViewType(position) == TYPE_ITEM && mDataList.size != 0) {
                val dataPosition = (position - 1) % mDataList.size
                viewHolder.detail.text = mDataList[dataPosition] + "　"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setData(dataList: MutableList<String>?) {
        mDataList = dataList ?: mutableListOf()

//        if (mDataList.isEmpty())
//            mDataList.add("(${MultiLanguagesApplication.appContext.getString(R.string.no_announcement)})")

        notifyDataSetChanged()//更新資料
    }


    open class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        open var detail: TextView = itemView.findViewById(R.id.tv_marquee)
    }

}