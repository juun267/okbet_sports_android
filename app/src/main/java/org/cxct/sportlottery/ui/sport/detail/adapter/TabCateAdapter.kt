package org.cxct.sportlottery.ui.sport.detail.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.TabOddsDetailBinding
import org.cxct.sportlottery.network.odds.detail.PlayCateType
import org.cxct.sportlottery.util.*

@SuppressLint("NotifyDataSetChanged")
class TabCateAdapter(val onItemSelected: (position: Int) -> Unit) :
    BindingAdapter<PlayCateType,TabOddsDetailBinding>() {

    open var selectedPosition: Int = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBinding(position: Int, binding: TabOddsDetailBinding, item: PlayCateType) {
        val isSelected = position == selectedPosition
        binding.tvTab.isSelected = isSelected
        binding.tvTab.paint.isFakeBoldText = isSelected
        binding.imgIcon.isVisible = isSelected
        binding.tvTab.text = item?.name
        binding.root.setOnClickListener {
            selectedPosition = position
            onItemSelected.invoke(position)
        }
    }
}