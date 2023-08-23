package org.cxct.sportlottery.ui.maintab.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.worldcup.WorldCupFragment
import org.cxct.sportlottery.ui.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui.maintab.games.OKLiveFragment
import org.cxct.sportlottery.ui.maintab.home.news.NewsHomeFragment
import org.cxct.sportlottery.ui.maintab.worldcup.WorldCupGameFragment
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.FragmentHelper
import org.cxct.sportlottery.util.Param

class HomeFragment: BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    private val rootId = View.generateViewId()
    private val fragmentHelper by lazy {

        FragmentHelper(childFragmentManager, rootId, arrayOf(
            Param(MainHomeFragment::class.java),
            Param(OKGamesFragment::class.java),
            Param(NewsHomeFragment::class.java, needRemove = true),
            Param(OKLiveFragment::class.java),
            Param(WorldCupFragment::class.java, needRemove = true),
            Param(WorldCupGameFragment::class.java, needRemove = true)
        ))
    }

    override fun createRootView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FrameLayout(requireContext()).apply { id = rootId }
    }

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
