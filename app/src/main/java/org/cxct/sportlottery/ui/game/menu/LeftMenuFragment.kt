package org.cxct.sportlottery.ui.game.menu

import android.content.Intent
import android.os.Bundle
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
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType
import org.cxct.sportlottery.network.sport.SportMenu
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.menu.ChangeAppearanceDialog
import org.cxct.sportlottery.ui.menu.ChangeOddsTypeFullScreenDialog
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.ui.vip.VipActivity
import org.cxct.sportlottery.ui.withdraw.BankActivity
import org.cxct.sportlottery.ui.withdraw.WithdrawActivity
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LanguageManager

class LeftMenuFragment : BaseDialog<GameViewModel>(GameViewModel::class) {
    private var newAdapter =
        LeftMenuItemNewAdapter(
            sConfigData?.thirdOpen == FLAG_OPEN,
            LeftMenuItemNewAdapter.HeaderSelectedListener(
                { //recharge
                    viewModel.checkRechargeSystem()
                    dismiss()
                },
                { //withdraw
                    viewModel.checkWithdrawSystem()
                    dismiss()
                },
                { //member level
                    startActivity(Intent(context, VipActivity::class.java))
                    dismiss()
                },
                { //promotion
                    context?.let {
                        JumpUtil.toInternalWeb(
                            it,
                            Constants.getPromotionUrl(
                                viewModel.token,
                                LanguageManager.getSelectLanguage(context)
                            ),
                            getString(R.string.promotion)
                        )
                    }
                    dismiss()
                },
                { //inPlay
                    viewModel.navDirectEntrance(MatchType.IN_PLAY, null)
                    dismiss()
                },
                { //premium
                    viewModel.navDirectEntrance(MatchType.EPS, null)
                    dismiss()
                }),
            LeftMenuItemNewAdapter.ItemSelectedListener(
                { sportType -> //點擊
                    navSportEntrance(sportType)
                },
                { gameType, addOrRemove -> //圖釘
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
                }
            ),
            LeftMenuItemNewAdapter.FooterSelectedListener(
                { //盤口設定
                    ChangeOddsTypeFullScreenDialog().show(parentFragmentManager, null)
                },
                { //外觀
                    ChangeAppearanceDialog().show(parentFragmentManager, null)
                },
                { //遊戲規則
                    JumpUtil.toInternalWeb(
                        requireContext(),
                        Constants.getGameRuleUrl(requireContext()),
                        getString(R.string.game_rule)
                    )
                    dismiss()
                }
            )
        )

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
    }

    private val unselectedList = mutableListOf<MenuItemData>()

    private fun initData(list: List<SportMenu>) {
        unselectedList.clear()
        list.forEach {
            when (it.gameType) {
                GameType.VB -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_left_menu_ball_vb,
                            getString(R.string.volleyball),
                            GameType.VB.key,
                            0
                        )
                    )
                }
                GameType.TN -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_left_menu_ball_tn,
                            getString(R.string.tennis),
                            GameType.TN.key,
                            0
                        )
                    )
                }
                GameType.BK -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_left_menu_ball_bk,
                            getString(R.string.basketball),
                            GameType.BK.key,
                            0
                        )
                    )
                }
                GameType.FT -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_left_menu_ball_ft,
                            getString(R.string.soccer),
                            GameType.FT.key,
                            0
                        )
                    )
                }

                GameType.BM -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_left_menu_ball_bm,
                            getString(R.string.badminton),
                            GameType.BM.key, 0
                        )
                    )
                }
                GameType.TT -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_left_menu_ball_pp,
                            getString(R.string.ping_pong),
                            GameType.TT.key, 0
                        )
                    )
                }
                GameType.IH -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_left_menu_ball_ih,
                            getString(R.string.ice_hockey),
                            GameType.IH.key, 0
                        )
                    )
                }
                GameType.BX -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_left_menu_ball_bx,
                            getString(R.string.boxing),
                            GameType.BX.key, 0
                        )
                    )
                }
                GameType.CB -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_left_menu_ball_cb,
                            getString(R.string.cue_ball),
                            GameType.CB.key, 0
                        )
                    )
                }
                GameType.CK -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_left_menu_ball_ck,
                            getString(R.string.cricket),
                            GameType.CK.key, 0
                        )
                    )
                }
                GameType.BB -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_left_menu_ball_bb,
                            getString(R.string.baseball),
                            GameType.BB.key, 0
                        )
                    )
                }
                GameType.RB -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_left_menu_ball_rb,
                            getString(R.string.rugby_football),
                            GameType.RB.key, 0
                        )
                    )
                }
                GameType.AFT -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_left_menu_ball_aft,
                            getString(R.string.america_football),
                            GameType.AFT.key, 0
                        )
                    )
                }
                GameType.MR -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_left_menu_ball_mr,
                            getString(R.string.motor_racing),
                            GameType.MR.key, 0
                        )
                    )
                }
                GameType.GF -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.selector_left_menu_ball_gf,
                            getString(R.string.golf),
                            GameType.GF.key, 0
                        )
                    )
                }
            }
        }
        viewModel.notifyFavorite(FavoriteType.SPORT)
    }

    fun initObserve() {
        viewModel.favorSportList.observe(this.viewLifecycleOwner) {
            updateMenuSport(it)
            updateFavorSport(it)
        }

        viewModel.isLogin.observe(this.viewLifecycleOwner) {
            newAdapter.isLogin = it
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

        viewModel.rechargeSystemOperation.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    startActivity(Intent(context, MoneyRechargeActivity::class.java))
                } else {
                    showPromptDialog(
                        getString(R.string.prompt),
                        getString(R.string.message_recharge_maintain)
                    ) {}
                }
            }
        }

        viewModel.withdrawSystemOperation.observe(viewLifecycleOwner) {
            val operation = it.getContentIfNotHandled()
            if (operation == false) {
                showPromptDialog(
                    getString(R.string.prompt),
                    getString(R.string.message_withdraw_maintain)
                ) {}
            }
        }

        viewModel.needToUpdateWithdrawPassword.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(
                        getString(R.string.withdraw_setting),
                        getString(R.string.please_setting_withdraw_password),
                        getString(R.string.go_to_setting),
                        true
                    ) {
                        startActivity(
                            Intent(
                                context,
                                SettingPasswordActivity::class.java
                            ).apply {
                                putExtra(
                                    SettingPasswordActivity.PWD_PAGE,
                                    SettingPasswordActivity.PwdPage.BANK_PWD
                                )
                            })
                    }
                } else {
                    viewModel.checkProfileInfoComplete()
                }
            }
        }

        viewModel.needToCompleteProfileInfo.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(
                        getString(R.string.withdraw_setting),
                        getString(R.string.please_complete_profile_info),
                        getString(R.string.go_to_setting),
                        true
                    ) {
                        startActivity(Intent(context, ProfileActivity::class.java))
                    }
                } else {
                    viewModel.checkBankCardPermissions()
                }
            }
        }

        viewModel.needToBindBankCard.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { messageId ->
                if (messageId != -1) {
                    showPromptDialog(
                        getString(R.string.withdraw_setting),
                        getString(messageId),
                        getString(R.string.go_to_setting),
                        true
                    ) {
                        startActivity(Intent(context, BankActivity::class.java))
                    }
                } else {
                    startActivity(Intent(context, WithdrawActivity::class.java))
                }
            }
        }

        viewModel.settingNeedToUpdateWithdrawPassword.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(
                        getString(R.string.withdraw_setting),
                        getString(R.string.please_setting_withdraw_password),
                        getString(R.string.go_to_setting),
                        true
                    ) {
                        startActivity(
                            Intent(
                                context,
                                SettingPasswordActivity::class.java
                            ).apply {
                                putExtra(
                                    SettingPasswordActivity.PWD_PAGE,
                                    SettingPasswordActivity.PwdPage.BANK_PWD
                                )
                            })
                    }
                } else if (!b) {
                    startActivity(Intent(context, BankActivity::class.java))
                }
            }
        }

        viewModel.settingNeedToCompleteProfileInfo.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(
                        getString(R.string.withdraw_setting),
                        getString(R.string.please_complete_profile_info),
                        getString(R.string.go_to_setting),
                        true
                    ) {
                        startActivity(Intent(context, ProfileActivity::class.java))
                    }
                } else if (!b) {
                    startActivity(Intent(context, BankActivity::class.java))
                }
            }
        }
    }

    private fun initRecyclerView() {

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

        newAdapter.addFooterAndSubmitList(unselectedList)
    }

    private fun updateFavorSport(favorSportTypeList: List<String>) {
        val selectedList = unselectedList.filter {
            !it.isHeaderOrFooter
        }.sortedBy {
            favorSportTypeList.indexOf(it.gameType)
        }.sortedByDescending {
            it.isSelected == 1
        }.toMutableList()
        newAdapter.addFooterAndSubmitList(selectedList)
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
            GameType.TT.name -> GameType.TT
            GameType.IH.name -> GameType.IH
            GameType.BX.name -> GameType.BX
            GameType.CB.name -> GameType.CB
            GameType.CK.name -> GameType.CK
            GameType.BB.name -> GameType.BB
            GameType.RB.name -> GameType.RB
            GameType.MR.name -> GameType.MR
            GameType.GF.name -> GameType.GF
            GameType.AFT.name -> GameType.AFT
            else -> GameType.FT
        }

        when{
            sportType == GameType.GF -> { //GF 只有冠軍
                viewModel.navSpecialEntrance(
                    MatchType.OUTRIGHT,
                    sportType
                )
                dismiss()
            }

            matchType != null -> {
                matchType.let {
                    viewModel.navSpecialEntrance(
                        it,
                        sportType
                    )
                    dismiss()
                }
            }

            else -> {
                setSnackBarMyFavoriteNotify(isGameClose = true, gameType = sportType)
                hideLoading()
            }
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