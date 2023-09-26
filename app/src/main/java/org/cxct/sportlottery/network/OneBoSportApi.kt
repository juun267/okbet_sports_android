package org.cxct.sportlottery.network

import org.cxct.sportlottery.network.appUpdate.AppUpdateService
import org.cxct.sportlottery.network.bank.BankService
import org.cxct.sportlottery.network.bet.BetService
import org.cxct.sportlottery.network.bettingStation.BettingStationService
import org.cxct.sportlottery.network.feedback.FeedbackService
import org.cxct.sportlottery.network.host.HostService
import org.cxct.sportlottery.network.index.IndexService
import org.cxct.sportlottery.network.infoCenter.InfoCenterService
import org.cxct.sportlottery.network.lottery.LotteryService
import org.cxct.sportlottery.network.manager.RequestManager
import org.cxct.sportlottery.network.match.MatchService
import org.cxct.sportlottery.network.matchresult.MatchResultService
import org.cxct.sportlottery.network.message.MessageService
import org.cxct.sportlottery.network.money.MoneyService
import org.cxct.sportlottery.network.myfavorite.FavoriteService
import org.cxct.sportlottery.network.news.NewsService
import org.cxct.sportlottery.network.odds.OddsService
import org.cxct.sportlottery.network.outright.OutrightService
import org.cxct.sportlottery.network.sport.SportService
import org.cxct.sportlottery.network.third_game.ThirdGameService
import org.cxct.sportlottery.network.uploadImg.UploadImgService
import org.cxct.sportlottery.network.user.UserService
import org.cxct.sportlottery.network.withdraw.WithdrawService


object OneBoSportApi {
    val retrofit
        get() =
            RequestManager.instance.retrofit


    val indexService: IndexService
        get() = RequestManager.instance
            .retrofit
            .create(IndexService::class.java)

    val sportService: SportService
        get() = RequestManager.instance
            .retrofit
            .create(SportService::class.java)

    val favoriteService: FavoriteService
        get() = RequestManager.instance
            .retrofit
            .create(FavoriteService::class.java)

    val matchService: MatchService
        get() = RequestManager.instance
            .retrofit
            .create(MatchService::class.java)


    val messageService: MessageService
        get() = RequestManager.instance
            .retrofit
            .create(MessageService::class.java)


    val oddsService: OddsService
        get() = RequestManager.instance
            .retrofit
            .create(OddsService::class.java)


    val betService: BetService
        get() = RequestManager.instance
            .retrofit
            .create(BetService::class.java)

    val matchResultService: MatchResultService
        get() = RequestManager.instance
            .retrofit
            .create(MatchResultService::class.java)


    val outrightService: OutrightService
        get() = RequestManager.instance
            .retrofit
            .create(OutrightService::class.java)


    val userService: UserService
        get() = RequestManager.instance
            .retrofit
            .create(UserService::class.java)


    val uploadImgService: UploadImgService
        get() = RequestManager.instance
            .retrofit
            .create(UploadImgService::class.java)


    val infoCenterService: InfoCenterService
        get() = RequestManager.instance
            .retrofit
            .create(InfoCenterService::class.java)


    val moneyService: MoneyService
        get() = RequestManager.instance
            .retrofit
            .create(MoneyService::class.java)


    val bankService: BankService
        get() = RequestManager.instance
            .retrofit
            .create(BankService::class.java)


    val withdrawService: WithdrawService
        get() = RequestManager.instance
            .retrofit
            .create(WithdrawService::class.java)


    val feedbackService: FeedbackService
        get() = RequestManager.instance
            .retrofit
            .create(FeedbackService::class.java)


    val appUpdateService: AppUpdateService
        get() = RequestManager.instance
            .retrofit
            .create(AppUpdateService::class.java)


    val thirdGameService: ThirdGameService
        get() = RequestManager.instance
            .retrofit
            .create(ThirdGameService::class.java)


    val hostService: HostService
        get() = RequestManager.instance
            .retrofit
            .create(HostService::class.java)

    val newsService: NewsService
        get() = RequestManager.instance
            .retrofit
            .create(NewsService::class.java)

    val bettingStationService: BettingStationService
        get() = RequestManager.instance
            .retrofit
            .create(BettingStationService::class.java)

    val lotteryService: LotteryService
        get() = RequestManager.instance
            .retrofit
            .create(LotteryService::class.java)

}