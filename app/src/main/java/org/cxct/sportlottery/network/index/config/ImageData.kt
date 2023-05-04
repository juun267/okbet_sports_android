package org.cxct.sportlottery.network.index.config

import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class ImageData(
    val id: Int?, //图片id
    val imageLink: String?, //图片跳转地址(可空)
    val imageName1: String?, //(1,2,3)图片名称,4.标题图片
    val imageName2: String?, //内容图片
    val imageName3: String?, //優惠活動圖片地址
    val imageName4: String?, //首页图片
    val imageSort: Int?, //排序
    val imageText1: String?, //标题文字
    val imageText2: String?, //内容文字
    val imageText3: String?, //優惠活動彈窗標題文字
    val imageType: Int?, //图类型:1PC轮播；2H5轮播；3App下载二维码；4.活动图; 12.OKGames顶部轮播图
    val imgUrl: String?, //图片地址（相对地址，需要加上config的resServerHost）
    val viewType: Int?, //0 文字正常显示 1 文字上方显示
    val lang: String?, //语言标记
    val startType: Int?, //0:首次启动 1:日常启动
    val createdAt: Long?,
    val isHidden: Boolean, //送审版本是否隐藏banner 1:是，0：否
)