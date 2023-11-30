package org.cxct.sportlottery.repository

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.sport.IndexResourceJsonResult
import org.cxct.sportlottery.util.LanguageManager

/**
 * 玩法、狀態多語系及其他設定(ex.是否支援盤口切換 ...)
 */
object GamePlayNameRepository {
    private var mResourceList: IndexResourceJsonResult.IndexResourceList? = null
    val resourceList: IndexResourceJsonResult.IndexResourceList? get() = mResourceList

    /**
     * 更新Name Resource List
     */
    fun postResourceList(indexResourceList: IndexResourceJsonResult.IndexResourceList) {
        mResourceList = indexResourceList
    }

    /**
     * 取得對應GameType的Resource
     *
     * @param gameType 球種
     * @return 對應球種的Resource
     */
    fun getGameTypeResources(gameType: GameType?): IndexResourceJsonResult.IndexResourceList.GameTypeResource? {
        if (gameType == null) return null
        return resourceList?.gameTypeResources?.firstOrNull { it.gameType == gameType.key }
    }

    fun getGameTypeResources(gameType: String?): IndexResourceJsonResult.IndexResourceList.GameTypeResource? {
        if (gameType == null) return null
        return resourceList?.gameTypeResources?.firstOrNull { it.gameType == gameType }
    }

    /**
     * 取得對應玩法的多語系名稱(大廳使用)
     *
     * @param gameType 球種
     * @param playCateCodeList 玩法Code列表
     * @return playCateMenuDetails Map<玩法Code, 多語系Map>
     */
    fun getPlayCateMenuDetailsListMap(
        gameType: String?,
        playCateCodeList: List<String>
    ): MutableMap<String?, MutableMap<String?, String?>?> {
        val playCateListMap = mutableMapOf<String?, MutableMap<String?, String?>?>()
        val gameTypeResource = getGameTypeResources(GameType.getGameType(gameType))
        playCateCodeList.forEach { playCateCode ->
            gameTypeResource?.playCateMenuDetails?.firstOrNull { it.code == playCateCode }?.nameMap?.let {
                playCateListMap[playCateCode] = it.toMutableMap<String?, String?>()
            }
        }

        return playCateListMap
    }

    /**
     * 取得對應玩法的多語系名稱(投注單使用)
     *
     * @param gameType 球種
     * @param playCateCodeList 玩法Code列表
     * @return Map<玩法Code, 多語系Map>
     */
    fun getBetPlayCateListMap(
        gameType: String?,
        playCateCodeList: List<String>
    ): MutableMap<String?, MutableMap<String?, String?>?> {
        val betPlayCateListMap = mutableMapOf<String?, MutableMap<String?, String?>?>()
        val gameTypeResource = getGameTypeResources(GameType.getGameType(gameType))
        playCateCodeList.forEach { playCateCode ->
            gameTypeResource?.playCates?.firstOrNull { it.code == playCateCode }?.nameMap?.let {
                betPlayCateListMap[playCateCode] = it.toMutableMap<String?, String?>()
            }
        }

        return betPlayCateListMap
    }

    /**
     * 取得對應玩法的多語系Map(詳情頁＆投注單使用)
     *
     * @param gameType 球種
     * @param playCateCode 玩法Code
     * @return playCates 多語系Map
     */
    fun getPlayCateMap(gameType: String?, playCateCode: String): Map<String?, String?> {
        val gameTypeResource = getGameTypeResources(GameType.getGameType(gameType))

        gameTypeResource?.playCates?.firstOrNull { it.code == playCateCode }?.nameMap.let {
            return it?.toMap<String?, String?>() ?: mapOf()
        }
    }

    /**
     * 取得對應玩法Odd的playCode的nameMap
     *
     * @param gameType 球種
     * @param playCodeList 玩法Code列表
     * @return Map<玩法Code, 多語系nameMap>
     */
    fun getPlayListNameMap(gameType: String?, playCodeList: Set<String>?): Map<String, Map<String?, String?>?> {
        val nameMapMap = mutableMapOf<String, Map<String?, String?>?>()
        val gameTypeResource = getGameTypeResources(GameType.getGameType(gameType))

        playCodeList?.forEach { playCode ->
            gameTypeResource?.playInfos?.firstOrNull { it.code == playCode }?.let {
                nameMapMap[playCode] = it.nameMap.toMap()
            }
        }
        return nameMapMap
    }

    /**
     * 取得對應玩法的nameMap
     *
     * @param gameType 球種
     * @param playCode 玩法Code
     * @return playCode nameMap
     */
    fun getPlayNameMap(gameType: String?, playCode: String): Map<String?, String?> {
        val gameTypeResource = getGameTypeResources(GameType.getGameType(gameType))

        gameTypeResource?.playInfos?.firstOrNull { it.code == playCode }.let {
            return it?.nameMap?.toMap() ?: mapOf()
        }
    }

    /**
     * 取得對應玩法列表是否有支援盤口切換功能
     *
     * @param gameType 球種
     * @param playCateList 玩法CateCode列表
     * @return Map<玩法CateCode, 是否支援盤口切換>
     */
    fun getPlayCateListSupportOddsTypeSwitch(gameType: String?, playCateList: Set<String>): Map<String, Boolean> {
        val switchMap = mutableMapOf<String, Boolean>()
        val gameTypeResource = getGameTypeResources(GameType.getGameType(gameType))

        playCateList.forEach { playCate ->
            gameTypeResource?.playCates?.firstOrNull { it.code == playCate }?.supportOddsTypeSwitch?.let {
                switchMap[playCate] = it
            }
        }

        return switchMap
    }

    /**
     * 取得對應玩法是否有支援盤口切換功能
     *
     * @param gameType 球種
     * @param playCateCode 玩法CateCode
     * @return 是否支援盤口切換
     */
    fun getPlayCateSupportOddsTypeSwitch(gameType: String?, playCateCode: String): Boolean? {
        val gameTypeResource = getGameTypeResources(GameType.getGameType(gameType))

        return gameTypeResource?.playCates?.firstOrNull { it.code == playCateCode }?.supportOddsTypeSwitch
    }

    fun getMatchStatusResources(statusCode: Int?): IndexResourceJsonResult.IndexResourceList.MatchStatusResource? {
        if (statusCode == null) return null
        return resourceList?.matchStatusResources?.firstOrNull { it.status == statusCode.toString() }
    }

    fun getStatusName(statusCode: Int): String {
        return getMatchStatusResources(statusCode)?.nameMap?.get(LanguageManager.getSelectLanguage(MultiLanguagesApplication.appContext).key) ?: ""
    }

    fun getIndexResourceJson() {
        GlobalScope.launch {
            val result: IndexResourceJsonResult? = kotlin.runCatching {
                OneBoSportApi.sportService.getIndexResourceJson()?.body()
            }.getOrNull()

            if (result?.success == true) {
                result.indexResourceList?.let { postResourceList(it) }
            }
        }
    }
}