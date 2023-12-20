package org.cxct.sportlottery.ui.maintab.home.view

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.inVisible
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemHomeMenuBinding
import org.cxct.sportlottery.databinding.ItemHomeMenuPageBinding
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.home.game.esport.ESportVenueFragment
import org.cxct.sportlottery.ui.maintab.home.game.live.LiveGamesFragment
import org.cxct.sportlottery.ui.maintab.home.game.slot.ElectGamesFragment
import org.cxct.sportlottery.ui.maintab.home.game.sport.SportVenueFragment
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.util.*

class HomeMenuAdapter(private val itemClick: (MenuTab) -> Boolean)
    : BindingAdapter<Array<HomeMenuAdapter.MenuTab?>, ItemHomeMenuPageBinding>() {

    val initiallyPosition = 9000000
    private val pageSize = 6
    private var selectedBg = R.drawable.bg_home_menu_sel
    private var normalBg = R.drawable.bg_home_menu_nor

    data class MenuTab(@DrawableRes val selectedIcon: Int,
                       @DrawableRes val norIcon: Int,
                       @StringRes val name: Int,
                       val content: Class<out BaseFragment<*>>?)

    private val datas = mutableListOf<MenuTab?>()

//    private val hotMenuItem = MenuTab(R.drawable.ic_home_menu_hot_sel, R.drawable.ic_home_menu_hot_nor, R.string.home_recommend, HomeHotFragment::class.java)
//    private val sportMenuItem = MenuTab(R.drawable.ic_home_menu_sport_sel, R.drawable.ic_home_menu_sport_nor, R.string.main_tab_sport, SportVenueFragment::class.java)
//    private val esportMenuItem = MenuTab(R.drawable.ic_home_menu_esport_sel, R.drawable.ic_home_menu_esport_nor, R.string.esports, ESportVenueFragment::class.java)
//    private val okGameMenuItem = MenuTab(R.drawable.ic_home_menu_live_sel, R.drawable.ic_home_menu_live_nor, R.string.J203, ElectGamesFragment::class.java)
//    private val okLiveGameItem = MenuTab(R.drawable.ic_home_menu_casino_sel, R.drawable.ic_home_menu_casino_nor, R.string.P160, LiveGamesFragment::class.java)
//    private val promotionMenuItem = MenuTab(R.drawable.ic_home_menu_promotion_sel, R.drawable.ic_home_menu_promotion_nor, R.string.promo, null)
//    private val sericeMenuItem = MenuTab(R.drawable.ic_home_menu_service_sel, R.drawable.ic_home_menu_service_nor, R.string.LT050_1, null)


    private val hotMenuItem = MenuTab(R.drawable.ic_chris_home_menu_hot_sel, R.drawable.ic_chris_home_menu_hot_nor, R.string.home_recommend, HomeHotFragment::class.java)
    private val sportMenuItem = MenuTab(R.drawable.ic_chris_home_menu_sport_sel, R.drawable.ic_chris_home_menu_sport_nor, R.string.main_tab_sport, SportVenueFragment::class.java)
    private val esportMenuItem = MenuTab(R.drawable.ic_chris_home_menu_esport_sel, R.drawable.ic_chris_home_menu_esport_nor, R.string.esports, ESportVenueFragment::class.java)
    private val okGameMenuItem = MenuTab(R.drawable.ic_chris_home_menu_casino_sel, R.drawable.ic_chris_home_menu_casino_nor, R.string.J203, ElectGamesFragment::class.java)
    private val okLiveGameItem = MenuTab(R.drawable.ic_chris_home_menu_live_sel, R.drawable.ic_chris_home_menu_live_nor, R.string.P160, LiveGamesFragment::class.java)
    private val promotionMenuItem = MenuTab(R.drawable.ic_chris_home_menu_promotion_sel, R.drawable.ic_chris_home_menu_promotion_nor, R.string.promo, null)
    private val sericeMenuItem = MenuTab(R.drawable.ic_chris_home_menu_service_nor, R.drawable.ic_chris_home_menu_service_nor, R.string.LT050_1, null)

    private var selectItem: MenuTab? = null
    private var selectedPosition = initiallyPosition

    init {
        reload()
    }

    fun pageCount() = data.size

    override fun getDefItemCount() = Int.MAX_VALUE

    override fun getItem(position: Int): Array<MenuTab?> {
        return super.getItem(position % data.size)
    }

    override fun getItemViewType(position: Int) = 0

    fun setChristmasStyle() {
        selectedBg = R.drawable.bg_chris_home_menu_sel
        normalBg = R.drawable.bg_chris_home_menu_nor
    }

    private fun setSelectedStyle(isSelected: Boolean, item: MenuTab, group: View, icon: ImageView) {
        group.isSelected = isSelected
        if (isSelected) {
            group.setBackgroundResource(selectedBg)
            icon.setImageResource(item.selectedIcon)
        } else {
            group.setBackgroundResource(normalBg)
            icon.setImageResource(item.norIcon)
        }
    }

    override fun onBinding(position: Int, binding: ItemHomeMenuPageBinding, item: Array<MenuTab?>, payloads: List<Any> ) = binding.run {
        payloads.forEach {
            val index = it as Int
            val itemData = item.getOrNull(index) ?: return@run
            val itemView = binding.root.getChildAt(index)
            val itemBinding = (itemView.tag as ItemHomeMenuBinding?) ?: ItemHomeMenuBinding.bind(itemView)
            setMaintanence(itemBinding.linMaintenance, itemData.content)
            setSelectedStyle(itemData == selectItem, itemData, itemView, itemBinding.ivIcon)
        }
    }

    override fun convert(helper: BindingVH<ItemHomeMenuPageBinding>, item: Array<MenuTab?>) {
        val binding = helper.vb
        val position = helper.absoluteAdapterPosition
        repeat(binding.root.childCount) { itemIndex->
            val itemData = item.getOrNull(itemIndex)
            val itemView = binding.root.getChildAt(itemIndex)
            if (itemData == null) {
                itemView.inVisible()
                itemView.isEnabled = false
            } else {
                itemView.visible()
                itemView.isEnabled = true
                val itemBinding = (itemView.tag as ItemHomeMenuBinding?) ?: ItemHomeMenuBinding.bind(itemView)
                itemBinding.tvName.text = context.getString(itemData.name)
                setMaintanence(itemBinding.linMaintenance, itemData.content)
                setSelectedStyle(itemData == selectItem, itemData, itemView, itemBinding.ivIcon)
                itemView.setOnClickListener { changeSelected(itemData, position, itemIndex) }
            }
        }
    }

    override fun onBinding(position: Int, binding: ItemHomeMenuPageBinding, item: Array<MenuTab?>) { }

    private fun setMaintanence(linMaintenance: View, fragmentClass: Class<out BaseFragment<*>>?){
        if ((fragmentClass == SportVenueFragment::class.java || fragmentClass == ESportVenueFragment::class.java)
            && getSportEnterIsClose()) {
            linMaintenance.gone()
            return
        }

        linMaintenance.gone()
    }

    private fun buildItem(){
        datas.clear()
        datas.add(hotMenuItem)
        if (StaticData.okSportOpened()){
            datas.add(sportMenuItem)
        }
        if (StaticData.okGameOpened()){
            datas.add(okGameMenuItem)
        }
        if (StaticData.okLiveOpened()){
            datas.add(okLiveGameItem)
        }
        if (StaticData.okBingoOpened()){
            datas.add(esportMenuItem)
        }
        datas.add(promotionMenuItem)
        datas.add(sericeMenuItem)

        val less = datas.size % pageSize
        if (less == 0 || datas.size < pageSize) {
            return
        }

        repeat(pageSize - less) { datas.add(null) }
    }


    fun reload(){
        buildItem()

        val list = mutableListOf<Array<MenuTab?>>()

        if (datas.size < pageSize) {
            list.add(datas.toTypedArray())
        } else {
            for (i in 0 until datas.size / pageSize) {
                list.add(datas.subList(i * pageSize, (i + 1) * pageSize).toTypedArray())
            }
        }

        selectItem = datas[0]
        selectedPosition = initiallyPosition
        setList(list)
    }

    private fun changeSelected(item: MenuTab, position: Int, position2: Int) {
        if (selectItem == item || !itemClick.invoke(item)) {
            return
        }

        val oldIndex = datas.indexOf(selectItem)
        if (oldIndex < 0) {
            return
        }

        selectItem = item
        val lastPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(position, position2)

        if (position % 2 == lastPosition % 2) { // 因为目前实际数据最多2页，%2的方式即可判断是否跟选中的在同一页
            notifyItemChanged(position, oldIndex % pageSize)
        } else {
            val oldIndex2 = oldIndex % pageSize
            notifyItemChanged(position - 1, oldIndex2)
            notifyItemChanged(position + 1, oldIndex2)
        }
    }

    fun selectedRecommend() = changeSelected(hotMenuItem, initiallyPosition, 0)

    fun checkMaintain() {
        if (selectItem == sportMenuItem || selectItem == esportMenuItem) {
            if (getSportEnterIsClose()) {
                selectedRecommend()
            }

            return
        }

        if (selectItem == okGameMenuItem) {
            if (!StaticData.okGameOpened()) {
                selectedRecommend()
            }
            return
        }

        if (selectItem == okLiveGameItem) {
            if (!StaticData.okLiveOpened()) {
                selectedRecommend()
            }
            return
        }
    }
}