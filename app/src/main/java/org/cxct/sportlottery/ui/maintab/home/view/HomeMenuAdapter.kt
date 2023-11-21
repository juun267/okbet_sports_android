package org.cxct.sportlottery.ui.maintab.home.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.inVisible
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemHomeMenuPageBinding
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.game.esport.ESportVenueFragment
import org.cxct.sportlottery.ui.maintab.home.game.live.LiveGamesFragment
import org.cxct.sportlottery.ui.maintab.home.game.slot.ElectGamesFragment
import org.cxct.sportlottery.ui.maintab.home.game.sport.SportVenueFragment
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.util.AppManager
import org.cxct.sportlottery.util.bindPromoClick
import org.cxct.sportlottery.util.getSportEnterIsClose
import org.cxct.sportlottery.util.setServiceClick

class HomeMenuAdapter(private val itemClick: (View, Triple<Int, Int, Class<BaseFragment<*>>?>) -> Unit)
    : BindingAdapter<Array<Triple<Int, Int, Class<BaseFragment<*>>?>>, ItemHomeMenuPageBinding>() {
    private val datas = mutableListOf(
        arrayOf(
//            Triple(R.drawable.selector_home_menu_home, R.string.bottom_nav_home, HomeHotFragment::class.java),
            Triple(R.drawable.selector_home_menu_hot, R.string.home_recommend, HomeHotFragment::class.java),
            Triple(R.drawable.selector_home_menu_sport, R.string.main_tab_sport, SportVenueFragment::class.java),
            Triple(R.drawable.selector_home_menu_casino, R.string.P230, ElectGamesFragment::class.java),
            Triple(R.drawable.selector_home_menu_live, R.string.P160, LiveGamesFragment::class.java),
            Triple(R.drawable.selector_home_menu_esport, R.string.esports, ESportVenueFragment::class.java),
            Triple(R.drawable.selector_home_menu_promotion, R.string.B005, null),
        ),
        arrayOf(
            Triple(R.drawable.selector_home_menu_service, R.string.LT050, null),
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
                setMaintanence(view.findViewById(R.id.linMaintenance),item[index].third)
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
                if (itemChild.third!=null){
                    setMaintanence(view.findViewById(R.id.linMaintenance),itemChild.third)
                    view.setOnClickListener {
                        selectItem  = itemChild
                        notifyDataSetChanged()
                        itemClick(it, itemChild)
                    }
                }else{
                    when(itemChild.second){
                        R.string.B005->view.bindPromoClick {  }
                        R.string.LT050->view.setServiceClick((AppManager.currentActivity() as MainTabActivity).supportFragmentManager) {  }
                    }
                }
                view.findViewById<ImageView>(R.id.ivIcon).setImageResource(itemChild.first)
                view.findViewById<TextView>(R.id.tvName).setText(itemChild.second)
                view.isSelected = itemChild == selectItem
            }
        }
    }
    private fun setMaintanence(linMaintenance: View, fragmentClass: Class<BaseFragment<*>>?){
        when(fragmentClass){
            SportVenueFragment::class.java,ESportVenueFragment::class.java->{
                //判断体育维护是否开启
                if(getSportEnterIsClose()||StaticData.okSportOpened()){
                    //展示维护中
                    linMaintenance.gone()
                }else{
                    linMaintenance.visible()
                }
            }
            ElectGamesFragment::class.java->{
                //判断体育维护是否开启
                if(StaticData.okGameOpened()){
                    //展示维护中
                    linMaintenance.gone()
                }else{
                    linMaintenance.visible()
                }
            }
            LiveGamesFragment::class.java->{
                //判断体育维护是否开启
                if(StaticData.okLiveOpened()){
                    //展示维护中
                    linMaintenance.gone()
                }else{
                    linMaintenance.visible()
                }
            }
            else->{
                linMaintenance.gone()
            }
        }
    }
}