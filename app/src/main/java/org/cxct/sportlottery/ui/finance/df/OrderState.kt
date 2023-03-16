package org.cxct.sportlottery.ui.finance.df

enum class OrderState(val code: Int) {
    //1: 處理中 2:提款成功 3:提款失败 4：待投注站出款
    PROCESSING(1),
    SUCCESS(2),
    FAILED(3),
    PENGING(4)
}