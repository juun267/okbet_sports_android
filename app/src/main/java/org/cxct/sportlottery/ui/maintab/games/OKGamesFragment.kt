package org.cxct.sportlottery.ui.maintab.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.fragment_main_home.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.event.MenuEvent
import org.cxct.sportlottery.databinding.FragmentOkgamesBinding
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.util.EventBusUtil
import org.cxct.sportlottery.util.FragmentHelper

// okgames主Fragment
class OKGamesFragment: BaseBottomNavigationFragment<OKGamesViewModel>(OKGamesViewModel::class) {

    private lateinit var binding: FragmentOkgamesBinding
    private val fragmentHelper by lazy {
        FragmentHelper(childFragmentManager, R.id.fragmentContainer, arrayOf(
            Pair(AllGamesFragment::class.java, null),
            Pair(PartGamesFragment::class.java, null)))
    }

    private inline fun mainTabActivity() = activity as MainTabActivity
    override fun createRootView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOkgamesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onBindView(view: View) {
        initToolBar(view)
        fragmentHelper.showFragment(0)
    }

    private fun initToolBar(view: View) {
        view.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
        homeToolbar.attach(this, mainTabActivity(), viewModel)
        homeToolbar.ivMenuLeft.setOnClickListener {
            EventBusUtil.post(MenuEvent(true))
            mainTabActivity().showLeftFrament(0, 0)
        }
    }
}