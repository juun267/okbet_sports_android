package org.cxct.sportlottery.ui.maintab

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R

class HomeFlipperAdapter : RecyclerView.Adapter<HomeFlipperAdapter.DetailViewHolder>() {
    protected enum class Type { ITEM, BLANK }

    private var mDataList: MutableList<String> = mutableListOf()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DetailViewHolder {
        val layoutView = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.content_marquee, viewGroup, false)

        //開頭結尾的空白過場，寬度設置跟 父層 Layout 一樣
        if (viewType == Type.BLANK.ordinal) layoutView.minimumHeight = viewGroup.measuredHeight

        return DetailViewHolder(layoutView)
    }

    override fun getItemCount(): Int {
        return if (mDataList.size > 0) Int.MAX_VALUE else 0
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> Type.BLANK.ordinal
            else -> Type.ITEM.ordinal
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: DetailViewHolder, position: Int) {
        try {
            if (getItemViewType(position) == Type.ITEM.ordinal && mDataList.size != 0) {
                val dataPosition = (position - 1) % mDataList.size
                viewHolder.detail.text = mDataList[dataPosition] + "　"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setData(dataList: MutableList<String>?) {
        mDataList = dataList ?: mutableListOf()

        if (mDataList.isEmpty())
            mDataList.add("(${MultiLanguagesApplication.appContext.getString(R.string.no_announcement)})")

        notifyDataSetChanged()//更新資料
    }


    open class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        open var detail: TextView = itemView.findViewById(R.id.tv_marquee)
    }

}