package org.cxct.sportlottery.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener


class MarqueeAdapter : RecyclerView.Adapter<MarqueeAdapter.DetailViewHolder>() {
    private enum class Type { ITEM, BLANK }

    private var mDataList: MutableList<String> = mutableListOf()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DetailViewHolder {
        val layoutView = LayoutInflater.from(viewGroup.context).inflate(R.layout.content_marquee, viewGroup, false)

        //開頭結尾的空白過場，寬度設置跟 父層 Layout 一樣
        if (viewType == Type.BLANK.ordinal) {
            layoutView.minimumWidth = viewGroup.measuredWidth
        }

        return DetailViewHolder(layoutView)
    }

    private var mOnSelectItemListener: OnSelectItemListener<String>? = null

    override fun getItemCount(): Int {
//        return if (mDataList.size == 0) 0 else Integer.MAX_VALUE
        //20190918 記錄問題: 使用 Integer.MAX_VALUE 的方式做輪播效果，在 android 4.4 系統手機會造成 StackOverFlowError 錯誤而 crash
        //現在使用監聽滑至底時，再從頭來過達到輪播效果
        return mDataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0, mDataList.lastIndex -> Type.BLANK.ordinal
            else -> Type.ITEM.ordinal
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: DetailViewHolder, position: Int) {
        try {
            viewHolder.detail.text = mDataList[position] + "　"

            viewHolder.detail.setOnClickListener {
                if (position != 0 && position != mDataList.lastIndex) //自己添加的開頭結尾空白過場要排除
                    mOnSelectItemListener?.onClick((position-1).toString())
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setOnSelectItemListener(onSelectItemListener: OnSelectItemListener<String>) {
        mOnSelectItemListener = onSelectItemListener
    }

    fun setData(dataList: MutableList<String>?) {
        mDataList = dataList?: mutableListOf()

        if (mDataList.isEmpty()) {
            mDataList.add("(${MultiLanguagesApplication.appContext.getString(R.string.no_announcement)})")

        } else {
            //多兩個欄位，當開頭跟結尾的空白過場
            mDataList.add(0, "")
            mDataList.add("")
        }

        notifyDataSetChanged()//更新資料
    }


    class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var detail: TextView = itemView.findViewById(R.id.tv_marquee)
    }

}