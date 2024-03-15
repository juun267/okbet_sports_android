package org.cxct.sportlottery.ui.sport.common

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.BetStatus
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.sport.list.adapter.OnOddClickListener
import org.cxct.sportlottery.ui.sport.oddsbtn.OddsButton2
import org.cxct.sportlottery.ui.sport.oddsbtn.PlayCateView
import org.cxct.sportlottery.util.BetPlayCateFunction.isCombination
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.LogUtil

class OddButtonPagerViewHolder2(val oddBtnList: PlayCateView) : RecyclerView.ViewHolder(oddBtnList) {

    private fun <K, V> Map<K, V>?.getPlayCateName(selectLanguage: LanguageManager.Language): String {
        val playCateName = this?.get<Any?, V>(selectLanguage.key) ?: this?.get<Any?, V>(
            LanguageManager.Language.EN.key)
        return playCateName.toString()
    }

    fun setupOddsButton2(
        matchInfo: MatchInfo?,
        playCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        odds: Pair<String?, List<Odd>>?,
        oddsType: OddsType,
        onOddClick: OnOddClickListener,
        matchType: MatchType,
    ) {
        if (setOddsButtonStatu(
                matchInfo,
                playCateNameMap,
                betPlayCateNameMap,
                odds,
                matchType)) {
            return
        }
        val odd = odds!!.second?.firstOrNull()
        val replaceScore = when{
            odd?.replaceScore?.isNotEmpty()==true ->odd.replaceScore?:""
            odd?.nextScore?.isNotEmpty()==true ->odd.nextScore?:""
            else -> ""
        }
        val extInfo = odd?.extInfo ?: ""
        val language = LanguageManager.getSelectLanguage(itemView.context)
        var playCateName = playCateNameMap!![odds.first]?.getPlayCateName(language)
            ?.replace(": ", " ")
            ?.replace("||", "\n")
            ?.replace("{S}", replaceScore)
            ?.replace("{E}", extInfo)
            ?.replace("{extInfo}", extInfo)
            ?.replace("{H}", "${matchInfo!!.homeName}")
            ?.replace("{C}", "${matchInfo.awayName}") ?: ""

        odds.second?.firstOrNull()?.replaceScore?.let { playCateName.replace("{S}", it) }
        if (playCateName == "null" || playCateName.isEmpty()){
            playCateName = "-"
        }


        LogUtil.d("getPlayCateName="+betPlayCateNameMap!![odds.first]?.getPlayCateName(language))
        LogUtil.d("replaceScore="+odds!!.second?.firstOrNull()?.replaceScore)
        LogUtil.d("nextScore="+odds!!.second?.firstOrNull()?.nextScore)
        val betPlayCateName = betPlayCateNameMap!![odds.first]
            ?.getPlayCateName(language)
            ?.replace(": ", " ")
            ?.replace("||", "\n")
            ?.replace("{E}", extInfo)
            ?.replace("{extInfo}", extInfo)
            ?.replace("{S}", replaceScore)
            ?.replace("{H}", "${matchInfo?.homeName}")
            ?.replace("{C}", "${matchInfo?.awayName}")
            ?: ""
        var playCateCode = odds.first ?: ""
        //去掉mappingCS playCateCode的後綴
        if (playCateCode.contains(PlayCate.CS.value) && playCateCode.contains("_")) {
            playCateCode = playCateCode.split("_")[0]
        }
//        Timber.e("playCateCode: $playCateCode")
        LogUtil.d("betPlayCateName="+betPlayCateName+",replaceScore="+replaceScore)
        bindPlayCateName(oddBtnList, "${matchInfo?.gameType}", playCateName, playCateCode, odds.second!!)
        val isDeactivated = (odds.second == null || odds.second!!.all { it == null })
        if (matchType == MatchType.CS && odds?.second?.size == 1) {
            val oddBtnOther = oddBtnList.getOtherOddsBtn()
            odds.second?.getOrNull(0).let {
                bindOddBtn(oddBtnOther, isDeactivated, playCateCode, it, odds.second, oddsType, isOtherBtn = true)
                bindOddClick(oddBtnOther, it, onOddClick, matchInfo, playCateCode, betPlayCateName, betPlayCateNameMap)
            }
            return
        }

        odds.second?.getOrNull(0).let {
            val oddBtnHome = oddBtnList.oddBtnHome
            bindOddBtn(oddBtnHome,
                isDeactivated,
                playCateCode,
                it,
                odds.second,
                oddsType,
                isDrawBtn = it?.name == "Draw")
            bindOddClick(oddBtnHome,
                it,
                onOddClick,
                matchInfo,
                playCateCode,
                betPlayCateName,
                betPlayCateNameMap)
        }

        odds.second?.getOrNull(1).let {
            val oddBtnAway = oddBtnList.oddBtnAway
            bindOddBtn(oddBtnAway,
                isDeactivated,
                playCateCode,
                it,
                odds.second,
                oddsType,
                isDrawBtn = it?.name == "Draw")
            bindOddClick(oddBtnAway,
                it,
                onOddClick,
                matchInfo,
                playCateCode,
                betPlayCateName,
                betPlayCateNameMap)
        }

        val drawOdd = odds.second?.getOrNull(2)
        if (odds.second?.size?: 0 > 2 && drawOdd != null) {
            val oddBtnDraw = oddBtnList.getDrawOddsBtn()
            bindOddBtn(oddBtnDraw,
                isDeactivated,
                playCateCode,
                drawOdd,
                odds.second,
                oddsType,
                isDrawBtn = drawOdd.name == "Draw")
            bindOddClick(oddBtnDraw,
                drawOdd,
                onOddClick,
                matchInfo,
                playCateCode,
                betPlayCateName,
                betPlayCateNameMap)
        } else {
            oddBtnList.disableDrawBtn()
        }
    }

    private val textSpanned by lazy {
        ForegroundColorSpan(Color.parseColor(if (MultiLanguagesApplication.isNightMode) "#a3a3a3" else "#6D7693"))
    }
    private val colorSpanned = ForegroundColorSpan(Color.parseColor("#b73a20"))
    private fun String.updatePlayCateColor(): Spanned {
        val spanned = SpannableStringBuilder(this)
        val index = this.indexOf("\n")
        if (index < 0) {
            spanned.setSpan(textSpanned, 0, this.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        } else {
            spanned.setSpan(textSpanned, 0, index, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            spanned.setSpan(colorSpanned, index, this.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }
        return spanned
    }

    private inline fun String.isSingleType(): Boolean {
        return this == (PlayCate.SINGLE.value) && !this.isCombination()
    }

    private inline fun String.isESport(): Boolean {
        return GameType.ES.key == this
    }

    private fun bindPlayCateName(playCateView: PlayCateView,
                                 gameType: String,
                                 playCateName: String,
                                 playCateCode: String,
                                 oddsList: List<Odd>) {


//        playCateView.setPlayCateName("", "", playCateName.updatePlayCateColor())
//        if (gameType.isESport() || !playCateCode.isSingleType()) {
//            playCateView.setPlayCateName("", "", playCateName.updatePlayCateColor())
//            return
//        }

        if (playCateCode.isSingleType() && gameType == GameType.FT.key) {
            playCateView.setPlayCateName("1", "2", if (oddsList.size < 3) "" else "X")
        } else {
            playCateView.setPlayCateName("", "", playCateName.updatePlayCateColor())
        }
    }

    private fun bindOddClick(oddsButton: OddsButton2,
                             itemOdd: Odd?,
                             onOddClick: OnOddClickListener,
                             matchInfo: MatchInfo?,
                             playCateCode: String,
                             betPlayCateName: String,
                             betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?) {

        oddsButton.setOnClickListener {
            if (matchInfo != null && itemOdd != null) {
                onOddClick.oddClick(matchInfo, itemOdd, playCateCode, betPlayCateName, betPlayCateNameMap, oddsButton)
            }
        }
    }

    private fun setOddsButtonStatu(
        matchInfo: MatchInfo?,
        playCateNameMap: Map<String?, Map<String?, String?>?>?,
        betPlayCateNameMap: Map<String?, Map<String?, String?>?>?,
        odds: Pair<String?, List<Odd?>?>?,
        matchType: MatchType?
    ): Boolean {

        if (matchInfo == null
            || betPlayCateNameMap.isNullOrEmpty()
            || playCateNameMap.isNullOrEmpty()
            || odds == null
            || odds.first == null
            || odds.second.isNullOrEmpty()) {
            oddBtnList.setOddsDeactivated()
            return true
        }

        if (matchType == MatchType.CS && odds.second?.size == 1) {
            oddBtnList.enableOtherOddsBtn()
        } else{
            oddBtnList.enableAllOddsBtn(odds.second?.size?: 0 > 2)
        }

        if (matchInfo.status == null || matchInfo.status == 2 || odds.first.toString().contains("EmptyData")) {
            oddBtnList.setOddsDeactivated()
            return true
        }

        oddBtnList.setBtnTypeVisible(matchType != MatchType.CS)
        if (odds!!.second?.all { odd -> odd == null || odd.status == BetStatus.DEACTIVATED.code } != false) {
            oddBtnList.setOddsDeactivated()
            return true
        }

        return false
    }

    fun update(
        matchInfo: MatchInfo?,
        playCateNameMap: Map<String?, Map<String?, String?>?>?,
        betPlayCateNameMap: Map<String?, Map<String?, String?>?>?,
        odds: Pair<String?, List<Odd>?>?,
        oddsType: OddsType,
        matchType: MatchType?,
    ) {

        if (setOddsButtonStatu(
                matchInfo,
                playCateNameMap,
                betPlayCateNameMap,
                odds,
                matchType)) {
            return
        }

        val playCateName = playCateNameMap!![odds!!.first]
            ?.getPlayCateName(LanguageManager.getSelectLanguage(itemView.context))
            ?.replace(": ", " ")
            ?.replace("||", "\n") ?: ""

        val playCateCode = odds.first ?: ""

        bindPlayCateName(oddBtnList, "${matchInfo!!.gameType}", playCateName, playCateCode, odds.second!!)

        val isDeactivated = (odds.second == null || odds.second!!.all { it == null })

        if (matchType == MatchType.CS && odds.second?.size == 1) {
            val oddBtnOther = oddBtnList.getOtherOddsBtn()
            bindOddBtn(oddBtnOther,
                isDeactivated,
                playCateCode,
                odds.second?.getOrNull(0),
                odds.second,
                oddsType,
                isOtherBtn = true)
            return
        }

        bindOddBtn(oddBtnList.oddBtnHome,
            isDeactivated,
            playCateCode,
            odds.second?.getOrNull(0),
            odds.second,
            oddsType,
            isDrawBtn = odds.second?.getOrNull(0)?.name == "Draw")
        bindOddBtn(oddBtnList.oddBtnAway,
            isDeactivated,
            playCateCode,
            odds.second?.getOrNull(1),
            odds.second,
            oddsType,
            isDrawBtn = odds.second?.getOrNull(1)?.name == "Draw")

        if ((odds.second?.size ?: 0) > 2) {
            bindOddBtn(oddBtnList.getDrawOddsBtn(),
                isDeactivated,
                playCateCode,
                odds.second?.getOrNull(2),
                odds.second,
                oddsType,
                isDrawBtn = odds.second?.getOrNull(2)?.name == "Draw")
        } else {
            oddBtnList.disableDrawBtn()
        }
    }

    private fun bindOddBtn(oddsButton: OddsButton2,
                           isDeactivated: Boolean,
                           playCateCode: String,
                           itemOdd: Odd?,
                           oddList: List<Odd?>?,
                           oddsType: OddsType,
                           isDrawBtn: Boolean = false,
                           isOtherBtn: Boolean = false) {

        val betStatus = when {
            isDeactivated -> {
                BetStatus.DEACTIVATED.code
            }
            ((oddList?.size?: 0 < 2 || itemOdd?.odds ?: 0.0 <= 0.0) && !isOtherBtn) -> {
                BetStatus.LOCKED.code
            }
            else -> {
                itemOdd?.status
            }
        }

        oddsButton.setupOdd4hall(playCateCode, itemOdd, betStatus, oddsType, isDrawBtn)

    }




}
