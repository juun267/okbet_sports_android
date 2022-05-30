package org.cxct.sportlottery.ui.game.menu

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import kotlinx.android.synthetic.main.fragment_left_menu.*
import kotlinx.android.synthetic.main.snackbar_login_notify.view.*
import kotlinx.android.synthetic.main.snackbar_my_favorite_notify.view.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.MyFavoriteNotifyType
import org.cxct.sportlottery.network.sport.SearchResponse
import org.cxct.sportlottery.network.sport.SearchResult
import org.cxct.sportlottery.network.sport.SportMenu
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.CustomSecurityDialog
import org.cxct.sportlottery.ui.component.overScrollView.OverScrollDecoratorHelper
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.ui.menu.ChangeAppearanceDialog
import org.cxct.sportlottery.ui.menu.ChangeOddsTypeFullScreenDialog
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.news.NewsActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.ui.vip.VipActivity
import org.cxct.sportlottery.ui.withdraw.BankActivity
import org.cxct.sportlottery.ui.withdraw.WithdrawActivity
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.listener.OnClickListener
import org.cxct.sportlottery.util.phoneNumCheckDialog
import org.cxct.sportlottery.widget.highLightTextView.HighlightTextView

/**
 * @app_destination 左邊選單
 */
@SuppressLint("NotifyDataSetChanged")
class LeftMenuFragment : BaseFragment<GameViewModel>(GameViewModel::class), OnClickListener {

    private var mCurMatchType: MatchType? = null

    private var mCloseMenuListener: View.OnClickListener? = null

    private var newAdapter =
        LeftMenuItemNewAdapter(
            sConfigData?.thirdOpen == FLAG_OPEN,
            LeftMenuItemNewAdapter.HeaderSelectedListener(
                backMainPageSelectedListener = {//backMainPage
                    startActivity(Intent(context, GamePublicityActivity::class.java))
                },
                { //recharge
                    viewModel.checkRechargeSystem()
                    closeMenuFragment()
                },
                { //withdraw
                    avoidFastDoubleClick()
                    viewModel.checkWithdrawSystem()
                    closeMenuFragment()
                },
                { //member level
                    startActivity(Intent(context, VipActivity::class.java))
                    closeMenuFragment()
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
                    closeMenuFragment()
                },
                { //inPlay
                    viewModel.navDirectEntrance(MatchType.IN_PLAY, null)
                    closeMenuFragment()
                },
                { //premium
                    viewModel.navDirectEntrance(MatchType.EPS, null)
                    closeMenuFragment()
                }),
            LeftMenuItemNewAdapter.ItemSelectedListener(
                { sportType -> //點擊
                    navSportEntrance(sportType)
                },
                { gameType, addOrRemove -> //圖釘
                    viewModel.leftPinFavorite(gameType, addOrRemove)
                }
            ),
            LeftMenuItemNewAdapter.FooterSelectedListener(
                { //盤口設定
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.pop_left_to_right_enter_opaque,
                            0,
                            0,
                            R.anim.push_right_to_left_exit_opaque
                        )
                        .replace(
                            R.id.fl_container,
                            ChangeOddsTypeFullScreenDialog()
                        )
                        .addToBackStack(ChangeOddsTypeFullScreenDialog::class.java.simpleName)
                        .commit()
                },
                { //外觀
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.pop_left_to_right_enter_opaque,
                            0,
                            0,
                            R.anim.push_right_to_left_exit_opaque
                        )
                        .replace(
                            R.id.fl_container,
                            ChangeAppearanceDialog()
                        )
                        .addToBackStack(ChangeAppearanceDialog::class.java.simpleName)
                        .commit()
                },
                { //遊戲規則
                    JumpUtil.toInternalWeb(
                        requireContext(),
                        Constants.getGameRuleUrl(requireContext()),
                        getString(R.string.game_rule)
                    )
                    closeMenuFragment()
                },
                {
                    startActivity(Intent(context, NewsActivity::class.java))
                }
            )
        )

    //提示
    private var snackBarMyFavoriteNotify: Snackbar? = null
    private var specialList: MutableList<MenuItemData> = mutableListOf()
    private var searchHistoryList = mutableListOf<String>()

    //簡訊驗證彈窗
    private var customSecurityDialog: CustomSecurityDialog? = null
    private lateinit var searchResultAdapter: CommonAdapter<SearchResult>


    override fun onItemClick(position: Int) {
        super.onItemClick(position)
        viewModel.navSpecialEntrance(
            MatchType.OTHER,
            null,
            specialList[position].gameType,
            specialList[position].title
        )
        closeMenuFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_left_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserve()
        initRecyclerView()
    }

    fun initView() {
        etSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                layoutSearch.visibility = View.VISIBLE
                rv_menu.visibility = View.GONE
                initSearch()
            } else {
                layoutSearch.visibility = View.GONE
                rv_menu.visibility = View.VISIBLE
            }
        }
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (etSearch.text.isNotEmpty()) {
                    startSearch()
                } else {
                    layoutSearch.visibility = View.VISIBLE
                    layoutSearchResult.visibility = View.GONE
                    searchHistoryAdapter?.notifyDataSetChanged()
                }
            }
        })
        tvClear.setOnClickListener {
            if (searchHistoryList.size != 0) {
                searchHistoryList.clear()
            }
            MultiLanguagesApplication.saveSearchHistory(searchHistoryList)
            searchHistoryAdapter?.notifyDataSetChanged()
        }
        layoutSearch.setOnClickListener {
            etSearch.clearFocus()
            layoutSearch.visibility = View.GONE
            layoutSearchResult.visibility = View.GONE
            rv_menu.visibility = View.VISIBLE


        }
        btn_close.setOnClickListener {
            closeMenuFragment()
        }
        img_menu.setOnClickListener {
            hideKeyboard()
            etSearch.clearFocus()
            etSearch.setText("")
            layoutSearch.visibility = View.GONE
            layoutSearchResult.visibility = View.GONE
            rv_menu.visibility = View.VISIBLE
        }
    }

    private val unselectedList = mutableListOf<MenuItemData>()

    private fun initData(list: List<SportMenu>) {
        unselectedList.clear()
        var game = ""
        mCurMatchType.let {
            if (it != null) {
                game = viewModel.getSportSelectedCode(it) ?: ""
            }
        }
        viewModel.getSearchResult()
        list.forEach {
            val matchType = it.entranceType

            when (it.gameType) {
                GameType.VB -> {
                    if (it.gameCount > 0 && matchType != null) {
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_volleyball,
                                getString(R.string.volleyball),
                                GameType.VB.key,
                                0,
                                it.gameCount,
                                game == GameType.VB.key
                            )
                        )
                    }
                }
                GameType.TN -> {
                    if (it.gameCount > 0 && matchType != null) {
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_tennis,
                                getString(R.string.tennis),
                                GameType.TN.key,
                                0,
                                it.gameCount,
                                game == GameType.TN.key
                            )
                        )
                    }
                }
                GameType.BK -> {
                    if (it.gameCount > 0 && matchType != null) {
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_basketball,
                                getString(R.string.basketball),
                                GameType.BK.key,
                                0,
                                it.gameCount,
                                game == GameType.BK.key
                            )
                        )
                    }
                }
                GameType.FT -> {
                    if (it.gameCount > 0 && matchType != null) {
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_soccer,
                                getString(R.string.soccer),
                                GameType.FT.key,
                                0,
                                it.gameCount,
                                game == GameType.FT.key
                            )
                        )
                    }
                }

                GameType.BM -> {
                    if (it.gameCount > 0 && matchType != null) {
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_badminton,
                                getString(R.string.badminton),
                                GameType.BM.key,
                                0,
                                it.gameCount,
                                game == GameType.BM.key
                            )
                        )
                    }
                }
                GameType.TT -> {
                    if (it.gameCount > 0 && matchType != null) {
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_pingpong,
                                getString(R.string.table_tennis),
                                GameType.TT.key,
                                0,
                                it.gameCount,
                                game == GameType.TT.key
                            )
                        )
                    }
                }
                GameType.IH -> {
                    if (it.gameCount > 0 && matchType != null) {
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_ice_hockey,
                                getString(R.string.ice_hockey),
                                GameType.IH.key,
                                0,
                                it.gameCount,
                                game == GameType.IH.key
                            )
                        )
                    }
                }
                GameType.BX -> {
                    if (it.gameCount > 0 && matchType != null) {
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_boxing,
                                getString(R.string.boxing),
                                GameType.BX.key,
                                0,
                                it.gameCount,
                                game == GameType.BX.key
                            )
                        )
                    }
                }
                GameType.CB -> {
                    if (it.gameCount > 0 && matchType != null) {
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_snooker,
                                getString(R.string.snooker),
                                GameType.CB.key,
                                0,
                                it.gameCount,
                                game == GameType.CB.key
                            )
                        )
                    }
                }
                GameType.CK -> {
                    if (it.gameCount > 0 && matchType != null) {
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_cricket,
                                getString(R.string.cricket),
                                GameType.CK.key,
                                0,
                                it.gameCount,
                                game == GameType.CK.key
                            )
                        )
                    }
                }
                GameType.BB -> {
                    if (it.gameCount > 0 && matchType != null) {
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_baseball,
                                getString(R.string.baseball),
                                GameType.BB.key,
                                0,
                                it.gameCount,
                                game == GameType.BB.key
                            )
                        )
                    }
                }
                GameType.RB -> {
                    if (it.gameCount > 0 && matchType != null) {
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_rugby,
                                getString(R.string.rugby_football),
                                GameType.RB.key,
                                0,
                                it.gameCount,
                                game == GameType.RB.key
                            )
                        )
                    }
                }
                GameType.AFT -> {
                    if (it.gameCount > 0 && matchType != null) {
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_amfootball,
                                getString(R.string.america_football),
                                GameType.AFT.key,
                                0,
                                it.gameCount,
                                game == GameType.AFT.key
                            )
                        )
                    }
                }
                GameType.MR -> {
                    if (it.gameCount > 0 && matchType != null) {
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_racing,
                                getString(R.string.motor_racing),
                                GameType.MR.key,
                                0,
                                it.gameCount,
                                game == GameType.MR.key
                            )
                        )
                    }
                }
                GameType.GF -> {
                    if (it.gameCount > 0 && matchType != null) {
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_golf,
                                getString(R.string.golf),
                                GameType.GF.key,
                                0,
                                it.gameCount,
                                game == GameType.GF.key
                            )
                        )
                    }
                }
                GameType.FB -> {
                    if (it.gameCount > 0 && matchType != null) {
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_finance,
                                getString(R.string.financial_bets),
                                GameType.FB.key,
                                0,
                                it.gameCount,
                                game == GameType.FB.key
                            )
                        )
                    }
                }
                //coming soon
                GameType.BB_COMING_SOON -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.img_baseball,
                            getString(R.string.baseball),
                            GameType.BB_COMING_SOON.key,
                            0,
                            -1,
                            false
                        )
                    )
                }
                GameType.ES_COMING_SOON -> {
                    unselectedList.add(
                        MenuItemData(
                            R.drawable.img_esports,
                            getString(R.string.esports),
                            GameType.ES_COMING_SOON.key,
                            0,
                            -1,
                            false
                        )
                    )
                }
                else -> {
                }
            }
        }
        viewModel.notifyFavorite(FavoriteType.SPORT)
    }

    fun initObserve() {
        viewModel.sportCouponMenuResult.observe(this.viewLifecycleOwner) {
            it.peekContent().let { data ->
                specialList.clear()
                data.sportCouponMenuData.forEachIndexed { _, sportCouponMenuData ->
                    val list = MenuItemData(
                        0,
                        sportCouponMenuData.couponName,
                        sportCouponMenuData.couponCode,
                        0,
                        0,
                        false
                    ).apply {
                        this.couponIcon = sportCouponMenuData.icon
                    }
                    specialList.add(list)
                }
                newAdapter.addSpecialEvent(specialList, this)
            }

        }

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

        viewModel.curMatchType.observe(viewLifecycleOwner) {
            updateCurMatchType(it)
        }

        viewModel.sportMenuList.observe(viewLifecycleOwner) {

            initData(it.peekContent())
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

        //TODO Bill 判斷使用者有沒有手機號碼
        viewModel.needToSendTwoFactor.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    context?.let { it ->
                        customSecurityDialog = CustomSecurityDialog(it).apply {
                            getSecurityCodeClickListener {
                                this.showSmeTimer300()
                                viewModel.sendTwoFactor()
                            }
                            positiveClickListener = CustomSecurityDialog.PositiveClickListener { number ->
                                viewModel.validateTwoFactor(ValidateTwoFactorRequest(number))
                            }
                        }
                        customSecurityDialog?.show(parentFragmentManager, null)
                    }
                }
            }
        }

        viewModel.errorMessageDialog.observe(viewLifecycleOwner) {
            val errorMsg = it ?: getString(R.string.unknown_error)
            this.context?.let { context -> CustomAlertDialog(context) }?.apply {
                setMessage(errorMsg)
                setNegativeButtonText(null)
                setCanceledOnTouchOutside(false)
                setCancelable(false)
            }?.show(childFragmentManager, null)
        }

        viewModel.twoFactorSuccess.observe(viewLifecycleOwner) {
            if (it == true)
                customSecurityDialog?.dismiss()
        }

        viewModel.twoFactorResult.observe(viewLifecycleOwner) {
            //傳送驗證碼成功後才能解鎖提交按鈕
            customSecurityDialog?.setPositiveBtnClickable(it?.success ?: false)
            sConfigData?.hasGetTwoFactorResult = true
        }

        //使用者沒有電話號碼
        viewModel.showPhoneNumberMessageDialog.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                context?.let { c ->
                    if (!b) phoneNumCheckDialog(c, childFragmentManager)
                }
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
        viewModel.searchResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { list ->
                if (!layoutSearchResult.isVisible) {
                    layoutSearchResult.visibility = View.VISIBLE
                    layoutSearch.visibility = View.GONE
                }
                if (list.isNotEmpty()) {
                    rvSearchResult.visibility = View.VISIBLE
                    layoutNoData.visibility = View.GONE
                    searchResult.clear()
                    searchResult.addAll(list)
                    searchResultAdapter.notifyDataSetChanged()
                } else {
                    rvSearchResult.visibility = View.GONE
                    layoutNoData.visibility = View.VISIBLE
                }
            }

        }

        viewModel.leftNotifyLogin.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                setSnackBarMyFavoriteNotify(isLogin = false)
            }
        }

        viewModel.leftNotifyFavorite.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                setSnackBarMyFavoriteNotify(myFavoriteNotifyType = it)
            }
        }

    }

    var searchResult: MutableList<SearchResult> = ArrayList()
    private var searchHistoryAdapter: CommonAdapter<String>? = null

    private fun initSearch() {
        layoutSearch.visibility = View.VISIBLE
        MultiLanguagesApplication.searchHistory?.let {
            searchHistoryList = it
        }
        searchHistoryList.let {
            if (it.size > 0) {
                rvHistory.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                rvHistory.isNestedScrollingEnabled = false
                layoutHistory.visibility = View.VISIBLE
                searchHistoryAdapter = object : CommonAdapter<String>(context, R.layout.item_search_history, it) {
                    override fun convert(holder: ViewHolder, t: String, position: Int) {
                        //holder.setText(R.id.tvHistory, t)
                        val tvHistory = holder.getView<TextView>(R.id.tvHistory)
                        tvHistory.text = t
                        tvHistory.setOnClickListener {
                            etSearch.setText(t)
                        }
                    }
                }
                rvHistory.adapter = searchHistoryAdapter
                OverScrollDecoratorHelper.setUpOverScroll(rvHistory, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
            }
        }
    }

    private fun startSearch() {
        if (searchHistoryList.any {
                it == etSearch.text.toString()
            }) {
            searchHistoryList.remove(etSearch.text.toString())
            searchHistoryList.add(0, etSearch.text.toString())
        } else if (searchHistoryList.size == 10) {
            searchHistoryList.removeAt(9)
            searchHistoryList.add(0, etSearch.text.toString())
        } else {
            searchHistoryList.add(0, etSearch.text.toString())
        }
        MultiLanguagesApplication.saveSearchHistory(searchHistoryList)
        viewModel.getSportSearch(etSearch.text.toString())
        //rvHostory.adapter?.notifyDataSetChanged()
    }

    private fun initRecyclerView() {
        rv_menu.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = newAdapter
            OverScrollDecoratorHelper.setUpOverScroll(rv_menu, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
        }
        rvSearchResult.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        rvSearchResult.isNestedScrollingEnabled = false
        searchResultAdapter = object : CommonAdapter<SearchResult>(context, R.layout.item_search_result_sport, searchResult) {
            override fun convert(holder: ViewHolder, t: SearchResult, position: Int) {
                holder.setText(R.id.tvResultTittle, t.sportTitle)
                val rvResultLeague = holder.getView<RecyclerView>(R.id.rvResultLeague)
                rvResultLeague.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                rvResultLeague.isNestedScrollingEnabled = false
                val adapter =
                    object : CommonAdapter<SearchResult.SearchResultLeague>(context, R.layout.item_search_result_league, t.searchResultLeague) {
                        override fun convert(holder: ViewHolder, it: SearchResult.SearchResultLeague, position: Int) {
                            val tvLeagueTittle = holder.getView<HighlightTextView>(R.id.tvLeagueTittle)
                            tvLeagueTittle.setCustomText(it.league)
                            tvLeagueTittle.highlight(etSearch.text.toString())
                            val rvResultMatch = holder.getView<RecyclerView>(R.id.rvResultMatch)
                            rvResultMatch.layoutManager =
                                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                            rvResultMatch.isNestedScrollingEnabled = false
                            val adapter = object :
                                CommonAdapter<SearchResponse.Row.LeagueMatch.MatchInfo>(
                                    context,
                                    R.layout.item_search_result_match,
                                    t.searchResultLeague[position].leagueMatchList
                                ) {
                                override fun convert(holder: ViewHolder, itt: SearchResponse.Row.LeagueMatch.MatchInfo, position: Int) {
                                    val time = SpannableString(TimeUtil.timeFormat(itt.startTime.toLong(), TimeUtil.DM_HM_FORMAT))
                                    time.setSpan(ForegroundColorSpan(ContextCompat.getColor(holder.convertView.context, R.color.color_A3A3A3_666666)),
                                        0, time.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                                    val tvTimeAndMatch = holder.getView<HighlightTextView>(R.id.tv_time_and_match)
                                    tvTimeAndMatch.setCustomText(itt.homeName + " v " + itt.awayName)
                                    tvTimeAndMatch.setSpannableTextWithoutHighlight(time)
                                    tvTimeAndMatch.needDivider(true)
                                    tvTimeAndMatch.highlight(etSearch.text.toString())
                                    tvTimeAndMatch.setOnClickListener {
                                        closeMenuFragment()
                                        viewModel.navSpecialEntrance(
                                            MatchType.DETAIL,
                                            GameType.getGameType(t.gameType)!!,
                                            itt.matchId,
                                            if (itt.isInPlay) MatchType.IN_PLAY else null
                                        )
                                    }
                                }
                            }
                            rvResultMatch.adapter = adapter
                        }
                    }
                rvResultLeague.adapter = adapter
            }
        }
        rvSearchResult.adapter = searchResultAdapter
        OverScrollDecoratorHelper.setUpOverScroll(rvSearchResult, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
    }

    private fun updateCurMatchType(matchType: MatchType?) {
        mCurMatchType = matchType
        updateCurMatchSelectedSport()
    }

    private fun updateCurMatchSelectedSport() {
        val matchSelectedGameType = mCurMatchType?.let {
            viewModel.getSportSelectedCode(it)
        }
        matchSelectedGameType?.let { selectedGameType ->
            unselectedList.forEach {
                it.isCurrentSportType = it.gameType == selectedGameType
            }
        }

        newAdapter.addFooterAndSubmitList(unselectedList)
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
            GameType.FB.name -> GameType.FB
            else -> GameType.FT
        }

        when {
            sportType == GameType.GF || sportType == GameType.FB -> { //GF、FB只有冠軍
                viewModel.navSpecialEntrance(
                    MatchType.OUTRIGHT,
                    sportType
                )
                closeMenuFragment()
            }

            matchType != null -> {
                matchType.let {
                    viewModel.navSpecialEntrance(
                        it,
                        sportType
                    )
                    closeMenuFragment()
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

    /**
     * 選單選擇結束，需透過 listener 讓上層關閉 選單
     */
    fun setCloseMenuListener(listener: View.OnClickListener?) {
        mCloseMenuListener = listener
    }

    private fun closeMenuFragment() {
        mCloseMenuListener?.onClick(null)
        clearLeftMenu()
    }

    fun clearLeftMenu() {
        if (childFragmentManager.backStackEntryCount > 0) childFragmentManager.popBackStack()
        etSearch.setText("")
    }

}