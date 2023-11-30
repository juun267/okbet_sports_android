package org.cxct.sportlottery.ui.maintab.home.view

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemHomeMenuBinding
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.home.game.esport.ESportVenueFragment
import org.cxct.sportlottery.ui.maintab.home.game.live.LiveGamesFragment
import org.cxct.sportlottery.ui.maintab.home.game.slot.ElectGamesFragment
import org.cxct.sportlottery.ui.maintab.home.game.sport.SportVenueFragment
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.util.*

class HomeMenuAdapter(private val itemClick: (MenuTab) -> Boolean)
    : BindingAdapter<HomeMenuAdapter.MenuTab, ItemHomeMenuBinding>(), OnItemClickListener {

    private var selectedBg = R.drawable.bg_home_menu_sel
    private var normalBg = R.drawable.bg_home_menu_nor

    data class MenuTab(@DrawableRes val selectedIcon: Int,
                       @DrawableRes val norIcon: Int,
                       @StringRes val name: Int,
                       val content: Class<out BaseFragment<*>>?)

    private val datas = mutableListOf<MenuTab>()

    private val hotMenuItem = MenuTab(R.drawable.ic_home_menu_hot_sel, R.drawable.ic_home_menu_hot_nor, R.string.home_recommend, HomeHotFragment::class.java)
    private val sportMenuItem = MenuTab(R.drawable.ic_home_menu_sport_sel, R.drawable.ic_home_menu_sport_nor, R.string.main_tab_sport, SportVenueFragment::class.java)
    private val esportMenuItem = MenuTab(R.drawable.ic_home_menu_esport_sel, R.drawable.ic_home_menu_esport_nor, R.string.esports, ESportVenueFragment::class.java)
    private val okGameMenuItem = MenuTab(R.drawable.ic_home_menu_casino_sel, R.drawable.ic_home_menu_casino_nor, R.string.J203, ElectGamesFragment::class.java)
    private val okLiveGameItem = MenuTab(R.drawable.ic_home_menu_live_sel, R.drawable.ic_home_menu_live_nor, R.string.P160, LiveGamesFragment::class.java)
    private val promotionMenuItem = MenuTab(R.drawable.ic_home_menu_promotion_sel, R.drawable.ic_home_menu_promotion_nor, R.string.promo, null)
    private val sericeMenuItem = MenuTab(R.drawable.ic_home_menu_service_sel, R.drawable.ic_home_menu_service_nor, R.string.LT050, null)

    private var selectItem: MenuTab? = null

    init {
        buildItem()
        selectItem = datas[0]
        setList(datas)
        setOnItemClickListener(this)
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

    override fun onBinding(position: Int, binding: ItemHomeMenuBinding, item: MenuTab, payloads: List<Any> ) = binding.run {
        setMaintanence(binding.linMaintenance, item.content)
        setSelectedStyle(item == selectItem, item, root.getChildAt(0), binding.ivIcon)
    }

    override fun onBinding(position: Int, binding: ItemHomeMenuBinding, item: MenuTab) = binding.run {
        tvName.text = context.getString(item.name)
        setSelectedStyle(item == selectItem, item, root.getChildAt(0), binding.ivIcon)
        setMaintanence(binding.linMaintenance, item.content)
    }

    private fun setMaintanence(linMaintenance: View, fragmentClass: Class<out BaseFragment<*>>?){
        when(fragmentClass){
            SportVenueFragment::class.java,ESportVenueFragment::class.java->{
                //判断体育维护是否开启
                if(getSportEnterIsClose()){
                    //展示维护中
                    linMaintenance.visible()
                }else{
                    linMaintenance.gone()
                }
            }
            else->{
                linMaintenance.gone()
            }
        }
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
    }
    fun reload(){
        buildItem()
        setList(datas)
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        setSelected(position)
    }

    private fun setSelected(position: Int) {
        val item = getItem(position)
        if (item == selectItem) {
            return
        }
        if (itemClick.invoke(item)) {
            val lastPosition = getItemPosition(selectItem)
            selectItem = item
            if (lastPosition >= 0) {
                notifyItemChanged(lastPosition, lastPosition)
            }
            notifyItemChanged(position, position)
        }
    }

    fun selectedRecommend() = setSelected(0)

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