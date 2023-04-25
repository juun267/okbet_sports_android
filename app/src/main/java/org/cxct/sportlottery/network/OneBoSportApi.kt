package org.cxct.sportlottery.network

import org.cxct.sportlottery.network.appUpdate.AppUpdateService
import org.cxct.sportlottery.network.bank.BankService
import org.cxct.sportlottery.network.bet.BetService
import org.cxct.sportlottery.network.bettingStation.BettingStationService
import org.cxct.sportlottery.network.chat.ChatService
import org.cxct.sportlottery.network.chat.SignService
import org.cxct.sportlottery.network.credential.CredentialService
import org.cxct.sportlottery.network.feedback.FeedbackService
import org.cxct.sportlottery.network.host.HostService
import org.cxct.sportlottery.network.index.IndexService
import org.cxct.sportlottery.network.index.PlayQuotaComService
import org.cxct.sportlottery.network.infoCenter.InfoCenterService
import org.cxct.sportlottery.network.league.LeagueService
import org.cxct.sportlottery.network.lottery.LotteryService
import org.cxct.sportlottery.network.manager.RequestManager
import org.cxct.sportlottery.network.match.MatchService
import org.cxct.sportlottery.network.matchCategory.MatchCategoryService
import org.cxct.sportlottery.network.matchresult.MatchResultService
import org.cxct.sportlottery.network.message.MessageService
import org.cxct.sportlottery.network.money.MoneyService
import org.cxct.sportlottery.network.myfavorite.FavoriteService
import org.cxct.sportlottery.network.news.NewsService
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


    val leagueService: LeagueService
        get() = RequestManager.instance
            .retrofit
            .create(LeagueService::class.java)


    val matchResultService: MatchResultService
        get() = RequestManager.instance
            .retrofit
            .create(MatchResultService::class.java)


    val playCateListService: PlayCateListService
        get() = RequestManager.instance
            .retrofit
            .create(PlayCateListService::class.java)


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


    val vipService: VipService
        get() = RequestManager.instance
            .retrofit
            .create(VipService::class.java)


    val playQuotaComService: PlayQuotaComService
        get() = RequestManager.instance
            .retrofit
            .create(PlayQuotaComService::class.java)

    val matchCategoryService: MatchCategoryService
        get() = RequestManager.instance
            .retrofit
            .create(MatchCategoryService::class.java)

    val credentialService: CredentialService
        get() = RequestManager.instance
            .retrofit
            .create(CredentialService::class.java)

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

    val chatService: ChatService
        get() = RequestManager.instance
            .chatGsonRetrofit
            .create(ChatService::class.java)

    val signService: SignService
        get() = RequestManager.instance
            .signRetrofit
            .create(SignService::class.java)
}