package org.cxct.sportlottery.ui.betRecord

import android.graphics.Typeface
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.canDelayClick
import org.cxct.sportlottery.common.extentions.replaceFragment
import org.cxct.sportlottery.databinding.ActivityBetRecordBinding
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.util.bindSportMaintenance

class BetRecordActivity:BindingActivity<MainViewModel,ActivityBetRecordBinding>() {
    //未结单
    private val unsettledFragment by lazy { UnsettledFragment() }
    //已结单
    private val settledFragment by lazy { SettledFragment() }
    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        setContentView(binding.root)

        binding.run {
            customToolBar.apply {
                tv_toolbar_title.typeface = Typeface.DEFAULT_BOLD
                tv_toolbar_title.setTextColor(ContextCompat.getColor(this.context,R.color.color_000000))
                setOnBackPressListener {
                    finish()
                }
            }
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when(tab?.position) {
                        0 -> {
                            if (tab.view.canDelayClick()){
                                replaceFragment(R.id.frameContainer,unsettledFragment)
                            }
                        }
                        1 -> {
                            if (tab.view.canDelayClick()){
                                replaceFragment(R.id.frameContainer,unsettledFragment)
                            }
                            replaceFragment(R.id.frameContainer,settledFragment)
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
        }
        bindSportMaintenance()
    }

    override fun onInitData() {
        super.onInitData()
        //默认选中未结单
        replaceFragment(R.id.frameContainer,unsettledFragment)
    }

}