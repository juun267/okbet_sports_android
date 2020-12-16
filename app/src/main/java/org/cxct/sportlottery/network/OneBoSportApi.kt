package org.cxct.sportlottery.network

import org.cxct.sportlottery.network.manager.RequestManager
import org.cxct.sportlottery.network.bet.BetService
import org.cxct.sportlottery.network.index.IndexService
import org.cxct.sportlottery.network.league.LeagueService
import org.cxct.sportlottery.network.match.MatchService
import org.cxct.sportlottery.network.message.MessageService
import org.cxct.sportlottery.network.odds.OddsService
import org.cxct.sportlottery.network.sport.SportService


object OneBoSportApi {

    val indexService: IndexService by lazy {
        RequestManager.instance
            .retrofit
            .create(IndexService::class.java)
    }

    val sportService: SportService by lazy {
        RequestManager.instance
            .retrofit
            .create(SportService::class.java)
    }

    val matchService: MatchService by lazy {
        RequestManager.instance
            .retrofit
            .create(MatchService::class.java)
    }

    val messageService: MessageService by lazy {
        RequestManager.instance
            .retrofit
            .create(MessageService::class.java)
    }

    val oddsService: OddsService by lazy {
        RequestManager.instance
            .retrofit
            .create(OddsService::class.java)
    }

    val betService: BetService by lazy {
        RequestManager.instance
            .retrofit
            .create(BetService::class.java)
    }

    val leagueService: LeagueService by lazy {
        RequestManager.instance
            .retrofit
            .create(LeagueService::class.java)
    }
}