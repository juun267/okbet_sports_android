package org.cxct.sportlottery.ui.betRecord

import android.graphics.Typeface
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityBetRecordBinding
import org.cxct.sportlottery.ui.base.BindingActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.bindSportMaintenance
import org.cxct.sportlottery.util.setTextTypeFace

class BetRecordActivity:BindingActivity<MainViewModel,ActivityBetRecordBinding>() {
    //未结单
    private val unsettledFragment=UnsettledFragment()
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
            customTabLayout.apply {
                tabLayoutCustom.setTabTextColors(ContextCompat.getColor(context,R.color.color_6D7693),ContextCompat.getColor(context,R.color.color_025BE8))
                tabLayoutCustom.isTabIndicatorFullWidth = false
                tabLayoutCustom.setSelectedTabIndicator(R.drawable.custom_tab_indicator_40_2)
                (tabLayoutCustom.layoutParams as LinearLayout.LayoutParams).apply {
                    setMargins(50.dp,0,50.dp,0)
                    tabLayoutCustom.layoutParams = this
                }
                setCustomTabSelectedListener { position ->
                        when(position) {
                            0 -> {
                                avoidFastDoubleClick()
                                replaceFragment(R.id.frameContainer,unsettledFragment)
                            }
                            1 -> {
                                avoidFastDoubleClick()
                                replaceFragment(R.id.frameContainer,settledFragment)
                            }
                        }
                    }
            }
        }
        bindSportMaintenance()
    }

    override fun onInitData() {
        super.onInitData()
        //默认选中未结单
        replaceFragment(R.id.frameContainer,unsettledFragment)
    }

}