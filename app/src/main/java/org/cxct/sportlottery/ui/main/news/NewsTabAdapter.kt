package org.cxct.sportlottery.ui.main.news

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_news_tab_rv.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.message.Row

class NewsTabAdapter(val context: Context?, private val mMessageList: List<Row>?) : RecyclerView.Adapter<NewsTabAdapter.ViewHolder>() {

    private var mSelectedPosition = 0
    private var mOnSelectItemListener: OnSelectItemListener<TabEntity>? = null

    //消息类型：1：游戏公告，2：会员福利，3：转账须知，4：劲爆推荐，5：导航网，6：其他
    private val mDataList by lazy {
        val showTabEntity = mutableListOf<TabEntity>()
        val allTabEntity = mutableListOf(
            TabEntity(1, context?.getString(R.string.game_announcement)),
            TabEntity(2, context?.getString(R.string.member_benefits)),
            TabEntity(3, context?.getString(R.string.transfer_notes)),
            TabEntity(4, context?.getString(R.string.best_recommend)),
            TabEntity(5, context?.getString(R.string.navigation_web)),
            TabEntity(6, context?.getString(R.string.other))
        )
        allTabEntity.forEach { tabEntity ->
            if (mMessageList?.any { tabEntity.msgType == it.msgType } == true)
                showTabEntity.add(tabEntity)
        }

        showTabEntity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.content_news_tab_rv, parent, false)
        view.minimumWidth = parent.measuredWidth / 3 //三等分
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val data = mDataList[position]
            holder.bind(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int = mDataList.size

    fun setOnSelectItemListener(onSelectItemListener: OnSelectItemListener<TabEntity>?) {
        mOnSelectItemListener = onSelectItemListener
    }

    fun selectItem(position: Int) {
        if (mDataList.isNotEmpty() && position in 0..mDataList.lastIndex) {
            val data = mDataList[position]
            notifyItemChanged(mSelectedPosition)
            mSelectedPosition = position
            notifyItemChanged(position)
            mOnSelectItemListener?.onClick(data)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: TabEntity) {
            itemView.apply {
                txv_tab.text = data.title
                itemView.setOnClickListener { selectItem(layoutPosition) }
                itemView.isSelected = mSelectedPosition == layoutPosition //選中改變背景

                //被選中時字體加粗
                val typeface = if (itemView.isSelected) Typeface.BOLD else Typeface.NORMAL
                txv_tab.setTypeface(null, typeface)
            }
        }
    }

    class TabEntity(val msgType: Long, val title: String?)

}