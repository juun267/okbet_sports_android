package org.cxct.sportlottery.ui.menu.results

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_settlement_rv.view.*
import org.cxct.sportlottery.databinding.ContentSettlementRvBinding

class SettlementRvAdapter() : RecyclerView.Adapter<SettlementRvAdapter.ItemViewHolder>() {

    var mDataList = listOf<SettlementItem>()
        set(value) {
            field = value

            mIsOpenList =
                MutableList(value.size) { false }//創建一個跟 DataList 一樣的 size，value 都為 true 的 List

            notifyDataSetChanged()
        }

    private var mIsOpenList: MutableList<Boolean> = mutableListOf()


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutView = LayoutInflater.from(viewGroup.context)
        val binding = ContentSettlementRvBinding.inflate(layoutView, viewGroup, false)
        return ItemViewHolder(binding)
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
                this.block_drawer_result.let { expandableLayout ->
                    expandableLayout.setExpanded(
                        !(expandableLayout.isExpanded),
                        true
                    )
                }
                mIsOpenList[viewHolder.adapterPosition] = !mIsOpenList[viewHolder.adapterPosition]
            }

            rv_game_result.adapter = GameResultRvAdapter()
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    class ItemViewHolder internal constructor(private val binding: ContentSettlementRvBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }
}