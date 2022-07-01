package org.cxct.sportlottery.ui.game.publicity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentPublicityBinding
import org.cxct.sportlottery.network.bet.FastBetDataBean
import org.cxct.sportlottery.network.common.FavoriteType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.ServiceConnectStatus
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.network.withdraw.uwcheck.ValidateTwoFactorRequest
import org.cxct.sportlottery.repository.FLAG_OPEN
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment
import org.cxct.sportlottery.ui.base.ChannelType
import org.cxct.sportlottery.ui.common.CustomSecurityDialog
import org.cxct.sportlottery.ui.common.SocketLinearManager
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.login.signUp.RegisterActivity
import org.cxct.sportlottery.ui.main.entity.EnterThirdGameResult
import org.cxct.sportlottery.ui.main.entity.GameItemData
import org.cxct.sportlottery.ui.main.entity.ThirdGameCategory
import org.cxct.sportlottery.ui.money.recharge.MoneyRechargeActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileActivity
import org.cxct.sportlottery.ui.statistics.StatisticsDialog
import org.cxct.sportlottery.ui.withdraw.BankActivity
import org.cxct.sportlottery.ui.withdraw.WithdrawActivity
import org.cxct.sportlottery.util.JumpUtil
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.addScrollListenerForBottomNavBar
import org.cxct.sportlottery.util.isCreditSystem
import timber.log.Timber

/**
 * @app_destination 宣傳頁
 */
class PublicityFragment : BaseBottomNavigationFragment<GameViewModel>(GameViewModel::class), View.OnClickListener {
    // This property is only valid between onCreateView and onDestroyView.
    private var _binding: FragmentPublicityBinding? = null

    private val binding get() = _binding!!
    private var customSecurityDialog: CustomSecurityDialog? = null

    private var isNewestDataFromApi = false
    private var mRecommendList: List<Recommend> = listOf()
    private val mPublicityAdapter =
        GamePublicityAdapter(
            GamePublicityAdapter.PublicityAdapterListener(
                onLogoClickListener = {
                    removeBetListFragment()
//                    if (sConfigData?.thirdOpen == FLAG_OPEN) {
//                        MainActivity.reStart(activity ?: requireActivity())
//                    }
                },
                onLanguageBlockClickListener = {
                    goSwitchLanguagePage()
                },
                onNoticeClickListener = {
                    clickNotice()
                },
                onMenuClickListener = {
                    clickMenu()
                    clickMenu()
                },
                onItemClickListener = {
                    goLoginPage()
                },
                onGoHomePageListener = {
                    goGamePage()
                },
                onGoNewsPageListener = {
                    clickNews()
                },
                onGoLoginListener = {
                    goLoginPage()
                },
                onGoRegisterListener = {
                    goRegisterPage()
                },
                onGoDepositListener = {
                    avoidFastDoubleClick()
                    if (viewModel.isLogin.value != true) {
                        goLoginPage()
                    } else {
                        viewModel.checkRechargeSystem()
                    }
                },
                onGoWithdrawListener = {
                    avoidFastDoubleClick()
                    if (viewModel.isLogin.value != true) {
                        goLoginPage()
                    } else {
                        viewModel.checkWithdrawSystem()
                    }
                },
                onGoThirdGamesListener = {
                    avoidFastDoubleClick()
                    if (viewModel.isLogin.value != true) {
                        showLoginNotify()
                    } else {
                        viewModel.requestEnterThirdGame(it)
                    }
                },
                onClickBetListener = { gameType, matchType, matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap, playCateMenuCode ->
                    if(mIsEnabled){
                        avoidFastDoubleClick()
                        addOddsDialog(
                            gameType,
                            matchType,
                            matchInfo,
                            odd,
                            playCateCode,
                            playCateName,
                            betPlayCateNameMap,
                            playCateMenuCode
                        )
                    }
                },
                onClickFavoriteListener = {
                    viewModel.pinFavorite(FavoriteType.MATCH, it)
                },
                onClickStatisticsListener = { matchId ->
                    showStatistics(matchId)
                }, onClickPlayTypeListener = { gameType, matchType, matchId, matchInfoList ->
                    navOddsDetailFragment(gameType, matchType, matchId, matchInfoList)
                }, onClickLiveIconListener = { gameType, matchType, matchId, matchInfoList ->
                    if (viewModel.checkLoginStatus()) {
                        navOddsDetailFragment(gameType, matchType, matchId, matchInfoList)
                    }
                },
                onClickAnimationIconListener = { gameType, matchType, matchId, matchInfoList ->
                    if (viewModel.checkLoginStatus()) {
                        navOddsDetailFragment(gameType, matchType, matchId, matchInfoList)
                    }
                })
        )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPublicityBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getConfigData()
        initOnClickListener()
        initRecommendView()
        initTitle()
        initObservers()
        initSocketObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        clearObservers()
    }

    private fun clearObservers() {
        viewModel.userInfo.removeObservers(viewLifecycleOwner)
        viewModel.oddsType.removeObservers(viewLifecycleOwner)
        viewModel.publicityRecommend.removeObservers(viewLifecycleOwner)
        viewModel.betInfoList.removeObservers(viewLifecycleOwner)

        receiver.serviceConnectStatus.removeObservers(viewLifecycleOwner)
        receiver.matchStatusChange.removeObservers(viewLifecycleOwner)
        receiver.matchClock.removeObservers(viewLifecycleOwner)
        receiver.oddsChange.removeObservers(viewLifecycleOwner)
        receiver.matchOddsLock.removeObservers(viewLifecycleOwner)
        receiver.leagueChange.removeObservers(viewLifecycleOwner)
        receiver.globalStop.removeObservers(viewLifecycleOwner)
        receiver.producerUp.removeObservers(viewLifecycleOwner)
    }

    private fun getConfigData() {
        viewModel.getConfigData()
    }

    private fun initOnClickListener() {
        binding.rvPublicity.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        avoidFastDoubleClick()
        with(binding) {
            when (v) {
                rvPublicity -> {
                    goLoginPage()
                }
            }
        }
    }

    private fun removeBetListFragment() {
        when (activity) {
            is GamePublicityActivity -> (activity as GamePublicityActivity).removeBetListFragment()
        }
    }

    private fun clickNotice() {
        when (activity) {
            is GamePublicityActivity -> (activity as GamePublicityActivity).fragmentClickNotice()
        }
    }

    private fun clickNews() {
        when (activity) {
            is GamePublicityActivity -> (activity as GamePublicityActivity).fragmentClickNews()
        }
    }

    private fun goSwitchLanguagePage() {
        when (activity) {
            is GamePublicityActivity -> (activity as GamePublicityActivity).goSwitchLanguagePage()
        }
    }

    private fun showLoginNotify() {
        when (activity) {
            is GamePublicityActivity -> (activity as GamePublicityActivity).showLoginNotify()
        }
    }

    private fun initRecommendView() {
        with(binding.rvPublicity) {
            layoutManager = SocketLinearManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = mPublicityAdapter
            addScrollListenerForBottomNavBar(
                onScrollDown = {
                    MultiLanguagesApplication.mInstance.setIsScrollDown(it)
                }
            )
        }
    }

    private fun initTitle() {
        with(mPublicityAdapter) {
            addTitle()
            addAnnouncement()
            if (!isCreditSystem()) addUserInfo() //非信用盤才顯示
            addSubTitle() //熱門推薦bar需常駐
            addPreload()
        }
    }

    private fun initObservers() {
        viewModel.isLogin.observe(viewLifecycleOwner, { isLogin ->
            mPublicityAdapter.isLogin = isLogin
        })

        viewModel.infoCenterRepository.unreadNoticeList.observe(viewLifecycleOwner, {
            mPublicityAdapter.hasNotice = it.isNotEmpty()
        })

        viewModel.userInfo.observe(viewLifecycleOwner) { userInfo ->
            val newDiscount = userInfo?.discount ?: 1.0F
            viewModel.publicityUpdateDiscount(mPublicityAdapter.discount, newDiscount)
            mPublicityAdapter.discount = newDiscount
            viewModel.getMoney()
        }

        viewModel.userMoney.observe(viewLifecycleOwner) {
            if (!isCreditSystem()) { //非信用盤才顯示
                mPublicityAdapter.updateUserInfoData(
                    viewModel.userInfo.value?.nickName.orEmpty(),
                    viewModel.userMoney.value ?: 0.0
                )
            }
        }

        viewModel.oddsType.observe(viewLifecycleOwner, {
            it?.let { oddsType ->
                mPublicityAdapter.oddsType = oddsType
            }
        })

        viewModel.publicityRecommend.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { recommendList ->
                hideLoading()
                if (recommendList.isEmpty()) {
                    mPublicityAdapter.removeData(recommendList)
                    mPublicityAdapter.removeData(GamePublicityAdapter.PreloadItem())
                    return@observe
                }
                isNewestDataFromApi = true
                mRecommendList = recommendList
                mPublicityAdapter.removeData(GamePublicityAdapter.PreloadItem())
                if (mPublicityAdapter.getRecommendData().size == 0) {
                    mPublicityAdapter.addRecommend(recommendList)
                    Timber.e("addRecommend")
                } else {
                    recommendList.forEachIndexed { index, recommend ->
                        recommend.runningTime = mPublicityAdapter.getRecommendData()[0].runningTime
                        mPublicityAdapter.updateRecommendData(index, recommend)
                    }
                }
                //先解除全部賽事訂閱
                unSubscribeChannelHallAll()
                subscribeQueryData(recommendList)
            }
        }

        viewModel.betInfoList.observe(viewLifecycleOwner) { event ->
            event?.peekContent()?.let { betList ->
                val targetList = getNewestRecommendData()
                targetList.forEachIndexed { index, recommend ->
                    var needUpdate = false
                    recommend.oddsMap?.values?.forEach { oddList ->
                        oddList?.forEach { odd ->
                            val newSelectStatus = betList.any { betInfoListData ->
                                betInfoListData.matchOdd.oddsId == odd?.id
                            }
                            if (odd?.isSelected != newSelectStatus) {
                                odd?.isSelected = newSelectStatus
                                needUpdate = true
                            }
                        }
                    }
                    if (needUpdate) {
                        mPublicityAdapter.updateRecommendData(index, recommend)
                    }
                }
            }
        }

        viewModel.favorMatchList.observe(viewLifecycleOwner) { favorMatchList ->
            val targetList = getNewestRecommendData()
            targetList.forEachIndexed { index, recommend ->
                var needUpdate = false
                val isFavorite = favorMatchList.contains(recommend.matchInfo?.id)
                if (recommend.matchInfo?.isFavorite != isFavorite) {
                    recommend.matchInfo?.isFavorite = isFavorite
                    needUpdate = true
                }
                if (needUpdate) {
                    mPublicityAdapter.updateRecommendData(index, recommend)
                }
            }
        }

        viewModel.gotConfig.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { isReload ->
                if (isReload) {
                    mPublicityAdapter.updateToolbarBannerImage()
                }
            }
        }

        viewModel.sportMenuFilterList.observe(viewLifecycleOwner){
            it.getContentIfNotHandled()?.let {
                queryData()
            }
        }

        viewModel.messageListResult.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { messageListResult ->
                val titleList: MutableList<String> = mutableListOf()
                messageListResult.rows?.forEach { data ->
                    if (data.type.toInt() == 1) titleList.add(data.title + " - " + data.message)
                }
                mPublicityAdapter.updateAnnouncementData(titleList)
            }
        }

        //第三方遊戲清單
        viewModel.gameCateDataList.observe(viewLifecycleOwner) {
            val cateData = it?.find { gameCateData ->
                gameCateData.categoryThird == ThirdGameCategory.QP //棋牌
            }
            val gameList = mutableListOf<GameItemData>()
            cateData?.tabDataList?.forEach { gameTabData ->
                gameTabData.gameList.run { gameList.addAll(this) }
            }
            val gameItemData = gameList.find { gameItemData ->
                gameItemData.thirdGameData != null
            }
            if (sConfigData?.thirdOpen == FLAG_OPEN) {
                mPublicityAdapter.updateEGamesData(gameItemData?.thirdGameData)
            }
        }

        viewModel.enterThirdGameResult.observe(viewLifecycleOwner) {
            if (isVisible)
                enterThirdGame(it)
        }

        //充值提現頁面相關
        viewModel.withdrawSystemOperation.observe(viewLifecycleOwner) {
            val operation = it.getContentIfNotHandled()
            if (operation == false) {
                showPromptDialog(
                    getString(R.string.prompt),
                    getString(R.string.message_withdraw_maintain)
                ) {}
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

        viewModel.needToUpdateWithdrawPassword.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    showPromptDialog(
                        getString(R.string.withdraw_setting),
                        getString(R.string.please_setting_withdraw_password),
                        getString(R.string.go_to_setting),
                        true
                    ) {
                        startActivity(Intent(context, SettingPasswordActivity::class.java).apply {
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
                        startActivity(Intent(context, SettingPasswordActivity::class.java).apply {
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

        viewModel.needToSendTwoFactor.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { b ->
                if (b) {
                    customSecurityDialog = CustomSecurityDialog(requireContext()).apply {
                        getSecurityCodeClickListener {
                            this.showSmeTimer300()
                            viewModel.sendTwoFactor()
                        }
                        positiveClickListener = CustomSecurityDialog.PositiveClickListener { number ->
                            viewModel.validateTwoFactor(ValidateTwoFactorRequest(number))
                        }
                    }
                    customSecurityDialog?.show(childFragmentManager, null)

                }
            }
        }

        //確認收到簡訊驗證碼
        viewModel.twoFactorResult.observe(viewLifecycleOwner) {
            //傳送驗證碼成功後才能解鎖提交按鈕
            customSecurityDialog?.setPositiveBtnClickable(it?.success ?: false)
            sConfigData?.hasGetTwoFactorResult = true
        }

        //簡訊驗證成功
        viewModel.twoFactorSuccess.observe(viewLifecycleOwner) {
            if (it == true)
                customSecurityDialog?.dismiss()
        }

        viewModel.intoWithdraw.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                startActivity(Intent(context, WithdrawActivity::class.java))
            }
        }
    }

    // TODO subscribe leagueChange: 此處尚無需實作邏輯, 看之後有沒有相關需求
    private fun initSocketObservers() {
        receiver.serviceConnectStatus.observe(viewLifecycleOwner) {
            it?.let {
                if (it == ServiceConnectStatus.CONNECTED) {
//                    loading()
                    viewModel.getSportMenuFilter()
                    subscribeSportChannelHall()
                }
            }
        }

        receiver.matchStatusChange.observe(viewLifecycleOwner, { event ->
            event?.let { matchStatusChangeEvent ->
                val targetList = getNewestRecommendData()

                targetList.forEachIndexed { index, recommend ->
                    val matchList = listOf(recommend).toMutableList()
                    if (SocketUpdateUtil.updateMatchStatus(
                            recommend.gameType,
                            matchList as MutableList<org.cxct.sportlottery.network.common.MatchOdd>,
                            matchStatusChangeEvent,
                            context
                        )
                    ) {
                        updateRecommendList(index, recommend)
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }
            }
        })

        receiver.matchClock.observe(viewLifecycleOwner, {
            it?.let { matchClockEvent ->
                val targetList = getNewestRecommendData()

                targetList.forEachIndexed { index, recommend ->
                    if (
                        SocketUpdateUtil.updateMatchClock(
                            recommend,
                            matchClockEvent
                        )
                    ) {
                        updateRecommendList(index, recommend)
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }
            }
        })

        receiver.oddsChange.observe(viewLifecycleOwner, { event ->
            event?.getContentIfNotHandled()?.let { oddsChangeEvent ->
                val targetList = getNewestRecommendData()
                targetList.forEachIndexed { index, recommend ->
                    if (recommend.id == oddsChangeEvent.eventId) {
                        recommend.sortOddsMap()

                        //region 翻譯更新
                        oddsChangeEvent.playCateNameMap?.let { playCateNameMap ->
                            recommend.playCateNameMap?.putAll(playCateNameMap)
                        }
                        oddsChangeEvent.betPlayCateNameMap?.let { betPlayCateNameMap ->
                            recommend.betPlayCateNameMap?.putAll(betPlayCateNameMap)
                        }
                        //endregion

                        if (SocketUpdateUtil.updateMatchOdds(context, recommend, oddsChangeEvent)) {
                            updateBetInfo(recommend, oddsChangeEvent)
                            updateRecommendList(index, recommend)
                            Timber.e("updateRecommendList")
                        }

                        if (isNewestDataFromApi)
                            isNewestDataFromApi = false
                    }
                }
            }
        })

        receiver.matchOddsLock.observe(viewLifecycleOwner, {
            it?.let { matchOddsLockEvent ->
                val targetList = getNewestRecommendData()

                targetList.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(recommend, matchOddsLockEvent)
                    ) {
                        updateRecommendList(index, recommend)
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }
            }
        })

        receiver.leagueChange.observe(viewLifecycleOwner, {
            it?.let { leagueChangeEvent ->
                viewModel.publicityLeagueChange(leagueChangeEvent)
            }
        })

        receiver.globalStop.observe(viewLifecycleOwner, {
            it?.let { globalStopEvent ->
                val targetList = getNewestRecommendData()

                targetList.forEachIndexed { index, recommend ->
                    if (SocketUpdateUtil.updateOddStatus(
                            recommend,
                            globalStopEvent
                        )
                    ) {
                        updateRecommendList(index, recommend)
                        //TODO 更新邏輯待補，跟進GameV3Fragment
                    }
                }
            }
        })

        receiver.producerUp.observe(viewLifecycleOwner, {
            it?.let {
                //先解除全部賽事訂閱
                unSubscribeChannelHallAll()
                subscribeQueryData(mPublicityAdapter.getRecommendData())
            }
        })
    }

    /**
     * 若投注單處於未開啟狀態且有加入注單的賠率項資訊有變動時, 更新投注單內資訊
     */
    private fun updateBetInfo(recommend: Recommend, oddsChangeEvent: OddsChangeEvent) {
        if (!getBetListPageVisible()) {
            //尋找是否有加入注單的賠率項
            if (recommend.matchInfo?.id == oddsChangeEvent.eventId && recommend.oddsMap?.values?.any { oddList ->
                    oddList?.any { odd ->
                        odd?.isSelected == true
                    } == true
                } == true
            ) {
                viewModel.updateMatchOdd(oddsChangeEvent)
            }
        }
    }

    private fun Recommend.sortOddsMap() {
        this.oddsMap?.forEach { (_, value) ->
            if (value?.size ?: 0 > 3 && value?.first()?.marketSort != 0 && (value?.first()?.odds != value?.first()?.malayOdds)) {
                value?.sortBy {
                    it?.marketSort
                }
            }
        }
    }

    /**
     * 賠率排序
     */
    private fun OddsChangeEvent.sortOddsMap() {
        this.odds.forEach { (_, value) ->
            if (value?.size ?: 0 > 3 && value?.first()?.marketSort != 0 && (value?.first()?.odds != value?.first()?.malayOdds)) {
                value?.sortBy {
                    it?.marketSort
                }
            }
        }
    }

    private fun queryData() {
        viewModel.getAnnouncement()
        viewModel.getRecommend()
        viewModel.getThirdGame()
    }

    private fun goLoginPage() {
        startActivity(Intent(context, LoginActivity::class.java))
    }

    private fun goRegisterPage() {
        startActivity(Intent(context, RegisterActivity::class.java))
    }

    private fun goDepositPage() {
        startActivity(Intent(context, RegisterActivity::class.java))
    }

    private fun goWithdrawPage() {
        startActivity(Intent(context, RegisterActivity::class.java))
    }

    private fun goGamePage() {
        GameActivity.reStart(activity?:requireActivity())
        activity?.finish()
    }

    private fun addOddsDialog(
        gameTypeCode: String,
        matchType: MatchType,
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        playCateMenuCode: String?
    ) {
        val gameType = GameType.getGameType(gameTypeCode)
        gameType?.let {
            matchInfo?.let { matchInfo ->
                val fastBetDataBean = FastBetDataBean(
                    matchType = matchType,
                    gameType = gameType,
                    playCateCode = playCateCode,
                    playCateName = playCateName,
                    matchInfo = matchInfo,
                    matchOdd = null,
                    odd = odd,
                    subscribeChannelType = ChannelType.HALL,
                    betPlayCateNameMap = betPlayCateNameMap,
                    playCateMenuCode
                )
                when (val fragmentActivity = activity) {
                    is GamePublicityActivity -> fragmentActivity.showFastBetFragment(fastBetDataBean)
                }
            }
        }
    }

    private fun showStatistics(matchId: String?) {
        StatisticsDialog.newInstance(matchId, StatisticsDialog.StatisticsClickListener { clickMenu() })
            .show(childFragmentManager, StatisticsDialog::class.java.simpleName)
    }

    private fun navOddsDetailFragment(
        gameTypeCode: String,
        matchType: MatchType?,
        matchId: String?,
        matchInfoList: List<MatchInfo>
    ) {
        val gameType = GameType.getGameType(gameTypeCode)
        val navMatchType = matchType ?: MatchType.DETAIL
        if (gameType != null && matchId != null) {
            findNavController().navigate(
                when (navMatchType == MatchType.IN_PLAY) {
                    true -> {
                        PublicityFragmentDirections.actionPublicityFragmentToOddsDetailLiveFragment(
                            navMatchType,
                            gameType,
                            matchId
                        )
                    }
                    else -> {
                        PublicityFragmentDirections.actionPublicityFragmentToOddsDetailFragment(
                            navMatchType,
                            gameType,
                            matchId,
                            matchInfoList.toTypedArray()
                        )
                    }
                }

            )
        }
    }

    /**
     * 根據menuList的PlayCate排序賠率玩法
     */
    //TODO 20220323 等新版socket更新方式調整完畢後再確認一次此處是否需要移動至別處進行
    private fun Recommend.sortOddsByMenu() {
        val sortOrder = this.menuList.firstOrNull()?.playCateList?.map { it.code }

        oddsMap?.let { map ->
            val filterPlayCateMap = map.filter { sortOrder?.contains(it.key) == true }
            val sortedMap = filterPlayCateMap.toSortedMap(compareBy<String> {
                sortOrder?.indexOf(it)
            }.thenBy { it })

            map.clear()
            map.putAll(sortedMap)
        }
    }

    private fun getNewestRecommendData(): List<Recommend> =
        if (isNewestDataFromApi) mRecommendList else mPublicityAdapter.getRecommendData()


    private fun updateRecommendList(index: Int, recommend: Recommend) {
        with(binding) {
            if (rvPublicity.scrollState == RecyclerView.SCROLL_STATE_IDLE && !rvPublicity.isComputingLayout) {
                mPublicityAdapter.updateRecommendData(index, recommend)
            }
        }
    }

    private fun subscribeQueryData(recommendList: List<Recommend>) {
        recommendList.forEach { subscribeChannelHall(it) }
    }

    private fun subscribeChannelHall(recommend: Recommend) {
        subscribeChannelHall(recommend.gameType, recommend.id)
    }

    private fun enterThirdGame(result: EnterThirdGameResult) {
        hideLoading()
        when (result.resultType) {
            EnterThirdGameResult.ResultType.SUCCESS -> context?.run {
                JumpUtil.toThirdGameWeb(
                    this,
                    result.url ?: "",
                    thirdGameCategoryCode = result.thirdGameCategoryCode
                )
            }
            EnterThirdGameResult.ResultType.FAIL -> showErrorPromptDialog(getString(R.string.error), result.errorMsg ?: "") {}
            EnterThirdGameResult.ResultType.NEED_REGISTER -> context?.startActivity(Intent(context, RegisterActivity::class.java))
            EnterThirdGameResult.ResultType.GUEST -> showErrorPromptDialog(getString(R.string.error), result.errorMsg ?: "") {}
            EnterThirdGameResult.ResultType.NONE -> {
            }
        }
        if (result.resultType != EnterThirdGameResult.ResultType.NONE)
            viewModel.clearThirdGame()
    }
}