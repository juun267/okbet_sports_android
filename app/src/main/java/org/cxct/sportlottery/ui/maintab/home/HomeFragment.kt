package org.cxct.sportlottery.ui.maintab.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OKGamesFragment
import org.cxct.sportlottery.ui.maintab.live.HomeLiveFragment
import org.cxct.sportlottery.util.FragmentHelper

class HomeFragment: BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    private val fragmentHelper by lazy {

        FragmentHelper(childFragmentManager, R.id.fl_content, arrayOf(
            Pair(MainHomeFragment::class.java, null),
            Pair(HomeLiveFragment::class.java, null),
            Pair(OKGamesFragment::class.java, null)
        ))

    }

    override fun layoutId() = R.layout.fragment_home1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switchTabByPosition(0)
    }

    fun switchTabByPosition(position: Int) {
        (activity as MainTabActivity).homeBackView(position > 0)
        fragmentHelper.showFragment(position)
    }

    fun onTabClickByPosition(position: Int) {

        when (position) {
            0 -> switchTabByPosition(0)
            1 -> switchTabByPosition(1)
            2 -> {
                (activity as MainTabActivity).jumpToTheSport(MatchType.IN_PLAY, GameType.ALL)
                (activity as MainTabActivity).findViewById<LinearLayout>(R.id.ll_home_back).gone()
            }
            3 -> {
                (activity as MainTabActivity).jumpToTheSport(MatchType.EARLY, GameType.FT)
                (activity as MainTabActivity).findViewById<LinearLayout>(R.id.ll_home_back).gone()
            }
            4 -> switchTabByPosition(2)
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
