package org.cxct.sportlottery.ui.maintab.home.view

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemHomeMenuBinding
import org.cxct.sportlottery.ui.maintab.entity.HomeMenuBean

class HomeMenuAdapter : BindingAdapter<HomeMenuBean, ItemHomeMenuBinding>() {
    private val datas =
        mutableListOf(
            HomeMenuBean.HOT,
            HomeMenuBean.SPORT,
            HomeMenuBean.CASINO,
            HomeMenuBean.LIVE,
            HomeMenuBean.ESPORT,
            HomeMenuBean.PROMOTION,
            HomeMenuBean.SERVICE,
         )
    var selectPos = -1
    set(value) {
        field =value
        notifyDataSetChanged()
    }

    init {
       setNewInstance(datas)
    }

    override fun onBinding(
        position: Int,
        binding: ItemHomeMenuBinding,
        item: HomeMenuBean,
    ) {
       binding.ivIcon.setImageResource(item.iconRes)
       binding.tvName.text = context.getString(item.title)
       binding.root.isSelected = selectPos==position
    }

}