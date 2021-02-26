package org.cxct.sportlottery.ui.main.next

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_main_more.*
import kotlinx.android.synthetic.main.main_more_cate_tab.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.Vp2FragmentAdapter
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.ui.main.entity.GameCateData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory

class MainMoreFragment : BaseFragment<MainViewModel>(MainViewModel::class) {
    private val args: MainMoreFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initObserve()
    }

    private fun initObserve() {
        //第三方遊戲清單
        viewModel.gameCateDataList.observe(viewLifecycleOwner, Observer {
            setGameData(it)
        })
    }

    private fun setGameData(cateDataList: List<GameCateData>?) {
        //第三方遊戲開啟才顯示 類別 tabLayout
        tab_layout.visibility = if (sConfigData?.thirdOpen == FLAG_OPEN) View.VISIBLE else View.GONE
        tab_layout.removeAllTabs()

        //過濾掉 遊戲數為 0 的類別
        val gameCateFilterList = cateDataList?.filter { cateData ->
            cateData.tabDataList.sumBy { it.gameList.size } > 0
        }?.toMutableList() ?: mutableListOf()

        //預設第一個類別添加 體育遊戲
        val spCate = ThirdGameCategory.LOCAL_SP
        spCate.title = getString(R.string.sport)
        gameCateFilterList.add(0, GameCateData(spCate))

        val gameFragList = createGameFragList(gameCateFilterList)
        view_pager.adapter = Vp2FragmentAdapter(gameFragList, this)
        view_pager.isUserInputEnabled = false //關閉 viewPager2 左右滑動功能

        //tabLayout、viewPager2 連動邏輯
        TabLayoutMediator(tab_layout, view_pager) { tab, position ->
            val tabCate = gameCateFilterList[position].categoryThird
            tab.setCustomView(R.layout.main_more_cate_tab)
            tab.customView?.apply {
                this.iv_icon.setImageResource(tabCate.iconRes)
                this.tv_title.text = tabCate.title
            }
        }.attach()
    }

    private fun createGameFragList(cateDataList: List<GameCateData>?): List<Fragment> {
        val gameFragList = mutableListOf<Fragment>()
        cateDataList?.forEach { cateData ->
            val gameFrag = when (cateData.categoryThird) {
                ThirdGameCategory.LOCAL_SP -> SportFragment()
                ThirdGameCategory.CGCP -> CGCPFragment()
                ThirdGameCategory.LIVE -> LiveFragment()
                ThirdGameCategory.QP -> QPFragment()
                ThirdGameCategory.DZ -> DZFragment()
                ThirdGameCategory.BY -> BYFragment()
                else -> null
            }

            if (gameFrag != null)
                gameFragList.add(gameFrag)
        }

        return gameFragList
    }

}