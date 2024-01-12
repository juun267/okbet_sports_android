package org.cxct.sportlottery.application

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import cn.jpush.android.api.JPushInterface
import com.appsflyer.AppsFlyerLib
import com.didichuxing.doraemonkit.DoKit
import com.xuexiang.xupdate.XUpdate
import com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION
import com.xuexiang.xupdate.utils.UpdateUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import me.jessyan.autosize.AutoSize
import me.jessyan.autosize.AutoSizeConfig
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.event.NetWorkEvent
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.extentions.runWithCatch
import org.cxct.sportlottery.network.manager.RequestManager
import org.cxct.sportlottery.network.user.UserInfo
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.service.ServiceBroadcastReceiver
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintenance.MaintenanceActivity
import org.cxct.sportlottery.util.*
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.*


/**
 * App 內部切換語系
 */
class MultiLanguagesApplication : Application() {

    init {
        mInstance = this
    }

    //private var userInfoData : UserInfo?= null
    private val sharedPref: SharedPreferences by lazy {
        getSharedPreferences(NAME_LOGIN, Context.MODE_PRIVATE)
    }
    private val _userInfo by lazy {
        return@lazy MutableLiveData<UserInfo?>(KvUtils.getObject(UserInfo::class.java))
    }
    val userInfo: LiveData<UserInfo?>
        get() = _userInfo

    val mOddsType = MutableLiveData<OddsType>()

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


    override fun attachBaseContext(base: Context) {
        //第一次进入app时保存系统选择语言(为了选择随系统语言时使用，如果不保存，切换语言后就拿不到了）
        LanguageManager.saveSystemCurrentLanguage()
        super.attachBaseContext(base)
//        super.attachBaseContext(MultiLanguages.attach(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //用户在系统设置页面切换语言时保存系统选择语言(为了选择随系统语言时使用，如果不保存，切换语言后就拿不到了）
        LanguageManager.saveSystemCurrentLanguage(newConfig)
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        // 非主进程不进行下面的初始化
        if (BuildConfig.APPLICATION_ID != getAppProcessName()) {
            return
        }
        // 初始化语种切换框架
//        MultiLanguages.init(this)
        asyncInit()
        AppViewModel.startKoin(this@MultiLanguagesApplication)
        AppManager.init(mInstance)
        AutoSizeConfig.getInstance().isExcludeFontScale = true  // 字体大小不随系统字体大小变化
        runWithCatch { AutoSize.initCompatMultiProcess(this) }
        setNightMode()
        LanguageManager.init(this)
        RequestManager.init(mInstance)

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
            CrashHandler.setup(mInstance) //错误日志收集
            DoKit.Builder(mInstance) //性能监控模块
                .build()
        }

    }

    private fun getAppProcessName(): String {
        if (Build.VERSION.SDK_INT >= 28) return getProcessName()
        val activityThread = Class.forName("android.app.ActivityThread")
        val methodName = if (Build.VERSION.SDK_INT >= 18) "currentProcessName" else "currentPackageName"
        return activityThread.getDeclaredMethod(methodName).invoke(null) as String
    }


    // 不需要在主线程初始化的进行异步初始化
    private fun asyncInit() = GlobalScope.async {

        runWithCatch { TimeZone.setDefault(TimeZoneUitl.timeZone) }
        //生成UUID作為設備識別碼
        setupDeviceCode()
        initAppsFlyerSDK()
        initJpush()
        initXUpdate()

        initNetWorkListener()
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
        AppsFlyerLib.getInstance().init(BuildConfig.AF_APPKEY, null, this)
        AppsFlyerLib.getInstance().setDebugLog(BuildConfig.DEBUG);
        AppsFlyerLib.getInstance().start(this);
    }

    private fun initJpush() {
        JPushInterface.setDebugMode(BuildConfig.DEBUG)
        JPushInterface.init(this)
    }

    private fun setNightMode(switch:Boolean=false) {
        saveNightMode(switch)
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
        KvUtils.putObject(UserInfo::class.java.name, userInfoData)
        _userInfo.postValue(userInfoData)
    }

    fun userInfo(): UserInfo? {
        return _userInfo.value
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

    private fun initXUpdate() {
        XUpdate.get()
            .debug(BuildConfig.DEBUG)
            .isWifiOnly(true) //默认设置只在wifi下检查版本更新
            .isGet(true) //默认设置使用get请求检查版本
            .isAutoMode(false) //默认设置非自动模式，可根据具体使用配置
            .param("versionCode", UpdateUtils.getVersionCode(this)) //设置默认公共请求参数
            .param("appKey", packageName)
            .setOnUpdateFailureListener { error ->

                //设置版本更新出错的监听
                if (error.code != CHECK_NO_NEW_VERSION) {          //对不同错误进行处理
                    ToastUtil.showToast(mInstance, error.toString())
                }
            }
            .supportSilentInstall(true) //设置是否支持静默安装，默认是true
            .setIUpdateHttpService(OKHttpUpdateHttpService()) //这个必须设置！实现网络请求功能。
            .init(this)
    }

    companion object {
        val myPref by lazy { mInstance.getSharedPreferences(mInstance.packageName + "_preferences", MODE_PRIVATE) }
        lateinit var appContext: Context
        const val UUID_DEVICE_CODE = "uuidDeviceCode"
        const val UUID = "uuid"
        lateinit var mInstance: MultiLanguagesApplication

        fun stringOf(@StringRes strId: Int): String {
            return mInstance.getString(strId)
        }

        fun saveSearchHistory(searchHistory: MutableList<String>?) {
            this.searchHistory = searchHistory
        }

        var searchHistory: MutableList<String>? = null
            get() {
                if (field == null) {
                    field = mutableListOf()
                    return field
                }

                val searchHistoryJson = myPref.getString("search_history", "")
                if (searchHistoryJson.isEmptyStr()) {
                    field = mutableListOf()
                    return field
                }

                field = JsonUtil.listFrom(searchHistoryJson!!, String::class.java)
                return field
            }
            set(value) {
                val editor = myPref.edit()
                if (value == null) {
                    editor.putString("search_history", "")
                } else {
                    editor.putString("search_history", JsonUtil.toJson(value))
                }
                editor.apply()
                field = value
            }

        fun saveNightMode(nightMode: Boolean) {
            isNightMode = nightMode
            colorModeChanging = true
        }

        var colorModeChanging: Boolean = false

        var isNightMode: Boolean
            get() = myPref.getBoolean("is_night_mode", false) ?: false
            set(check) {
                val editor = myPref.edit()
                editor.putBoolean("is_night_mode", check)
                editor.apply()
            }


        fun getChangeModeColorCode(defaultColor: String, nightModeColor: String): String {
            return if (isNightMode) nightModeColor else defaultColor
        }

        fun getInstance(): MultiLanguagesApplication {
            return mInstance
        }
        fun saveOddsType(oddsType: OddsType) {
            mInstance.sOddsType = oddsType.code
            mInstance.mOddsType.postValue(oddsType)
        }
    }

    open fun setupSystemStatusChange(owner: LifecycleOwner) {
        ServiceBroadcastReceiver.onSystemStatusChange.observe(owner) {
            if (it) {
                if (AppManager.currentActivity() !is MaintenanceActivity) {
                    startActivity(Intent(this, MaintenanceActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            } else {
                if (AppManager.currentActivity() !is MainTabActivity) {
                    MainTabActivity.reStart(this)
                }
            }
        }
    }

    private var lastTime=0L
    private fun initNetWorkListener(){
        this.let {context->
            val manager=context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val request= NetworkRequest.Builder().build()
            manager.requestNetwork(request,object: ConnectivityManager.NetworkCallback(){
                //网络恢复
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    val nowTime=System.currentTimeMillis()
                    if(nowTime-lastTime>1000){
                        lastTime=nowTime
                        //恢复网络event
                        EventBusUtil.post(NetWorkEvent(true))
                    }
                }

                //网络断开
                override fun onLost(network: Network) {
                    super.onLost(network)
                    EventBusUtil.post(NetWorkEvent(false))
                }
            })
        }
    }
}