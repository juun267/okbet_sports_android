package org.cxct.sportlottery.network.interceptor

import android.content.Context
import android.text.TextUtils
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.network.bet.MATCH_BET_ADD
import org.cxct.sportlottery.network.bet.MATCH_BET_INFO
import org.cxct.sportlottery.network.bet.MATCH_BET_LIST
import org.cxct.sportlottery.network.index.INDEX_LOGIN
import org.cxct.sportlottery.network.league.LEAGUE_LIST
import org.cxct.sportlottery.network.match.MATCH_PRELOAD
import org.cxct.sportlottery.network.message.MESSAGE_LIST
import org.cxct.sportlottery.network.odds.MATCH_ODDS_Detail
import org.cxct.sportlottery.network.odds.MATCH_ODDS_LIST
import org.cxct.sportlottery.network.sport.SPORT_MENU
import org.cxct.sportlottery.util.FileUtil.readStringFromInputStream
import java.io.IOException
import kotlin.jvm.Throws

class MockApiInterceptor(private val context: Context) : Interceptor {

    companion object {
        private val TAG = MockApiInterceptor::class.java.simpleName
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val path = chain.request().url.toUri().path
        var response: Response?

        response = interceptRequestWhenDebug(chain, path)

        if (response == null) {
            response = chain.proceed(chain.request())
        }

        return response
    }

    private fun interceptRequestWhenDebug(chain: Interceptor.Chain, path: String): Response? {
        var response: Response? = null

        if (BuildConfig.DEBUG) {
            val request = chain.request()

            when {
                path.contains(INDEX_LOGIN) -> {
                    response = getMockJsonData(request, "index_login.mock")
                }
                path.contains(MESSAGE_LIST) -> {
                    response = getMockJsonData(request, "message_list.mock")
                }
                path.contains(MATCH_BET_INFO) -> {
                    response = getMockJsonData(request, "match_bet_info.mock")
                }
                path.contains(MATCH_BET_ADD) -> {
                    response = getMockJsonData(request, "match_bet_add.mock")
                }
                path.contains(MATCH_BET_LIST) -> {
                    response = getMockJsonData(request, "match_bet_list.mock")
                }
                path.contains(MATCH_PRELOAD) -> {
                    response = getMockJsonData(request, "match_preload.mock")
                }
                path.contains(MATCH_ODDS_LIST) -> {
                    response = getMockJsonData(request, "match_odds_list.mock")
                }
                path.contains(MATCH_ODDS_Detail) -> {
                    response = getMockJsonData(request, "match_odds_detail.mock")
                }
                path.contains(SPORT_MENU) -> {
                    response = getMockJsonData(request, "sport_menu.mock")
                }
                path.contains(LEAGUE_LIST) -> {
                    response = getMockJsonData(request, "league_list.mock")
                }
            }
        }
        return response
    }

    private fun getMockJsonData(request: Request, fileName: String): Response? {
        val assetManager = context.assets
        val data: String?

        data = try {
            readStringFromInputStream(assetManager.open("mock_api/$fileName"))
        } catch (e: IOException) {
            Log.e(TAG, "getMockJsonData exception: $e")
            return null
        }

        return getHttpSuccessResponse(request, data)
    }

    private fun getHttpSuccessResponse(request: Request, dataJson: String?): Response {
        return if (TextUtils.isEmpty(dataJson)) {
            Log.w(TAG, "getHttpSuccessResponse: dataJson is empty!")

            Response.Builder()
                .code(500)
                .protocol(Protocol.HTTP_1_0)
                .request(request)
                .build()
        } else {

            Response.Builder()
                .code(200)
                .message(dataJson!!)
                .request(request)
                .protocol(Protocol.HTTP_1_0)
                .addHeader("Content-Type", "application/json")
                .body(dataJson.toResponseBody("application/json".toMediaType())).build()
        }
    }
}