package org.cxct.sportlottery.ui.profileCenter.versionUpdate

import org.cxct.sportlottery.network.appUpdate.CheckAppVersionResult

class AppMinVersionState(
    val isShowUpdateDialog: Boolean, //是否顯示更新提示視窗
    val isForceUpdate: Boolean, //是否強制更新
    val version: String,
    val checkAppVersionResult: CheckAppVersionResult? = null,
)