package org.cxct.sportlottery.ui.game.menu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_left_menu.*
import kotlinx.android.synthetic.main.snackbar_login_notify.view.*
import kotlinx.android.synthetic.main.snackbar_my_favorite_notify.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType
import org.cxct.sportlottery.network.sport.SportMenu
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.menu.ChangeOddsTypeFullScreenDialog
import org.cxct.sportlottery.util.JumpUtil

class LeftMenuFragment : BaseDialog<GameViewModel>(GameViewModel::class) {
/*

    //點擊置頂後
    private var unselectedAdapter =
        LeftMenuItemAdapter(LeftMenuItemAdapter.ItemClickListener { gameType ->
            when (viewModel.userInfo.value?.testFlag) {
                TestFlag.NORMAL.index -> {
                    viewModel.pinFavorite(
                        FavoriteType.SPORT,
                        gameType
                    )
                    setSnackBarMyFavoriteNotify(myFavoriteNotifyType = MyFavoriteNotifyType.SPORT_ADD.code)
                }
                else -> { //遊客 //尚未登入
                    setSnackBarMyFavoriteNotify(isLogin = false)
                }
            }
        }, LeftMenuItemAdapter.SportClickListener { sportType -> navSportEntrance(sportType) })

    //取消置頂
    private var selectedAdapter =
        LeftMenuItemSelectedAdapter(LeftMenuItemSelectedAdapter.ItemClickListener { gameType ->
            when (viewModel.userInfo.value?.testFlag) {
                TestFlag.NORMAL.index -> {
                    viewModel.pinFavorite(FavoriteType.SPORT, gameType)
                    setSnackBarMyFavoriteNotify(myFavoriteNotifyType = MyFavoriteNotifyType.SPORT_REMOVE.code)
                }
                else -> { //遊客 //尚未登入
                    setSnackBarMyFavoriteNotify(isLogin = false)
                }
            }
        }, LeftMenuItemAdapter.SportClickListener { sportType -> navSportEntrance(sportType) }
            , LeftMenuItemSelectedAdapter.InPlayClickListener {
                viewModel.navDirectEntrance(MatchType.IN_PLAY, null)
                dismiss()
            }, LeftMenuItemSelectedAdapter.PremiumOddsClickListener {
                viewModel.navDirectEntrance(MatchType.EPS, null)
                dismiss()
            })
*/

    private var newAdapter =
        LeftMenuItemNewAdapter(LeftMenuItemNewAdapter.ItemSelectedListener { gameType, addOrRemove ->
            when (viewModel.userInfo.value?.testFlag) {
                TestFlag.NORMAL.index -> {
                    viewModel.pinFavorite(
                        FavoriteType.SPORT,
                        gameType
                    )
                    setSnackBarMyFavoriteNotify(myFavoriteNotifyType = addOrRemove)
                }
                else -> { //遊客 //尚未登入
                    setSnackBarMyFavoriteNotify(isLogin = false)
                }
            }
        }, LeftMenuItemNewAdapter.SportClickListener { sportType -> navSportEntrance(sportType) })

    //提示
    private var snackBarMyFavoriteNotify: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_left_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setWindowAnimations(R.style.LeftMenu)
        initObserve()
        initRecyclerView()
        initButton()
    }


    private fun initButton() {
        // 返回
        btn_close.setOnClickListener {
            dismiss()
        }
        //遊戲規則
        ct_game_rule.setOnClickListener {
            JumpUtil.toInternalWeb(
                requireContext(),
                Constants.getGameRuleUrl(requireContext()),
                getString(R.string.game_rule)
            )
            dismiss()
        }
        //盤口設定
        tv_odds_type.setOnClickListener {
            ChangeOddsTypeFullScreenDialog().show(parentFragmentManager, null)
        }
    }

    private val unselectedList = mutableListOf<MenuItemData>()

    private fun initData(list: List<SportMenu>) {
        unselectedList.clear()
        list.forEach {
            when (it.gameType) {
                GameType.VB -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_sport_type_item_img_vb_v4,
                            getString(R.string.volleyball),
                            GameType.VB.key,
                            0
                        )
                    )
                }
                GameType.TN -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_sport_type_item_img_tn_v4,
                            getString(R.string.tennis),
                            GameType.TN.key,
                            0
                        )
                    )
                }
                GameType.BK -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_sport_type_item_img_bk_v4,
                            getString(R.string.basketball),
                            GameType.BK.key,
                            0
                        )
                    )
                }
                GameType.FT -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_sport_type_item_img_ft_v4,
                            getString(R.string.soccer),
                            GameType.FT.key,
                            0
                        )
                    )
                }

                GameType.BM -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_sport_type_item_img_ft_v4,
                            getString(R.string.badminton),
                            GameType.BM.key, 0
                        )
                    )
                }
                GameType.PP -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_sport_type_item_img_ft_v4,
                            getString(R.string.ping_pong),
                            GameType.PP.key, 0
                        )
                    )
                }
                GameType.IH -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_sport_type_item_img_ft_v4,
                            getString(R.string.ice_hockey),
                            GameType.IH.key, 0
                        )
                    )
                }
                GameType.BX -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_sport_type_item_img_ft_v4,
                            getString(R.string.boxing),
                            GameType.BX.key, 0
                        )
                    )
                }
                GameType.CB -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_sport_type_item_img_ft_v4,
                            getString(R.string.cue_ball),
                            GameType.CB.key, 0
                        )
                    )
                }
                GameType.CK -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_sport_type_item_img_ft_v4,
                            getString(R.string.cricket),
                            GameType.CK.key, 0
                        )
                    )
                }
                GameType.BB -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_sport_type_item_img_ft_v4,
                            getString(R.string.baseball),
                            GameType.BB.key, 0
                        )
                    )
                }
                GameType.RB -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_sport_type_item_img_ft_v4,
                            getString(R.string.rugby_football),
                            GameType.RB.key, 0
                        )
                    )
                }
                GameType.MR -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_sport_type_item_img_ft_v4,
                            getString(R.string.motor_racing),
                            GameType.MR.key, 0
                        )
                    )
                }
                GameType.GF -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_sport_type_item_img_ft_v4,
                            getString(R.string.golf),
                            GameType.GF.key, 0
                        )
                    )
                }
            }
        }
//        unselectedAdapter.data = unselectedArray

        Log.e(">>>", "initData unselectedList = $unselectedList")
//        newAdapter.addFooterAndSubmitList(unselectedList)

        viewModel.notifyFavorite(FavoriteType.SPORT)
    }

    fun initObserve() {
        viewModel.favorSportList.observe(this.viewLifecycleOwner) {
            updateMenuSport(it)
            updateFavorSport(it)
        }

        viewModel.isLoading.observe(this.viewLifecycleOwner) {
            if (it)
                loading()
            else
                hideLoading()
        }

        viewModel.sportMenuList.observe(viewLifecycleOwner) {
            it.peekContent().let { list ->
                initData(list)
            }
        }
    }

    private fun initRecyclerView() {
/*
        rv_unselect.apply {
            setHasFixedSize(false)
            layoutManager =
                LinearLayoutManager(rv_unselect.context, LinearLayoutManager.VERTICAL, false)
            adapter = unselectedAdapter
        }

        rv_selected.apply {
            setHasFixedSize(false)
            layoutManager =
                LinearLayoutManager(rv_selected.context, LinearLayoutManager.VERTICAL, false)
            adapter = selectedAdapter
        }
*/

        rv_menu.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = newAdapter
        }
    }

    private fun updateMenuSport(favorSportTypeList: List<String>) {
        unselectedList.forEach { menuSport ->
            menuSport.isSelected =
                if (favorSportTypeList.isNotEmpty() && favorSportTypeList.contains(menuSport.gameType)) 1 else 0
        }
//        unselectedAdapter.notifyDataSetChanged()

        Log.e(">>>", "updateMenuSport = $unselectedList")
        newAdapter.addFooterAndSubmitList(unselectedList)
    }

    private fun updateFavorSport(favorSportTypeList: List<String>) {
        /*
        val selectedList: MutableList<MenuItemData> = unselectedList.filter {
            favorSportTypeList.contains(it.gameType)
        }.sortedBy {
            favorSportTypeList.indexOf(it.gameType)
        }.toMutableList()
*/
        val selectedList = unselectedList.sortedBy {
            favorSportTypeList.indexOf(it.gameType)
        }.sortedByDescending {
            it.isSelected == 1
        }

//        selectedList.add(0, MenuItemData(0, "123", "123", 0)) //add header
//        selectedAdapter.data = selectedList
//        Log.e(">>>", "https://youtu.be/FR91CB5SBWU")
        Log.e(">>>", "selectedList = $selectedList")
        newAdapter.addFooterAndSubmitList(selectedList)

        line_pin.visibility =
            if (selectedList.isNotEmpty() && selectedList.size < 13) View.VISIBLE else View.GONE
    }

    private fun navSportEntrance(sport: String) {
        loading()
        val matchType = viewModel.sportMenuList.value?.peekContent()
            ?.find { it.gameType.key == sport }?.entranceType

        val sportType = when (sport) {
            GameType.FT.name -> GameType.FT
            GameType.BK.name -> GameType.BK
            GameType.TN.name -> GameType.TN
            GameType.VB.name -> GameType.VB
            GameType.BM.name -> GameType.BM
            GameType.PP.name -> GameType.PP
            GameType.IH.name -> GameType.IH
            GameType.BX.name -> GameType.BX
            GameType.CB.name -> GameType.CB
            GameType.CK.name -> GameType.CK
            GameType.BB.name -> GameType.BB
            GameType.RB.name -> GameType.RB
            GameType.MR.name -> GameType.MR
            GameType.GF.name -> GameType.GF
            else -> GameType.FT
        }
        if (matchType != null) {
            matchType.let {
                viewModel.navSpecialEntrance(
                    it,
                    sportType
                )
                dismiss()
            }
        } else {
            setSnackBarMyFavoriteNotify(isGameClose = true, gameType = sportType)
            hideLoading()
        }
    }

    private fun setSnackBarMyFavoriteNotify(
        myFavoriteNotifyType: Int? = null,
        isGameClose: Boolean? = false,
        gameType: GameType? = null,
        isLogin: Boolean? = true
    ) {
        val title = when {
            isLogin == false -> getString(R.string.login_notify)
            isGameClose == true -> String.format(
                getString(R.string.message_no_sport_game),
                getString(gameType?.string ?: 0)
            )
            else -> {
                when (myFavoriteNotifyType) {

                    MyFavoriteNotifyType.SPORT_ADD.code -> getString(R.string.myfavorite_notify_league_add)

                    MyFavoriteNotifyType.SPORT_REMOVE.code -> getString(R.string.myfavorite_notify_league_remove)

                    else -> ""
                }
            }
        }

        val layout =
            if (isLogin == true) R.layout.snackbar_my_favorite_notify else R.layout.snackbar_login_notify

        snackBarMyFavoriteNotify = activity?.let {
            Snackbar.make(
                this@LeftMenuFragment.requireView(),
                title,
                Snackbar.LENGTH_LONG
            ).apply {
                val snackView: View = layoutInflater.inflate(
                    layout,
                    activity?.findViewById(android.R.id.content),
                    false
                )
                if (isLogin == true)
                    snackView.txv_title.text = title
                else snackView.tv_notify.text = title

                (this.view as Snackbar.SnackbarLayout).apply {
                    findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
                        visibility = View.INVISIBLE
                    }
                    background.alpha = 0
                    addView(snackView, 0)
                    setPadding(0, 0, 0, 0)
                }
            }
        }
        snackBarMyFavoriteNotify?.show()
    }

}