package org.cxct.sportlottery.network.index

data class ImageData(
    val id: Int?, //图片id
    val imageLink: String?, //图片跳转地址(可空)
    val imageName1: String?, //(1,2,3)图片名称,4.标题图片
    val imageName2: String?, //内容图片
    val imageSort: Int?, //排序
    val imageText1: String?, //标题文字
    val imageText2: String?, //内容文字
    val imageType: Int?, //图类型:1PC轮播；2H5轮播；3App下载二维码；4.活动图
    val imgUrl: String? //图片地址（相对地址，需要加上config的resServerHost）
)