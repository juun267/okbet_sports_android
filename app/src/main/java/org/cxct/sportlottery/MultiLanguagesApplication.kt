package org.cxct.sportlottery

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import cn.jpush.android.api.JPushInterface
import com.github.jokar.multilanguages.library.MultiLanguage
import org.cxct.sportlottery.db.SportRoomDatabase
import org.cxct.sportlottery.network.manager.NetworkStatusManager
import org.cxct.sportlottery.network.manager.RequestManager
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.bet.record.BetRecordViewModel
import org.cxct.sportlottery.ui.feedback.FeedbackViewModel
import org.cxct.sportlottery.ui.finance.FinanceViewModel
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.helpCenter.HelpCenterViewModel
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.ui.infoCenter.InfoCenterViewModel
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.ui.login.signUp.RegisterViewModel
import org.cxct.sportlottery.ui.maintenance.MaintenanceViewModel
import org.cxct.sportlottery.ui.money.recharge.MoneyRechViewModel
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordViewModel
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.OtherBetRecordViewModel
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyProfileInfoViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileModel
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateViewModel
import org.cxct.sportlottery.ui.results.SettlementViewModel
import org.cxct.sportlottery.ui.splash.HostRepository
import org.cxct.sportlottery.ui.splash.SplashViewModel
import org.cxct.sportlottery.ui.withdraw.WithdrawViewModel
import org.cxct.sportlottery.util.LanguageManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber
import timber.log.Timber.DebugTree


/**
 * App 內部切換語系
 */
class MultiLanguagesApplication : Application() {
    companion object {
        lateinit var appContext: Context
    }

    private val viewModelModule = module {
        viewModel { SplashViewModel(get(), get()) }
        viewModel { MoneyRechViewModel(get(), get(), get(), get()) }
        viewModel { MainViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { LoginViewModel(get(), get()) }
        viewModel { RegisterViewModel(get(), get()) }
        viewModel { SettlementViewModel(get(), get(), get(), get(), get()) }
        viewModel { BetRecordViewModel(get(), get(), get()) }
        viewModel { InfoCenterViewModel(get(), get(), get(), get()) }
        viewModel { HelpCenterViewModel(get(), get()) }
        viewModel { WithdrawViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { ProfileModel(get(), get(), get(), get(), get()) }
        viewModel { ModifyProfileInfoViewModel(get(), get(), get(), get(), get()) }
        viewModel { SettingPasswordViewModel(get(), get(), get(), get(), get()) }
        viewModel { FinanceViewModel(get(), get(), get()) }
        viewModel { ProfileCenterViewModel(get(), get(), get(), get()) }
        viewModel { FeedbackViewModel(get(), get(), get(), get(),get()) }
        viewModel { VersionUpdateViewModel(get()) }
        viewModel { MoneyTransferViewModel(get(), get(), get(), get(), get()) }
        viewModel { GameViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { MaintenanceViewModel(get(), get()) }
        viewModel { OtherBetRecordViewModel(get(), get(), get(), get()) }
    }

    private val repoModule = module {
        single { UserInfoRepository(get()) }
        single { LoginRepository(get(), get()) }
        single { SportMenuRepository() }
        single { SettlementRepository() }
        single { InfoCenterRepository() }
        single { MoneyRepository(get()) }
        single { BetInfoRepository() }
        single { FeedbackRepository() }
        single { HostRepository(get()) }
    }

    private val dbModule = module {
        single { SportRoomDatabase.getDatabase(get()) }
        single { get<SportRoomDatabase>().userInfoDao() }
    }

    override fun attachBaseContext(base: Context) {
        //第一次进入app时保存系统选择语言(为了选择随系统语言时使用，如果不保存，切换语言后就拿不到了）
        LanguageManager.saveSystemCurrentLanguage(base)
        super.attachBaseContext(MultiLanguage.setLocal(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //用户在系统设置页面切换语言时保存系统选择语言(为了选择随系统语言时使用，如果不保存，切换语言后就拿不到了）
        LanguageManager.saveSystemCurrentLanguage(applicationContext, newConfig)
        MultiLanguage.onConfigurationChanged(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        MultiLanguage.init { context ->
            //返回自己本地保存选择的语言设置
            return@init LanguageManager.getSetLanguageLocale(context)
        }
        MultiLanguage.setApplicationLanguage(this)

        startKoin {
            androidContext(this@MultiLanguagesApplication)
            modules(
                listOf(
                    viewModelModule,
                    repoModule,
                    dbModule
                )
            )
        }
        RequestManager.init(this)
        NetworkStatusManager.init(this)

        setupTimber()

        initJPush()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    //極光推播
    private fun initJPush() {
        JPushInterface.setDebugMode(true) //参数为 true 表示打开调试模式，可看到 sdk 的日志。
        JPushInterface.init(this)
    }
}