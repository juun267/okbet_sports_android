package org.cxct.sportlottery.ui.game.menu

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.zhy.adapter.recyclerview.CommonAdapter
import com.zhy.adapter.recyclerview.base.ViewHolder
import kotlinx.android.synthetic.main.dialog_combo_detail.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_left_menu.*
import kotlinx.android.synthetic.main.fragment_left_menu.btn_close
import kotlinx.android.synthetic.main.home_sport_table_4.view.*
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
import org.cxct.sportlottery.repository.TestFlag
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.ui.common.CustomAlertDialog
import org.cxct.sportlottery.ui.common.CustomSecurityDialog
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.hall.GameV3FragmentDirections
import org.cxct.sportlottery.ui.game.home.gameTable4.OtherMatch
import org.cxct.sportlottery.ui.menu.ChangeAppearanceDialog
import org.cxct.sportlottery.ui.menu.ChangeOddsTypeFullScreenDialog
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.ui.vip.VipActivity
import org.cxct.sportlottery.ui.withdraw.BankActivity
import org.cxct.sportlottery.ui.withdraw.WithdrawActivity
import org.cxct.sportlottery.util.GameConfigManager
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.listener.OnClickListener
import org.cxct.sportlottery.widget.highLightTextView.HighlightTextView

class LeftMenuFragment : BaseDialog<GameViewModel>(GameViewModel::class), OnClickListener {
    private var newAdapter =
        LeftMenuItemNewAdapter(
            sConfigData?.thirdOpen == FLAG_OPEN,
            LeftMenuItemNewAdapter.HeaderSelectedListener(
                { //recharge
                    viewModel.checkRechargeSystem()
                    dismiss()
                },
                { //withdraw
                    avoidFastDoubleClick()
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
    var specialList: MutableList<MenuItemData> = mutableListOf()
    var searchHistoryList = mutableListOf<String>()
        //簡訊驗證彈窗
    private var customSecurityDialog: CustomSecurityDialog? = null
    lateinit var searchResultAdapter: CommonAdapter<SearchResult>


    override fun onItemClick(position: Int) {
        super.onItemClick(position)
        viewModel.navSpecialEntrance(
            MatchType.OTHER,
            null,
            specialList[position].gameType,
            specialList[position].title
        )
        dismiss()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_left_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setWindowAnimations(R.style.LeftMenu)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        initView()
        initObserve()
        initRecyclerView()
    }

    fun initView(){
        etSearch.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                layoutSearch.visibility = View.VISIBLE
                rv_menu.visibility = View.GONE
                initSearch()
            }
        }
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if(etSearch.text.isNotEmpty()){
                    startSearch()
                }else{
                    layoutSearch.visibility = View.VISIBLE
                    layoutSearchResult.visibility = View.GONE
                }
            }
        })
        tvClear.setOnClickListener {
            if (searchHistoryList!!.size != 0) {
                searchHistoryList!!.clear()
            }
            MultiLanguagesApplication.saveSearchHistory(searchHistoryList)
            searchHistoryAdapter.notifyDataSetChanged()
        }
        layoutSearch.setOnClickListener {
            etSearch.clearFocus()
            layoutSearch.visibility = View.GONE
            layoutSearchResult.visibility = View.GONE
            rv_menu.visibility = View.VISIBLE


        }
        btn_close.setOnClickListener {
            dismiss()
        }
    }

    private val unselectedList = mutableListOf<MenuItemData>()

    private fun initData(list: List<SportMenu>) {
        unselectedList.clear()
        var game = ""
        val selectGame = viewModel.curMatchType.value
        selectGame.let {
            if(it != null){
                game = viewModel.getSportSelectedCode(it) ?: ""
            }
        }
        viewModel.getSearchResult()
        list.forEach {
            val matchType = viewModel.sportMenuList.value?.peekContent()
                ?.find { matchType -> matchType.gameType.key == it.gameType.key }?.entranceType

            when (it.gameType) {
                GameType.VB -> {
                    if(it.gameCount > 0 && matchType != null){
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
                    if(it.gameCount > 0 && matchType != null){
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
                    if(it.gameCount > 0 && matchType != null){
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
                    if(it.gameCount > 0 && matchType != null){
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
                    if(it.gameCount > 0 && matchType != null){
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
                    if(it.gameCount > 0 && matchType != null){
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_pingpong,
                                getString(R.string.ping_pong),
                                GameType.TT.key,
                                0,
                                it.gameCount,
                                game == GameType.TT.key
                            )
                        )
                    }
                }
                GameType.IH -> {
                    if(it.gameCount > 0 && matchType != null){
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_icehockey,
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
                    if(it.gameCount > 0 && matchType != null){
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
                    if(it.gameCount > 0 && matchType != null){
                        unselectedList.add(
                            MenuItemData(
                                R.drawable.img_snooker,
                                getString(R.string.cue_ball),
                                GameType.CB.key,
                                0,
                                it.gameCount,
                                game == GameType.CB.key
                            )
                        )
                    }
                }
                GameType.CK -> {
                    if(it.gameCount > 0 && matchType != null){
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
                    if(it.gameCount > 0 && matchType != null){
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
                    if(it.gameCount > 0 && matchType != null){
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
                    if(it.gameCount > 0 && matchType != null){
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
                    if(it.gameCount > 0 && matchType != null){
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
                    if(it.gameCount > 0 && matchType != null){
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
            }
        }
        viewModel.notifyFavorite(FavoriteType.SPORT)
    }

    fun initObserve() {
        viewModel.sportCouponMenuResult.observe(this.viewLifecycleOwner) {
            it.peekContent().let { data ->
                specialList.clear()
                data.sportCouponMenuData.forEachIndexed { index, sportCouponMenuData ->
                    var list = MenuItemData(
                        0,
                        sportCouponMenuData.couponName,
                        sportCouponMenuData.couponCode,
                        0,
                        0,
                        false
                    )
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

        //TODO Bill 判斷使用者有沒有手機號碼
        viewModel.needToSendTwoFactor.observe(this) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    context?.let { it ->
                        customSecurityDialog = CustomSecurityDialog(it).apply {
                            getSecurityCodeClickListener {
                                this.showSmeTimer300()
                                viewModel.sendTwoFactor()
                            }
                            positiveClickListener = CustomSecurityDialog.PositiveClickListener{ number ->
                                viewModel.validateTwoFactor(ValidateTwoFactorRequest(number))
                            }
                        }
                        customSecurityDialog?.show(parentFragmentManager,null)
                    }
                }
            }
        }

        viewModel.errorMessageDialog.observe(viewLifecycleOwner){
            val errorMsg = it ?: getString(R.string.unknown_error)
            this.context?.let { context -> CustomAlertDialog(context) }?.apply {
                setMessage(errorMsg)
                setNegativeButtonText(null)
                setCanceledOnTouchOutside(false)
                setCancelable(false)
            }?.show()
            customSecurityDialog?.showErrorStatus(true)
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
                if(!b){
                    val errorMsg = getString(R.string.dialog_security_need_phone)
                    this.context?.let { context -> CustomAlertDialog(context) }?.apply {
                        setMessage(errorMsg)
                        setNegativeButtonText(null)
                        setCanceledOnTouchOutside(false)
                        setCancelable(false)
                    }?.show()
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
            it.getContentIfNotHandled()?.let {  list->
                if(!layoutSearchResult.isVisible){
                    layoutSearchResult.visibility = View.VISIBLE
                    layoutSearch.visibility = View.GONE
                }
                if(list.isNotEmpty()){
                    rvSearchResult.visibility = View.VISIBLE
                    layoutNoData.visibility = View.GONE
                    searchResult.clear()
                    searchResult.addAll(list)
                    searchResultAdapter.notifyDataSetChanged()
                }else{
                    rvSearchResult.visibility = View.GONE
                    layoutNoData.visibility = View.VISIBLE
                }
            }

        }

    }

    var searchResult:MutableList<SearchResult> = ArrayList()
    lateinit var searchHistoryAdapter: CommonAdapter<String>

    private fun initSearch(){
        layoutSearch.visibility = View.VISIBLE
        MultiLanguagesApplication.searchHistory?.let {
            searchHistoryList = it
        }
        searchHistoryList?.let {
            if(it.size!! > 0){
                rvHistory.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                rvHistory.isNestedScrollingEnabled = false
                layoutHistory.visibility = View.VISIBLE
                searchHistoryAdapter = object : CommonAdapter<String>( context, R.layout.item_search_history, it ) {
                    override fun convert(holder: ViewHolder, t: String, position: Int ) {
                        //holder.setText(R.id.tvHistory, t)
                        var tvHistory = holder.getView<TextView>(R.id.tvHistory)
                        tvHistory.text = t
                        tvHistory.setOnClickListener {
                            etSearch.setText(t)
                        }
                    }
                }
                rvHistory.adapter = searchHistoryAdapter
            }
        }
    }

    private fun startSearch(){
        if (searchHistoryList!!.size == 10) {
            searchHistoryList!!.removeAt(9)
        }
        searchHistoryList!!.add(0,etSearch.text.toString())
        MultiLanguagesApplication.saveSearchHistory(searchHistoryList)
        viewModel.getSportSearch(etSearch.text.toString())
        //rvHostory.adapter?.notifyDataSetChanged()
    }

    private fun initRecyclerView() {
        rv_menu.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = newAdapter
        }
        rvSearchResult.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        rvSearchResult.isNestedScrollingEnabled = false
        searchResultAdapter = object : CommonAdapter<SearchResult>( context, R.layout.item_search_result_sport, searchResult ) {
            override fun convert(holder: ViewHolder, t: SearchResult, position: Int ) {
                holder.setText(R.id.tvResultTittle,t.sportTitle)
                var rvResultLeague = holder.getView<RecyclerView>(R.id.rvResultLeague)
                rvResultLeague.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                rvResultLeague.isNestedScrollingEnabled = false
                var adapter = object : CommonAdapter<SearchResult.SearchResultLeague>( context, R.layout.item_search_result_league, t.searchResultLeague ) {
                    override fun convert(holder: ViewHolder, it: SearchResult.SearchResultLeague, position: Int ) {
                        var tvLeagueTittle = holder.getView<HighlightTextView>(R.id.tvLeagueTittle)
                        tvLeagueTittle.setCustomText(it.league)
                        tvLeagueTittle.highlight(etSearch.text.toString())
                        var rvResultMatch = holder.getView<RecyclerView>(R.id.rvResultMatch)
                        rvResultMatch.layoutManager =
                            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                        rvResultMatch.isNestedScrollingEnabled = false
                        var adapter = object : CommonAdapter<SearchResponse.Row.LeagueMatch.MatchInfo>( context, R.layout.item_search_result_match, t.searchResultLeague[position].leagueMatchList ) {
                            override fun convert(holder: ViewHolder, itt: SearchResponse.Row.LeagueMatch.MatchInfo, position: Int ) {
                                holder.setText(R.id.tvTime,
                                    TimeUtil.timeFormat(itt.startTime.toLong(), TimeUtil.MD_HM_FORMAT)+" ｜ ")
                                var tvMatch = holder.getView<HighlightTextView>(R.id.tvMatch)
                                tvMatch.setCustomText(itt.homeName+" v " + itt.awayName)
                                tvMatch.highlight(etSearch.text.toString())
                                tvMatch.setOnClickListener { v ->
                                    dismiss()
                                    viewModel.navSpecialEntrance(MatchType.DETAIL,GameType.getGameType(t.gameType)!!,itt.matchId)
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

        when {
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