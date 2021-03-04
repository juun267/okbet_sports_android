package org.cxct.sportlottery.ui.main.entity

class EnterThirdGameResult(val resultType: ResultType, val url: String?, val errorMsg: String? = null) {
    //20200302 記錄問題：新增一個 NONE type，來清除狀態，避免 fragment 畫面重啟馬上就會觸發 observe，重複開啟第三方遊戲
    enum class ResultType { SUCCESS, FAIL, NEED_REGISTER, NONE }
}