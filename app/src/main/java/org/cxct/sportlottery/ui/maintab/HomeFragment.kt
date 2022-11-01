package org.cxct.sportlottery.ui.maintab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.live.HomeLiveFragment
import org.cxct.sportlottery.util.FragmentHelper

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
        fragmentHelper.showFragment(position)
        if (position>0){
            (activity as MainTabActivity).homeBackView(true)
        }else{
            (activity as MainTabActivity).homeBackView(false)
        }
    }

    fun onTabClickByPosition(position: Int) {
        when (position) {
            0 -> switchTabByPosition(0)
            1 -> switchTabByPosition(1)
            2 -> (activity as MainTabActivity).switchTabByPosition(1)
            3 -> switchTabByPosition(2)
            4 -> switchTabByPosition(3)
            5 -> switchTabByPosition(4)
        }
    }
}
