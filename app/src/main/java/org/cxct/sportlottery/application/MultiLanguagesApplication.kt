package org.cxct.sportlottery.application

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.multidex.MultiDex
import com.appsflyer.AppsFlyerLib
import com.didichuxing.doraemonkit.DoKit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import me.jessyan.autosize.AutoSize
import org.cxct.sportlottery.common.OddsType
import org.cxct.sportlottery.common.ResourceWrapper
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.R
import org.cxct.sportlottery.util.ResourceWrapper
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.manager.RequestManager
import org.cxct.sportlottery.network.money.RedEnveLopeModel
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.feedback.FeedbackViewModel
import org.cxct.sportlottery.ui.finance.FinanceViewModel
import org.cxct.sportlottery.ui.game.betList.BetListViewModel
import org.cxct.sportlottery.ui.helpCenter.HelpCenterViewModel
import org.cxct.sportlottery.ui.infoCenter.InfoCenterViewModel
import org.cxct.sportlottery.ui.login.foget.ForgetViewModel
import org.cxct.sportlottery.ui.login.signIn.LoginViewModel
import org.cxct.sportlottery.ui.login.signUp.RegisterViewModel
import org.cxct.sportlottery.ui.maintab.MainHomeViewModel
import org.cxct.sportlottery.ui.maintab.MainTabViewModel
import org.cxct.sportlottery.ui.maintab.SportViewModel
import org.cxct.sportlottery.ui.maintenance.MaintenanceViewModel
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.ui.maintab.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.ui.money.recharge.MoneyRechViewModel
import org.cxct.sportlottery.ui.news.NewsViewModel
import org.cxct.sportlottery.ui.profileCenter.ProfileCenterViewModel
import org.cxct.sportlottery.ui.profileCenter.authbind.AuthViewModel
import org.cxct.sportlottery.ui.profileCenter.cancelaccount.CancelAccountViewModel
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordViewModel
import org.cxct.sportlottery.ui.profileCenter.identity.VerifyIdentityDialog
import org.cxct.sportlottery.ui.profileCenter.money_transfer.MoneyTransferViewModel
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyProfileInfoViewModel
import org.cxct.sportlottery.ui.profileCenter.otherBetRecord.OtherBetRecordViewModel
import org.cxct.sportlottery.ui.profileCenter.profile.ProfileModel
import org.cxct.sportlottery.ui.profileCenter.versionUpdate.VersionUpdateViewModel
import org.cxct.sportlottery.ui.results.SettlementViewModel
import org.cxct.sportlottery.ui.selflimit.SelfLimitViewModel
import org.cxct.sportlottery.ui.splash.SplashViewModel
import org.cxct.sportlottery.ui.sport.SportListViewModel
import org.cxct.sportlottery.ui.sport.SportTabViewModel
import org.cxct.sportlottery.ui.sport.favorite.FavoriteViewModel
import org.cxct.sportlottery.ui.sport.filter.LeagueSelectViewModel
import org.cxct.sportlottery.ui.transactionStatus.TransactionStatusViewModel
import org.cxct.sportlottery.ui.withdraw.WithdrawViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.dialog.AgeVerifyDialog
import org.cxct.sportlottery.view.dialog.promotion.PromotionPopupDialog
import org.cxct.sportlottery.util.AppManager.OnAppStatusChangedListener
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.*


/**
 * App 內部切換語系
 */
class MultiLanguagesApplication : Application() {
    //private var userInfoData : UserInfo?= null
    private val sharedPref: SharedPreferences by lazy {
        getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }
    private val _userInfo = MutableLiveData<UserInfo?>()
    val userInfo: LiveData<UserInfo?>
        get() = _userInfo
    private var isNewsShowed = false
    private var isAgeVerifyNeedShow = true

    val mOddsType = MutableLiveData<OddsType>()
    var doNotReStartPublicity = false

    /**
     * HandicapType.NULL.name為尚未配置後端設置的預設盤口
     */
    var sOddsType: String?
        get() {
            val handicapType = sharedPref.getString(KEY_ODDS_TYPE, HandicapType.NULL.name)
            if (handicapType != HandicapType.NULL.name && !isOddsTypeEnable(handicapType ?: "")) {
                updateDefaultHandicapType()
                return HandicapType.NULL.name
            }

            return handicapType
        }
        set(value) {
            with(sharedPref.edit()) {
                putString(KEY_ODDS_TYPE, value)
                commit()
            }
        }


    private val viewModelModule = module {
        viewModel { SplashViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { MoneyRechViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
        viewModel { MainViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { LoginViewModel(get(), get(), get(), get(), get()) }
        viewModel { RegisterViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { SettlementViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { InfoCenterViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { HelpCenterViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { WithdrawViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { ProfileModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { ModifyProfileInfoViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { SettingPasswordViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { FeedbackViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { SelfLimitViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { FinanceViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { ProfileCenterViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { VersionUpdateViewModel(get(), get(), get(), get()) }
        viewModel { MoneyTransferViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { MaintenanceViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { OtherBetRecordViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { AccountHistoryViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { TransactionStatusViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { NewsViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { RedEnveLopeModel(get(), get(), get(), get(), get(), get()) }
        viewModel { MainTabViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { SportViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { FavoriteViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { LeagueSelectViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { SportListViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { SportTabViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { CancelAccountViewModel(get(), get(), get(), get(), get(), get()) }
        viewModel { MainHomeViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { ForgetViewModel(get(), get(), get(), get()) }
        viewModel { BetListViewModel(get(), get(), get(), get(), get(), get(), get()) }
        viewModel { AuthViewModel(get(), get(), get(), get(), get(), get(), get()) }
    }

    private val repoModule = module {
        single { UserInfoRepository }
        single { LoginRepository }
        single { SportMenuRepository }
        single { SettlementRepository() }
        single { InfoCenterRepository() }
        single { MoneyRepository }
        single { BetInfoRepository }
        single { AvatarRepository(get()) }
        single { FeedbackRepository() }
        single { HostRepository(get()) }
        single { WithdrawRepository }
        single { PlayQuotaComRepository() }
        single { MyFavoriteRepository() }
        single { SelfLimitRepository() }
    }


    private val serviceModule = module {
        factory { ServiceBroadcastReceiver(get(), get()) }
    }

    override fun attachBaseContext(base: Context) {
        //第一次进入app时保存系统选择语言(为了选择随系统语言时使用，如果不保存，切换语言后就拿不到了）
        LanguageManager.saveSystemCurrentLanguage(base)
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //用户在系统设置页面切换语言时保存系统选择语言(为了选择随系统语言时使用，如果不保存，切换语言后就拿不到了）
        LanguageManager.saveSystemCurrentLanguage(applicationContext, newConfig)
//        MultiLanguage.onConfigurationChanged(applicationContext)
    }

    private fun getDefaultSharedPreferences(): SharedPreferences {
        return getSharedPreferences(packageName + "_preferences", MODE_PRIVATE)
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        instance = this
        mInstance = this
        AppManager.init(this)
        AppManager.getInstance().addOnAppStatusChangedListener(object : OnAppStatusChangedListener {
            override fun onForeground(var1: Activity?) {
                LotteryManager.instance.getLotteryInfo()
            }

            override fun onBackground(var1: Activity?) {

            }
        })
        myPref = getDefaultSharedPreferences()
        AutoSize.initCompatMultiProcess(this)
        TimeZone.setDefault(timeZone)
        startKoin {
            androidContext(this@MultiLanguagesApplication)
            modules(
                listOf(
                    viewModelModule, repoModule, serviceModule
                )
            )
        }

        RequestManager.init(this)
        setupTimber()
        setNightMode()
        LanguageManager.init(this)
        //生成UUID作為設備識別碼
        setupDeviceCode()
        initAppsFlyerSDK()

        if (BuildConfig.DEBUG) {
            CrashHandler.setup(this) //错误日志收集
            DoKit.Builder(this) //性能监控模块
                .build()
        }

    }

    private val localeResources by lazy {
        ResourceWrapper(
            this@MultiLanguagesApplication, super.getResources()
        )
    }

    override fun getResources(): Resources {
        return localeResources
    }

    private fun initAppsFlyerSDK() {
        AppsFlyerLib.getInstance().init("G7q8UBYftYQfKAxnortTSN", null, this)
        AppsFlyerLib.getInstance().setDebugLog(BuildConfig.DEBUG);
        AppsFlyerLib.getInstance().start(this);
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
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
        if (devicePreferences.getString(UUID, null).isNullOrEmpty()) devicePreferences.edit()
            .putString(UUID, java.util.UUID.randomUUID().toString()).apply()
    }

    fun saveUserInfo(userInfoData: UserInfo?) {
        _userInfo.postValue(userInfoData)
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
        return true
        /*return if (BuildConfig.CHANNEL_NAME == "spkx") {
//            isGameDetailAnimationNeedShow
            true
        } else {
            true
        }*/
    }

    fun isAgeVerifyNeedShow(): Boolean {
        return isAgeVerifyNeedShow
    }

    fun setIsAgeVerifyShow(show: Boolean) {
        this.isAgeVerifyNeedShow = show
    }

    fun getOddsType() {
        //若為HandicapType.NULL是為尚未配置, 無需更新View
        when (mInstance.sOddsType) {
            OddsType.EU.code -> mInstance.mOddsType.postValue(OddsType.EU)
            OddsType.HK.code -> mInstance.mOddsType.postValue(OddsType.HK)
            OddsType.MYS.code -> mInstance.mOddsType.postValue(OddsType.MYS)
            OddsType.IDN.code -> mInstance.mOddsType.postValue(OddsType.IDN)
        }
    }

    companion object {
        var myPref: SharedPreferences? = null
        lateinit var appContext: Context
        const val UUID_DEVICE_CODE = "uuidDeviceCode"
        const val UUID = "uuid"
        private var instance: MultiLanguagesApplication? = null
        lateinit var mInstance: MultiLanguagesApplication
        const val isGooglePlayVersion = BuildConfig.FLAVOR.equals("google")

        private val loginSharedPref: SharedPreferences by lazy {
            mInstance.getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
        }

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
            return if (isNightMode) nightModeColor else defaultColor
        }

        var timeZone: TimeZone
            get() {
                var displayName = myPref?.getString("timeZone", null)
                if (displayName.isNullOrBlank()) {
                    return TimeZone.getDefault()
                } else {
                    var zone = TimeZone.getTimeZone(displayName)
                    zone.id = myPref?.getString("timeZoneId", null)
                    return zone
                }
            }
            set(zone) {
                val editor = myPref?.edit()
                editor?.putString("timeZone", zone.getDisplayName(false, TimeZone.SHORT))
                editor?.putString("timeZoneId", zone.id)
                editor?.apply()
            }

        fun getInstance(): MultiLanguagesApplication? {
            if (instance == null) throw IllegalStateException("Application not be created yet.")
            return instance
        }

        //確認年齡彈窗
        fun showAgeVerifyDialog(activity: FragmentActivity) {
            if (isCreditSystem()) return //信用盤不顯示彈窗
            if (getInstance()?.isAgeVerifyNeedShow() == false) return
            AgeVerifyDialog(activity, object : AgeVerifyDialog.OnAgeVerifyCallBack {
                override fun onConfirm() {
                    //當玩家點擊"I AM OVER 21 YEARS OLD"後，關閉此視窗
                    getInstance()?.setIsAgeVerifyShow(false)
                    showPromotionPopupDialog(activity)
                }

                override fun onExit() {
                    //當玩家點擊"EXIT"後，徹底關閉APP
                    AppManager.AppExit()
                }

            }).show()
        }

        open fun showPromotionPopupDialog(activity: FragmentActivity) {
            val token = loginSharedPref.getString(KEY_TOKEN, "")

            if (!isCreditSystem() && sConfigData?.imageList?.any { it.imageType == ImageType.PROMOTION.code && !it.imageName3.isNullOrEmpty() } == true) PromotionPopupDialog(
                activity, PromotionPopupDialog.PromotionPopupListener(onClickImageListener = {
                    JumpUtil.toInternalWeb(
                        activity, Constants.getPromotionUrl(
                            token, LanguageManager.getSelectLanguage(activity)
                        ), LocalUtils.getString(R.string.promotion)
                    )
                })
            ).show()
        }

        fun saveOddsType(oddsType: OddsType) {
            mInstance.sOddsType = oddsType.code
            mInstance.mOddsType.postValue(oddsType)
        }

        fun showKYCVerifyDialog(activity: FragmentActivity) {
            VerifyIdentityDialog().show(activity.supportFragmentManager, null)
        }
    }

}