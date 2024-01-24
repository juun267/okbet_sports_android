package org.cxct.sportlottery.application

import android.app.Application
import org.cxct.sportlottery.network.money.RedEnveLopeModel
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.betList.BetListViewModel
import org.cxct.sportlottery.ui.betRecord.TransactionStatusViewModel
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
import org.cxct.sportlottery.ui.redeem.RedeemViewModel
import org.cxct.sportlottery.ui.results.SettlementViewModel
import org.cxct.sportlottery.ui.selflimit.SelfLimitViewModel
import org.cxct.sportlottery.ui.splash.SplashViewModel
import org.cxct.sportlottery.ui.sport.SportTabViewModel
import org.cxct.sportlottery.ui.sport.SportViewModel
import org.cxct.sportlottery.ui.sport.filter.LeagueSelectViewModel
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object AppViewModel {

    private val viewModelModule = module {
        viewModel { SplashViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { MoneyRechViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
        viewModel { MainViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { LoginViewModel(get(), get(), get(), get(), get()) }
        viewModel { SettlementViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { InfoCenterViewModel(get(), get(), get(), get(), get()) }
        viewModel { HelpCenterViewModel(get(), get(), get(), get()) }
        viewModel { WithdrawViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { ProfileModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { ModifyProfileInfoViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { SettingPasswordViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { FeedbackViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { SelfLimitViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { FinanceViewModel(get(), get(), get(), get(), get()) }
        viewModel { ProfileCenterViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { VersionUpdateViewModel(get(), get(), get(), get()) }
        viewModel { MoneyTransferViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { MaintenanceViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { OtherBetRecordViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { AccountHistoryViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { TransactionStatusViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { NewsViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { RedEnveLopeModel(get(), get(), get(), get(), get()) }
        viewModel { MainTabViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { SportViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { LeagueSelectViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { SportListViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { SportTabViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { CancelAccountViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { MainHomeViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { ForgetViewModel(get(), get(), get(), get()) }
        viewModel { BetListViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { AuthViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { BindInfoViewModel(get(), get(), get(), get()) }
        viewModel { RegisterInfoViewModel(get(), get(), get(), get()) }
        viewModel { OKGamesViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { OKLiveViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { ChatViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { SportLeftMenuViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { RedeemViewModel(get(), get(), get(), get(), get(), get()) }
    }

    private val repoModule = module {
        single { UserInfoRepository }
        single { LoginRepository }
        single { SportMenuRepository }
        single { SettlementRepository() }
        single { InfoCenterRepository }
        single { MoneyRepository }
        single { BetInfoRepository }
        single { AvatarRepository(get()) }
        single { FeedbackRepository() }
        single { HostRepository(get()) }
        single { WithdrawRepository }
        single { MyFavoriteRepository() }
        single { SelfLimitRepository() }
        single { GamePlayNameRepository }
    }


    fun startKoin(application: Application) {
        org.koin.core.context.startKoin {
            androidContext(application)
            modules(listOf(viewModelModule, repoModule))
        }
    }

}