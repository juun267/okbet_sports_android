package org.cxct.sportlottery.network.money

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MoneyPayWayData(
    var title: String,
    var subtitle: String,
    var image: String, //icon
    var rechType: String, //onlinePayment：在线支付，bankTransfer：银行汇款，alipay：支付宝支付，weixin：微信支付，cft：财付通
    var onlineType: Int //在线充值类型：1-网银在线充值、2-支付宝在线充值、3-微信在线充值、4-qq在线充值、5-出款、6、信用卡在线充值、7-百度钱包、8-京东钱包、9-银联快捷(扫码)
)
