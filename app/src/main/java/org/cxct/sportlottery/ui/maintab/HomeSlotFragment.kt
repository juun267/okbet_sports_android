package org.cxct.sportlottery.ui.maintab

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gyf.immersionbar.ImmersionBar
import kotlinx.android.synthetic.main.fragment_home_slot.*
import kotlinx.android.synthetic.main.view_toolbar_home.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterOkActivity
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.GridItemDecoration
import org.cxct.sportlottery.util.observe
import org.greenrobot.eventbus.EventBus

/**
 * 首页棋牌
 */
class HomeSlotFragment :
    BaseBottomNavigationFragment<MainHomeViewModel>(MainHomeViewModel::class) {

    companion object {
        fun newInstance(): HomeSlotFragment {
            val args = Bundle()
            val fragment = HomeSlotFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val homeTabAdapter by lazy {
        HomeTabAdapter(HomeTabAdapter.getItems(), 4).apply {
            setOnItemClickListener { adapter, view, position ->
                (parentFragment as HomeFragment).onTabClickByPosition(position)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_home_slot, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getConfigData()
        initView()
        initObservable()
        viewModel.getGameEntryConfig(2, 1)
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.getGameEntryConfig(2, 1)
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
        viewModel.homeGameData.observe(viewLifecycleOwner) {
            it?.let {
//                homeElecAdapter.setNewData(it)
            }
        }
//        viewModel.cardGameData.observe(viewLifecycleOwner) {
//            homeGameCardAdapter.setNewData(it.toMutableList())
//            homeGameCardAdapter.setOnItemClickListener { adapter, view, position ->
//                viewModel.requestEnterThirdGame(homeGameCardAdapter.getItem(position))
//            }
//            homeGameCardAdapter.removeAllFooterView()
//            homeGameCardAdapter.addFooterView(LayoutInflater.from(requireContext())
//                .inflate(R.layout.item_home_game_empty, null))
//            homeGameCardAdapter.footerLayout.apply {
//                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT)
//            }
//        }
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
            post {
                smoothScrollToPosition(homeTabAdapter.selectPos)
            }
        }
    }

    private fun initListView() {
        with(rv_slot) {
            if (layoutManager == null) {
                layoutManager =
                    GridLayoutManager(requireContext(), 3)
            }
//            if (adapter == null) {
//                adapter = homeElecAdapter
//            }
            if (itemDecorationCount == 0) {
                addItemDecoration(GridItemDecoration(12.dp, 12.dp, Color.TRANSPARENT, false))
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
