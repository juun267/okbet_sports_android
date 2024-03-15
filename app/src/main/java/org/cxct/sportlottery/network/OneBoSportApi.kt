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

    val retrofit = RequestManager.instance.retrofit

    val indexService: IndexService by lazy { retrofit .create(IndexService::class.java) }

    val sportService: SportService  by lazy { retrofit.create(SportService::class.java) }

    val favoriteService: FavoriteService  by lazy { retrofit.create(FavoriteService::class.java) }

    val matchService: MatchService by lazy { retrofit.create(MatchService::class.java) }

    val messageService: MessageService by lazy { retrofit.create(MessageService::class.java) }

    val oddsService: OddsService by lazy { retrofit.create(OddsService::class.java) }

    val betService: BetService by lazy { retrofit.create(BetService::class.java) }

    val matchResultService: MatchResultService by lazy { retrofit.create(MatchResultService::class.java) }

    val outrightService: OutrightService by lazy { retrofit.create(OutrightService::class.java) }

    val userService: UserService by lazy { retrofit.create(UserService::class.java) }

    val uploadImgService: UploadImgService by lazy { retrofit.create(UploadImgService::class.java) }

    val infoCenterService: InfoCenterService by lazy { retrofit.create(InfoCenterService::class.java) }

    val moneyService: MoneyService by lazy { retrofit.create(MoneyService::class.java) }

    val bankService: BankService by lazy { retrofit.create(BankService::class.java) }

    val withdrawService: WithdrawService by lazy { retrofit.create(WithdrawService::class.java) }

    val feedbackService: FeedbackService by lazy { retrofit.create(FeedbackService::class.java) }

    val appUpdateService: AppUpdateService by lazy { retrofit.create(AppUpdateService::class.java) }

    val thirdGameService: ThirdGameService by lazy { retrofit.create(ThirdGameService::class.java) }

    val hostService: HostService by lazy { retrofit.create(HostService::class.java) }

    val newsService: NewsService by lazy { retrofit.create(NewsService::class.java) }

    val bettingStationService: BettingStationService by lazy { retrofit.create(BettingStationService::class.java) }

    val lotteryService: LotteryService by lazy { retrofit.create(LotteryService::class.java) }

}