package org.cxct.sportlottery.network.quest.info

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.proguards.KeepMembers


@KeepMembers
@Parcelize
data class LimitedGame(
    val gameCategory: String?,
    val firmCode: String?,
    val firmType: String?,
    val gameId: Int?,
    val gameType: String?,
    val leagueId: String?,
    val matchId: String?,
    val type: String?
): Parcelable{
    companion object{
        const val TYPE_SPORT = "sport"
        const val TYPE_THIRD = "third"

        const val GAME_CATEGORY_LIVE = "LIVE"
        const val GAME_CATEGORY_DZ = "DZ"
    }
}