package org.cxct.sportlottery.ui.sport.list.adapter

sealed class SportMatchEvent {

    object OddSelected : SportMatchEvent()

    object FavoriteChanged : SportMatchEvent()

    object OddsChanged : SportMatchEvent()

    object MatchStatuChanged : SportMatchEvent()

    object GlobalStop: SportMatchEvent()

    object CashoutStauts: SportMatchEvent()

}
