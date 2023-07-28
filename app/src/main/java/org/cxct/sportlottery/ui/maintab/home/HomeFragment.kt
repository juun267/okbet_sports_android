package org.cxct.sportlottery.ui.maintab.home

import android.os.Bundle
import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.toIntS
import org.cxct.sportlottery.network.service.EventType
import org.cxct.sportlottery.network.service.sys_maintenance.SportMaintenanceEvent
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.worldcup.WorldCupFragment
import org.cxct.sportlottery.ui.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui.maintab.games.OKLiveFragment
import org.cxct.sportlottery.ui.maintab.home.news.NewsHomeFragment
import org.cxct.sportlottery.ui.maintab.worldcup.WorldCupGameFragment
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.FragmentHelper

class HomeFragment: BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    private val fragmentHelper by lazy {

        FragmentHelper(childFragmentManager, R.id.fl_content, mutableListOf(
//
            Pair(MainHomeFragment::class.java, null),
            Pair(OKGamesFragment::class.java, null),
            Pair(NewsHomeFragment::class.java, null),
            Pair(OKLiveFragment::class.java, null)
        ).apply {
            if (StaticData.worldCupOpened()){
                add(Pair(WorldCupFragment::class.java, null))
                add(Pair(WorldCupGameFragment::class.java, null))
            }
        }.toTypedArray()
        )
    }

    override fun layoutId() = R.layout.fragment_home1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switchTabByPosition(0)
    }

    private fun switchTabByPosition(position: Int) {
        fragmentHelper.showFragment(position)
    }

    fun backMainHome() = switchTabByPosition(0)

    fun jumpToOKGames() = switchTabByPosition(1)

    fun jumpToNews() = switchTabByPosition(2)

    fun jumpToOKLive() = switchTabByPosition(3)

    fun jumpToWorldCup() = switchTabByPosition(4)

    fun jumpToWorldCupGame() = switchTabByPosition(5)



    fun jumpToInplaySport() {
        (activity as MainTabActivity).jumpToInplaySport()
    }

    fun jumpToDefaultSport() {
        (activity as MainTabActivity).jumpToTheSport()
    }

    fun jumpToEarlySport() {
        (activity as MainTabActivity).jumpToEarlySport()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        fragmentHelper.getFragmentList().find {
            it != null && it.isAdded && it.isVisible
        }?.let {
            it.onHiddenChanged(hidden)
        }
    }

    open fun getCurrentFragment() = fragmentHelper.getCurrentFragment()


    override fun onResume() {
        super.onResume()
        viewModel.getConfigData()
    }
}
