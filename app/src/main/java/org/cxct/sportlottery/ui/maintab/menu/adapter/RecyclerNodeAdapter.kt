package org.cxct.sportlottery.ui.maintab.menu.adapter

import android.graphics.Typeface
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemSingleNodeBinding
import org.cxct.sportlottery.ui.maintab.entity.NodeBean
import org.cxct.sportlottery.view.setColors

class RecyclerNodeAdapter: BindingAdapter<NodeBean,ItemSingleNodeBinding>() {
    override fun onBinding(position: Int, binding: ItemSingleNodeBinding, item: NodeBean) {
        binding.tvName.text=item.name
        if(item.icon==null){
            binding.ivIcon.gone()
        }else{
            binding.ivIcon.visible()
            binding.ivIcon.load(item.icon)
        }
        if(item.select){
            binding.ivCheckBox.setImageResource(R.drawable.ic_left_check_press)
            binding.tvName.setColors(R.color.color_025BE8)
            binding.tvName.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
        }else{
            binding.ivCheckBox.setImageResource(R.drawable.ic_left_check_normal)
            binding.tvName.setColors(R.color.color_6D7693)
            binding.tvName.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        }
    }
}