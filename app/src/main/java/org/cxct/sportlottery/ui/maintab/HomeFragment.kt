package org.cxct.sportlottery.ui.maintab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.HomeTabEvent
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.elec.HomeElecFragment
import org.cxct.sportlottery.ui.maintab.live.HomeLiveFragment
import org.cxct.sportlottery.ui.maintab.slot.HomeSlotFragment
import org.cxct.sportlottery.ui.maintab.worldcup.HomeWorldCupFragment
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.FragmentHelper

class HomeFragment : BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    val containerId = 0x08989
    val fragmentHelper: FragmentHelper by lazy {
        FragmentHelper(childFragmentManager, containerId, arrayOf(
            HomeWorldCupFragment::class.java,
            MainHomeFragment::class.java,
            HomeLiveFragment::class.java,
//            HomeWorldCupFragment::class.java,
            HomeElecFragment::class.java,
            HomeSlotFragment::class.java))
    }

    override fun createRootView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FrameLayout(requireContext()).apply { id = containerId }
    }

    override fun onBindView(view: View) {
        switchTabByPosition(0)
    }

    fun switchTabByPosition(position: Int) {
        if (position>0) {
            (activity as MainTabActivity).homeBackView(true)
        } else {
            (activity as MainTabActivity).homeBackView(false)
        }

        val fragment = fragmentHelper.showFragment(position)
        EventBusUtil.post(HomeTabEvent(fragment))
    }

    fun onTabClickByPosition(position: Int) {
        when (position) {
            0 -> switchTabByPosition(0)
            1 -> switchTabByPosition(1)
            2 -> {
                (activity as MainTabActivity).jumpToTheSport(MatchType.EARLY, GameType.FT)
                (activity as MainTabActivity).findViewById<LinearLayout>(R.id.ll_home_back)
                    .visibility = View.GONE
            }
            3 ->
                if (sConfigData?.worldCupOpen == 1) {
                    switchTabByPosition(2)
                } else {
                    (activity as MainTabActivity).jumpToTheSport(MatchType.IN_PLAY, GameType.ALL)
                    (activity as MainTabActivity).findViewById<LinearLayout>(R.id.ll_home_back)
                        .visibility = View.GONE
                }
            4 -> switchTabByPosition(3)
            5 -> switchTabByPosition(4)
        }
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
