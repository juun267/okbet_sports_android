package org.cxct.sportlottery.ui.main.entity

class EnterThirdGameResult(val resultType: ResultType, val url: String?, val errorMsg: String? = null) {
    enum class ResultType { SUCCESS, FAIL, NEED_LOGIN }
}