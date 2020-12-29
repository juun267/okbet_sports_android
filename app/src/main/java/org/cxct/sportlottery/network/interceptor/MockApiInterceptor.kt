package org.cxct.sportlottery.network.interceptor

import android.content.Context
import android.text.TextUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.network.Constants.INDEX_LOGIN
import org.cxct.sportlottery.network.Constants.LEAGUE_LIST
import org.cxct.sportlottery.network.Constants.MATCH_BET_ADD
import org.cxct.sportlottery.network.Constants.MATCH_BET_INFO
import org.cxct.sportlottery.network.Constants.MATCH_BET_LIST
import org.cxct.sportlottery.network.Constants.MATCH_ODDS_DETAIL
import org.cxct.sportlottery.network.Constants.MATCH_ODDS_LIST
import org.cxct.sportlottery.network.Constants.MATCH_PRELOAD
import org.cxct.sportlottery.network.Constants.MATCH_RESULT_LIST
import org.cxct.sportlottery.network.Constants.MATCH_RESULT_PLAY_LIST
import org.cxct.sportlottery.network.Constants.MESSAGE_LIST
import org.cxct.sportlottery.network.Constants.OUTRIGHT_RESULT_LIST
import org.cxct.sportlottery.network.Constants.PLAYCATE_TYPE_LIST
import org.cxct.sportlottery.network.Constants.SPORT_MENU
import org.cxct.sportlottery.util.FileUtil.readStringFromInputStream
import timber.log.Timber
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
                path.contains(MATCH_ODDS_DETAIL) -> {
                    response = getMockJsonData(request, "match_odds_detail.mock")
                }
                path.contains(SPORT_MENU) -> {
                    response = getMockJsonData(request, "sport_menu.mock")
                }
                path.contains(LEAGUE_LIST) -> {
                    response = getMockJsonData(request, "league_list.mock")
                }
                path.contains(MATCH_RESULT_LIST) -> {
                    response = getMockJsonData(request, "match_result_list.mock")
                }
                path.contains(MATCH_RESULT_PLAY_LIST) -> {
                    response = getMockJsonData(request, "match_result_play_list.mock")
                }
                path.contains(PLAYCATE_TYPE_LIST) -> {
                    response = getMockJsonData(request, "playcate_type_list.mock")
                }
                path.contains(OUTRIGHT_RESULT_LIST) -> {
                    response = getMockJsonData(request, "outright_result_list.mock")
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
            Timber.e("getMockJsonData exception: $e")
            return null
        }

        return getHttpSuccessResponse(request, data)
    }

    private fun getHttpSuccessResponse(request: Request, dataJson: String?): Response {
        return if (TextUtils.isEmpty(dataJson)) {
            Timber.w("getHttpSuccessResponse: dataJson is empty!")

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