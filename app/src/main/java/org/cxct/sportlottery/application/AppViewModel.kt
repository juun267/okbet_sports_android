package org.cxct.sportlottery.application

import android.app.Application
import org.cxct.sportlottery.network.money.RedEnveLopeModel
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.betList.BetListViewModel
import org.cxct.sportlottery.ui.betRecord.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.chat.ChatViewModel
import org.cxct.sportlottery.ui.feedback.FeedbackViewModel
import org.cxct.sportlottery.ui.finance.FinanceViewModel
import org.cxct.sportlottery.ui.helpCenter.HelpCenterViewModel
import org.cxct.sportlottery.ui.infoCenter.InfoCenterViewModel
import org.cxct.sportlottery.ui.login.foget.ForgetViewModel
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.ui.login.signUp.info.RegisterInfoViewModel
import org.cxct.sportlottery.ui.maintab.MainTabViewModel
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.ui.maintab.games.OKLiveViewModel
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.menu.viewmodel.SportLeftMenuViewModel
import org.cxct.sportlottery.ui.maintenance.MaintenanceViewModel
import org.cxct.sportlottery.ui.money.recharge.MoneyRechViewModel
import org.cxct.sportlottery.ui.money.withdraw.WithdrawViewModel
import org.cxct.sportlottery.ui.news.NewsViewModel
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.authbind.AuthViewModel
import org.cxct.sportlottery.ui.profileCenter.cancelaccount.CancelAccountViewModel
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordViewModel
import org.cxct.sportlottery.ui.profileCenter.modify.BindInfoViewModel
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyProfileInfoViewModel
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.OtherBetRecordViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileModel
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateViewModel
import org.cxct.sportlottery.ui.profileCenter.vip.VipViewModel
import org.cxct.sportlottery.ui.redeem.RedeemViewModel
import org.cxct.sportlottery.ui.results.SettlementViewModel
import org.cxct.sportlottery.ui.selflimit.SelfLimitViewModel
import org.cxct.sportlottery.ui.splash.SplashViewModel
import org.cxct.sportlottery.ui.sport.SportTabViewModel
import org.cxct.sportlottery.ui.sport.SportViewModel
import org.cxct.sportlottery.ui.sport.endcard.EndCardVM
import org.cxct.sportlottery.ui.sport.filter.LeagueSelectViewModel
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object AppViewModel {

    private val viewModelModule = module {
        viewModel { SplashViewModel(get()) }
        viewModel { MoneyRechViewModel(get()) }
        viewModel { MainViewModel(get()) }
        viewModel { LoginViewModel(get()) }
        viewModel { SettlementViewModel(get()) }
        viewModel { InfoCenterViewModel(get()) }
        viewModel { HelpCenterViewModel(get()) }
        viewModel { WithdrawViewModel(get()) }
        viewModel { ProfileModel(get()) }
        viewModel { ModifyProfileInfoViewModel(get()) }
        viewModel { SettingPasswordViewModel(get()) }
        viewModel { FeedbackViewModel(get()) }
        viewModel { SelfLimitViewModel(get()) }
        viewModel { FinanceViewModel(get()) }
        viewModel { ProfileCenterViewModel(get()) }
        viewModel { VersionUpdateViewModel(get()) }
        viewModel { MoneyTransferViewModel(get()) }
        viewModel { MaintenanceViewModel(get()) }
        viewModel { OtherBetRecordViewModel(get()) }
        viewModel { AccountHistoryViewModel(get()) }
        viewModel { NewsViewModel(get()) }
        viewModel { RedEnveLopeModel(get()) }
        viewModel { MainTabViewModel(get()) }
        viewModel { SportViewModel(get()) }
        viewModel { LeagueSelectViewModel(get()) }
        viewModel { SportListViewModel(get()) }
        viewModel { SportTabViewModel(get()) }
        viewModel { CancelAccountViewModel(get()) }
        viewModel { MainHomeViewModel(get()) }
        viewModel { ForgetViewModel(get()) }
        viewModel { BetListViewModel(get()) }
        viewModel { AuthViewModel(get()) }
        viewModel { BindInfoViewModel(get()) }
        viewModel { RegisterInfoViewModel(get()) }
        viewModel { OKGamesViewModel(get()) }
        viewModel { OKLiveViewModel(get()) }
        viewModel { ChatViewModel(get()) }
        viewModel { SportLeftMenuViewModel(get()) }
        viewModel { RedeemViewModel(get()) }
        viewModel { EndCardVM(get()) }
        viewModel { VipViewModel(get()) }
    }

    private val repoModule = module {
        single { UserInfoRepository }
        single { LoginRepository }
        single { SettlementRepository }
        single { InfoCenterRepository }
        single { MoneyRepository }
        single { BetInfoRepository }
        single { AvatarRepository }
        single { FeedbackRepository }
        single { HostRepository }
        single { FavoriteRepository }
        single { SelfLimitRepository }
        single { GamePlayNameRepository }
    }


    fun startKoin(application: Application) {
        org.koin.core.context.startKoin {
            androidContext(application)
            modules(listOf(viewModelModule, repoModule))
        }
    }

}