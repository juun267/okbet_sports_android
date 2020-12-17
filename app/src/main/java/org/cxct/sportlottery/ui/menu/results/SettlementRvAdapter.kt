package org.cxct.sportlottery.ui.menu.results

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_settlement_rv.view.*
import net.cachapa.expandablelayout.ExpandableLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentSettlementRvBinding

class SettlementRvAdapter() : RecyclerView.Adapter<SettlementRvAdapter.ItemViewHolder>() {

    private var mIsOpenList: MutableList<Boolean> = mutableListOf()
    private var mDataList: List<SettlementItem> = listOf()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutView = LayoutInflater.from(viewGroup.context)
        val binding = ContentSettlementRvBinding.inflate(layoutView, viewGroup, false)
        val viewHolder = ItemViewHolder(binding)
        return viewHolder
    }

    override fun onBindViewHolder(viewHolder: ItemViewHolder, position: Int) {
        viewHolder.itemView.apply {
            tv_type.text = mDataList[position].gameType
            if (mIsOpenList[position]) { //TODO Dean : 箭頭旋轉
                block_drawer_result.expand(false)
            } else {
                block_drawer_result.collapse(false)
            }
            block_type.setOnClickListener {
                Log.e("Dean" , "click")
                this.block_drawer_result.let { expandableLayout ->  expandableLayout.setExpanded(!(expandableLayout.isExpanded), true) }
                mIsOpenList[viewHolder.adapterPosition] = !mIsOpenList[viewHolder.adapterPosition]
            }
            rv_game_result.adapter = GameResultRvAdapter()
        }
    }

    fun setData(mDataList: List<SettlementItem>){
        this.mDataList = mDataList
        mIsOpenList = MutableList(mDataList.size) { false } //創建一個跟 DataList 一樣的 size，value 都為 true 的 List
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }
    class ItemViewHolder internal constructor(private val binding: ContentSettlementRvBinding) : RecyclerView.ViewHolder(binding.root){
}
}