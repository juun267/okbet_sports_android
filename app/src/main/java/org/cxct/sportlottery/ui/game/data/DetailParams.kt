package org.cxct.sportlottery.ui.game.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo

@Parcelize
data class DetailParams(
    val matchType: MatchType?,
    val gameType: GameType,
    val matchId: String?,
    val matchInfo: MatchInfo? = null,
) : Parcelable