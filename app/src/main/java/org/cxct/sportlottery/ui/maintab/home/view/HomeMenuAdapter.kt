package org.cxct.sportlottery.ui.maintab.home.view

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
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
import org.cxct.sportlottery.ui.maintab.home.game.perya.MiniGameListFragment
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
    private val selectedBgMap = mutableMapOf<Int, Int>()


    data class MenuTab(@DrawableRes val selectedIcon: Int,
                       @DrawableRes val norIcon: Int,
                       @StringRes val name: Int,
                       val content: Class<out BaseFragment<*,*>>?)

    private val datas = mutableListOf<MenuTab?>()

    private var hotMenuItem = MenuTab(R.drawable.ic_home_menu_hot_sel, R.drawable.ic_home_menu_hot_nor, R.string.home_recommend, HomeHotFragment::class.java)
    private var sportMenuItem = MenuTab(R.drawable.ic_home_menu_sport_sel, R.drawable.ic_home_menu_sport_nor, R.string.main_tab_sport, SportVenueFragment::class.java)
    private var esportMenuItem = MenuTab(R.drawable.ic_home_menu_esport_sel, R.drawable.ic_home_menu_esport_nor, R.string.esports, ESportVenueFragment::class.java)
    private var okGameMenuItem = MenuTab(R.drawable.ic_home_menu_casino_sel, R.drawable.ic_home_menu_casino_nor, R.string.B23, ElectGamesFragment::class.java)
    private var okLiveGameItem = MenuTab(R.drawable.ic_home_menu_live_sel, R.drawable.ic_home_menu_live_nor, R.string.B24, LiveGamesFragment::class.java)
    private var promotionMenuItem = MenuTab(R.drawable.ic_home_menu_promotion_sel, R.drawable.ic_home_menu_promotion_nor, R.string.promo, null)
    private var sericeMenuItem = MenuTab(R.drawable.ic_home_menu_service_sel, R.drawable.ic_home_menu_service_nor, R.string.LT050_1, null)
    private var endcardMenuItem = MenuTab(R.drawable.ic_home_menu_endcard_sel, R.drawable.ic_home_menu_endcard_nor, R.string.P333, null)
    private var peryaMenuItem = MenuTab(R.drawable.ic_home_menu_perya_sel, R.drawable.ic_home_menu_perya_nor, R.string.P452, MiniGameListFragment::class.java)

    private var selectItem: MenuTab? = null

    fun pageCount() = data.size

    override fun getDefItemCount() = if (data.isEmpty()) 0 else Int.MAX_VALUE

    override fun getItem(position: Int): Array<MenuTab?> {
        return super.getItem(position % data.size)
    }

    override fun getItemViewType(position: Int) = 0

    private fun setSelectedStyle(isSelected: Boolean, item: MenuTab, group: View, icon: ImageView) {
        group.isSelected = isSelected
        if (isSelected) {
            group.setBackgroundResource(selectedBgMap.getOrDefault(item.name, selectedBg))
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
            setMaintanence(itemBinding.linMaintenance, itemData.name)
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
                setMaintanence(itemBinding.linMaintenance, itemData.name)
                setSelectedStyle(itemData == selectItem, itemData, itemView, itemBinding.ivIcon)
                itemView.setOnClickListener { changeSelected(itemData, position, itemIndex) }
            }
        }
    }

    override fun onBinding(position: Int, binding: ItemHomeMenuPageBinding, item: Array<MenuTab?>) { }

    private fun setMaintanence(linMaintenance: View, @StringRes name: Int){
        if ((name == R.string.main_tab_sport || name == R.string.esports || name == R.string.P333)) {
            linMaintenance.isVisible = getSportEnterIsClose()
            return
        }

        if (name == R.string.P452) {
            linMaintenance.isVisible = !StaticData.miniGameOpened()
            return
        }

        linMaintenance.gone()
    }

    private fun buildItem(): MutableList<MenuTab?> {
        val itemDatas = mutableListOf<MenuTab?>()
        itemDatas.add(hotMenuItem)

        if (StaticData.okSportOpened()){
            itemDatas.add(sportMenuItem)
        }
        if (StaticData.okGameOpened()){
            itemDatas.add(okGameMenuItem)
        }

        if (StaticData.miniGameOpened()) {
            itemDatas.add(peryaMenuItem)
        }

        if (StaticData.okLiveOpened()){
            itemDatas.add(okLiveGameItem)
        }
        if (StaticData.bkEndCardOpened()){
            itemDatas.add(endcardMenuItem)
        }
        if (StaticData.okBingoOpened()){
            itemDatas.add(esportMenuItem)
        }
        itemDatas.add(promotionMenuItem)

        itemDatas.add(sericeMenuItem)

        val less = itemDatas.size % pageSize
        if (less == 0 || itemDatas.size < pageSize) {
            return itemDatas
        }

        repeat(pageSize - less) { itemDatas.add(null) }

        return itemDatas
    }


    fun reload(){
        val items = buildItem()
        if (items.size == datas.size) { // 如果item没有变化就不重新绑定数据
            var asSame = true
            items.forEachIndexed { index, menuTab ->
                if (menuTab != datas[index]) {
                    asSame = false
                    return@forEachIndexed
                }
            }
            if (asSame) {
                return
            }
        }

        val list = mutableListOf<Array<MenuTab?>>()
        datas.clear()
        datas.addAll(items)

        if (datas.size < pageSize) {
            list.add(datas.toTypedArray())
        } else {
            for (i in 0 until datas.size / pageSize) {
                list.add(datas.subList(i * pageSize, (i + 1) * pageSize).toTypedArray())
            }
        }

        selectItem = datas[0]
        setList(list)
        itemClick.invoke(selectItem!!)
    }

    private fun changeSelected(item: MenuTab, position: Int, position2: Int, check: Boolean = true) {
        if (selectItem == item || (!itemClick.invoke(item) && check)) {
            return
        }

        val oldIndex = datas.indexOf(selectItem)
        if (oldIndex < 0) {
            return
        }

        selectItem = item
        notifyDataSetChanged()

//        notifyItemChanged(position, position2)
//        if (position % 2 == lastPosition % 2) { // 因为目前实际数据最多2页，%2的方式即可判断是否跟选中的在同一页
//            notifyItemChanged(position, oldIndex % pageSize)
//        } else {
//            val oldIndex2 = oldIndex % pageSize
//            notifyItemChanged(position - 1, oldIndex2)
//            notifyItemChanged(position + 1, oldIndex2)
//        }
    }

    fun selectedRecommend() = changeSelected(hotMenuItem, initiallyPosition, 0, false)
    fun selectedPerya(): Int {
        val index = datas.indexOf(peryaMenuItem)
        changeSelected(peryaMenuItem, initiallyPosition, index, false)
        return initiallyPosition
    }

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

        if (selectItem == peryaMenuItem) {
            if (!StaticData.miniGameOpened()) {
                selectedRecommend()
            }
            return
        }
    }

    fun applyHalloweenStyle() {

        hotMenuItem = MenuTab(R.drawable.ic_home_menu_hot_sel_h, R.drawable.ic_home_menu_hot_nor_h, R.string.home_recommend, HomeHotFragment::class.java)
        sportMenuItem = MenuTab(R.drawable.ic_home_menu_sport_sel_h, R.drawable.ic_home_menu_sport_nor_h, R.string.main_tab_sport, SportVenueFragment::class.java)
        esportMenuItem = MenuTab(R.drawable.ic_home_menu_esport_sel_h, R.drawable.ic_home_menu_esport_nor_h, R.string.esports, ESportVenueFragment::class.java)
        okGameMenuItem = MenuTab(R.drawable.ic_home_menu_casino_sel_h, R.drawable.ic_home_menu_casino_nor_h, R.string.B23, ElectGamesFragment::class.java)
        okLiveGameItem = MenuTab(R.drawable.ic_home_menu_live_sel_h, R.drawable.ic_home_menu_live_nor_h, R.string.B24, LiveGamesFragment::class.java)
        promotionMenuItem = MenuTab(R.drawable.ic_home_menu_promotion_sel_h, R.drawable.ic_home_menu_promotion_nor_h, R.string.promo, null)
        sericeMenuItem = MenuTab(R.drawable.ic_home_menu_service_sel_h, R.drawable.ic_home_menu_service_nor_h, R.string.LT050_1, null)
        endcardMenuItem = MenuTab(R.drawable.ic_home_menu_endcard_sel_h, R.drawable.ic_home_menu_endcard_nor_h, R.string.P333, null)
        peryaMenuItem = MenuTab(R.drawable.ic_home_menu_perya_sel_h, R.drawable.ic_home_menu_perya_nor_h, R.string.P452, MiniGameListFragment::class.java)

        selectedBg = R.drawable.bg_home_menu_sel_h
        normalBg = R.drawable.bg_home_menu_nor_h

        selectedBgMap[R.string.home_recommend] = R.drawable.bg_home_menu_sel_h
        selectedBgMap[R.string.main_tab_sport] = R.drawable.bg_home_menu_sel_h_1
        selectedBgMap[R.string.B23] = R.drawable.bg_home_menu_sel_h_2
        selectedBgMap[R.string.B24] = R.drawable.bg_home_menu_sel_h_4
        selectedBgMap[R.string.P452] = R.drawable.bg_home_menu_sel_h_3
        selectedBgMap[R.string.P333] = R.drawable.bg_home_menu_sel_h_5
        selectedBgMap[R.string.esports] = R.drawable.bg_home_menu_sel_h_6
        selectedBgMap[R.string.promo] = R.drawable.bg_home_menu_sel_h_7
        selectedBgMap[R.string.LT050_1] = R.drawable.bg_home_menu_sel_h_8

        notifyDataSetChanged()
    }
}