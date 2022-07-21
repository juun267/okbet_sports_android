package org.cxct.sportlottery.ui.game.publicity

import org.cxct.sportlottery.network.sport.SportMenu
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues

class PublicityMenuData(
    var sportMenuDataList: List<SportMenu>? = null,
    var eGameMenuData: ThirdDictValues? = null,
    var casinoMenuData: ThirdDictValues? = null,
    var sabongMenuData: ThirdDictValues? = null
) {
}