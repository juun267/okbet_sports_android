package org.cxct.sportlottery.net.news.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@Parcelize
@KeepMembers
class NewsCategory(
    val id: Int,
    val categoryName: String?,
    val detailList: List<NewsItem>?,
):Parcelable

@Parcelize
@KeepMembers
data class NewsItem(
    val id: Int,
    val title: String?,
    val summary: String?,
    val image: String?,
    val state: String?,
    val markHome: Boolean = false,
    val markRecommend: Boolean = false,
    val metaKeys: String?,
    val metaDetail: String?,
    val tagNameList: List<String>?,
    val createTimeInMillisecond: Long,
    val updateTimeInMillisecond: Long,
):Parcelable
