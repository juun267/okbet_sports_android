package org.cxct.sportlottery.ui.maintab

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gyf.immersionbar.ImmersionBar
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_home_elec.*
import kotlinx.android.synthetic.main.fragment_home_live.rv_tab_home
import kotlinx.android.synthetic.main.view_toolbar_home.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterOkActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.greenrobot.eventbus.EventBus

class HomeWorldCupFragment :
    BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    companion object {
        fun newInstance(): HomeWorldCupFragment {
            val args = Bundle()
            val fragment = HomeWorldCupFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val homeTabAdapter by lazy {
        HomeTabAdapter(HomeTabAdapter.getItems(), 5).apply {
            setOnItemClickListener { adapter, view, position ->
                (parentFragment as HomeFragment).onTabClickByPosition(position)
            }
        }
    }
    private val homeElecAdapter by lazy {
        HomeElecAdapter(mutableListOf())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_home_elec, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getConfigData()
        initView()
        initObservable()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.getRecommend()
        }
    }

    override fun onPause() {
        super.onPause()
    }

    private fun initView() {
        initToolBar()
        initTabView()
        initListView()

    }

    fun initToolBar() {
        view?.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
        iv_menu_left.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(true))
            (activity as MainTabActivity).showLeftFrament(0)
        }
        btn_register.setOnClickListener {
            startActivity(Intent(requireActivity(), RegisterOkActivity::class.java))
        }
        btn_login.setOnClickListener {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
        }
//        lin_search.setOnClickListener {
//            startActivity(Intent(requireActivity(), SportSearchtActivity::class.java))
//        }
        setupLogin()
    }

    private fun initObservable() {
        if (viewModel == null) {
            return
        }

    }

    private fun initTabView() {
        with(rv_tab_home) {
            if (layoutManager == null) {
                layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            }
            if (adapter == null) {
                adapter = homeTabAdapter
            }
        }
    }

    private fun initListView() {
        with(rv_elec) {
            if (layoutManager == null) {
                layoutManager =
                    GridLayoutManager(requireContext(), 3)
            }
            if (adapter == null) {
                adapter = homeElecAdapter
            }
            if (itemDecorationCount == 0) {
                addItemDecoration(GridSpacingItemDecoration(3, 15.dp, false))
            }
        }
    }

    private fun setupLogin() {
        viewModel.isLogin.value?.let {
            btn_register.isVisible = !it
            btn_login.isVisible = !it
//            lin_search.visibility = if (it) View.VISIBLE else View.INVISIBLE
            ll_user_money.visibility = if (it) View.VISIBLE else View.GONE
        }
    }
}
