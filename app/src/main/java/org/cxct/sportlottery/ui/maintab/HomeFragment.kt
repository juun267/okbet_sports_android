package org.cxct.sportlottery.ui.maintab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.HomeTabEvent
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.elec.HomeElecFragment
import org.cxct.sportlottery.ui.maintab.live.HomeLiveFragment
import org.cxct.sportlottery.ui.maintab.slot.HomeSlotFragment
import org.cxct.sportlottery.ui.maintab.worldcup.HomeWorldCupFragment
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.FragmentHelper
import org.cxct.sportlottery.util.isCreditSystem

class HomeFragment: BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    private val fragmentHelper by lazy {
        if (isCreditSystem()) {
            FragmentHelper(childFragmentManager, R.id.fl_content, arrayOf(
                Pair(MainHomeFragment::class.java, null),
                Pair(HomeLiveFragment::class.java, null),
                Pair(HomeWorldCupFragment::class.java, null),
                Pair(HomeSlotFragment::class.java, Bundle().apply { putInt("position", 4) }),
                Pair(HomeSlotFragment::class.java, Bundle().apply { putInt("position", 5) }),
                Pair(HomeSlotFragment::class.java, Bundle().apply { putInt("position", 6) })
            ))
        } else {
            FragmentHelper(childFragmentManager, R.id.fl_content, arrayOf(
                Pair(MainHomeFragment::class.java, null),
                Pair(HomeLiveFragment::class.java, null),
                Pair(HomeWorldCupFragment::class.java, null),
                Pair(HomeElecFragment::class.java, null),
                Pair(HomeSlotFragment::class.java, Bundle().apply { putInt("position", 5) }),
            ))
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_home1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switchTabByPosition(0)
    }

    fun switchTabByPosition(position: Int) {
        (activity as MainTabActivity).homeBackView(position > 0)
        val lastPosition = fragmentHelper.getCurrentPosition()
        val fragment = fragmentHelper.showFragment(position)
        EventBusUtil.post(HomeTabEvent(fragment))

        if (fragment is HomeWorldCupFragment && (lastPosition != position) && fragment.isInitedWeb) {
            fragment.reloadWeb()
        }
    }

    fun onTabClickByPosition(position: Int) {

        when (position) {
            0 -> switchTabByPosition(0)
            1 -> switchTabByPosition(1)
            2 ->
                if (StaticData.worldCupOpened()) {
                    switchTabByPosition(2)
                } else {
                    (activity as MainTabActivity).jumpToTheSport(MatchType.IN_PLAY, GameType.ALL)
                    (activity as MainTabActivity).findViewById<LinearLayout>(R.id.ll_home_back)
                        .visibility = View.GONE
                }
            3 -> {
                (activity as MainTabActivity).jumpToTheSport(MatchType.EARLY, GameType.FT)
                (activity as MainTabActivity).findViewById<LinearLayout>(R.id.ll_home_back)
                    .visibility = View.GONE
            }
            4 -> switchTabByPosition(3)
            5 -> switchTabByPosition(4)
            6 -> switchTabByPosition(5)
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

    fun isWorldCupTab(): Boolean {
        return fragmentHelper.getCurrentFragment() is HomeWorldCupFragment
    }
}
