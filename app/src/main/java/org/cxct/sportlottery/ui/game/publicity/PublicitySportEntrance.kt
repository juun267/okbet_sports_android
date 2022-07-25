package org.cxct.sportlottery.ui.game.publicity

import com.google.gson.annotations.SerializedName
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import java.io.Serializable


class PublicitySportEntrance(val matchType: MatchType, val gameType: GameType) : Serializable {
}