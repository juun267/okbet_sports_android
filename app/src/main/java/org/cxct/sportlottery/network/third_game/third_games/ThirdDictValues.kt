package org.cxct.sportlottery.network.third_game.third_games

import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class ThirdDictValues(
    val id: Int?, //主键
    val gameCategory: String?, //一级分类
    val firmType: String?, //所属第三方
    val firmCode: String?, //第三方游戏编码
    var gameCode: String?, //预留字段
    val gameType: String?, //具体游戏
    val chineseName: String?, //游戏中文名
    val englishName: String?, //游戏英文名
    val imageName: String?, //图片
    val isH5: Int?, //支持手机端（0：支持，1：不支持）
    val isFlash: Int?, //支持电脑端（0:支持，1：不支持）
    val h5ImageName: String?, //手机端图片名称
    val sort: Double? //游戏排序
) {
    //APP端新增 從第二層資料傳遞
    //20200213 review API 已經沒有回傳ban 改成open
    var open: Int? = null // 維護狀態： 0 - 維護, 1 - 正常
    var firmName: String? = null
}