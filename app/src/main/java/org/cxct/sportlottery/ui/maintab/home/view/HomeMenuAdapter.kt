package org.cxct.sportlottery.ui.maintab.home.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.inVisible
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemHomeMenuBinding
import org.cxct.sportlottery.databinding.ItemHomeMenuPageBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.home.game.GameVenueFragment
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment

class HomeMenuAdapter(private val itemClick: (View, Triple<Int, Int, Class<BaseFragment<*>>?>) -> Unit)
    : BindingAdapter<List<Triple<Int, Int, Class<BaseFragment<*>>?>>, ItemHomeMenuPageBinding>() {
    private val datas = mutableListOf(
        listOf(
            Triple(R.drawable.selector_home_menu_home, R.string.bottom_nav_home, HomeHotFragment::class.java),
            Triple(R.drawable.selector_home_menu_hot, R.string.home_recommend, GameVenueFragment::class.java),
            Triple(R.drawable.selector_home_menu_sport, R.string.main_tab_sport, null),
            Triple(R.drawable.selector_home_menu_casino, R.string.P230, GameVenueFragment::class.java),
            Triple(R.drawable.selector_home_menu_live, R.string.home_live, GameVenueFragment::class.java),
            Triple(R.drawable.selector_home_menu_esport, R.string.esports, GameVenueFragment::class.java),
            Triple(R.drawable.selector_home_menu_promotion, R.string.promotion, GameVenueFragment::class.java),
        ),
        listOf(
            Triple(R.drawable.selector_home_menu_service, R.string.btn_service, GameVenueFragment::class.java),
        ),
    )

    private var selectItem: Triple<Int, Int, Class<BaseFragment<*>>?>?=null

    init {
       setNewInstance(datas as MutableList<List<Triple<Int, Int, Class<BaseFragment<*>>?>>>)
    }

    override fun onBinding(
        position: Int,
        binding: ItemHomeMenuPageBinding,
        item: List<Triple<Int, Int, Class<BaseFragment<*>>?>>,
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
        item: List<Triple<Int, Int, Class<BaseFragment<*>>?>>) = binding.run {
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