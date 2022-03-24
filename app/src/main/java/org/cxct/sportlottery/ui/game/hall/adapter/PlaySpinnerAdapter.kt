package org.cxct.sportlottery.ui.game.hall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import org.cxct.sportlottery.databinding.ItemPlaySpinnerBinding
import org.cxct.sportlottery.network.sport.query.PlayCate

class PlaySpinnerAdapter(playList: MutableList<PlayCate> = mutableListOf()) : BaseAdapter() {

    //加一項作為預設項目
    private val itemList = playList.apply { add(PlayCate("-99", "Default")) }

    //最後一項為預設項目，不顯示
    override fun getCount(): Int = itemList.size - 1

    override fun getItem(position: Int): Any = itemList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val binding: ItemPlaySpinnerBinding
        val viewHolder: PlayItemViewHolder
        if (convertView == null) {
            binding = ItemPlaySpinnerBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
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

    inner class PlayItemViewHolder(private val binding: ItemPlaySpinnerBinding) {
        fun bind(playCate: PlayCate, position: Int) {
            with(binding) {
                tvPlay.text = playCate.name
                ivTick.visibility = if (playCate.isSelected) View.VISIBLE else View.INVISIBLE
                viewDivider.visibility = if (position >= count - 1) View.GONE else View.VISIBLE
            }
        }
    }
}