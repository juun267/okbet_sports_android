package org.cxct.sportlottery.network.money

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MoneyPayWayData(
    @Json(name = "titleNameMap")
    var titleNameMap: Map<String, String>,
    @Json(name = "subtitle")
    var subtitle: String,
    @Json(name = "image")
    var image: String, //icon
    @Json(name = "rechType")
    var rechType: String, //onlinePayment：在线支付，bankTransfer：银行汇款，alipay：支付宝支付，weixin：微信支付，cft：财付通
    @Json(name = "onlineType")
    var onlineType: Int //在线充值类型：1-网银在线充值、2-支付宝在线充值、3-微信在线充值、4-qq在线充值、5-出款、6、信用卡在线充值、7-百度钱包、8-京东钱包、9-银联快捷 11-虚拟币支付 12-虚拟币出款 102-菲律賓在線網銀
)

enum class OnlineType(val type : Int) {
    WY(1), //1-网银在线充值
    ZFB(2), //2-支付宝在线充值
    WX(3), //3-微信在线充值
    QQ(4), //4-qq在线充值
    CK(5), //5-出款
    XYK(6), //6、信用卡在线充值
    BD(7), //7-百度钱包
    JD(8), //8-京东钱包
    YL(9), //9-银联快捷
    CRYPTO_PAY(11), //11-虚拟币支付
    CRYPTO_OUT(12), //12-虚拟币出款
    NWY(101),
    MMP(102),
    ZLP(103),
    TEL(104),
    WPA(105),
    JUAN(201),//201-菲律賓在線網銀
}
