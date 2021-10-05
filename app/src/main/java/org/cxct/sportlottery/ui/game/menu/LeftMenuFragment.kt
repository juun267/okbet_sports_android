package org.cxct.sportlottery.ui.game.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_left_menu.*
import kotlinx.android.synthetic.main.snackbar_my_favorite_notify.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.menu.ChangeOddsTypeFullScreenDialog
import org.cxct.sportlottery.util.JumpUtil

class LeftMenuFragment : BaseDialog<GameViewModel>(GameViewModel::class) {

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
    var selectedAdapter =
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
        }, LeftMenuItemAdapter.SportClickListener { sportType -> navSportEntrance(sportType) })

    //提示
    var snackBarMyFavoriteNotify: Snackbar? = null

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
        initData()
        initButton()
    }

    private fun initButton() {
        // 返回
        btn_close.setOnClickListener {
            dismiss()
        }
        //滾球
        ct_inplay.setOnClickListener { }
        //特優賠率
        ct_premium_odds.setOnClickListener { }
        //遊戲規則
        ct_game_rule.setOnClickListener {
            JumpUtil.toInternalWeb(requireContext(), Constants.getGameRuleUrl(requireContext()), getString(R.string.game_rule))
            dismiss()
        }
        //盤口設定
        tv_odds_type.setOnClickListener {
            ChangeOddsTypeFullScreenDialog().show(parentFragmentManager, null)
        }
    }

    private fun initData() {
        unselectedAdapter.data = ArrayList(
            listOf(
                MenuItemData(
                    R.drawable.selector_sport_type_item_img_ft_v4,
                    getString(R.string.soccer),
                    GameType.FT.key,
                    0
                ),
                MenuItemData(
                    R.drawable.selector_sport_type_item_img_bk_v4,
                    getString(R.string.basketball),
                    GameType.BK.key,
                    0
                ),
                MenuItemData(
                    R.drawable.selector_sport_type_item_img_tn_v4,
                    getString(R.string.tennis),
                    GameType.TN.key,
                    0
                ),
                MenuItemData(
                    R.drawable.selector_sport_type_item_img_vb_v4,
                    getString(R.string.volleyball),
                    GameType.VB.key,
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

        viewModel.isLoading.observe(this.viewLifecycleOwner, {
            if (it)
                loading()
            else
                hideLoading()
        })
    }

    private fun initRecyclerView() {
        rv_unselect.apply {
            layoutManager =
                object : LinearLayoutManager(rv_unselect.context, VERTICAL, false) {
                    override fun canScrollVertically(): Boolean {
                        return false
                    }
                }

            adapter = unselectedAdapter
        }

        rv_selected.apply {
            layoutManager =
                object : LinearLayoutManager(rv_selected.context, VERTICAL, false) {
                    override fun canScrollVertically(): Boolean {
                        return false
                    }
                }

            adapter = selectedAdapter
        }
    }

    private fun updateMenuSport(favorSportTypeList: List<String>) {
        unselectedAdapter.data.forEach { menuSport ->
            menuSport.isSelected =
                if (favorSportTypeList.isNotEmpty() && favorSportTypeList.contains(menuSport.gameType)) 1 else 0
        }
        unselectedAdapter.notifyDataSetChanged()
    }

    private fun updateFavorSport(favorSportTypeList: List<String>) {
        val selectedList = unselectedAdapter.data.filter {
            favorSportTypeList.contains(it.gameType)
        }.sortedBy {
            favorSportTypeList.indexOf(it.gameType)
        }
        selectedAdapter.data = selectedList
        line_pin.visibility = if(selectedList.isNotEmpty() && selectedList.size < 4) View.VISIBLE else View.GONE
    }

    private fun navSportEntrance(sport:String){
        loading()
        val matchType = when (sport) {
            GameType.FT.name -> viewModel.cardMatchTypeFT.value
            GameType.BK.name -> viewModel.cardMatchTypeBK.value
            GameType.TN.name -> viewModel.cardMatchTypeTN.value
            GameType.VB.name -> viewModel.cardMatchTypeVB.value
            else -> MatchType.TODAY
        }
        val sportType = when (sport) {
            GameType.FT.name -> GameType.FT
            GameType.BK.name -> GameType.BK
            GameType.TN.name -> GameType.TN
            GameType.VB.name -> GameType.VB
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

        snackBarMyFavoriteNotify = activity?.let {
            Snackbar.make(
                this@LeftMenuFragment.requireView(),
                title,
                Snackbar.LENGTH_LONG
            ).apply {
                val snackView: View = layoutInflater.inflate(
                    R.layout.snackbar_my_favorite_notify,
                    activity?.findViewById(android.R.id.content),
                    false
                )
                snackView.txv_title.text = title
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