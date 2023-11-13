package org.cxct.sportlottery.ui.maintab.home.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.inVisible
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemHomeMenuPageBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.home.game.slot.ElecGamesFragement
import org.cxct.sportlottery.ui.maintab.home.game.sport.SportVenueFragment
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment

class HomeMenuAdapter(private val itemClick: (View, Triple<Int, Int, Class<BaseFragment<*>>?>) -> Unit)
    : BindingAdapter<Array<Triple<Int, Int, Class<BaseFragment<*>>?>>, ItemHomeMenuPageBinding>() {
    private val datas = mutableListOf(
        arrayOf(
            Triple(R.drawable.selector_home_menu_home, R.string.bottom_nav_home, HomeHotFragment::class.java),
            Triple(R.drawable.selector_home_menu_hot, R.string.home_recommend, SportVenueFragment::class.java),
            Triple(R.drawable.selector_home_menu_sport, R.string.main_tab_sport, null),
            Triple(R.drawable.selector_home_menu_casino, R.string.P230, ElecGamesFragement::class.java),
            Triple(R.drawable.selector_home_menu_live, R.string.home_live, ElecGamesFragement::class.java),
            Triple(R.drawable.selector_home_menu_esport, R.string.esports, SportVenueFragment::class.java),
            Triple(R.drawable.selector_home_menu_promotion, R.string.promotion, SportVenueFragment::class.java),
        ),
        arrayOf(
            Triple(R.drawable.selector_home_menu_service, R.string.btn_service, SportVenueFragment::class.java),
        ),
    )

    private var selectItem: Triple<Int, Int, Class<BaseFragment<*>>?>?=null

    init {
        selectItem = datas[0][0] as Triple<Int, Int, Class<BaseFragment<*>>?>
        setNewInstance(datas as MutableList<Array<Triple<Int, Int, Class<BaseFragment<*>>?>>>)
    }

    override fun onBinding(
        position: Int,
        binding: ItemHomeMenuPageBinding,
        item: Array<Triple<Int, Int, Class<BaseFragment<*>>?>>,
        payloads: List<Any>
    ) {
        binding.root.children.forEachIndexed { index, view ->
            if (index>=item.size){
                view.inVisible()
            }else{
                view.visible()
                view.isSelected = item[index] == selectItem
            }
        }
    }


    override fun onBinding(
        position: Int,
        binding: ItemHomeMenuPageBinding,
        item: Array<Triple<Int, Int, Class<BaseFragment<*>>?>>) = binding.run {
        binding.root.children.forEachIndexed { index, view ->
            if (index>=item.size){
                view.inVisible()
            }else{
                val itemChild = item[index]
                view.visible()
                view.setOnClickListener {
                    selectItem  = itemChild
                    notifyDataSetChanged()
                    itemClick(it, itemChild)
                }
                view.findViewById<ImageView>(R.id.ivIcon).setImageResource(itemChild.first)
                view.findViewById<TextView>(R.id.tvName).setText(itemChild.second)
                view.isSelected = itemChild == selectItem
            }
        }
    }

}