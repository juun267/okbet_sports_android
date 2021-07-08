package org.cxct.sportlottery.ui.game.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_left_menu.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.SportType
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel

class LeftMenuFragment(var clickListener: GameActivity.OnMenuClickListener) :
    BaseFragment<GameViewModel>(GameViewModel::class) {

    //點擊置頂後
    private var unselectedAdapter =
        LeftMenuItemAdapter(LeftMenuItemAdapter.ItemClickListener { sportType ->
            viewModel.pinFavorite(FavoriteType.SPORT, sportType)
        })

    //取消置頂
    var selectedAdapter =
        LeftMenuItemSelectedAdapter(LeftMenuItemSelectedAdapter.ItemClickListener { sportType ->
            viewModel.pinFavorite(FavoriteType.SPORT, sportType)
        })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_left_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserve()
        initRecyclerView()
        initData()
        initButton()
    }

    //TODO
    private fun initButton() {
        // 返回
        btn_close.setOnClickListener {
            clickListener.onClick(GameActivity.MenuStatusType.CLOSE.ordinal)
        }
        //滾球
        ct_inplay.setOnClickListener {  }
        //特優賠率
        ct_premium_odds.setOnClickListener {  }
        //遊戲規則
        ct_game_rule.setOnClickListener {  }
    }

    private fun initData() {
        unselectedAdapter.data = ArrayList(
            listOf(
                MenuItemData(
                    R.drawable.selector_sport_type_item_img_ft_v4,
                    getString(R.string.soccer),
                    SportType.FOOTBALL.code,
                    0
                ),
                MenuItemData(
                    R.drawable.selector_sport_type_item_img_bk_v4,
                    getString(R.string.basketball),
                    SportType.BASKETBALL.code,
                    0
                ),
                MenuItemData(
                    R.drawable.selector_sport_type_item_img_tn_v4,
                    getString(R.string.tennis),
                    SportType.TENNIS.code,
                    0
                ),
                MenuItemData(
                    R.drawable.selector_sport_type_item_img_vb_v4,
                    getString(R.string.volleyball),
                    SportType.VOLLEYBALL.code,
                    0
                )
            )
        )

        viewModel.notifyFavorite(FavoriteType.SPORT)
    }

    fun initObserve() {
        viewModel.favorSportList.observe(this.viewLifecycleOwner, {
            updateMenuSport(it)
            updateFavorSport(it)
        })

        viewModel.isLoading.observe(this.viewLifecycleOwner,{
            if (it)
                loading()
            else
                hideLoading()
        })
    }

    private fun initRecyclerView() {

        rv_unselect.layoutManager =
            object : LinearLayoutManager(rv_unselect.context, VERTICAL, false) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        rv_unselect.adapter = unselectedAdapter

        rv_selected.layoutManager =
            object : LinearLayoutManager(rv_selected.context, VERTICAL, false) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        rv_selected.adapter = selectedAdapter
    }

    private fun updateMenuSport(favorSportTypeList: List<String>) {
        unselectedAdapter.data.forEach { menuSport ->
            menuSport.isSelected =
                if (favorSportTypeList.isNotEmpty() && favorSportTypeList.contains(menuSport.sportType)) 1 else 0
        }
        unselectedAdapter.notifyDataSetChanged()
    }

    private fun updateFavorSport(favorSportTypeList: List<String>) {
        val selectedList = unselectedAdapter.data.filter {
            favorSportTypeList.contains(it.sportType)
        }
        selectedAdapter.data = selectedList
    }
}