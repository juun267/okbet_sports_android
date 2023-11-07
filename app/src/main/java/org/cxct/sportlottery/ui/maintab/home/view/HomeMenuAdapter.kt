package org.cxct.sportlottery.ui.maintab.home.view

import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.databinding.ItemHomeMenuBinding
import org.cxct.sportlottery.databinding.ItemHomeMenuPageBinding

class HomeMenuAdapter : BindingAdapter<MutableList<HomeMenuBean>, ItemHomeMenuPageBinding>() {
    private val datas =
        mutableListOf(
            mutableListOf(
                HomeMenuBean(1, 1),
                HomeMenuBean(1, 1),
                HomeMenuBean(1, 1),
                HomeMenuBean(1, 1),
                HomeMenuBean(1, 1),
                HomeMenuBean(1, 1),
            ),
            mutableListOf(
                HomeMenuBean(1, 1),
            )
         )


    init {
       setNewInstance(datas)
    }

    override fun onBinding(
        position: Int,
        binding: ItemHomeMenuPageBinding,
        item: MutableList<HomeMenuBean>,
    ) {

    }
}

@KeepMembers
data class HomeMenuBean(
    val nameResId: Int,
    val IconResId: Int,
)