package org.cxct.sportlottery.ui.betRecord

import android.graphics.Typeface
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.ShowInPlayEvent
import org.cxct.sportlottery.databinding.ActivityBetRecordBinding
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.view.onClick
import org.cxct.sportlottery.view.setColors

class BetRecordActivity:BindingActivity<MainViewModel,ActivityBetRecordBinding>() {
    //未结单
    private val unsettledFragment=UnsettledFragment()
    //已结单
    private val settledFragment by lazy { SettledFragment() }
    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        setContentView(binding.root)

        binding.run {

            ivBack.onClick {
                finish()
                EventBusUtil.post(ShowInPlayEvent())
            }

            //未结单
            tvUnsettled.onClick(1000) {
                changeTabStyle(0)
                replaceFragment(R.id.frameContainer,unsettledFragment)
            }
            //已结单
            tvSettled.onClick(1000) {
                changeTabStyle(1)
                replaceFragment(R.id.frameContainer,settledFragment)
            }

        }
    }

    override fun onInitData() {
        super.onInitData()
        //默认选中未结单
        binding.tvUnsettled.performClick()
//        changeTabStyle(0)
//        replaceFragment(R.id.frameContainer,unsettledFragment)
    }


    private fun changeTabStyle(index:Int){
        binding.tvUnsettled.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        binding.tvSettled.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
        binding.tvUnsettled.setColors(R.color.color_6D7693)
        binding.tvSettled.setColors(R.color.color_6D7693)
        selectTabStyle(index)
    }



    private fun selectTabStyle(index:Int){
        when(index){
            0->{
                binding.tvUnsettled.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                binding.tvUnsettled.setColors(R.color.color_000000)
            }
            1->{
                binding.tvSettled.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                binding.tvSettled.setColors(R.color.color_000000)
            }
        }
    }


}