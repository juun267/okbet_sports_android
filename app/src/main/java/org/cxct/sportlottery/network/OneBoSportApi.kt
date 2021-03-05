package org.cxct.sportlottery.network

import org.cxct.sportlottery.network.appUpdate.AppUpdateService
import org.cxct.sportlottery.network.bank.BankService
import org.cxct.sportlottery.network.bet.BetService
import org.cxct.sportlottery.network.feedback.FeedbackService
import org.cxct.sportlottery.network.host.HostService
import org.cxct.sportlottery.network.index.IndexService
import org.cxct.sportlottery.network.infoCenter.InfoCenterService
import org.cxct.sportlottery.network.league.LeagueService
import org.cxct.sportlottery.network.manager.RequestManager
import org.cxct.sportlottery.network.match.MatchService
import org.cxct.sportlottery.network.matchresult.MatchResultService
import org.cxct.sportlottery.network.message.MessageService
import org.cxct.sportlottery.network.money.MoneyService
import org.cxct.sportlottery.network.odds.OddsService
import org.cxct.sportlottery.network.outright.OutrightService
import org.cxct.sportlottery.network.playcate.PlayCateListService
import org.cxct.sportlottery.network.sport.SportService
import org.cxct.sportlottery.network.third_game.ThirdGameService
import org.cxct.sportlottery.network.uploadImg.UploadImgService
import org.cxct.sportlottery.network.user.UserService
import org.cxct.sportlottery.network.vip.VipService
import org.cxct.sportlottery.network.withdraw.WithdrawService


object OneBoSportApi {
    val retrofit by lazy {
        RequestManager.instance.retrofit
    }

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

    val matchResultService: MatchResultService by lazy {
        RequestManager.instance
            .retrofit
            .create(MatchResultService::class.java)
    }

    val playCateListService: PlayCateListService by lazy {
        RequestManager.instance
            .retrofit
            .create(PlayCateListService::class.java)
    }

    val outrightService: OutrightService by lazy {
        RequestManager.instance
            .retrofit
            .create(OutrightService::class.java)
    }

    val userService: UserService by lazy {
        RequestManager.instance
            .retrofit
            .create(UserService::class.java)
    }

    val uploadImgService: UploadImgService by lazy {
        RequestManager.instance
            .retrofit
            .create(UploadImgService::class.java)
    }

    val infoCenterService: InfoCenterService by lazy {
        RequestManager.instance
            .retrofit
            .create(InfoCenterService::class.java)
    }

    val moneyService: MoneyService by lazy {
        RequestManager.instance
            .retrofit
            .create(MoneyService::class.java)
    }

    val bankService: BankService by lazy {
        RequestManager.instance
            .retrofit
            .create(BankService::class.java)
    }

    val withdrawService: WithdrawService by lazy {
        RequestManager.instance
            .retrofit
            .create(WithdrawService::class.java)
    }

    val feedbackService: FeedbackService by lazy {
        RequestManager.instance
            .retrofit
            .create(FeedbackService::class.java)
    }

    val appUpdateService: AppUpdateService by lazy {
        RequestManager.instance
            .retrofit
            .create(AppUpdateService::class.java)
    }

    val thirdGameService: ThirdGameService by lazy {
        RequestManager.instance
            .retrofit
            .create(ThirdGameService::class.java)
    }

    val hostService: HostService by lazy {
        RequestManager.instance
            .retrofit
            .create(HostService::class.java)
    }

    val vipService: VipService by lazy {
        RequestManager.instance
            .retrofit
            .create(VipService::class.java)
    }
}