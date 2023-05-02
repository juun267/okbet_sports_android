package org.cxct.sportlottery.ui.maintab.home

import android.os.Bundle
import android.view.View
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.JumpInPlayEvent
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui.maintab.live.HomeLiveFragment
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.FragmentHelper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeFragment: BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    private val fragmentHelper by lazy {

        FragmentHelper(childFragmentManager, R.id.fl_content, arrayOf(
            Pair(MainHomeFragment::class.java, null),
//            Pair(HomeLiveFragment::class.java, null),
            Pair(MainHomeFragment2::class.java, null),
            Pair(OKGamesFragment::class.java, null)
        ))

    }

    override fun layoutId() = R.layout.fragment_home1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switchTabByPosition(0)
        EventBusUtil.targetLifecycle(this)
    }

    private fun switchTabByPosition(position: Int) {
        (activity as MainTabActivity).homeBackView(position > 0)
        fragmentHelper.showFragment(position)
    }

    fun backMainHome() = switchTabByPosition(0)

    fun jumpToLive() = switchTabByPosition(1)

    fun jumpToOKGames() = switchTabByPosition(2)

    fun jumpToInplaySport() {
        (activity as MainTabActivity).jumpToInplaySport()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onJumpToInPlayEvent(event: JumpInPlayEvent){
        jumpToInplaySport()
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

}
