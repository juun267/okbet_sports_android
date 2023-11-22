package org.cxct.sportlottery.ui.maintab.home.view

import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BindingAdapter
import org.cxct.sportlottery.common.adapter.BindingVH
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.load
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemHomeMenuBinding
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.home.game.esport.ESportVenueFragment
import org.cxct.sportlottery.ui.maintab.home.game.live.LiveGamesFragment
import org.cxct.sportlottery.ui.maintab.home.game.slot.ElectGamesFragement
import org.cxct.sportlottery.ui.maintab.home.game.sport.SportVenueFragment
import org.cxct.sportlottery.ui.maintab.home.hot.HomeHotFragment
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp

class HomeMenuAdapter(private val itemClick: (View, Triple<Int, Int, Class<BaseFragment<*>>?>) -> Unit)
    : BindingAdapter<Triple<Int, Int, Class<BaseFragment<*>>?>, ItemHomeMenuBinding>() {

    private val datas = mutableListOf<Triple<Int, Int, Class<BaseFragment<*>>?>>()
    private val cache = arrayOf(
        Triple(R.drawable.selector_home_menu_hot, R.string.home_recommend, HomeHotFragment::class.java),
        Triple(R.drawable.selector_home_menu_sport, R.string.main_tab_sport, SportVenueFragment::class.java),
        Triple(R.drawable.selector_home_menu_casino, R.string.J203, ElectGamesFragement::class.java),
        Triple(R.drawable.selector_home_menu_live, R.string.P160, LiveGamesFragment::class.java),
        Triple(R.drawable.selector_home_menu_esport, R.string.esports, ESportVenueFragment::class.java),
        Triple(R.drawable.selector_home_menu_promotion, R.string.promo, null),
        Triple(R.drawable.selector_home_menu_service, R.string.LT050, null)
    )
    private val itemWith by lazy { (ScreenUtil.getScreenWidth(context)-12.dp*2)/6 }
    private var selectItem: Triple<Int, Int, Class<BaseFragment<*>>?>?=null

    init {
        buildItem()
        selectItem = datas[0]
        setList(datas)
    }

    override fun onBinding(
        position: Int,
        binding: ItemHomeMenuBinding,
        item: Triple<Int, Int, Class<BaseFragment<*>>?>,
        payloads: List<Any>
    )=binding.run {
        setMaintanence(binding.linMaintenance,item.third)
        root.isSelected = item == selectItem
    }


    override fun onBinding(
        position: Int,
        binding: ItemHomeMenuBinding,
        item: Triple<Int, Int, Class<BaseFragment<*>>?>) = binding.run {
        root.layoutParams.apply {
            width = itemWith
            height = itemWith
        }
        ivIcon.setImageResource(item.first)
        tvName.text = context.getString(item.second)
        root.isSelected = item == selectItem
        if (item.third!=null){
            setMaintanence(linMaintenance,item.third)
            root.setOnClickListener {
                selectItem  = item
                notifyDataSetChanged()
                itemClick(it, item)
            }
        }else{
            when(item.second){
                R.string.B005->root.bindPromoClick {  }
                R.string.LT050->root.setServiceClick((AppManager.currentActivity() as MainTabActivity).supportFragmentManager) {  }
            }
        }
    }
    private fun setMaintanence(linMaintenance: View, fragmentClass: Class<BaseFragment<*>>?){
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
        datas.add(cache[0] as Triple<Int, Int, Class<BaseFragment<*>>?>)
        if (StaticData.okSportOpened()){
            datas.add(cache[1] as Triple<Int, Int, Class<BaseFragment<*>>?>)
        }
        if (StaticData.okGameOpened()){
            datas.add(cache[2] as Triple<Int, Int, Class<BaseFragment<*>>?>)
        }
        if (StaticData.okLiveOpened()){
            datas.add(cache[3] as Triple<Int, Int, Class<BaseFragment<*>>?>)
        }
        if (StaticData.okSportOpened()){
            datas.add(cache[4] as Triple<Int, Int, Class<BaseFragment<*>>?>)
        }
        datas.add(cache[5] as Triple<Int, Int, Class<BaseFragment<*>>?>)
        datas.add(cache[6] as Triple<Int, Int, Class<BaseFragment<*>>?>)
    }
    fun reload(){
        buildItem()
        setList(datas)
    }
}