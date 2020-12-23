package org.cxct.sportlottery.ui.bet_record

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityBetRecordBinding
import org.cxct.sportlottery.ui.base.BaseActivity

class BetRecordActivity : BaseActivity<BetRecordViewModel>(BetRecordViewModel::class) {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration : AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityBetRecordBinding>(this, R.layout.activity_bet_record)

        setUpNav(binding)
    }

    private fun setUpNav(binding: ActivityBetRecordBinding) {
        setUpDrawerLayout(binding)
    }

    private fun setUpDrawerLayout(binding: ActivityBetRecordBinding) {
        drawerLayout = binding.drawerLayout
        val navController = this.findNavController(R.id.fragment_host_bet_record)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)

        //下一頁面是否顯示drawer
        navController.addOnDestinationChangedListener { nc: NavController, nd: NavDestination, bundle: Bundle? ->
            if (nd.id == nc.graph.startDestination) { //當第一頁時顯示
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
        NavigationUI.setupWithNavController(binding.navRight, navController)
    }

    override fun onSupportNavigateUp(): Boolean { //按下漢堡選單按鈕
        val navController = this.findNavController(R.id.fragment_host_bet_record)
        return navController.navigateUp()
    }
}