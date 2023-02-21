package org.cxct.sportlottery.ui.component

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.item_play_spinner.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemPlaySpinnerNewBinding
import org.cxct.sportlottery.ui.common.StatusSheetData

class StatusSpinnerNewAdapter(dataItems: MutableList<StatusSheetData> = mutableListOf()) : BaseAdapter() {

    //加一項作為預設項目
    private val itemList = dataItems

    private var color = 0;
    private var selectColor = 0;
    private var maxWidth = 0

    //最後一項為預設項目，不顯示
    override fun getCount(): Int = itemList.size

    override fun getItem(position: Int): Any = itemList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val binding: ItemPlaySpinnerNewBinding
        val viewHolder: PlayItemViewHolder
        if (convertView == null) {
            binding = ItemPlaySpinnerNewBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
            view = binding.root
            viewHolder = PlayItemViewHolder(binding)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as PlayItemViewHolder
        }
        view.tvPlay.setTextColor(color)
        viewHolder.bind(itemList[position], position)
        return view
    }

    fun setItmeColor(itemColor: Int){
        this.color = itemColor
    }

    inner class PlayItemViewHolder(private val binding: ItemPlaySpinnerNewBinding) {
        fun bind(data: StatusSheetData, position: Int) {
            with(binding) {
                tvPlay.text = data.showName
                if (data.isChecked) {
                    tvPlay.setTextColor(tvPlay.context.getColor(R.color.color_025BE8))
                } else {

                }
              //  ivTick.visibility = if (data.isChecked) View.VISIBLE else View.INVISIBLE
                viewDivider.visibility = if (position >= count - 1) View.GONE else View.VISIBLE
            }

        }
    }
}