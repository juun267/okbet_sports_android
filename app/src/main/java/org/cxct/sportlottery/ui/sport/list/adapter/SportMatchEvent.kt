package org.cxct.sportlottery.ui.sport.list.adapter

sealed class SportMatchEvent {

    object OddSelected : SportMatchEvent()

    object FavoriteChanged : SportMatchEvent()

    object OddsChanged : SportMatchEvent()

}
