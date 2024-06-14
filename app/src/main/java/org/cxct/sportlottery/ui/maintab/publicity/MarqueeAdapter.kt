package org.cxct.sportlottery.ui.maintab.publicity

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R

open class MarqueeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_ITEM = 100
        const val TYPE_BLANK = 200
    }

    private var mDataList: MutableList<String> = mutableListOf()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val textView = AppCompatTextView(viewGroup.context)
        textView.layoutParams = ViewGroup.LayoutParams(-2, -1)
        textView.gravity = Gravity.CENTER_VERTICAL
        textView.textSize = 14.0f
        textView.maxLines = 1
        textView.setTextColor(viewGroup.context.getColor(R.color.color_6D7693))
        //開頭結尾的空白過場，寬度設置跟 父層 Layout 一樣
        if (viewType == TYPE_BLANK) textView.minimumWidth = viewGroup.measuredWidth

        return MarqueeVH(textView, textView)
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
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        try {
            if (getItemViewType(position) == TYPE_ITEM && mDataList.size != 0) {
                val dataPosition = (position - 1) % mDataList.size
                (viewHolder.itemView as TextView).text = mDataList[dataPosition] + "　"
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

    class MarqueeVH(val textView: TextView, itemView: View): RecyclerView.ViewHolder(itemView)


}