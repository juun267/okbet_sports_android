package org.cxct.sportlottery.net.sport.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
@Parcelize
data class RecommendLeague(
    val addTime: String,
    val category: String,
    val categoryCode: String,
    val categoryIcon: String,
    val categoryName: String,
    val cxId: String,
    val end: String,
    val gameType: String,
    val icon: String,
    val id: String,
    val name: String,
    val popular: Int,
    val sort: Int,
    val source: Int,
    val start: String,
    val status: Int,
    val updateTime: String
):Parcelable