package org.cxct.sportlottery.ui.maintab.home.view

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.databinding.ItemHomeMenuBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.home.game.GameVenueFragment
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment

class HomeMenuAdapter(private val itemClick: (View, Triple<Int, Int, Class<BaseFragment<*>>?>) -> Unit)
    : BindingAdapter<Triple<Int, Int, Class<BaseFragment<*>>?>, ItemHomeMenuBinding>()
    , OnItemClickListener {

    private val datas = mutableListOf(
        Triple(R.drawable.selector_home_menu_home, R.string.bottom_nav_home, HomeHotFragment::class.java),
        Triple(R.drawable.selector_home_menu_hot, R.string.home_recommend, GameVenueFragment::class.java),
        Triple(R.drawable.selector_home_menu_sport, R.string.main_tab_sport, null),
        Triple(R.drawable.selector_home_menu_casino, R.string.P230, GameVenueFragment::class.java),
        Triple(R.drawable.selector_home_menu_live, R.string.home_live, GameVenueFragment::class.java),
        Triple(R.drawable.selector_home_menu_esport, R.string.esports, GameVenueFragment::class.java),
        Triple(R.drawable.selector_home_menu_promotion, R.string.promotion, GameVenueFragment::class.java),
        Triple(R.drawable.selector_home_menu_service, R.string.btn_service, GameVenueFragment::class.java),
    )

    private var selectPos = 0

    init {
       setNewInstance(datas as MutableList<Triple<Int, Int, Class<BaseFragment<*>>?>>)
       setOnItemClickListener(this)
    }

    override fun onBinding(
        position: Int,
        binding: ItemHomeMenuBinding,
        item: Triple<Int, Int, Class<BaseFragment<*>>?>,
        payloads: List<Any>
    ) {
        binding.root.isSelected = selectPos == position
    }

    override fun onBinding(
        position: Int,
        binding: ItemHomeMenuBinding,
        item: Triple<Int, Int, Class<BaseFragment<*>>?>) = binding.run {
       ivIcon.setImageResource(item.first)
       tvName.text = context.getString(item.second)
        root.isSelected = selectPos == position
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val last = selectPos
        selectPos = position
        notifyItemChanged(position, position)
        notifyItemChanged(last, last)
        itemClick(view, getItem(position))
    }

}