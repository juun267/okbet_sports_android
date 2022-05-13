package org.cxct.sportlottery

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import cn.jpush.android.api.JPushInterface
import com.github.jokar.multilanguages.library.MultiLanguage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.dialog_change_appearance.view.*
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.manager.NetworkStatusManager
import org.cxct.sportlottery.network.manager.RequestManager
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.feedback.FeedbackViewModel
import org.cxct.sportlottery.ui.finance.FinanceViewModel
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.helpCenter.HelpCenterViewModel
import org.cxct.sportlottery.ui.infoCenter.InfoCenterViewModel
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.ui.login.signUp.RegisterViewModel
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.ui.maintenance.MaintenanceViewModel
import org.cxct.sportlottery.ui.money.recharge.MoneyRechViewModel
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordViewModel
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyProfileInfoViewModel
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.OtherBetRecordViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileModel
import org.cxct.sportlottery.ui.profileCenter.sportRecord.BetRecordViewModel
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateViewModel
import org.cxct.sportlottery.ui.results.SettlementViewModel
import org.cxct.sportlottery.repository.HostRepository
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.favorite.MyFavoriteViewModel
import org.cxct.sportlottery.ui.main.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.profileCenter.creditrecord.CreditRecordViewModel
import org.cxct.sportlottery.ui.selflimit.SelfLimitViewModel
import org.cxct.sportlottery.ui.splash.SplashViewModel
import org.cxct.sportlottery.ui.statistics.StatisticsViewModel
import org.cxct.sportlottery.ui.transactionStatus.TransactionStatusViewModel
import org.cxct.sportlottery.ui.vip.VipViewModel
import org.cxct.sportlottery.ui.withdraw.WithdrawViewModel
import org.cxct.sportlottery.util.AppManager
import org.cxct.sportlottery.util.LanguageManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber
import timber.log.Timber.DebugTree
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cxct.sportlottery.ui.game.quick.TestViewModel
import org.cxct.sportlottery.ui.news.NewsViewModel
import org.cxct.sportlottery.ui.permission.GooglePermissionViewModel

/**
 * App 內部切換語系
 */
class MultiLanguagesApplication : Application() {
    //private var userInfoData : UserInfo?= null
    private var _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo = _userInfo.asStateFlow()
    private var isNewsShowed = false
    private var isGameDetailAnimationNeedShow = false


    private val viewModelModule = module {
        viewModel { SplashViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { MoneyRechViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
        viewModel { MainViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
        viewModel { LoginViewModel(get(), get(), get(), get(), get()) }
        viewModel { RegisterViewModel(get(), get(), get(), get(), get()) }
        viewModel { SettlementViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { BetRecordViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { InfoCenterViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { HelpCenterViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { WithdrawViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { ProfileModel(get(), get(), get(), get(), get(), get(), get(), get()) }
        viewModel { ModifyProfileInfoViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { SettingPasswordViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { FeedbackViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { SelfLimitViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { FinanceViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { ProfileCenterViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
        viewModel { VersionUpdateViewModel(get(), get(), get(), get()) }
        viewModel { MoneyTransferViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { GameViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
        viewModel { MaintenanceViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { OtherBetRecordViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { VipViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { AccountHistoryViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { TransactionStatusViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { MyFavoriteViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { CreditRecordViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { StatisticsViewModel(get(), get(), get(), get()) }
        viewModel { GooglePermissionViewModel(get(), get(), get()) }
        viewModel { TestViewModel(get(), get(), get(), get()) }
        viewModel { NewsViewModel(get(), get(), get(), get(), get(), get()) }
    }

    private val repoModule = module {
        single { UserInfoRepository(get()) }
        single { LoginRepository(get()) }
        single { SportMenuRepository() }
        single { SettlementRepository() }
        single { InfoCenterRepository() }
        single { MoneyRepository() }
        single { BetInfoRepository(get()) }
        single { AvatarRepository(get()) }
        single { FeedbackRepository() }
        single { HostRepository(get()) }
        single { ThirdGameRepository() }
        single { WithdrawRepository(get()) }
        single { PlayQuotaComRepository() }
        single { MyFavoriteRepository() }
        single { SelfLimitRepository() }
        single { IntentRepository() }
    }


    private val serviceModule = module {
        factory { ServiceBroadcastReceiver(get()) }
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
        instance = this
        AppManager.init(this)
        myPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)

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
                    serviceModule
                )
            )
        }
        RequestManager.init(this)
        NetworkStatusManager.init(this)

        setupTimber()

        initJPush()

        setNightMode()

        //生成UUID作為設備識別碼
        setupDeviceCode()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    //極光推播
    private fun initJPush() {
        JPushInterface.setDebugMode(false) //参数为 true 表示打开调试模式，可看到 sdk 的日志。
        JPushInterface.init(this)

        //参数为 true 表示打开调试模式，可看到 sdk 的日志。
        //[Martin] 拔掉JAnalytics功能是因為上架被阻擋
//        JAnalyticsInterface.init(this);
//        JAnalyticsInterface.initCrashHandler(this);
//        JAnalyticsInterface.setDebugMode(false);
    }

    private fun setNightMode() {
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun setupDeviceCode() {
        val devicePreferences = getSharedPreferences(UUID_DEVICE_CODE, Context.MODE_PRIVATE)
        if (devicePreferences.getString(UUID, null).isNullOrEmpty())
            devicePreferences
                .edit()
                .putString(UUID, java.util.UUID.randomUUID().toString())
                .apply()
    }

    fun saveUserInfo(userInfoData: UserInfo?) {
        _userInfo.value = userInfoData
    }

    fun userInfo(): UserInfo? {
        return _userInfo.value
    }

    fun isNewsShow(): Boolean {
        return isNewsShowed
    }

    fun setIsNewsShow(show: Boolean) {
        this.isNewsShowed = show
    }

    fun getGameDetailAnimationNeedShow(): Boolean {
        return if (BuildConfig.CHANNEL_NAME == "spkx") {
            isGameDetailAnimationNeedShow
        } else {
            true
        }
    }

    companion object {
        private var myPref: SharedPreferences? = null
        lateinit var appContext: Context
        const val UUID_DEVICE_CODE = "uuidDeviceCode"
        const val UUID = "uuid"
        private var instance: MultiLanguagesApplication? = null

        fun saveSearchHistory(searchHistory: MutableList<String>?) {
            this.searchHistory = searchHistory
        }

        var searchHistory: MutableList<String>?
            get() {
                val searchHistoryJson = myPref?.getString("search_history", "")
                val gson = Gson()
                val type = object : TypeToken<MutableList<String>?>() {}.type
                var searchHistoryList: MutableList<String>? = gson.fromJson(searchHistoryJson, type)
                return searchHistoryList
            }
            set(searchHistoryList) {
                val gson = Gson()
                val searchHistoryJson = gson.toJson(searchHistoryList)
                val editor = myPref?.edit()
                editor?.putString("search_history", searchHistoryJson)
                editor?.apply()
            }

        fun saveNightMode(nightMode: Boolean) {
            isNightMode = nightMode
            colorModeChanging = true
        }

        var colorModeChanging: Boolean = false

        var isNightMode: Boolean
            get() = myPref?.getBoolean("is_night_mode", false) ?: false
            set(check) {
                val editor = myPref?.edit()
                editor?.putBoolean("is_night_mode", check)
                editor?.apply()
            }

        fun getChangeModeColorCode(defaultColor: String, nightModeColor: String): String {
            return if(isNightMode) nightModeColor else defaultColor
        }

        fun getInstance(): MultiLanguagesApplication? {
            if (instance == null) throw IllegalStateException("Application not be created yet.")
            return instance
        }

    }
}