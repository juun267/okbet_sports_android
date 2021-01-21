package org.cxct.sportlottery.network.money

import java.io.Serializable

class MoneyRechCfg {
    class Data {
        var banks: MutableList<Bank>? = null //银行lfny
        var rechCfgs: MutableList<RechConfig>? = null //充值配置
        var rechTypes: MutableList<RechType>? =
            null //充值类型，name：名称，value：类型（onlinePayment：在线支付，bankTransfer：银行汇款，alipay：支付宝支付，weixin：微信支付，cft：财付通）
        var withdrawCfg: WithdrawCfg? = null //提现相关设定 单笔提现最大金额 单笔提现最小金额 提现费率
    }

    class Bank {
        var name: String? = null //名称
        var value: String? = null //值
    }

    class RechConfig : Serializable {
        var id: Int? = null //主键
        var rechType: String? =
            null //onlinePayment：在线支付，bankTransfer：银行汇款，alipay：支付宝支付，weixin：微信支付，cft：财付通
        var rechName: String? = null //充值名称
        var payeeName: String? = null //充值账户名
        var payee: String? = null //充值账号
        var remark: String? = null //前端显示说明
        var remark2: String? = null //后端显示备注说明
        var qrCode: String? = null //二维码支付图片
        var prodName: String? = null //商品名称
        var para1: String? = null //扩展参数
        var minMoney: Double? = null //充值最小金额
        var maxMoney: Double? = null //充值最大金额
        var pageDesc: String? = null //前端页面提示文字
        var onlineTypeId: Int? = null //在线充值类型id
        var onlineType: Int? =
            null //在线充值类型：1-网银在线充值、2-支付宝在线充值、3-微信在线充值、4-qq在线充值、5-出款、6、信用卡在线充值、7-百度钱包、8-京东钱包、9-银联快捷(扫码)
        var pcMobile: Int? = null //支持类型:0-都支付，1-电脑版，2-手机版
        var banks: MutableList<RechBank>? = null //银行列表，银行在线支付有此列表
        var rebateFee: Double? = null//赠送金额
        var payUrl: String? = null //支付入口
        var exchangeRate: Double? = null//汇率
        var exchangeList: MutableList<ExchangeList>? = null//汇率列表


    }

    class RechType {
        var name: String? = null //名称
        var value: String? = null //值
    }

    class RechBank {
        var value: String? = null //银行代码
        var ico: String? = null //ico
        var bankName: String? = null //银行名称
    }

    class ExchangeList {
        var exchangeCurrency: String? = null//汇率币总
        var exchangeRate: Double? = null//汇率
    }

    class WithdrawCfg {
        var wdRate: Double? = null//用户提现费率
        var withDrawBalanceLimit: Long? = null//每笔最小提现金额
        var maxWithdrawMoney: Long? = null//每笔最高提现金额
    }
}