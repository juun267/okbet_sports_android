package org.cxct.sportlottery.ui.maintab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.util.FragmentHelper

class HomeFragment :
    BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    lateinit var fragmentHelper: FragmentHelper
    var fragments = arrayOf<Fragment>(
        MainHomeFragment.newInstance(),
        HomeLiveFragment.newInstance(),
        HomeLiveFragment.newInstance(),
        HomeLiveFragment.newInstance(),
        HomeLiveFragment.newInstance()
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
    }

}
