package org.cxct.sportlottery.ui.game.home.gameDrawer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.home_game_rv_header.view.*
import kotlinx.android.synthetic.main.home_game_rv_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener

class RvGameDrawerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mOnSelectItemListener: OnSelectItemListener<GameEntity>? = null
    private var mOnSelectFooterListener: OnSelectItemListener<GameEntity>? = null
    private var mDataList: MutableList<GameEntity> = mutableListOf()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.HEADER.ordinal -> {
                val layout = LayoutInflater.from(viewGroup.context).inflate(R.layout.home_game_rv_header, viewGroup, false)
                HeaderViewHolder(layout)
            }

            else -> {
                val layout = LayoutInflater.from(viewGroup.context).inflate(R.layout.home_game_rv_item, viewGroup, false)
                ItemViewHolder(layout)
            }
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return mDataList[position].itemType.ordinal
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        try {
            val data = mDataList[position]
            when (viewHolder) {
                is HeaderViewHolder -> updateViewHolderUI(viewHolder, data)
                is ItemViewHolder -> updateViewHolderUI(viewHolder, data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateViewHolderUI(viewHolder: HeaderViewHolder, data: GameEntity) {
        viewHolder.itemView.apply {
            tv_game.text = data.name
        }
    }

    private fun updateViewHolderUI(viewHolder: ItemViewHolder, data: GameEntity) {
        viewHolder.itemView.apply {
            team1.text = data.match?.homeName
            team2.text = data.match?.awayName

            //TODO simon test tv_time 要改掉
            tv_time.text = "HH:mm:ss"
            if (data.itemType == ItemType.FOOTER) {
                line_item.visibility = View.GONE
                card_footer.visibility = View.VISIBLE
                line_footer.visibility = View.VISIBLE
            } else {
                line_item.visibility = View.VISIBLE
                card_footer.visibility = View.GONE
                line_footer.visibility = View.GONE
            }

            tv_footer_title.text = String.format(context.getString(R.string.label_all_something_in_play), data.name)
            tv_footer_count.text = data.num.toString()

            card_item.setOnClickListener {
                if (data.match != null)
                    mOnSelectItemListener?.onClick(data)
            }

            card_footer.setOnClickListener {
                mOnSelectFooterListener?.onClick(data)
            }
        }
    }

    fun setOnSelectItemListener(onSelectItemListener: OnSelectItemListener<GameEntity>?) {
        mOnSelectItemListener = onSelectItemListener
    }

    fun setOnSelectFooterListener(onSelectFooterListener: OnSelectItemListener<GameEntity>?) {
        mOnSelectFooterListener = onSelectFooterListener
    }

    fun setData(dataList: MutableList<GameEntity>?) {
        mDataList = dataList ?: mutableListOf()
        notifyDataSetChanged()
    }


    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}