package org.cxct.sportlottery.ui.maintab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.elec.HomeElecFragment
import org.cxct.sportlottery.ui.maintab.live.HomeLiveFragment
import org.cxct.sportlottery.ui.maintab.slot.HomeSlotFragment
import org.cxct.sportlottery.util.FragmentHelper
import org.cxct.sportlottery.util.LogUtil

class HomeFragment :
    BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {
    lateinit var fragmentHelper: FragmentHelper
    var fragments = arrayOf<Fragment>(
        MainHomeFragment.newInstance(),
        HomeLiveFragment.newInstance(),
        HomeWorldCupFragment.newInstance(),
        HomeElecFragment.newInstance(),
        HomeSlotFragment.newInstance()
    )

    companion object {
        fun newInstance(): HomeFragment {
            val args = Bundle()
            val fragment = HomeFragment()
            fragment.arguments = args
            return fragment
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
        fragmentHelper = FragmentHelper(childFragmentManager, R.id.fl_content, fragments)
        switchTabByPosition(0)
    }

    fun switchTabByPosition(position: Int) {
        if (position>0){
            (activity as MainTabActivity).homeBackView(true)
        }else{
            (activity as MainTabActivity).homeBackView(false)
        }
        fragmentHelper.showFragment(position)
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
//        (fragments[0] as MainHomeFragment).let {
//            if (it.isAdded&&it.isVisible){
//                it.showChangeFragment()
//            }
//        }
        fragments.find {
            it.isAdded&&it.isVisible
        }?.let {
            it.onHiddenChanged(hidden)
        }
    }
}
