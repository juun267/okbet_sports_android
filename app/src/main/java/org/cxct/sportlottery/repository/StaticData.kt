package org.cxct.sportlottery.repository

import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.net.RetrofitHolder
import org.cxct.sportlottery.network.index.config.ConfigData
import org.cxct.sportlottery.repository.HandicapType.NULL
import org.cxct.sportlottery.repository.ImageType.PROMOTION
import org.cxct.sportlottery.ui.maintab.home.view.HomeTopView
import org.cxct.sportlottery.util.KvUtils
import org.cxct.sportlottery.util.LogUtil

const val FLAG_OPEN = "1"
const val FLAG_CLOSE = "0"
const val FLAG_LIVE = "1"
const val FLAG_NO_LIVE = "0"
const val FLAG_IS_NEED_UPDATE_PAY_PW = 1 //尚未設置過資金密碼的 flag，需要更新資金密碼
const val FLAG_NICKNAME_IS_SET = 1 //已經設置過暱稱
const val FLAG_CREDIT_OPEN = 1

// TODO 蜜蜂說體育彩沒有遊客試玩，這邊先保留GUEST狀態，後續需要確認是否有為1的狀況 by Hewie
enum class TestFlag(val index: Long) { NORMAL(0), GUEST(1), TEST(2) } //是否测试用户（0-正常用户，1-游客，2-内部测试）

/**
 * config圖片清單類型
 *
 * @property PROMOTION app端優惠活動彈窗
 * @see org.cxct.sportlottery.network.index.config.ConfigData.imageList
 * @see org.cxct.sportlottery.network.index.config.ImageData.imageType
 */
enum class ImageType(val code: Int) {
    PROMOTION_LIST(4),
    PROMOTION(5),
    DIALOG_HOME(7),
    DIALOG_SPORT(14),
    DIALOG_OKGAME(16),
    DIALOG_OKLIVE(-1),
    LOGIN_SUMMARY(20)
}

/**
 * config 前端展示的盘口(handicapShow)類型
 * @property NULL 預設盤口尚未設置, 獲取預設盤口時config尚未取得
 */
enum class HandicapType() {
    EU, HK, MY, ID, NULL
}

const val LOGIN_SRC: Long = 2 //登录来源，WEB(0), MOBILE_BROWSER(1), ANDROID(2), IOS(3);

const val PLATFORM_CODE = BuildConfig.CHANNEL_NAME //平台代碼
const val PROJECT_CODE = "cx_sports" //項目代碼
const val APP_NAME = "okbet" //okgame的包需要加一些特定的参数

var sConfigData: ConfigData? = null
    set(value) {
        KvUtils.putObject(ConfigData::class.java.name, value)
        field = value
        value?.chatHost?.let { RetrofitHolder.changeChatHost(it) }
        value?.idScanHost?.let { RetrofitHolder.changeORCHost(it) }
    }
    get() {
        if (field == null) {
            field = KvUtils.getObject(ConfigData::class.java)
        }
        return field
    }


/**
 * 當前需要顯示的幣種符號
 * 若沒有登入者幣種符號則顯示系統預設幣種符號
 */
val showCurrencySign: String?
    get() = getLoginCurrency() ?: sConfigData?.systemCurrencySign ?: ""

private fun getLoginCurrency(): String? =
    MultiLanguagesApplication.mInstance.userInfo.value?.currencySign

// 通过应用商店升级应用
inline fun upgradeFromMarket(): Boolean {
    return BuildConfig.FLAVOR.startsWith("google", true)
}

/**
 * 紀錄是否第一次開啟app取得configData
 */
var gotConfigData: Boolean = false

class StaticData {
    companion object {
        fun getTestFlag(index: Long?): TestFlag? {
            return TestFlag.values().find { it.index == index }
        }

        fun worldCupOpened(): Boolean {
            return sConfigData?.fibaConfig?.fibaEnable == 1
        }
        fun okLiveOpened(): Boolean {
            sConfigData?.homeGamesList?.forEach {
                if(it.uniqueName== HomeTopView.OkLive){
                    //status==1  为开启
                    return it.status==1
                }
            }
            return true
        }


        //获取okGame是否开启
        fun okGameOpened(): Boolean{
            sConfigData?.homeGamesList?.forEach {
                if(it.uniqueName== HomeTopView.OkGame){
                    //status==1  为开启
                    return it.status==1
                }
            }
            return true
        }


        //获取okSport菜单是否开启
        fun okSportOpened():Boolean {
            sConfigData?.homeGamesList?.forEach {
                if(it.uniqueName== HomeTopView.OkSport){
                    //status==1  为开启
                    return it.status==1
                }
            }
            return true
        }

        //获取okBingo是否开启
        fun okBingoOpened():Boolean {
            sConfigData?.homeGamesList?.forEach {
                if(it.uniqueName== HomeTopView.OkBingo){
                    //status==1  为开启
                    return it.status==1
                }
            }
            return true
        }

        fun isNeedOTPBank() = 1 == sConfigData?.isNeedOTPBank

    }

}