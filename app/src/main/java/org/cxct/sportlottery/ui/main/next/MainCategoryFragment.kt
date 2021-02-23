package org.cxct.sportlottery.ui.main.next

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_main_category.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.home.MainViewModel

class MainCategoryFragment : BaseFragment<MainViewModel>(MainViewModel::class) {

    private val tabTitleList by lazy {
        listOf(R.string.sport, R.string.cp, R.string.live, R.string.qp, R.string.dz, R.string.by)
    }
    private val tabIconList by lazy {
        listOf(R.drawable.ic_sport, R.drawable.ic_cp, R.drawable.ic_live, R.drawable.ic_qp, R.drawable.ic_dz, R.drawable.ic_by)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view_pager.adapter = VpFragmentPagerAdapter(tabTitleList, parentFragmentManager)

        tab_layout.clearOnTabSelectedListeners()
        view_pager.clearOnPageChangeListeners()
        tab_layout.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(view_pager) {})
        view_pager.addOnPageChangeListener(object : TabLayout.TabLayoutOnPageChangeListener(tab_layout) {})
    }
}

class VpFragmentPagerAdapter(private val titleList: List<Int>, fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager) {

    override fun getCount(): Int {
        return titleList.size
    }

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> return SportFragment()
            1 -> return CPFragment()
            2 -> return LiveFragment()
            3 -> return QPFragment()
            4 -> return DZFragment()
            5 -> return BYFragment()
        }
        return SportFragment()
    }
}
