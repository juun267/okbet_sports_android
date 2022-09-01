package org.cxct.sportlottery.ui.sport

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import org.cxct.sportlottery.databinding.ItemSportSpinnerBinding
import org.cxct.sportlottery.network.sport.Item

class SportTypeTextAdapter(dataItems: List<Item> = listOf()) : BaseAdapter() {

    //加一項作為預設項目
    private val itemList = dataItems

    //最後一項為預設項目，不顯示
    override fun getCount(): Int = itemList.size

    override fun getItem(position: Int): Any = itemList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val binding: ItemSportSpinnerBinding
        val viewHolder: PlayItemViewHolder
        if (convertView == null) {
            binding =
                ItemSportSpinnerBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
            view = binding.root
            viewHolder = PlayItemViewHolder(binding)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as PlayItemViewHolder
        }

        viewHolder.bind(itemList[position], position)

        return view
    }

    inner class PlayItemViewHolder(private val binding: ItemSportSpinnerBinding) {
        fun bind(data: Item, position: Int) {
            with(binding) {
                tvPlay.text = data.name
//                ivTick.visibility = if (data.isChecked) View.VISIBLE else View.INVISIBLE
                viewDivider.visibility = if (position >= count - 1) View.GONE else View.VISIBLE
            }
        }
    }
}