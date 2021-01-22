package org.cxct.sportlottery

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.github.jokar.multilanguages.library.MultiLanguage
import org.cxct.sportlottery.db.SportRoomDatabase
import org.cxct.sportlottery.network.manager.NetworkStatusManager
import org.cxct.sportlottery.network.manager.RequestManager
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.bet.record.BetRecordViewModel
import org.cxct.sportlottery.ui.home.MainViewModel
import org.cxct.sportlottery.ui.infoCenter.InfoCenterViewModel
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.ui.login.signUp.RegisterViewModel
import org.cxct.sportlottery.ui.results.SettlementViewModel
import org.cxct.sportlottery.ui.money.recharge.MoneyRechViewModel
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordViewModel
import org.cxct.sportlottery.ui.profileCenter.nickname.NicknameModel
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileModel
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
        viewModel { SplashViewModel(get()) }
        viewModel { MoneyRechViewModel(get(), get()) }
        viewModel { MainViewModel(get(), get(), get(), get()) }
        viewModel { LoginViewModel(get(), get()) }
        viewModel { RegisterViewModel(get(), get()) }
        viewModel { SettlementViewModel(get(), get()) }
        viewModel { BetRecordViewModel(get()) }
        viewModel { InfoCenterViewModel(get(), get()) }
        viewModel { WithdrawViewModel(get(), get()) }
        viewModel { ProfileModel(get()) }
        viewModel { NicknameModel(get()) }
        viewModel { SettingPasswordViewModel(get()) }
    }

    private val repoModule = module {
        single { LoginRepository(get(), get()) }
        single { SportMenuRepository() }
        single { SettlementRepository() }
        single { InfoCenterRepository() }
        single { MoneyRepository() }
        single { BetInfoRepository() }
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

    override fun onConfigurationChanged(newConfig: Configuration?) {
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
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }
}